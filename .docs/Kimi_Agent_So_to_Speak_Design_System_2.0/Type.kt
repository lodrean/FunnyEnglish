package com.funnyenglish.design.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
object AppFontFamily {
    val Nunito = FontFamily(
        Font(com.funnyenglish.design.R.font.nunito_light, FontWeight.Light),
        Font(com.funnyenglish.design.R.font.nunito_regular, FontWeight.Normal),
        Font(com.funnyenglish.design.R.font.nunito_medium, FontWeight.Medium),
        Font(com.funnyenglish.design.R.font.nunito_semibold, FontWeight.SemiBold),
        Font(com.funnyenglish.design.R.font.nunito_bold, FontWeight.Bold),
        Font(com.funnyenglish.design.R.font.nunito_extrabold, FontWeight.ExtraBold)
    )
}

@Stable
val AppTypography = Typography(
    // Display - Large titles, brand moments
    displayLarge = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 68.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 54.sp,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 43.sp,
        letterSpacing = (-0.5).sp
    ),

    // Headline - Section headers, important content
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.5).sp
    ),

    // Title - Card titles, list headers
    titleLarge = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 19.sp,
        letterSpacing = (-0.5).sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 17.sp,
        letterSpacing = (-0.5).sp
    ),

    // Body - Primary reading text
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp
    ),

    // Label - Buttons, captions, small text
    labelLarge = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.25.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily.Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.25.sp
    )
)
