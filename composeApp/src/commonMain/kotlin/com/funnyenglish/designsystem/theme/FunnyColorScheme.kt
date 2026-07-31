package com.funnyenglish.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.funnyenglish.designsystem.tokens.*

/**
 * FunnyEnglish Extended Color Scheme
 *
 * Wraps gamification and semantic colors that are not part of the standard Material 3
 * color scheme, but are essential for the app's design language.
 *
 * Usage:
 * ```
 * val colors = MaterialTheme.funnyColors
 * Text("Streak", color = colors.streak)
 * ```
 */
@Immutable
data class FunnyColorScheme(
    // Gamification colors
    val streak: Color,
    val streakContainer: Color,
    val xp: Color,
    val xpContainer: Color,
    val achievement: Color,
    val achievementContainer: Color,
    val gem: Color,
    val gemContainer: Color,

    // Semantic extended colors
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val info: Color,
    val infoContainer: Color,

    // Leaderboard medals
    val gold: Color,
    val silver: Color,
    val bronze: Color,

    // Utility
    val isDark: Boolean
)

/**
 * Light extended color scheme
 */
val LightFunnyColorScheme = FunnyColorScheme(
    streak = StreakOrange,
    streakContainer = StreakOrange.copy(alpha = 0.12f),
    xp = XPGold,
    xpContainer = XPGold.copy(alpha = 0.12f),
    achievement = AchievementPurple,
    achievementContainer = AchievementPurple.copy(alpha = 0.12f),
    gem = GemTeal,
    gemContainer = GemTeal.copy(alpha = 0.12f),

    success = SuccessLight,
    successContainer = SuccessLight.copy(alpha = 0.12f),
    warning = WarningLight,
    warningContainer = WarningLight.copy(alpha = 0.12f),
    info = InfoLight,
    infoContainer = InfoLight.copy(alpha = 0.12f),

    gold = Gold,
    silver = SilverLight,
    bronze = BronzeLight,

    isDark = false
)

/**
 * Dark extended color scheme
 */
val DarkFunnyColorScheme = FunnyColorScheme(
    streak = StreakOrangeLight,
    streakContainer = StreakOrange.copy(alpha = 0.20f),
    xp = XPGoldLight,
    xpContainer = XPGold.copy(alpha = 0.20f),
    achievement = AchievementPurpleLight,
    achievementContainer = AchievementPurple.copy(alpha = 0.20f),
    gem = GemTealLight,
    gemContainer = GemTeal.copy(alpha = 0.20f),

    success = SuccessDark,
    successContainer = SuccessDark.copy(alpha = 0.20f),
    warning = WarningDark,
    warningContainer = WarningDark.copy(alpha = 0.20f),
    info = InfoDark,
    infoContainer = InfoDark.copy(alpha = 0.20f),

    gold = Gold,
    silver = SilverLight,
    bronze = BronzeLight,

    isDark = true
)

/**
 * CompositionLocal for the extended FunnyEnglish color scheme.
 */
val LocalFunnyColorScheme = staticCompositionLocalOf { LightFunnyColorScheme }

/**
 * Extension property to access the extended FunnyEnglish color scheme from MaterialTheme.
 */
val MaterialTheme.funnyColors: FunnyColorScheme
    @Composable
    get() = LocalFunnyColorScheme.current
