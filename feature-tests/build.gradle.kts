plugins {
    id("funnyandroid.library")
    id("funnykotlin.multiplatform")
    id("funnycompose")
    id("funnyserialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feature-api"))
            api(project(":core:domain"))
            api(project(":core:presentation"))
            api(project(":shared"))
            
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            
            api(libs.lifecycle.viewmodel.compose)
            api(libs.navigation.compose)
            
            implementation(libs.coil.compose)
            implementation(libs.kotlinx.datetime)
            
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        
        androidMain.dependencies {
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.session)
        }
    }
}

android {
    namespace = "com.sotospeak.feature.tests"
}
