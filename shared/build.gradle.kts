plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
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

    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
                }
            }
        }
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.library()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val desktopMain by getting
        
        // Disable WASM tests (kotest not supported on WASM)
        // commonTest is automatically included in all targets, so we disable
        // the entire wasmJs test compilation to avoid kotest unresolved references
        @Suppress("UNUSED_VARIABLE")
        val wasmJsTest by getting {
            kotlin.setSrcDirs(emptyList<File>())
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            // Napier added per-target below (not supported on WASM)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.media3.exoplayer)
        }

        desktopMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.jlayer)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        val wasmJsMain by getting
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("io.kotest:kotest-framework-engine:5.8.0")
            implementation("io.kotest:kotest-assertions-core:5.8.0")
            implementation("io.kotest:kotest-property:5.8.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        }
    }
    
    // Exclude dependencies not supported on WASM
    configurations.matching { it.name.contains("wasmJs") }.all {
        exclude(group = "io.github.aakira", module = "napier")
        exclude(group = "io.kotest")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-test")
    }
}

// Disable WASM test compilation completely (Kotest not supported on WASM)
afterEvaluate {
    tasks.findByName("compileTestKotlinWasmJs")?.enabled = false
    tasks.findByName("wasmJsTest")?.enabled = false
    tasks.findByName("wasmJsTestDevelopmentExecutableCompileSync")?.enabled = false
    tasks.findByName("wasmJsTestProductionExecutableCompileSync")?.enabled = false
}

android {
    namespace = "com.sotospeak.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
