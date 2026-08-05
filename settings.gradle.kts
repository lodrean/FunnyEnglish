rootProject.name = "SoToSpeak"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":backend")
include(":shared")
include(":composeApp")
include(":app")

// Core module with shared infrastructure
include(":core")
include(":core:domain")
include(":core:data")
include(":core:presentation")

// Feature API module for inter-feature communication
include(":feature-api")

// Feature modules (can be toggled on/off)
include(":feature-home")
include(":feature-auth")
include(":feature-tests")
include(":feature-profile")
include(":feature-gamification")

include(":feature-leaderboard")
include(":feature-learning")

// Design System module
include(":design")
