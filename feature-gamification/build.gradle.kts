plugins {
    id("funnyandroid.library")
    id("funnykotlin.multiplatform")
    id("funnycompose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feature-api"))
            api(project(":core:domain"))
            api(project(":core:presentation"))
            
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            
            api(libs.lifecycle.viewmodel.compose)
            api(libs.navigation.compose)
        }
    }
}

android {
    namespace = "com.sotospeak.feature.gamification"
}
