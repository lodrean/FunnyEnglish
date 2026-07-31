package com.funnyenglish.designsystem.tokens

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * FunnyEnglish Color System
 * 
 * Philosophy: "Playful but Clear" for children 7-14 years
 * - Pastel base with bright accents
 * - Soft semantic colors for reduced anxiety
 * - Gamification colors with high saturation
 */

// ==================== Primary Palette ====================
// Primary: Blue #2563EB (from design)
val PrimaryLight = Color(0xFF2563EB)
val PrimaryDark = Color(0xFF3B82F6)
val OnPrimary = Color(0xFFFFFFFF)

// Secondary: Green #10B981 (from design)  
val SecondaryLight = Color(0xFF10B981)
val SecondaryDark = Color(0xFF34D399)
val OnSecondary = Color(0xFFFFFFFF)

// Tertiary: Orange #F59E0B (from design)
val TertiaryLight = Color(0xFFF59E0B)
val TertiaryDark = Color(0xFFFBBF24)
val OnTertiary = Color(0xFF000000)

// ==================== Semantic Colors ====================
val SuccessLight = Color(0xFF6BCB8A)
val SuccessDark = Color(0xFF4CAF50)
val OnSuccess = Color(0xFF000000)

val WarningLight = Color(0xFFFFD166)
val WarningDark = Color(0xFFFFB300)
val OnWarning = Color(0xFF000000)

val ErrorLight = Color(0xFFFF6B6B)
val ErrorDark = Color(0xFFE53935)
val OnError = Color(0xFFFFFFFF)

val InfoLight = Color(0xFF4ECDC4)
val InfoDark = Color(0xFF26A69A)
val OnInfo = Color(0xFF000000)

// ==================== Backgrounds & Surfaces ====================
val BackgroundLight = Color(0xFFF8F9FA)
val BackgroundDark = Color(0xFF1A1D21)
val OnBackgroundLight = Color(0xFF1A1D21)
val OnBackgroundDark = Color(0xFFF8F9FA)

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF252A30)
val OnSurfaceLight = Color(0xFF1A1D21)
val OnSurfaceDark = Color(0xFFF8F9FA)

val SurfaceVariantLight = Color(0xFFF1F3F5)
val SurfaceVariantDark = Color(0xFF2D333B)
val OnSurfaceVariantLight = Color(0xFF5C6B7F)
val OnSurfaceVariantDark = Color(0xFFB0B8C4)

// ==================== Outline ====================
val OutlineLight = Color(0xFFDDE2E8)
val OutlineDark = Color(0xFF3D444D)
val OutlineVariantLight = Color(0xFFE8EBF0)
val OutlineVariantDark = Color(0xFF333A42)

// ==================== Gamification Colors ====================
/**
 * Fire Orange - Streak mechanics
 * Highest saturation for urgency and attention
 */
val StreakOrange = Color(0xFFFF6B35)
val StreakOrangeLight = Color(0xFFFF8A5B)

/**
 * Gold - XP and value accumulation
 */
val XPGold = Color(0xFFFFD166)
val XPGoldLight = Color(0xFFFFE08A)

/**
 * Teal - Gems and premium currency
 */
val GemTeal = Color(0xFF4ECDC4)
val GemTealLight = Color(0xFF7EDDD6)

/**
 * Achievement Purple - Recognition and milestones
 */
val AchievementPurple = Color(0xFF9B7EDE)
val AchievementPurpleLight = Color(0xFFB8A4E8)

/**
 * Medal Colors - Leaderboard
 */
val Gold = Color(0xFFFFD700)
val SilverLight = Color(0xFFC0C0C0)
val BronzeLight = Color(0xFFCD7F32)

// ==================== Light Color Scheme ====================
fun funnyLightColorScheme() = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryDark,
    
    secondary = SecondaryLight,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = SecondaryDark,
    
    tertiary = TertiaryLight,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = TertiaryDark,
    
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    
    error = ErrorLight,
    onError = OnError,
    errorContainer = ErrorLight.copy(alpha = 0.12f),
    onErrorContainer = ErrorDark,
    
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    
    scrim = Color(0xFF000000).copy(alpha = 0.5f),
    inverseSurface = SurfaceDark,
    inverseOnSurface = OnSurfaceDark,
    inversePrimary = PrimaryDark
)

// ==================== Dark Color Scheme ====================
fun funnyDarkColorScheme() = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight.copy(alpha = 0.20f),
    onPrimaryContainer = PrimaryLight,
    
    secondary = SecondaryDark,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryLight.copy(alpha = 0.20f),
    onSecondaryContainer = SecondaryLight,
    
    tertiary = TertiaryDark,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryLight.copy(alpha = 0.20f),
    onTertiaryContainer = TertiaryLight,
    
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    
    error = ErrorDark,
    onError = OnError,
    errorContainer = ErrorLight.copy(alpha = 0.20f),
    onErrorContainer = ErrorLight,
    
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    
    scrim = Color(0xFF000000).copy(alpha = 0.7f),
    inverseSurface = SurfaceLight,
    inverseOnSurface = OnSurfaceLight,
    inversePrimary = PrimaryLight
)

// ==================== Extension Properties ====================
fun isLightColorScheme(background: Color): Boolean {
    return background == BackgroundLight
}
