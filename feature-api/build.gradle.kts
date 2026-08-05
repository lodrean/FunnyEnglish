plugins {
    id("funnyandroid.library")
    id("funnykotlin.multiplatform")
    id("funnycompose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(project(":core:presentation"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)

            api(libs.navigation.compose)
            api(libs.koin.core)
        }
    }
}

android {
    namespace = "com.sotospeak.featureapi"
}
