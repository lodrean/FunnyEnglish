package com.funnyenglish.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp

@Immutable
object AppShapeTokens {
    val None = 0.dp
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 28.dp
    val Full = 1000.dp
}

@Stable
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(AppShapeTokens.ExtraSmall),
    small = RoundedCornerShape(AppShapeTokens.Small),
    medium = RoundedCornerShape(AppShapeTokens.Medium),
    large = RoundedCornerShape(AppShapeTokens.Large),
    extraLarge = RoundedCornerShape(AppShapeTokens.ExtraLarge)
)

@Immutable
object FunnyEnglishShapes {
    // Chips, badges, small indicators
    val Small = RoundedCornerShape(AppShapeTokens.Small)

    // Cards, buttons, input fields
    val Medium = RoundedCornerShape(AppShapeTokens.Medium)

    // Dialogs, bottom sheets, menus
    val Large = RoundedCornerShape(AppShapeTokens.Large)

    // Full-screen cards, onboarding screens
    val ExtraLarge = RoundedCornerShape(AppShapeTokens.ExtraLarge)

    // Circular shapes for avatars, FABs
    val Circular = RoundedCornerShape(AppShapeTokens.Full)

    // No rounding for dividers, separators
    val None = RoundedCornerShape(AppShapeTokens.None)

    // Specific component shapes
    val Button = Medium
    val Card = Medium
    val Chip = Small
    val Dialog = Large
    val BottomSheet = ExtraLarge
    val TextField = Medium
    val Avatar = Circular
    val Badge = Small
}
