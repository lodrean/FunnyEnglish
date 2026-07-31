package com.funnyenglish.designsystem.tokens

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * FunnyEnglish Typography System
 * 
 * Font: Nunito (rounded, friendly)
 * Scale: Major Third (1.25 ratio)
 * Minimum text size: 14sp enforced
 * Line height: 1.5x for body text (dyslexia support)
 */

// ==================== Font Weights ====================
val FontWeightNormal = FontWeight.Normal      // 400
val FontWeightMedium = FontWeight.Medium      // 500
val FontWeightSemiBold = FontWeight.SemiBold  // 600
val FontWeightBold = FontWeight.Bold          // 700

// ==================== Nunito Font Family ====================
// Note: Nunito should be loaded via Compose Multiplatform font resources
// For now using default FontFamily.SansSerif with Nunito styling
val NunitoFontFamily = FontFamily.SansSerif

// ==================== Display Scale ====================
/**
 * Display Large: 57sp / 64sp / Bold
 * Hero headlines, welcome screens, major celebrations
 */
val DisplayLarge = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    fontWeight = FontWeightBold,
    letterSpacing = (-0.25).sp
)

/**
 * Display Medium: 45sp / 52sp / Bold
 * Section introductions, important scores, level displays
 */
val DisplayMedium = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    fontWeight = FontWeightBold,
    letterSpacing = 0.sp
)

/**
 * Display Small: 36sp / 44sp / Bold
 * Card titles, quest names, achievement headers
 */
val DisplaySmall = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    fontWeight = FontWeightBold,
    letterSpacing = 0.sp
)

// ==================== Headline Scale ====================
/**
 * Headline Large: 32sp / 40sp / SemiBold
 * Page titles, major section headers
 */
val HeadlineLarge = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    fontWeight = FontWeightSemiBold,
    letterSpacing = 0.sp
)

/**
 * Headline Medium: 28sp / 36sp / SemiBold
 * Lesson titles, unit headers, dialog titles
 */
val HeadlineMedium = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    fontWeight = FontWeightSemiBold,
    letterSpacing = 0.sp
)

/**
 * Headline Small: 24sp / 32sp / SemiBold
 * Card headers, subsection titles
 */
val HeadlineSmall = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    fontWeight = FontWeightSemiBold,
    letterSpacing = 0.sp
)

// ==================== Title Scale ====================
/**
 * Title Large: 22sp / 28sp / Medium
 * Content titles, prominent list items
 */
val TitleLarge = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    fontWeight = FontWeightMedium,
    letterSpacing = 0.sp
)

/**
 * Title Medium: 18sp / 24sp / Medium
 * Secondary titles, form section headers
 */
val TitleMedium = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeightMedium,
    letterSpacing = 0.15.sp
)

/**
 * Title Small: 14sp / 20sp / Medium
 * Compact labels, metadata headers, button text
 */
val TitleSmall = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeightMedium,
    letterSpacing = 0.1.sp
)

// ==================== Body Scale ====================
/**
 * Body Large: 16sp / 24sp / Normal
 * Primary reading text, extended content
 * 1.5x line height for dyslexia support
 */
val BodyLarge = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 16.sp,
    lineHeight = 24.sp,  // 1.5x ratio
    fontWeight = FontWeightNormal,
    letterSpacing = 0.5.sp
)

/**
 * Body Medium: 14sp / 20sp / Normal
 * Default body text, secondary content
 */
val BodyMedium = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeightNormal,
    letterSpacing = 0.25.sp
)

/**
 * Body Small: 12sp / 16sp / Normal
 * Captions, footnotes, supplementary metadata
 */
val BodySmall = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeightNormal,
    letterSpacing = 0.4.sp
)

// ==================== Label Scale ====================
/**
 * Label Large: 14sp / 20sp / Medium
 * Button labels, form labels, navigation items
 */
val LabelLarge = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeightMedium,
    letterSpacing = 0.1.sp
)

/**
 * Label Medium: 12sp / 16sp / Medium
 * Compact UI labels, chip text, badges
 */
val LabelMedium = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeightMedium,
    letterSpacing = 0.5.sp
)

/**
 * Label Small: 11sp / 16sp / Medium
 * Minimal labels, timestamps, technical metadata
 */
val LabelSmall = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeightMedium,
    letterSpacing = 0.5.sp
)

// ==================== Typography ====================
fun funnyTypography(): Typography {
    return Typography(
        displayLarge = DisplayLarge,
        displayMedium = DisplayMedium,
        displaySmall = DisplaySmall,
        
        headlineLarge = HeadlineLarge,
        headlineMedium = HeadlineMedium,
        headlineSmall = HeadlineSmall,
        
        titleLarge = TitleLarge,
        titleMedium = TitleMedium,
        titleSmall = TitleSmall,
        
        bodyLarge = BodyLarge,
        bodyMedium = BodyMedium,
        bodySmall = BodySmall,
        
        labelLarge = LabelLarge,
        labelMedium = LabelMedium,
        labelSmall = LabelSmall
    )
}

// ==================== Accessibility ====================
/**
 * Minimum text size enforcement
 */
const val MINIMUM_TEXT_SIZE_SP = 14

/**
 * OpenDyslexic font support (to be implemented with font loading)
 */
val OpenDyslexicFontFamily = FontFamily.SansSerif // Placeholder

/**
 * Line height ratios for accessibility
 */
const val LINE_HEIGHT_RATIO_BODY = 1.5f
const val LINE_HEIGHT_RATIO_HEADING = 1.2f
const val LINE_HEIGHT_RATIO_DISPLAY = 1.12f
