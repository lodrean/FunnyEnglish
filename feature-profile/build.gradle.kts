plugins {
    id("funnyandroid.library")
    id("funnykotlin.multiplatform")
    id("funnycompose")
    id("funnykoin")
    id("funnyserialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(project(":core:data"))
            api(project(":core:presentation"))
            api(project(":feature-api"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)

            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)

            api(libs.lifecycle.viewmodel.compose)
            api(libs.navigation.compose)

            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "com.funnyenglish.feature.profile"
}
