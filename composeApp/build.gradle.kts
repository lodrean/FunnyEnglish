import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dropshots)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    // Web (WASM) target for browser testing
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).copy(
                    port = 8082
                ).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(project.projectDir.path + "/src/wasmJsMain/resources")
                    }
                }
            }
        }
        binaries.executable()
    }

    // Override generated index.html with the custom shell that includes
    // crypto.randomUUID polyfill, theme sync and cache-busting.
    tasks.named<Copy>("wasmJsBrowserDistribution") {
        doLast {
            copy {
                from("src/wasmJsMain/resources/index.html")
                into(layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
            }
        }
    }

    sourceSets {
        val desktopMain by getting
        
        // Disable WASM tests (kotest not supported)
        wasmJsTest {
            kotlin.setSrcDirs(emptyList<File>())
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.napier)
            // Видеоплеер Speaking-тренажёра (спека Part 2 §3.1, v1.7: Compose-first)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.ui.compose)
            implementation(libs.androidx.media3.ui.compose.material3)
            // Единый HTTP-стек ExoPlayer на Ktor (bd 4d1): KtorDataSource.Factory вместо DefaultHttpDataSource
            implementation(libs.androidx.media3.datasource.ktor)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            api(projects.shared) {
                exclude(group = "io.github.aakira", module = "napier")
            }

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Napier excluded from WASM - using stub in wasmJsMain
            // implementation(libs.napier)
            
            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.napier)
        }

        val wasmJsMain by getting
        wasmJsMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(libs.ktor.client.js)
            
            // Exclude Napier - not supported on WASM
            configurations["wasmJsMainApi"].exclude(group = "io.github.aakira", module = "napier")
            configurations["wasmJsMainImplementation"].exclude(group = "io.github.aakira", module = "napier")
        }

        commonTest.dependencies {
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.test)
            implementation(libs.ktor.client.mock)
            implementation("io.kotest:kotest-framework-engine:5.8.0")
            implementation("io.kotest:kotest-assertions-core:5.8.0")
        }

        // Скриншот-тесты (Dropshots, golden-эталоны в src/androidInstrumentedTest/screenshots)
        val androidInstrumentedTest by getting
        androidInstrumentedTest.dependencies {
            implementation(libs.dropshots)
            implementation(libs.junit4)
            implementation(libs.androidx.ui.test.junit4)
            implementation(libs.androidx.ui.test.manifest)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.activity.compose)
        }
    }
    
    // Exclude dependencies not supported on WASM
    configurations.all {
        // Note: napier excluded from WASM specifically in wasmJsMain dependencies
        // Do not exclude globally as it's needed for desktop and android
        exclude(group = "io.kotest", module = "kotest-framework-engine")
        exclude(group = "io.kotest", module = "kotest-assertions-core")
    }
}

android {
    namespace = "com.sotospeak.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    testOptions {
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
    
    lint {
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    val apiBaseUrl = providers.gradleProperty("SOTOSPEAK_API_BASE_URL")
        .orElse("http://10.0.2.2:8080/")

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl.get()}\"")
            buildConfigField("boolean", "ENABLE_NETWORK_LOGS", "true")
            buildConfigField("boolean", "ENABLE_DEBUG_TOOLS", "true")
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        getByName("release") {
            buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl.get()}\"")
            buildConfigField("boolean", "ENABLE_NETWORK_LOGS", "false")
            buildConfigField("boolean", "ENABLE_DEBUG_TOOLS", "false")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "com.sotospeak.app.MainKt"

        // JVM args for better networking on Windows
        jvmArgs("-Djava.net.preferIPv4Stack=true")
        jvmArgs("-Dsun.net.client.defaultConnectTimeout=30000")
        jvmArgs("-Dsun.net.client.defaultReadTimeout=30000")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SoToSpeak"
            packageVersion = "1.0.0"
        }
    }
}

// UI tests configuration
tasks.withType<Test>().configureEach {
    // All tests run by default
}

// Kover запускает ВСЕ test-задачи проекта при генерации репорта (by design).
// testDebugUnitTest/testReleaseUnitTest гоняют commonTest UI-тесты без desktop-окружения
// и падают (гейт тестов — desktopTest); uiTest дублирует desktopTest. Исключаем их
// из авто-запуска и замера покрытия.
kover {
    currentProject {
        instrumentation {
            disabledForTestTasks.addAll("testDebugUnitTest", "testReleaseUnitTest", "uiTest")
        }
    }
    // Пороги покрытия (koverVerify), bd FunnyEnglish-qbq.5. Стартовые значения
    // консервативные — поднимать постепенно по факту измерений (прецедент: грабля №88).
    reports {
        verify {
            rule {
                name = "Minimal line coverage"
                bound {
                    minValue = 20
                    metric = kotlinx.kover.gradle.plugin.dsl.MetricType.LINE
                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}

// detekt подключён 2026-08-30 (bd FunnyEnglish-qbq.5, аудит AR-7, грабля №8).
// Baseline общий с backend: config/detekt/baseline.xml (см. комментарий в backend/build.gradle.kts).
detekt {
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    baseline = rootProject.file("config/detekt/baseline.xml")
}

// Separate task for UI tests (делегирует конфигурацию desktopTest с фильтром **/tests/**)
tasks.register<Test>("uiTest") {
    description = "Runs UI tests only"
    group = "verification"
    val dt = tasks.named<Test>("desktopTest")
    testClassesDirs = dt.get().testClassesDirs
    classpath = dt.get().classpath
    include("**/tests/**")
}

// WASM distribution task (prod bundle → build/wasm-dist)
apply(from = "build-wasm-distribution.gradle.kts")
