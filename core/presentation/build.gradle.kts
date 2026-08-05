plugins {
    id("funnyandroid.library")
    id("funnykotlin.multiplatform")
    id("funnycompose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.sotospeak.core.presentation"
}
