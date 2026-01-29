package com.funnyenglish.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * FunnyEnglish color palette matching Stitch design
 * Supports both light and dark themes
 */
object FunnyColors {
    // Primary - Blue
    val Primary = Color(0xFF2094F3)
    val PrimaryDark = Color(0xFF1976D2)
    val OnPrimary = Color.White

    // Accent - Purple
    val AccentPurple = Color(0xFF7C4DFF)
    val AccentPurpleDark = Color(0xFF651FFF)
    val AccentPurpleLight = Color(0xFFA78BFA)

    // Secondary - Orange
    val Secondary = Color(0xFFF97316)
    val SecondaryDark = Color(0xFFEA580C)
    val OnSecondary = Color.White

    // Accent Colors
    val Success = Color(0xFF10B981)
    val SuccessLight = Color(0xFF34D399)
    val Error = Color(0xFFEF4444)
    val Warning = Color(0xFFFBBF24)
    val Info = Color(0xFF3B82F6)

    // Fun gradient colors
    val Pink = Color(0xFFEC4899)
    val Purple = Color(0xFF8B5CF6)
    val Green = Color(0xFF22C55E)
    val Yellow = Color(0xFFFBBF24)
    val Cyan = Color(0xFF06B6D4)
    val Orange = Color(0xFFF97316)
    val Indigo = Color(0xFF6366F1)
    val Blue = Color(0xFF3B82F6)

    // Medal colors
    val Gold = Color(0xFFFFD700)
    val Silver = Color(0xFFC0C0C0)
    val Bronze = Color(0xFFCD7F32)

    // Stars
    val StarFilled = Color(0xFFFFB300)
    val StarEmpty = Color(0xFFE2E8F0)

    // Light Theme
    val Background = Color(0xFFF5F7F8)
    val Surface = Color.White
    val SurfaceVariant = Color(0xFFF1F5F9)
    val OnBackground = Color(0xFF0D151C)
    val OnSurface = Color(0xFF334155)
    val TextSecondary = Color(0xFF64748B)
    val TextMuted = Color(0xFF94A3B8)
    val Border = Color(0xFFE2E8F0)

    // Dark Theme
    val BackgroundDark = Color(0xFF121212)
    val SurfaceDark = Color(0xFF1E1E1E)
    val SurfaceVariantDark = Color(0xFF1A222C)
    val OnBackgroundDark = Color(0xFFF5F5F5)
    val OnSurfaceDark = Color(0xFFE2E8F0)
    val TextSecondaryDark = Color(0xFFA0A0A0)
    val TextMutedDark = Color(0xFF64748B)
    val BorderDark = Color(0xFF2C2C2C)
    val CardDark = Color(0xFF1E1E1E)

    // Difficulty colors
    val DifficultyEasy = Color(0xFF22C55E)
    val DifficultyMedium = Color(0xFFF97316)
    val DifficultyHard = Color(0xFFEF4444)

    // Category colors
    val CategoryYellow = Color(0xFFFEF3C7)
    val CategoryOrange = Color(0xFFFFEDD5)
    val CategoryPink = Color(0xFFFCE7F3)
    val CategoryBlue = Color(0xFFDBEAFE)
    val CategoryIndigo = Color(0xFFE0E7FF)
    val CategoryPurple = Color(0xFFF3E8FF)
}

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
    secondary = FunnyColors.AccentPurple,
    onSecondary = FunnyColors.OnSecondary,
    tertiary = FunnyColors.Secondary,
    error = FunnyColors.Error,
    background = FunnyColors.Background,
    surface = FunnyColors.Surface,
    onBackground = FunnyColors.OnBackground,
    onSurface = FunnyColors.OnSurface,
    surfaceVariant = FunnyColors.SurfaceVariant,
    outline = FunnyColors.Border
)

private val DarkColorScheme = darkColorScheme(
    primary = FunnyColors.Primary,
    onPrimary = FunnyColors.OnPrimary,
    secondary = FunnyColors.AccentPurple,
    onSecondary = FunnyColors.OnSecondary,
    tertiary = FunnyColors.Secondary,
    error = FunnyColors.Error,
    background = FunnyColors.BackgroundDark,
    surface = FunnyColors.SurfaceDark,
    onBackground = FunnyColors.OnBackgroundDark,
    onSurface = FunnyColors.OnSurfaceDark,
    surfaceVariant = FunnyColors.SurfaceVariantDark,
    outline = FunnyColors.BorderDark
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
 * FunnyEnglish app theme with dark mode support
 */
@Composable
fun FunnyEnglishTheme(
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
