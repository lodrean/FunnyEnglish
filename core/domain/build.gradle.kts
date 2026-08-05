plugins {
    id("funnyandroid.library")
    id("funnykotlin.multiplatform")
    id("funnyserialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.sotospeak.core.domain"
}
