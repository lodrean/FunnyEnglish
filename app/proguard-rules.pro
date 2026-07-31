# FunnyEnglish ProGuard Rules

# Keep Compose entry points from composeApp module
-keep class com.funnyenglish.app.AppKt { *; }
-keep class com.funnyenglish.app.di.CoilInitializerKt { *; }

# Suppress warnings for missing classes referenced by generated code
-dontwarn com.funnyenglish.app.AppKt
-dontwarn com.funnyenglish.app.di.CoilInitializerKt

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }

# Napier
-keep class io.github.aakira.napier.** { *; }

# Java management (not available on Android but referenced by Ktor)
-dontwarn java.lang.management.**
