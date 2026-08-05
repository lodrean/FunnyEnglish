package com.sotospeak.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Extended colors for theme-aware components
 */
data class ExtendedColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onBackground: Color,
    val onSurface: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val card: Color,
    val isDark: Boolean
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        background = FunnyColors.Background,
        surface = FunnyColors.Surface,
        surfaceVariant = FunnyColors.SurfaceVariant,
        onBackground = FunnyColors.OnBackground,
        onSurface = FunnyColors.OnSurface,
        textSecondary = FunnyColors.TextSecondary,
        textMuted = FunnyColors.TextMuted,
        border = FunnyColors.Border,
        card = Color.White,
        isDark = false
    )
}

private val LightColorScheme = lightColorScheme(
    primary = FunnyColors.Primary,
    onPrimary = FunnyColors.OnPrimary,
    primaryContainer = FunnyColors.Primary.copy(alpha = 0.12f),
    onPrimaryContainer = FunnyColors.PrimaryDark,
    secondary = FunnyColors.Secondary,
    onSecondary = FunnyColors.OnSecondary,
    secondaryContainer = FunnyColors.Secondary.copy(alpha = 0.12f),
    onSecondaryContainer = FunnyColors.SecondaryDark,
    tertiary = FunnyColors.Tertiary,
    onTertiary = FunnyColors.OnTertiary,
    tertiaryContainer = FunnyColors.Tertiary.copy(alpha = 0.12f),
    onTertiaryContainer = FunnyColors.TertiaryDark,
    error = FunnyColors.Error,
    onError = Color.White,
    errorContainer = FunnyColors.Error.copy(alpha = 0.12f),
    onErrorContainer = FunnyColors.Error,
    background = FunnyColors.Background,
    onBackground = FunnyColors.OnBackground,
    surface = FunnyColors.Surface,
    onSurface = FunnyColors.OnSurface,
    surfaceVariant = FunnyColors.SurfaceVariant,
    onSurfaceVariant = FunnyColors.TextSecondary,
    outline = FunnyColors.Border,
    outlineVariant = FunnyColors.Border.copy(alpha = 0.5f),
    scrim = Color.Black.copy(alpha = 0.5f)
)

private val DarkColorScheme = darkColorScheme(
    primary = FunnyColors.Primary,
    onPrimary = FunnyColors.OnPrimary,
    primaryContainer = FunnyColors.Primary.copy(alpha = 0.20f),
    onPrimaryContainer = FunnyColors.Primary.copy(alpha = 0.80f),
    secondary = FunnyColors.Secondary,
    onSecondary = FunnyColors.OnSecondary,
    secondaryContainer = FunnyColors.Secondary.copy(alpha = 0.20f),
    onSecondaryContainer = FunnyColors.Secondary.copy(alpha = 0.80f),
    tertiary = FunnyColors.Tertiary,
    onTertiary = FunnyColors.OnTertiary,
    tertiaryContainer = FunnyColors.Tertiary.copy(alpha = 0.20f),
    onTertiaryContainer = FunnyColors.Tertiary.copy(alpha = 0.80f),
    error = FunnyColors.Error,
    onError = Color.White,
    errorContainer = FunnyColors.Error.copy(alpha = 0.20f),
    onErrorContainer = FunnyColors.Error.copy(alpha = 0.80f),
    background = FunnyColors.BackgroundDark,
    onBackground = FunnyColors.OnBackgroundDark,
    surface = FunnyColors.SurfaceDark,
    onSurface = FunnyColors.OnSurfaceDark,
    surfaceVariant = FunnyColors.SurfaceVariantDark,
    onSurfaceVariant = FunnyColors.TextSecondaryDark,
    outline = FunnyColors.BorderDark,
    outlineVariant = FunnyColors.BorderDark.copy(alpha = 0.5f),
    scrim = Color.Black.copy(alpha = 0.7f)
)

private val LightExtendedColors = ExtendedColors(
    background = FunnyColors.Background,
    surface = FunnyColors.Surface,
    surfaceVariant = FunnyColors.SurfaceVariant,
    onBackground = FunnyColors.OnBackground,
    onSurface = FunnyColors.OnSurface,
    textSecondary = FunnyColors.TextSecondary,
    textMuted = FunnyColors.TextMuted,
    border = FunnyColors.Border,
    card = Color.White,
    isDark = false
)

private val DarkExtendedColors = ExtendedColors(
    background = FunnyColors.BackgroundDark,
    surface = FunnyColors.SurfaceDark,
    surfaceVariant = FunnyColors.SurfaceVariantDark,
    onBackground = FunnyColors.OnBackgroundDark,
    onSurface = FunnyColors.OnSurfaceDark,
    textSecondary = FunnyColors.TextSecondaryDark,
    textMuted = FunnyColors.TextMutedDark,
    border = FunnyColors.BorderDark,
    card = FunnyColors.CardDark,
    isDark = true
)

val FunnyShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * So to Speak app theme with dark mode support
 */
@Composable
fun SoToSpeakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = FunnyShapes,
            content = content
        )
    }
}

/**
 * Access extended colors from composables
 */
object FunnyTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
