package com.sotospeak.designsystem.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Speaking Trainer icon set for So to Speak EdTech application.
 * Translated 1:1 from `.docs/design-system/icons.svg` (24x24, stroke 2, round caps).
 *
 * Stroke icons use `fill = null` + `stroke = SolidColor(Color.Black)` so that
 * `Icon(imageVector, tint = ...)` recolors both fill- and stroke-based icons uniformly.
 */
object SpeakingIcons {

    // ==================== MIC ====================

    /**
     * Microphone icon (i-mic) - stroke variant.
     * Used for voice recording start in speaking trainer.
     */
    val Mic: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingMic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // rect x=9 y=2 w=6 h=12 rx=3
                moveTo(12f, 2f)
                arcTo(3f, 3f, 0f, false, true, 15f, 5f)
                verticalLineTo(11f)
                arcTo(3f, 3f, 0f, false, true, 12f, 14f)
                arcTo(3f, 3f, 0f, false, true, 9f, 11f)
                verticalLineTo(5f)
                arcTo(3f, 3f, 0f, false, true, 12f, 2f)
                close()
                // M5 10a7 7 0 0 0 14 0
                moveTo(5f, 10f)
                arcTo(7f, 7f, 0f, false, false, 19f, 10f)
                // line 12,17 -> 12,21
                moveTo(12f, 17f)
                verticalLineTo(21f)
                // line 8,21 -> 16,21
                moveTo(8f, 21f)
                horizontalLineTo(16f)
            }
        }.build()

    // ==================== MIC OFF ====================

    /**
     * Muted microphone icon (i-mic-off) - stroke variant.
     * Used when microphone access is denied or recording is disabled.
     */
    val MicOff: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingMicOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // line 2,2 -> 22,22
                moveTo(2f, 2f)
                lineTo(22f, 22f)
                // M9 5v4a3 3 0 0 0 5.12 2.12
                moveTo(9f, 5f)
                verticalLineTo(9f)
                arcToRelative(3f, 3f, 0f, false, false, 5.12f, 2.12f)
                // M15 9.34V5a3 3 0 0 0-5.94-.6
                moveTo(15f, 9.34f)
                verticalLineTo(5f)
                arcToRelative(3f, 3f, 0f, false, false, -5.94f, -0.6f)
                // M5 10a7 7 0 0 0 11.9 5.1
                moveTo(5f, 10f)
                arcToRelative(7f, 7f, 0f, false, false, 11.9f, 5.1f)
                // M19 10a6.97 6.97 0 0 1-.5 2.6
                moveTo(19f, 10f)
                arcToRelative(6.97f, 6.97f, 0f, false, true, -0.5f, 2.6f)
                // line 12,17 -> 12,21
                moveTo(12f, 17f)
                verticalLineTo(21f)
            }
        }.build()

    // ==================== PLAY ====================

    /**
     * Play icon (i-play) - fill variant.
     * Used for playing reference/user recordings.
     */
    val Play: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingPlay",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                // M8 5.5v13a1 1 0 0 0 1.54.84l10-6.5a1 1 0 0 0 0-1.68l-10-6.5A1 1 0 0 0 8 5.5z
                moveTo(8f, 5.5f)
                verticalLineToRelative(13f)
                arcToRelative(1f, 1f, 0f, false, false, 1.54f, 0.84f)
                lineToRelative(10f, -6.5f)
                arcToRelative(1f, 1f, 0f, false, false, 0f, -1.68f)
                lineToRelative(-10f, -6.5f)
                arcTo(1f, 1f, 0f, false, false, 8f, 5.5f)
                close()
            }
        }.build()

    // ==================== PAUSE ====================

    /**
     * Pause icon (i-pause) - fill variant.
     * Used for pausing playback.
     */
    val Pause: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingPause",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                // rect x=6 y=4 w=4 h=16 rx=1.5
                moveTo(7.5f, 4f)
                horizontalLineTo(8.5f)
                arcTo(1.5f, 1.5f, 0f, false, true, 10f, 5.5f)
                verticalLineTo(18.5f)
                arcTo(1.5f, 1.5f, 0f, false, true, 8.5f, 20f)
                horizontalLineTo(7.5f)
                arcTo(1.5f, 1.5f, 0f, false, true, 6f, 18.5f)
                verticalLineTo(5.5f)
                arcTo(1.5f, 1.5f, 0f, false, true, 7.5f, 4f)
                close()
                // rect x=14 y=4 w=4 h=16 rx=1.5
                moveTo(15.5f, 4f)
                horizontalLineTo(16.5f)
                arcTo(1.5f, 1.5f, 0f, false, true, 18f, 5.5f)
                verticalLineTo(18.5f)
                arcTo(1.5f, 1.5f, 0f, false, true, 16.5f, 20f)
                horizontalLineTo(15.5f)
                arcTo(1.5f, 1.5f, 0f, false, true, 14f, 18.5f)
                verticalLineTo(5.5f)
                arcTo(1.5f, 1.5f, 0f, false, true, 15.5f, 4f)
                close()
            }
        }.build()

    // ==================== STOP ====================

    /**
     * Stop icon (i-stop) - fill variant.
     * Used for stopping recording/playback.
     */
    val Stop: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingStop",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                // rect x=6 y=6 w=12 h=12 rx=2.5
                moveTo(8.5f, 6f)
                horizontalLineTo(15.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 18f, 8.5f)
                verticalLineTo(15.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 15.5f, 18f)
                horizontalLineTo(8.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 6f, 15.5f)
                verticalLineTo(8.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 8.5f, 6f)
                close()
            }
        }.build()

    // ==================== DELETE ====================

    /**
     * Trash/delete icon (i-delete) - stroke variant.
     * Used for deleting user recordings.
     */
    val Delete: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingDelete",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // M3 6h18
                moveTo(3f, 6f)
                horizontalLineTo(21f)
                // M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2
                moveTo(8f, 6f)
                verticalLineTo(4f)
                arcTo(1f, 1f, 0f, false, true, 9f, 3f)
                horizontalLineTo(15f)
                arcTo(1f, 1f, 0f, false, true, 16f, 4f)
                verticalLineTo(6f)
                // M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6
                moveTo(19f, 6f)
                lineTo(18f, 20f)
                arcTo(2f, 2f, 0f, false, true, 16f, 22f)
                horizontalLineTo(8f)
                arcTo(2f, 2f, 0f, false, true, 6f, 20f)
                lineTo(5f, 6f)
                // line 10,11 -> 10,17
                moveTo(10f, 11f)
                verticalLineTo(17f)
                // line 14,11 -> 14,17
                moveTo(14f, 11f)
                verticalLineTo(17f)
            }
        }.build()

    // ==================== REFRESH ====================

    /**
     * Refresh/retry icon (i-refresh) - stroke variant.
     * Used for retrying pronunciation attempts.
     */
    val Refresh: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingRefresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // M21 12a9 9 0 1 1-2.64-6.36
                moveTo(21f, 12f)
                arcToRelative(9f, 9f, 0f, true, true, -2.64f, -6.36f)
                // polyline 21 3 21 9 15 9
                moveTo(21f, 3f)
                verticalLineTo(9f)
                horizontalLineTo(15f)
            }
        }.build()

    // ==================== CLOSED CAPTIONS ====================

    /**
     * Closed captions icon (i-cc) - stroke variant.
     * Used for toggling transcript/subtitles display.
     */
    val ClosedCaptions: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingClosedCaptions",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // rect x=2 y=4 w=20 h=16 rx=3
                moveTo(5f, 4f)
                horizontalLineTo(19f)
                arcTo(3f, 3f, 0f, false, true, 22f, 7f)
                verticalLineTo(17f)
                arcTo(3f, 3f, 0f, false, true, 19f, 20f)
                horizontalLineTo(5f)
                arcTo(3f, 3f, 0f, false, true, 2f, 17f)
                verticalLineTo(7f)
                arcTo(3f, 3f, 0f, false, true, 5f, 4f)
                close()
                // M10.5 10.2a2.5 2.5 0 1 0 0 3.6
                moveTo(10.5f, 10.2f)
                arcToRelative(2.5f, 2.5f, 0f, true, false, 0f, 3.6f)
                // M17.5 10.2a2.5 2.5 0 1 0 0 3.6
                moveTo(17.5f, 10.2f)
                arcToRelative(2.5f, 2.5f, 0f, true, false, 0f, 3.6f)
            }
        }.build()

    // ==================== LOCK ====================

    /**
     * Lock icon (i-lock) - stroke variant.
     * Used for locked lessons/exercises.
     */
    val Lock: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingLock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // rect x=4 y=11 w=16 h=10 rx=2.5
                moveTo(6.5f, 11f)
                horizontalLineTo(17.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 20f, 13.5f)
                verticalLineTo(18.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 17.5f, 21f)
                horizontalLineTo(6.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 4f, 18.5f)
                verticalLineTo(13.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 6.5f, 11f)
                close()
                // M8 11V7a4 4 0 0 1 8 0v4
                moveTo(8f, 11f)
                verticalLineTo(7f)
                arcTo(4f, 4f, 0f, false, true, 16f, 7f)
                verticalLineTo(11f)
            }
        }.build()

    // ==================== UPLOAD ====================

    /**
     * Cloud upload icon (i-upload) - stroke variant.
     * Used for uploading recordings to the backend.
     */
    val Upload: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingUpload",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // M17.5 18.5a4.5 4.5 0 0 0 .42-8.98 6 6 0 0 0-11.7 1.62A4 4 0 0 0 7 18.5h10.5z
                moveTo(17.5f, 18.5f)
                arcToRelative(4.5f, 4.5f, 0f, false, false, 0.42f, -8.98f)
                arcToRelative(6f, 6f, 0f, false, false, -11.7f, 1.62f)
                arcTo(4f, 4f, 0f, false, false, 7f, 18.5f)
                horizontalLineTo(17.5f)
                close()
                // polyline 9 14 12 11 15 14
                moveTo(9f, 14f)
                lineTo(12f, 11f)
                lineTo(15f, 14f)
                // line 12,11 -> 12,17
                moveTo(12f, 11f)
                verticalLineTo(17f)
            }
        }.build()

    // ==================== CHECK CIRCLE ====================

    /**
     * Check circle icon (i-check-circle) - stroke variant.
     * Used for successful pronunciation/completion states.
     */
    val CheckCircle: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingCheckCircle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // circle cx=12 cy=12 r=9.5
                moveTo(21.5f, 12f)
                arcTo(9.5f, 9.5f, 0f, true, true, 2.5f, 12f)
                arcTo(9.5f, 9.5f, 0f, true, true, 21.5f, 12f)
                close()
                // polyline 7.5 12.5 10.5 15.5 16.5 8.5
                moveTo(7.5f, 12.5f)
                lineTo(10.5f, 15.5f)
                lineTo(16.5f, 8.5f)
            }
        }.build()

    // ==================== WAVEFORM ====================

    /**
     * Waveform icon (i-waveform) - stroke variant.
     * Used for audio visualization/recording indicator.
     */
    val Waveform: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingWaveform",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(2f, 10f)
                verticalLineTo(14f)
                moveTo(6f, 7f)
                verticalLineTo(17f)
                moveTo(10f, 4f)
                verticalLineTo(20f)
                moveTo(14f, 8f)
                verticalLineTo(16f)
                moveTo(18f, 5f)
                verticalLineTo(19f)
                moveTo(22f, 10f)
                verticalLineTo(14f)
            }
        }.build()

    // ==================== CLOCK ====================

    /**
     * Clock icon (i-clock) - stroke variant.
     * Used for recording duration/time limits.
     */
    val Clock: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingClock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // circle cx=12 cy=12 r=9.5
                moveTo(21.5f, 12f)
                arcTo(9.5f, 9.5f, 0f, true, true, 2.5f, 12f)
                arcTo(9.5f, 9.5f, 0f, true, true, 21.5f, 12f)
                close()
                // polyline 12 6.5 12 12 16 14
                moveTo(12f, 6.5f)
                verticalLineTo(12f)
                lineTo(16f, 14f)
            }
        }.build()

    // ==================== CHEVRON RIGHT ====================

    /**
     * Chevron right icon (i-chevron-right) - stroke variant.
     * Used for navigation to next exercise/screen.
     */
    val ChevronRight: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // polyline 9 5 16 12 9 19
                moveTo(9f, 5f)
                lineTo(16f, 12f)
                lineTo(9f, 19f)
            }
        }.build()

    // ==================== SHIELD ====================

    /**
     * Shield icon (i-shield) - stroke variant.
     * Used for privacy/safety indicators (kids mode).
     */
    val Shield: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingShield",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // M12 2.5 4.5 6v5.5c0 5 3.2 8.6 7.5 10 4.3-1.4 7.5-5 7.5-10V6z
                moveTo(12f, 2.5f)
                lineTo(4.5f, 6f)
                verticalLineTo(11.5f)
                curveToRelative(0f, 5f, 3.2f, 8.6f, 7.5f, 10f)
                curveToRelative(4.3f, -1.4f, 7.5f, -5f, 7.5f, -10f)
                verticalLineTo(6f)
                close()
                // polyline 8.5 12 11 14.5 15.5 9.5
                moveTo(8.5f, 12f)
                lineTo(11f, 14.5f)
                lineTo(15.5f, 9.5f)
            }
        }.build()

    // ==================== BOTTOMNAV (аудит 2026-08-01: home/send/user) ====================

    /** Home icon (i-home) — bottomnav «Темы» */
    val Home: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingHome",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // M3 10.5 12 3l9 7.5
                moveTo(3f, 10.5f)
                lineTo(12f, 3f)
                lineTo(21f, 10.5f)
                // M5.5 9.5V20a1 1 0 0 0 1 1h11a1 1 0 0 0 1-1V9.5
                moveTo(5.5f, 9.5f)
                verticalLineTo(20f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 1f)
                horizontalLineToRelative(11f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, -1f)
                verticalLineTo(9.5f)
                // M9.5 21v-6h5v6
                moveTo(9.5f, 21f)
                verticalLineToRelative(-6f)
                horizontalLineToRelative(5f)
                verticalLineToRelative(6f)
            }
        }.build()

    /** Send icon (i-send) — bottomnav «Отправки» */
    val Send: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingSend",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // M21 3 10.5 13.5
                moveTo(21f, 3f)
                lineTo(10.5f, 13.5f)
                // M21 3 14 21l-3.5-7.5L3 10z
                moveTo(21f, 3f)
                lineTo(14f, 21f)
                lineTo(10.5f, 13.5f)
                lineTo(3f, 10f)
                close()
            }
        }.build()

    /** User icon (i-user) — bottomnav «Профиль» */
    val User: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingUser",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // circle cx=12 cy=8 r=4
                moveTo(16f, 8f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = true, isPositiveArc = false, 8f, 8f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = true, isPositiveArc = false, 16f, 8f)
                // M4.5 20.5c1.5-3.5 4.5-5 7.5-5s6 1.5 7.5 5
                moveTo(4.5f, 20.5f)
                curveToRelative(1.5f, -3.5f, 4.5f, -5f, 7.5f, -5f)
                reflectiveCurveToRelative(6f, 1.5f, 7.5f, 5f)
            }
        }.build()

    // ==================== FULLSCREEN ====================

    /**
     * Fullscreen icon (развернуть видео) - stroke variant, углы наружу.
     * Используется в control-bar видеоплеера (спека Part 2 §2.3, v1.7).
     */
    val Fullscreen: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingFullscreen",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // M8 3H5a2 2 0 0 0-2 2v3
                moveTo(8f, 3f)
                horizontalLineTo(5f)
                arcTo(2f, 2f, 0f, false, false, 3f, 5f)
                verticalLineTo(8f)
                // M21 8V5a2 2 0 0 0-2-2h-3
                moveTo(21f, 8f)
                verticalLineTo(5f)
                arcTo(2f, 2f, 0f, false, false, 19f, 3f)
                horizontalLineTo(16f)
                // M16 21h3a2 2 0 0 0 2-2v-3
                moveTo(16f, 21f)
                horizontalLineTo(19f)
                arcTo(2f, 2f, 0f, false, false, 21f, 19f)
                verticalLineTo(16f)
                // M3 16v3a2 2 0 0 0 2 2h3
                moveTo(3f, 16f)
                verticalLineTo(19f)
                arcTo(2f, 2f, 0f, false, false, 5f, 21f)
                horizontalLineTo(8f)
            }
        }.build()

    /**
     * Fullscreen-exit icon (свернуть видео) - stroke variant, углы внутрь.
     */
    val FullscreenExit: ImageVector
        get() = ImageVector.Builder(
            name = "SpeakingFullscreenExit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                // M8 3v3a2 2 0 0 1-2 2H3
                moveTo(8f, 3f)
                verticalLineTo(6f)
                arcTo(2f, 2f, 0f, false, true, 6f, 8f)
                horizontalLineTo(3f)
                // M21 8h-3a2 2 0 0 1-2-2V3
                moveTo(21f, 8f)
                horizontalLineTo(18f)
                arcTo(2f, 2f, 0f, false, true, 16f, 6f)
                verticalLineTo(3f)
                // M16 21v-3a2 2 0 0 1 2-2h3
                moveTo(16f, 21f)
                verticalLineTo(18f)
                arcTo(2f, 2f, 0f, false, true, 18f, 16f)
                horizontalLineTo(21f)
                // M3 16h3a2 2 0 0 1 2 2v3
                moveTo(3f, 16f)
                horizontalLineTo(6f)
                arcTo(2f, 2f, 0f, false, true, 8f, 18f)
                verticalLineTo(21f)
            }
        }.build()
}
