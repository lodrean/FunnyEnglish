package com.funnyenglish.designsystem.tokens

import androidx.compose.ui.unit.dp

/**
 * FunnyEnglish Spacing System
 * 
 * Base unit: 4dp
 * Mathematical relationships: 2x, 4x, 6x, 8x, 12x, 16x
 * 
 * Applications:
 * - xs: Micro-adjustments, icon padding
 * - sm: Related element separation
 * - md: Standard spacing
 * - lg: Section breaks
 * - xl: Page padding
 * - xxl: Hero spacing
 * - xxxl: Maximum emphasis
 */

// ==================== Base Unit ====================
val SpaceBase = 4.dp

// ==================== Spacing Tokens ====================
/**
 * Extra Small: 4dp
 * Micro-adjustments, icon padding, tight grouping
 */
val SpaceXs = 4.dp

/**
 * Small: 8dp (2x base)
 * Related element separation, internal component padding
 */
val SpaceSm = 8.dp

/**
 * Medium: 16dp (4x base)
 * Standard spacing, card internal padding, section separation
 */
val SpaceMd = 16.dp

/**
 * Large: 24dp (6x base)
 * Major section breaks, expanded card padding
 */
val SpaceLg = 24.dp

/**
 * Extra Large: 32dp (8x base)
 * Page-level padding, major content divisions
 */
val SpaceXl = 32.dp

/**
 * Extra Extra Large: 48dp (12x base)
 * Hero section spacing, prominent separations
 */
val SpaceXxl = 48.dp

/**
 * Extra Extra Extra Large: 64dp (16x base)
 * Maximum emphasis, focused content, brand moments
 */
val SpaceXxxl = 64.dp

// ==================== Touch Targets ====================
/**
 * Minimum touch target size
 * WCAG 2.1 Level AA compliance
 */
val TouchTargetMin = 48.dp

/**
 * Recommended touch target for primary actions
 * Enhanced for children's motor development
 */
val TouchTargetRecommended = 56.dp

/**
 * Minimum spacing between adjacent touch targets
 * Prevents accidental taps
 */
val TouchTargetSpacing = 8.dp

// ==================== Component Dimensions ====================
/**
 * Button heights
 */
val ButtonHeightSmall = 36.dp
val ButtonHeightMedium = 48.dp
val ButtonHeightLarge = 56.dp  // CTA size

/**
 * Button padding
 */
val ButtonPaddingHorizontalSmall = 16.dp
val ButtonPaddingHorizontalMedium = 24.dp
val ButtonPaddingHorizontalLarge = 24.dp

/**
 * Icon sizes
 */
val IconSizeSmall = 20.dp
val IconSizeMedium = 24.dp
val IconSizeLarge = 32.dp
val IconSizeXLarge = 48.dp

/**
 * Card specifications
 */
val CardRadius = 16.dp  // MD3 large shape, playful deviation from strict 12dp medium
val CardPadding = 16.dp
val CardElevationDefault = 1.dp  // MD3 level 1 tonal elevation
val CardElevationFeatured = 3.dp  // MD3 level 2 tonal elevation

/**
 * Input field specifications
 */
val InputHeight = 56.dp
val InputRadius = 12.dp
val InputBorderWidth = 1.dp
val InputBorderWidthFocused = 2.dp

/**
 * Chip specifications
 */
val ChipRadius = 50.dp  // Pill shape
val ChipPaddingHorizontal = 12.dp
val ChipPaddingVertical = 6.dp

/**
 * Bottom sheet specifications
 */
val BottomSheetRadiusTop = 28.dp  // MD3 extra-large shape

/**
 * Sidebar specifications
 */
val SidebarWidth = 200.dp
val SidebarItemHeight = 48.dp

/**
 * Elevation levels
 */
// MD3 tonal elevation levels ( communicated via surface color, not just shadow )
val ElevationLevel0 = 0.dp
val ElevationLevel1 = 1.dp
val ElevationLevel2 = 3.dp
val ElevationLevel3 = 6.dp
val ElevationLevel4 = 8.dp
val ElevationLevel5 = 12.dp

// Legacy aliases for backward compatibility during migration
val ElevationSmall = ElevationLevel1
val ElevationMedium = ElevationLevel2
val ElevationLarge = ElevationLevel4
val ElevationXLarge = ElevationLevel5

// ==================== Focus Ring ====================
/**
 * Focus ring width for keyboard navigation
 */
val FocusRingWidth = 2.dp

/**
 * Focus ring padding
 */
val FocusRingPadding = 2.dp
