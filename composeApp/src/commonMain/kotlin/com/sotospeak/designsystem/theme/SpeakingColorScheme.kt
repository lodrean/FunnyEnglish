// GENERATED FILE — не редактировать вручную.
// Источник: .docs/design-system/tokens.json; генератор: scripts/generate_design_tokens.py.
package com.sotospeak.designsystem.theme

import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.designsystem.tokens.NunitoFontFamily

/**
 * M3 color scheme Playful Coach — DSM-5 §1.1 (docs/design/M3_IMPLEMENTATION_MAPPING.md),
 * значения HEX 1:1 из tokens.json v1.3.1.
 *
 * Ключевое правило WCAG (спека §3): light `primary` = primaryStrong #3B6FD4,
 * потому что M3 кладёт белый onPrimary на primary (белый на #5B8DEF = 3.23:1 FAIL).
 * «Красивый» #5B8DEF остаётся в surfaceTint / иконках / ссылках.
 *
 * Отступление от DSM-5 (dark onPrimary/onSecondary): в таблице §1.1 указан #FFFFFF,
 * но белый на #8FB3F5/#B79EED = ~2.2:1 FAIL. Взята тёмная пара #1A2F5E (M3-конвенция
 * dark: тёмный контент на светлом primary) — вопрос вынесен в отчёт владельцу.
 */
fun speakingLightColorScheme() = lightColorScheme(
    primary = Color(0xFF3B6FD4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE8FD),
    onPrimaryContainer = Color(0xFF1A2F5E),
    secondary = Color(0xFF9B7EDE),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5DCFF),
    onSecondaryContainer = Color(0xFF5B3FA8),
    tertiary = Color(0xFF006C4C),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFE53935),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFEEF3FF),
    onBackground = Color(0xFF2D3561),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2D3561),
    surfaceVariant = Color(0xFFD8E2FA),
    onSurfaceVariant = Color(0xFF58609A),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F8FF),
    surfaceContainer = Color(0xFFE9EFFE),
    surfaceContainerHigh = Color(0xFFE2E9FB),
    surfaceContainerHighest = Color(0xFFD8E2FA),
    outline = Color(0xFFB9C7EE),
    outlineVariant = Color(0xFFD4DDF5),
    inverseSurface = Color(0xFF2D3561),
    inverseOnSurface = Color(0xFFEEF3FF),
    inversePrimary = Color(0xFF8FB3F5),
    scrim = Color(0x80000000),
    surfaceTint = Color(0xFF5B8DEF)
)

fun speakingDarkColorScheme() = darkColorScheme(
    primary = Color(0xFF8FB3F5),
    onPrimary = Color(0xFF1A2F5E),
    primaryContainer = Color(0xFF2E3E6E),
    onPrimaryContainer = Color(0xFFDDE8FD),
    secondary = Color(0xFFB79EED),
    onSecondary = Color(0xFF1A2F5E),
    secondaryContainer = Color(0xFF46366F),
    onSecondaryContainer = Color(0xFFE5DCFF),
    tertiary = Color(0xFF006C4C),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFE53935),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFF161A2E),
    onBackground = Color(0xFFE8EAF6),
    surface = Color(0xFF1F2440),
    onSurface = Color(0xFFE8EAF6),
    surfaceVariant = Color(0xFF2B3152),
    onSurfaceVariant = Color(0xFF9AA0C4),
    surfaceContainerLowest = Color(0xFF101424),
    surfaceContainerLow = Color(0xFF181D36),
    surfaceContainer = Color(0xFF1F2440),
    surfaceContainerHigh = Color(0xFF262B49),
    surfaceContainerHighest = Color(0xFF2B3152),
    outline = Color(0xFF3D4568),
    outlineVariant = Color(0xFF2E3556),
    inverseSurface = Color(0xFFE8EAF6),
    inverseOnSurface = Color(0xFF2D3561),
    inversePrimary = Color(0xFF3B6FD4),
    scrim = Color(0x80000000),
    surfaceTint = Color(0xFF8FB3F5)
)

/**
 * M3 type scale Playful Coach — DSM-5 §2. Размеры/веса Nunito без изменений
 * (tokens.json font.scale), роли — по M3.
 * Основной шрифт — bundled Nunito (composeResources), таймер/таймстемпы — mono tnum.
 */
@Composable
fun speakingTypography() = Typography(
    // timerDisplay 64 · mono tnum · 700
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        fontFeatureSettings = "tnum"
    ),
    // headlineSmall 31 · 800
    headlineSmall = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 31.sp
    ),
    // questionText 25/1.35 · 600
    titleLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp,
        lineHeight = 34.sp
    ),
    // titleMedium 20 · 800
    titleMedium = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp
    ),
    // bodyMedium 16 · 400
    bodyLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    // bodySmall 14 · 400
    bodyMedium = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    // labelSmall 12 · 800 · caps
    labelSmall = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        letterSpacing = 0.72.sp
    ),
    // timestamps · mono tnum
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        fontFeatureSettings = "tnum"
    ),
    // Кнопки M3 (labelLarge): 16 · 800 (weight extrabold из tokens; Nunito 800 даёт акцент,
    // uppercase не нужен — как в MUI-override Theme.ts)
    labelLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp
    )
)

/** M3 shapes-шкала Playful Coach — DSM-5 §2: small=12(chip), medium=16(button), large=22(card), extraLarge=28(sheet/dialog) */
fun speakingShapes() = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
