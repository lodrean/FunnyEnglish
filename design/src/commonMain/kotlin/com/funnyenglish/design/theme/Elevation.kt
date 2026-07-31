package com.funnyenglish.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp

@Immutable
object ElevationTokens {
    // Level 0 - Flat elements
    val Level0 = 0.dp

    // Level 1 - Cards at rest, switches
    val Level1 = 1.dp

    // Level 2 - Raised cards, search bars
    val Level2 = 3.dp

    // Level 3 - Refresh indicators, quick entry
    val Level3 = 6.dp

    // Level 4 - App bars, FABs
    val Level4 = 8.dp

    // Level 5 - Dialogs, menus, sub menus
    val Level5 = 12.dp
}

@Immutable
data class ElevationLevels(
    val level0: androidx.compose.ui.unit.Dp,
    val level1: androidx.compose.ui.unit.Dp,
    val level2: androidx.compose.ui.unit.Dp,
    val level3: androidx.compose.ui.unit.Dp,
    val level4: androidx.compose.ui.unit.Dp,
    val level5: androidx.compose.ui.unit.Dp
)

@Stable
val LightElevation = ElevationLevels(
    level0 = ElevationTokens.Level0,
    level1 = ElevationTokens.Level1,
    level2 = ElevationTokens.Level2,
    level3 = ElevationTokens.Level3,
    level4 = ElevationTokens.Level4,
    level5 = ElevationTokens.Level5
)

@Stable
val DarkElevation = ElevationLevels(
    level0 = ElevationTokens.Level0,
    level1 = ElevationTokens.Level1,
    level2 = ElevationTokens.Level2,
    level3 = ElevationTokens.Level3,
    level4 = ElevationTokens.Level4,
    level5 = ElevationTokens.Level5
)

@Immutable
object FunnyEnglishElevation {
    // Flat elements - dividers, separators
    val None = ElevationTokens.Level0

    // Cards at rest, switches
    val CardResting = ElevationTokens.Level1

    // Raised cards, search bars
    val CardRaised = ElevationTokens.Level2

    // Refresh indicators, quick entry
    val Indicator = ElevationTokens.Level3

    // App bars, FABs
    val AppBar = ElevationTokens.Level4
    val FAB = ElevationTokens.Level4

    // Dialogs, menus, bottom sheets
    val Dialog = ElevationTokens.Level5
    val Menu = ElevationTokens.Level5
    val BottomSheet = ElevationTokens.Level5
    val Modal = ElevationTokens.Level5

    // Component-specific elevations
    val Button = ElevationTokens.Level0
    val ButtonPressed = ElevationTokens.Level0
    val Card = ElevationTokens.Level1
    val CardHovered = ElevationTokens.Level2
    val Chip = ElevationTokens.Level0
    val TextField = ElevationTokens.Level0
    val Snackbar = ElevationTokens.Level4
    val Tooltip = ElevationTokens.Level5
}

/**
 * Speaking Trainer — Playful Coach v1.1 (tokens.json elevation).
 * Жёсткая «оттопыренная» тень rec-кнопки не выражается Dp-elevation:
 * рисуется как box-shadow `0 <offsetY> 0 RecordShadow(α=0.55)`; при нажатии схлопывается до 1dp.
 */
@Immutable
object SpeakingElevation {
    /** Смещение жёсткой тени rec-кнопки в покое: `0 4px 0 rgba(217,114,56,0.55)` */
    val RecorderShadowOffsetY = 4.dp

    /** При нажатии тень схлопывается: `0 1px 0 ...` */
    val RecorderShadowPressedOffsetY = 1.dp

    /** Тень bottom sheet: `0 -4px 24px rgba(45,53,97,0.12)` */
    val Sheet = ElevationTokens.Level5
}
