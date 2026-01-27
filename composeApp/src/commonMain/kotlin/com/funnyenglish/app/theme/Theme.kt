package com.funnyenglish.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Bright, kid-friendly colors
object FunnyColors {
    // Primary - Bright Blue
    val Primary = Color(0xFF4FC3F7)
    val PrimaryDark = Color(0xFF0288D1)
    val OnPrimary = Color.White

    // Secondary - Bright Orange
    val Secondary = Color(0xFFFF9800)
    val SecondaryDark = Color(0xFFF57C00)
    val OnSecondary = Color.White

    // Accent Colors
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFE53935)
    val Warning = Color(0xFFFFEB3B)
    val Info = Color(0xFF2196F3)

    // Fun gradient colors
    val Pink = Color(0xFFE91E63)
    val Purple = Color(0xFF9C27B0)
    val Green = Color(0xFF8BC34A)
    val Yellow = Color(0xFFFFC107)
    val Cyan = Color(0xFF00BCD4)

    // Stars
    val StarFilled = Color(0xFFFFD700)
    val StarEmpty = Color(0xFFE0E0E0)

    // Background
    val Background = Color(0xFFF5F5F5)
    val Surface = Color.White
    val SurfaceVariant = Color(0xFFEEEEEE)

    // Text
    val OnBackground = Color(0xFF212121)
    val OnSurface = Color(0xFF424242)
    val TextSecondary = Color(0xFF757575)

    // Difficulty colors
    val DifficultyEasy = Color(0xFF4CAF50)
    val DifficultyMedium = Color(0xFFFF9800)
    val DifficultyHard = Color(0xFFE53935)
}

private val LightColorScheme = lightColorScheme(
    primary = FunnyColors.Primary,
    onPrimary = FunnyColors.OnPrimary,
    secondary = FunnyColors.Secondary,
    onSecondary = FunnyColors.OnSecondary,
    error = FunnyColors.Error,
    background = FunnyColors.Background,
    surface = FunnyColors.Surface,
    onBackground = FunnyColors.OnBackground,
    onSurface = FunnyColors.OnSurface,
    surfaceVariant = FunnyColors.SurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = FunnyColors.PrimaryDark,
    onPrimary = FunnyColors.OnPrimary,
    secondary = FunnyColors.SecondaryDark,
    onSecondary = FunnyColors.OnSecondary,
    error = FunnyColors.Error,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color.White,
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C)
)

val FunnyShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun FunnyEnglishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = FunnyShapes,
        content = content
    )
}
