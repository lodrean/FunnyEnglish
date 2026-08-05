package com.sotospeak.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * So to Speak Shape System
 *
 * Base: Material Design 3 shape tokens with intentional deviations for playful child-friendly UI.
 * MD3 defaults: none(0), extraSmall(4), small(8), medium(12), large(16),
 *               extraLarge(28), full(9999/pill).
 *
 * Intentional deviations from MD3:
 * - CardShape: 16.dp (MD3 medium) instead of 12.dp — larger corners feel more friendly for kids.
 * - InputFieldShape: 12.dp (between MD3 small and medium) — balance between spec and playfulness.
 */

// ==================== MD3-Aligned Tokens ====================

/**
 * MD3 extra-small: 4dp
 * Chips, snackbars, small badges
 */
val ShapeExtraSmall = RoundedCornerShape(4.dp)

/**
 * MD3 small: 8dp
 * Text fields (MD3 spec), compact tags
 */
val ShapeSmall = RoundedCornerShape(8.dp)

/**
 * MD3 medium: 12dp
 * Cards (MD3 spec), medium containers
 */
val ShapeMedium = RoundedCornerShape(12.dp)

/**
 * MD3 large: 16dp
 * FABs, navigation drawers, featured cards
 */
val ShapeLarge = RoundedCornerShape(16.dp)

/**
 * MD3 extra-large: 28dp
 * Dialogs, bottom sheets, modals
 */
val ShapeExtraLarge = RoundedCornerShape(28.dp)

/**
 * MD3 full: pill / fully rounded
 * Buttons, chips, badges, tags
 */
val ShapeFull = RoundedCornerShape(percent = 50)

// ==================== So to Speak Component Shapes ====================

/**
 * Button shape: pill/full (MD3 spec for all button variants).
 * All M3 buttons (Filled, Outlined, Text, Elevated, Tonal) use fully rounded corners.
 */
val ButtonShape = ShapeFull

/**
 * Card shape: 16dp (MD3 large).
 * Intentional deviation from strict 12dp medium — 16dp feels more approachable for children.
 */
val CardShape = ShapeLarge

/**
 * Input field shape: 12dp.
 * Between MD3 small (8dp) and medium (12dp). Retained for visual warmth in a kids app.
 */
val InputFieldShape = ShapeMedium

/**
 * Chip/Tag shape: pill/full
 */
val ChipShape = ShapeFull

/**
 * Bottom sheet shape: 28dp top corners only (MD3 extra-large).
 */
val BottomSheetShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 28.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

/**
 * Dialog shape: 28dp all corners (MD3 extra-large).
 */
val DialogShape = ShapeExtraLarge

/**
 * Small component shape: 8dp (MD3 small).
 */
val SmallComponentShape = ShapeSmall

/**
 * Medium component shape: 12dp (MD3 medium).
 */
val MediumComponentShape = ShapeMedium

/**
 * Large component shape: 16dp (MD3 large).
 */
val LargeComponentShape = ShapeLarge

// ==================== Material 3 Shapes ====================
/**
 * Material 3 shapes configuration for the theme.
 *
 * MD3 mapping:
 * - small  -> 4.dp  (chips, snackbars)
 * - medium -> 12.dp (cards)
 * - large  -> 16.dp (FABs, navigation drawer)
 */
fun funnyShapes(): Shapes {
    return Shapes(
        small = ShapeExtraSmall,
        medium = ShapeMedium,
        large = ShapeLarge
    )
}

// ==================== Extension Functions ====================
/**
 * Create a custom rounded shape with specified corner radius
 */
fun roundedShape(cornerRadius: Int): Shape {
    return RoundedCornerShape(cornerRadius.dp)
}

/**
 * Create a shape with different radii for each corner
 */
fun customShape(
    topStart: Int = 0,
    topEnd: Int = 0,
    bottomStart: Int = 0,
    bottomEnd: Int = 0
): Shape {
    return RoundedCornerShape(
        topStart = topStart.dp,
        topEnd = topEnd.dp,
        bottomStart = bottomStart.dp,
        bottomEnd = bottomEnd.dp
    )
}
