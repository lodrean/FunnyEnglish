import java.util.Properties

plugins {
    id("com.android.application")
}

apply(plugin = "org.jetbrains.kotlin.android")
apply(plugin = "org.jetbrains.kotlin.plugin.compose")

android {
    namespace = "com.sotospeak.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sotospeak.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
        targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    val apiBaseUrl = providers.gradleProperty("SOTOSPEAK_API_BASE_URL")
        .orElse("http://10.0.2.2:8080/")

    signingConfigs {
        create("release") {
            // Release signing: CI (env) → local.properties (local dev)
            // CI env: KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
            // local.properties:
            // RELEASE_STORE_FILE=release.keystore
            // RELEASE_STORE_PASSWORD=password
            // RELEASE_KEY_ALIAS=sotospeak
            // RELEASE_KEY_PASSWORD=password
            val envStoreFile = System.getenv("KEYSTORE_FILE")
            if (envStoreFile != null && file(envStoreFile).exists()) {
                storeFile = file(envStoreFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            } else {
                val localProps = rootProject.file("local.properties")
                if (localProps.exists()) {
                    val props = Properties()
                    localProps.inputStream().use { props.load(it) }
                    val storeFilePath = props.getProperty("RELEASE_STORE_FILE")
                    if (storeFilePath != null && file(storeFilePath).exists()) {
                        storeFile = file(storeFilePath)
                        storePassword = props.getProperty("RELEASE_STORE_PASSWORD", "")
                        keyAlias = props.getProperty("RELEASE_KEY_ALIAS", "")
                        keyPassword = props.getProperty("RELEASE_KEY_PASSWORD", "")
                    }
                }
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl.get()}\"")
            buildConfigField("boolean", "ENABLE_NETWORK_LOGS", "true")
            buildConfigField("boolean", "ENABLE_DRAG_DROP_QUESTIONS", "true")
            buildConfigField("boolean", "ENABLE_IMAGE_WORD_MATCH", "true")
            buildConfigField("boolean", "ENABLE_DEBUG_TOOLS", "true")
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        // QA-сборка для тестирования на устройстве в LAN (docs/TESTING_ON_LAN.md):
        // ставится рядом с debug/release (applicationIdSuffix), debug-меню включено,
        // cleartext http разрешён (backend по http://<LAN-IP>:8080)
        create("qa") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
            matchingFallbacks += listOf("debug") // composeApp — androidLibrary без qa-типа
        }
        getByName("release") {
            isMinifyEnabled = true
            val releaseSigning = signingConfigs.findByName("release")
            signingConfig = if (releaseSigning?.storeFile != null) releaseSigning else signingConfigs.getByName("debug")
            buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl.get()}\"")
            buildConfigField("boolean", "ENABLE_NETWORK_LOGS", "false")
            buildConfigField("boolean", "ENABLE_DRAG_DROP_QUESTIONS", "true")
            buildConfigField("boolean", "ENABLE_IMAGE_WORD_MATCH", "true")
            buildConfigField("boolean", "ENABLE_DEBUG_TOOLS", "false")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.napier)
}
