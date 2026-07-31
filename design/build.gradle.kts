import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("funnyandroid.library")
    id("funnykotlin.multiplatform")
    id("funnycompose")
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.library()
    }

    sourceSets {
        // Disable WASM tests
        wasmJsTest {
            kotlin.setSrcDirs(emptyList<File>())
        }

        commonMain.dependencies {
            // Additional Compose dependencies not covered by convention plugin
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.animation)
            implementation(compose.animationGraphics)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.funnyenglish.design"
    buildFeatures {
        compose = true
    }
}
