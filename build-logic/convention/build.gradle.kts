plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "funnyandroid.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "funnyandroid.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("kotlinMultiplatform") {
            id = "funnykotlin.multiplatform"
            implementationClass = "KotlinMultiplatformConventionPlugin"
        }
        register("compose") {
            id = "funnycompose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("serialization") {
            id = "funnyserialization"
            implementationClass = "SerializationConventionPlugin"
        }
        register("ktor") {
            id = "funnyktor"
            implementationClass = "KtorConventionPlugin"
        }
        register("koin") {
            id = "funnykoin"
            implementationClass = "KoinConventionPlugin"
        }
    }
}

dependencies {
    compileOnly(buildLibs.android.gradlePlugin)
    compileOnly(buildLibs.kotlin.gradlePlugin)
    compileOnly(buildLibs.compose.gradlePlugin)
}
