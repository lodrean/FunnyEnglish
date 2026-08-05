package com.sotospeak.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * So to Speak color palette matching Stitch design
 * Supports both light and dark themes
 */
object FunnyColors {
    // Primary - Blue (from design #2563EB)
    val Primary = Color(0xFF2563EB)
    val PrimaryDark = Color(0xFF1D4ED8)
    val OnPrimary = Color.White

    // Secondary - Green (from design #10B981)
    val Secondary = Color(0xFF10B981)
    val SecondaryDark = Color(0xFF059669)
    val OnSecondary = Color.White

    // Tertiary - Orange (from design #F59E0B)
    val Tertiary = Color(0xFFF59E0B)
    val TertiaryDark = Color(0xFFD97706)
    val OnTertiary = Color.White

    // Legacy Accent - Purple (for backward compatibility)
    val AccentPurple = Primary
    val AccentPurpleDark = PrimaryDark
    val AccentPurpleLight = Color(0xFF93C5FD)

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
    val Background = Color(0xFFF8F9FA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1F3F5)
    val OnBackground = Color(0xFF1A1D21)
    val OnSurface = Color(0xFF1A1D21)
    val TextSecondary = Color(0xFF5C6B7F)
    val TextMuted = Color(0xFF8B9AAB)
    val Border = Color(0xFFDDE2E8)

    // Dark Theme
    val BackgroundDark = Color(0xFF1A1D21)
    val SurfaceDark = Color(0xFF252A30)
    val SurfaceVariantDark = Color(0xFF2D333B)
    val OnBackgroundDark = Color(0xFFF8F9FA)
    val OnSurfaceDark = Color(0xFFF8F9FA)
    val TextSecondaryDark = Color(0xFFB0B8C4)
    val TextMutedDark = Color(0xFF6B7885)
    val BorderDark = Color(0xFF3D444D)
    val CardDark = Color(0xFF252A30)

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
