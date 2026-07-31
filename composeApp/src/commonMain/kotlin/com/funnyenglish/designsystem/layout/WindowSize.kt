package com.funnyenglish.designsystem.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * FunnyEnglish Adaptive Layout utilities
 *
 * Material Design 3 window size classes for responsive navigation and layout.
 *
 * Breakpoints:
 * - Compact:   width < 600dp   (phones)
 * - Medium:    600dp <= width < 840dp   (small tablets, foldables)
 * - Expanded:  840dp <= width < 1200dp  (large tablets)
 * - Large:     1200dp <= width < 1600dp (small desktops)
 * - ExtraLarge: width >= 1600dp         (large desktops)
 */

enum class WindowWidthSizeClass {
    COMPACT,    // < 600dp
    MEDIUM,     // 600dp – 839dp
    EXPANDED,   // 840dp – 1199dp
    LARGE,      // 1200dp – 1599dp
    EXTRALARGE  // >= 1600dp
}

enum class WindowHeightSizeClass {
    COMPACT,    // < 480dp
    MEDIUM,     // 480dp – 899dp
    EXPANDED    // >= 900dp
}

/**
 * Calculates the window width size class from the given width.
 */
fun calculateWindowWidthSizeClass(width: Dp): WindowWidthSizeClass {
    return when {
        width >= 1600.dp -> WindowWidthSizeClass.EXTRALARGE
        width >= 1200.dp -> WindowWidthSizeClass.LARGE
        width >= 840.dp -> WindowWidthSizeClass.EXPANDED
        width >= 600.dp -> WindowWidthSizeClass.MEDIUM
        else -> WindowWidthSizeClass.COMPACT
    }
}

/**
 * Calculates the window height size class from the given height.
 */
fun calculateWindowHeightSizeClass(height: Dp): WindowHeightSizeClass {
    return when {
        height >= 900.dp -> WindowHeightSizeClass.EXPANDED
        height >= 480.dp -> WindowHeightSizeClass.MEDIUM
        else -> WindowHeightSizeClass.COMPACT
    }
}

/**
 * Recommended max content width for readability on large screens.
 * MD3 guidance: constrain text content to 840–1040dp on large+ windows.
 */
val MaxContentWidth = 1040.dp

/**
 * Determines the appropriate navigation type based on window width.
 *
 * - COMPACT  -> Bottom navigation bar
 * - MEDIUM   -> Navigation rail
 * - EXPANDED+ -> Navigation drawer (modal or permanent)
 */
fun WindowWidthSizeClass.toNavigationType(): NavigationType {
    return when (this) {
        WindowWidthSizeClass.COMPACT -> NavigationType.BOTTOM_NAVIGATION
        WindowWidthSizeClass.MEDIUM -> NavigationType.NAVIGATION_RAIL
        WindowWidthSizeClass.EXPANDED,
        WindowWidthSizeClass.LARGE,
        WindowWidthSizeClass.EXTRALARGE -> NavigationType.NAVIGATION_DRAWER
    }
}

enum class NavigationType {
    BOTTOM_NAVIGATION,
    NAVIGATION_RAIL,
    NAVIGATION_DRAWER
}

/**
 * Determines if the current width supports a two-pane / list-detail layout.
 * Typically true for EXPANDED and above.
 */
fun WindowWidthSizeClass.supportsListDetail(): Boolean {
    return this >= WindowWidthSizeClass.EXPANDED
}

/**
 * Determines if content should be constrained to [MaxContentWidth] for readability.
 * True for LARGE and EXTRALARGE.
 */
fun WindowWidthSizeClass.shouldConstrainContentWidth(): Boolean {
    return this >= WindowWidthSizeClass.LARGE
}
