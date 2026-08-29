package com.sotospeak.designsystem.tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sotospeak.composeapp.generated.resources.Res
import com.sotospeak.composeapp.generated.resources.nunito_bold
import com.sotospeak.composeapp.generated.resources.nunito_extrabold
import com.sotospeak.composeapp.generated.resources.nunito_medium
import com.sotospeak.composeapp.generated.resources.nunito_regular
import com.sotospeak.composeapp.generated.resources.nunito_semibold
import org.jetbrains.compose.resources.Font

/**
 * So to Speak Typography System
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
// Bundled Nunito (Playful Coach, tokens.json font.family.brand) через composeResources —
// статические веса 400/500/600/700/800, полные TTF с кириллицей
// (composeApp/src/commonMain/composeResources/font/).
val NunitoFontFamily: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.nunito_regular, FontWeight.Normal),
        Font(Res.font.nunito_medium, FontWeight.Medium),
        Font(Res.font.nunito_semibold, FontWeight.SemiBold),
        Font(Res.font.nunito_bold, FontWeight.Bold),
        Font(Res.font.nunito_extrabold, FontWeight.ExtraBold)
    )

// ==================== Display Scale ====================
/**
 * Display Large: 57sp / 64sp / Bold
 * Hero headlines, welcome screens, major celebrations
 */
val DisplayLarge: TextStyle
    @Composable get() = TextStyle(
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
val DisplayMedium: TextStyle
    @Composable get() = TextStyle(
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
val DisplaySmall: TextStyle
    @Composable get() = TextStyle(
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
val HeadlineLarge: TextStyle
    @Composable get() = TextStyle(
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
val HeadlineMedium: TextStyle
    @Composable get() = TextStyle(
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
val HeadlineSmall: TextStyle
    @Composable get() = TextStyle(
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
val TitleLarge: TextStyle
    @Composable get() = TextStyle(
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
val TitleMedium: TextStyle
    @Composable get() = TextStyle(
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
val TitleSmall: TextStyle
    @Composable get() = TextStyle(
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
val BodyLarge: TextStyle
    @Composable get() = TextStyle(
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
val BodyMedium: TextStyle
    @Composable get() = TextStyle(
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
val BodySmall: TextStyle
    @Composable get() = TextStyle(
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
val LabelLarge: TextStyle
    @Composable get() = TextStyle(
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
val LabelMedium: TextStyle
    @Composable get() = TextStyle(
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
val LabelSmall: TextStyle
    @Composable get() = TextStyle(
    fontFamily = NunitoFontFamily,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeightMedium,
    letterSpacing = 0.5.sp
)

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
