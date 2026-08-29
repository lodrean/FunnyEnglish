package com.sotospeak.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.sotospeak.designsystem.accessibility.LocalReduceMotion
import com.sotospeak.designsystem.accessibility.platformReduceMotionEnabled

/**
 * So to Speak Theme
 *
 * Main theme composition for the application.
 * Integrates Material 3 with So to Speak design tokens.
 *
 * Features:
 * - Light/Dark theme support
 * - Accessibility features (Reduce Motion)
 * - Consistent color scheme, typography, and shapes
 */

/**
 * CompositionLocal for theme configuration
 */
val LocalFunnyThemeConfig = staticCompositionLocalOf {
    FunnyThemeConfig()
}

/**
 * Theme configuration data class
 */
data class FunnyThemeConfig(
    val reduceMotion: Boolean = false,
    val useOpenDyslexic: Boolean = false,
    val highContrast: Boolean = false,
    val textScale: Float = 1.0f
)

/**
 * Main So to Speak Theme
 *
 * @param darkTheme Whether to use dark theme (default: system setting)
 * @param reduceMotion Whether to reduce motion for accessibility
 * @param useOpenDyslexic Whether to use OpenDyslexic font
 * @param highContrast Whether to use high contrast mode
 * @param textScale Text scale factor (1.0 = 100%)
 * @param content Content to be themed
 */
@Composable
fun FunnyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    reduceMotion: Boolean = platformReduceMotionEnabled(),
    useOpenDyslexic: Boolean = false,
    highContrast: Boolean = false,
    textScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    // M3 color scheme — Playful Coach (DSM-5 §1.1, tokens v1.3.0).
    val colorScheme = if (darkTheme) speakingDarkColorScheme() else speakingLightColorScheme()

    val speakingColors = if (darkTheme) DarkSpeakingColors else LightSpeakingColors

    val typography = speakingTypography()
    val shapes = speakingShapes()

    val themeConfig = FunnyThemeConfig(
        reduceMotion = reduceMotion,
        useOpenDyslexic = useOpenDyslexic,
        highContrast = highContrast,
        textScale = textScale
    )

    CompositionLocalProvider(
        LocalReduceMotion provides reduceMotion,
        LocalFunnyThemeConfig provides themeConfig,
        LocalSpeakingColors provides speakingColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}

/**
 * So to Speak Surface with proper theming
 */
@Composable
fun FunnySurface(
    content: @Composable () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        content = content
    )
}

/**
 * Preview themes for Compose Preview
 */
@Composable
fun FunnyThemePreview(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    FunnyTheme(
        darkTheme = darkTheme,
        reduceMotion = false,
        content = content
    )
}
