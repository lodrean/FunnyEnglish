// GENERATED FILE — не редактировать вручную.
// Источник: .docs/design-system/tokens.json; генератор: scripts/generate_design_tokens.py.
package com.sotospeak.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.designsystem.tokens.NunitoFontFamily

/**
 * Speaking Trainer — токены Playful Coach v1.3.1 (.docs/design-system/tokens.json).
 *
 * Используются speaking-экранами (legacy-палитра FunnyColorScheme удалена, bd FunnyEnglish-2oz.6).
 * Доступ: `MaterialTheme.speakingColors` или `LocalSpeakingColors.current`.
 *
 * WCAG: на record-фоне (#FF9F6B) — только тёмный текст [text] (5.8:1);
 * белый на record = 2.0:1 (FAIL). textMuted 3.9:1 — только large text.
 */
@Immutable
data class SpeakingColors(
    val primary: Color,            // #5B8DEF — навигация, play-контролы
    val primaryStrong: Color,      // #3B6FD4 — белый текст на кнопках/чипах/nav (4.76:1 AA, аудит 2026-08-01)
    val onPrimary: Color,
    val primaryContainer: Color,   // #DDE8FD
    val onPrimaryContainer: Color, // #1A2F5E
    val secondary: Color,          // #9B7EDE — фирменный фиолетовый
    val onSecondary: Color,        // контент на secondary (dark: #1A2F5E — белый на #B79EED = 2.2:1 FAIL)
    val secondaryContainer: Color, // #E5DCFF (note-bg)
    val onSecondaryContainer: Color, // текст на secondaryContainer (аватар профиля)
    val background: Color,         // #EEF3FF светлый / #161A2E тёмный
    val surface: Color,
    val surfaceVariant: Color,     // трек таймер-кольца
    val text: Color,
    val textMuted: Color,
    val outline: Color,
    val record: Color,             // #FF9F6B — персиковый, НЕ error!
    val onRecord: Color,           // тёмный текст на record (WCAG AA)
    val recordActive: Color,       // #D97238 — waveform при записи (аудит 2026-08-01)
    val recordShadow: Color,       // #D97238 — жёсткая тень rec-кнопки
    val recordContainer: Color,    // #FFE3D1 — подложка record-элементов (аудит 2026-08-01)
    val onRecordContainer: Color,  // #8A3B0E — текст на recordContainer
    val waveformPlayback: Color,   // #5B8DEF
    val timerLevel80: Color,
    val timerLevel50: Color,
    val timerLevel30: Color,
    val statusNew: Color,          // #FB8C00
    val statusNewContainer: Color, // #FFE0B2
    val statusReviewed: Color,     // #43A047
    val statusReviewedContainer: Color, // #C8E6C9
    val success: Color,
    val error: Color,              // #E53935
    val errorText: Color,          // #B3261E — мелкий текст ошибок (WCAG AA 6.4:1, --color-error даёт только 4.29:1)
    val scrimSubtitle: Color,      // #000000B3 — подложка субтитров 70%
    val scrimVideoControls: Color  // #00000080
)

val LightSpeakingColors = SpeakingColors(
    primary = Color(0xFF5B8DEF),
    primaryStrong = Color(0xFF3B6FD4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE8FD),
    onPrimaryContainer = Color(0xFF1A2F5E),
    secondary = Color(0xFF9B7EDE),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5DCFF),
    onSecondaryContainer = Color(0xFF5B3FA8),
    background = Color(0xFFEEF3FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFD8E2FA),
    text = Color(0xFF2D3561),
    textMuted = Color(0xFF58609A),
    outline = Color(0xFFB9C7EE),
    record = Color(0xFFFF9F6B),
    onRecord = Color(0xFF2D3561),
    recordActive = Color(0xFFD97238),
    recordShadow = Color(0xFFD97238),
    recordContainer = Color(0xFFFFE3D1),
    onRecordContainer = Color(0xFF8A3B0E),
    waveformPlayback = Color(0xFF5B8DEF),
    timerLevel80 = Color(0xFF4A7FE8),
    timerLevel50 = Color(0xFF8A68D6),
    timerLevel30 = Color(0xFFD97238),
    statusNew = Color(0xFFFB8C00),
    statusNewContainer = Color(0xFFFFE0B2),
    statusReviewed = Color(0xFF43A047),
    statusReviewedContainer = Color(0xFFC8E6C9),
    success = Color(0xFF43A047),
    error = Color(0xFFE53935),
    errorText = Color(0xFFB3261E), // нет в tokens.json: M3 error text role, кандидат на расширение токенов
    scrimSubtitle = Color(0xB3000000),
    scrimVideoControls = Color(0x80000000)
)

