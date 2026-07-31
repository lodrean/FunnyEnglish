plugins {
    id("funnyandroid.library")
    id("funnykotlin.multiplatform")
    id("funnyserialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.serialization.kotlinx.json)
            api(libs.ktor.client.logging)
            api(libs.ktor.client.auth)
            
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
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
    namespace = "com.funnyenglish.core.data"
}
