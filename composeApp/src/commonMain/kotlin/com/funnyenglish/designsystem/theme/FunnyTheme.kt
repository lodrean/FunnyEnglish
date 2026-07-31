package com.funnyenglish.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion
import com.funnyenglish.designsystem.tokens.funnyDarkColorScheme
import com.funnyenglish.designsystem.tokens.funnyLightColorScheme
import com.funnyenglish.designsystem.tokens.funnyShapes
import com.funnyenglish.designsystem.tokens.funnyTypography

/**
 * FunnyEnglish Theme
 *
 * Main theme composition for the application.
 * Integrates Material 3 with FunnyEnglish design tokens.
 *
 * Features:
 * - Light/Dark theme support
 * - Extended gamification color scheme (LocalFunnyColorScheme)
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
 * Main FunnyEnglish Theme
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
    reduceMotion: Boolean = false,
    useOpenDyslexic: Boolean = false,
    highContrast: Boolean = false,
    textScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> if (darkTheme) {
            funnyDarkColorScheme().copy(
                background = funnyDarkColorScheme().background,
                surface = funnyDarkColorScheme().surface,
                onBackground = funnyDarkColorScheme().onBackground,
                onSurface = funnyDarkColorScheme().onSurface
            )
        } else {
            funnyLightColorScheme().copy(
                background = funnyLightColorScheme().background,
                surface = funnyLightColorScheme().surface,
                onBackground = funnyLightColorScheme().onBackground,
                onSurface = funnyLightColorScheme().onSurface
            )
        }
        darkTheme -> funnyDarkColorScheme()
        else -> funnyLightColorScheme()
    }

    val extendedColorScheme = if (darkTheme) DarkFunnyColorScheme else LightFunnyColorScheme
    val speakingColors = if (darkTheme) DarkSpeakingColors else LightSpeakingColors

    val typography = funnyTypography()
    val shapes = funnyShapes()

    val themeConfig = FunnyThemeConfig(
        reduceMotion = reduceMotion,
        useOpenDyslexic = useOpenDyslexic,
        highContrast = highContrast,
        textScale = textScale
    )

    CompositionLocalProvider(
        LocalReduceMotion provides reduceMotion,
        LocalFunnyThemeConfig provides themeConfig,
        LocalFunnyColorScheme provides extendedColorScheme,
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
 * FunnyEnglish Surface with proper theming
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