val DarkSpeakingColors = LightSpeakingColors.copy(
    primary = Color(0xFF8FB3F5),
    primaryStrong = Color(0xFF8FB3F5),
    onPrimary = Color(0xFF1A2F5E),            // errata dark-ролей: белый на #8FB3F5 = 2.2:1 FAIL
    onSecondary = Color(0xFF1A2F5E),          // errata dark-ролей: белый на #B79EED = 2.2:1 FAIL
    primaryContainer = Color(0xFF2E3E6E),     // v1.3.0 M3 dark
    onPrimaryContainer = Color(0xFFDDE8FD),   // v1.3.0 M3 dark
    secondary = Color(0xFFB79EED),
    secondaryContainer = Color(0xFF46366F),   // v1.3.0 M3 dark
    onSecondaryContainer = Color(0xFFE5DCFF), // v1.3.0 M3 dark
    background = Color(0xFF161A2E),
    surface = Color(0xFF1F2440),
    surfaceVariant = Color(0xFF2B3152),
    text = Color(0xFFE8EAF6),
    textMuted = Color(0xFF9AA0C4),
    outline = Color(0xFF3D4568),
    record = Color(0xFFFFB27D),
    onRecord = Color(0xFF161A2E),
    recordContainer = Color(0xFF59311C),      // v1.3.0 M3 dark (был #4A2A18)
    onRecordContainer = Color(0xFFFFD9C2),    // v1.3.0 M3 dark (был #FFCCAA)
    statusNew = Color(0xFFFFB74D),           // нет в tokens.json: dark-статусы, кандидат на расширение
    statusNewContainer = Color(0xFF3D2A0A),  // нет в tokens.json
    statusReviewed = Color(0xFF81C784),      // нет в tokens.json
    statusReviewedContainer = Color(0xFF1B4D1F), // нет в tokens.json
    errorText = Color(0xFFF2B8B5)  // M3 dark error: на #161A2E читается по AA (нет в tokens.json)
)

val LocalSpeakingColors = staticCompositionLocalOf { LightSpeakingColors }

/** Текстовые стили Speaking Trainer (tokens.json font.scale).
 * Базовый шрифт — bundled Nunito (composeResources), таймер — mono tnum. */
@Immutable
object SpeakingTextStyles {
    /** Вопросы читаются с расстояния вытянутой руки: 25sp, w600, lineHeight 1.35 */
    val QuestionText: TextStyle
        @Composable get() = TextStyle(
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 25.sp,
            lineHeight = 34.sp
        )

    /** Таймер: моноширинные tabular-цифры (tnum) — не прыгает по ширине, 64sp */
    val TimerDisplay = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        fontFeatureSettings = "tnum"
    )

    /** Субтитры поверх scrim-подложки: 17sp, lineHeight 1.4 */
    val SubtitleText: TextStyle
        @Composable get() = TextStyle(
            fontFamily = NunitoFontFamily,
            fontSize = 17.sp,
            lineHeight = 24.sp
        )
}

/** Формы Speaking Trainer (tokens.json radius) */
@Immutable
object SpeakingShapes {
    val Recorder = RoundedCornerShape(22.dp)   // squircle кнопки записи, НЕ круг
    val Card = RoundedCornerShape(22.dp)       // фирменный радиус Variant B
    val CardLarge = RoundedCornerShape(26.dp)  // onb-emoji карточка онбординга
    val Button = RoundedCornerShape(16.dp)     // кнопки и input'ы auth (radius-button)
    val Chip = RoundedCornerShape(12.dp)
    val Sheet = RoundedCornerShape(28.dp)      // top corners bottom sheet
    val StatusPill = RoundedCornerShape(999.dp)
}

/** Жёсткая «оттопыренная» тень rec-кнопки: 0 4px 0 recordShadow; при нажатии — 1dp */
@Immutable
object SpeakingElevation {
    val RecorderShadowOffsetY = 4.dp
    val RecorderShadowPressedOffsetY = 1.dp
}

/** Motion-токены Speaking Trainer (tokens.json motion) */
@Immutable
object SpeakingMotion {
    /** Основной easing UI-переходов: cubic-bezier(0.16, 1, 0.3, 1) */
    val EasingStandard = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    /** Игровой overshoot для ✅ попыток и появления кнопок: cubic-bezier(0.34, 1.56, 0.64, 1) */
    val EasingBounce = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

    const val DurationFast = 150
    const val DurationMedium = 300
    const val DurationSlow = 500
    /** Пульсация REC; при Reduce motion — статичный индикатор */
    const val RecPulseMs = 1600
    /** Токен motion.timerAnnounceInterval (5s): TalkBack-анонс остатка таймера
     * не чаще раза в 5с, чтобы не спамить скринридер (бриф §3, §3.1 Д2) */
    const val TimerAnnounceIntervalSeconds = 5

    // M3 motion (tokens v1.3.0 / DSM-5 §3)
    /** M3 Standard: cubic-bezier(0.2, 0, 0, 1) — стандартные переходы */
    val EasingM3Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    /** M3 Emphasized: cubic-bezier(0.05, 0.7, 0.1, 1) — экранные переходы */
    val EasingM3Emphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    /** M3 state-анимации (hover/press/смена цвета уровня таймера) */
    const val DurationState = 200

    fun <T> tweenFast(): TweenSpec<T> = tween(DurationFast, easing = EasingStandard)
    fun <T> tweenMedium(): TweenSpec<T> = tween(DurationMedium, easing = EasingStandard)
    fun <T> tweenSlow(): TweenSpec<T> = tween(DurationSlow, easing = EasingStandard)
    fun <T> tweenBounce(): TweenSpec<T> = tween(DurationSlow, easing = EasingBounce)
}
