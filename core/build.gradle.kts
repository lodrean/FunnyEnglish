plugins {
    id("funnyandroid.library")
    id("funnykotlin.multiplatform")
    id("funnycompose")
    id("funnyserialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":shared"))
            api(project(":design"))
            
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            
            implementation(libs.koin.core)
            
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            
            implementation(libs.navigation.compose)
            implementation(libs.multiplatform.settings)
            implementation(libs.napier)
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.funnyenglish.core"
}
