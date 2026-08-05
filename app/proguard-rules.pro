# So to Speak ProGuard Rules

# Keep Compose entry points from composeApp module
-keep class com.sotospeak.app.AppKt { *; }
-keep class com.sotospeak.app.di.CoilInitializerKt { *; }

# Suppress warnings for missing classes referenced by generated code
-dontwarn com.sotospeak.app.AppKt
-dontwarn com.sotospeak.app.di.CoilInitializerKt

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
