package com.sotospeak.design.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom icon library for So to Speak EdTech application.
 * Contains Material-style icons in Filled and Outlined variants.
 */
object CustomIcons {

    // ==================== AUDIO WAVEFORM ====================

    /**
     * Audio waveform icon - Filled variant.
     * Used for audio tests and listening exercises.
     */
    val AudioWaveformFilled: ImageVector
        get() = ImageVector.Builder(
            name = "AudioWaveformFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 3f)
                curveTo(12.55f, 3f, 13f, 3.45f, 13f, 4f)
                verticalLineTo(20f)
                curveTo(13f, 20.55f, 12.55f, 21f, 12f, 21f)
                curveTo(11.45f, 21f, 11f, 20.55f, 11f, 20f)
                verticalLineTo(4f)
                curveTo(11f, 3.45f, 11.45f, 3f, 12f, 3f)
                close()
                moveTo(7f, 7f)
                curveTo(7.55f, 7f, 8f, 7.45f, 8f, 8f)
                verticalLineTo(16f)
                curveTo(8f, 16.55f, 7.55f, 17f, 7f, 17f)
                curveTo(6.45f, 17f, 6f, 16.55f, 6f, 16f)
                verticalLineTo(8f)
                curveTo(6f, 7.45f, 6.45f, 7f, 7f, 7f)
                close()
                moveTo(17f, 7f)
                curveTo(17.55f, 7f, 18f, 7.45f, 18f, 8f)
                verticalLineTo(16f)
                curveTo(18f, 16.55f, 17.55f, 17f, 17f, 17f)
                curveTo(16.45f, 17f, 16f, 16.55f, 16f, 16f)
                verticalLineTo(8f)
                curveTo(16f, 7.45f, 16.45f, 7f, 17f, 7f)
                close()
                moveTo(3f, 10f)
                curveTo(3.55f, 10f, 4f, 10.45f, 4f, 11f)
                verticalLineTo(13f)
                curveTo(4f, 13.55f, 3.55f, 14f, 3f, 14f)
                curveTo(2.45f, 14f, 2f, 13.55f, 2f, 13f)
                verticalLineTo(11f)
                curveTo(2f, 10.45f, 2.45f, 10f, 3f, 10f)
                close()
                moveTo(21f, 10f)
                curveTo(21.55f, 10f, 22f, 10.45f, 22f, 11f)
                verticalLineTo(13f)
                curveTo(22f, 13.55f, 21.55f, 14f, 21f, 14f)
                curveTo(20.45f, 14f, 20f, 13.55f, 20f, 13f)
                verticalLineTo(11f)
                curveTo(20f, 10.45f, 20.45f, 10f, 21f, 10f)
                close()
            }
        }.build()

    /**
     * Audio waveform icon - Outlined variant.
     * Used for audio tests and listening exercises.
     */
    val AudioWaveformOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "AudioWaveformOutlined",
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
                moveTo(12f, 4f)
                verticalLineTo(20f)
                moveTo(7f, 8f)
                verticalLineTo(16f)
                moveTo(17f, 8f)
                verticalLineTo(16f)
                moveTo(3f, 11f)
                verticalLineTo(13f)
                moveTo(21f, 11f)
                verticalLineTo(13f)
            }
        }.build()

    // ==================== MICROPHONE ====================

    /**
     * Microphone icon - Filled variant.
     * Used for speaking exercises and voice recording.
     */
    val MicrophoneFilled: ImageVector
        get() = ImageVector.Builder(
            name = "MicrophoneFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 14f)
                curveTo(13.66f, 14f, 15f, 12.66f, 15f, 11f)
                verticalLineTo(5f)
                curveTo(15f, 3.34f, 13.66f, 2f, 12f, 2f)
                curveTo(10.34f, 2f, 9f, 3.34f, 9f, 5f)
                verticalLineTo(11f)
                curveTo(9f, 12.66f, 10.34f, 14f, 12f, 14f)
                close()
                moveTo(19f, 11f)
                curveTo(19f, 14.53f, 16.39f, 17.44f, 13f, 17.93f)
                verticalLineTo(21f)
                horizontalLineTo(11f)
                verticalLineTo(17.93f)
                curveTo(7.61f, 17.44f, 5f, 14.53f, 5f, 11f)
                horizontalLineTo(7f)
                curveTo(7f, 13.76f, 9.24f, 16f, 12f, 16f)
                curveTo(14.76f, 16f, 17f, 13.76f, 17f, 11f)
                horizontalLineTo(19f)
                close()
            }
        }.build()

    /**
     * Microphone icon - Outlined variant.
     * Used for speaking exercises and voice recording.
     */
    val MicrophoneOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "MicrophoneOutlined",
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
                moveTo(12f, 14f)
                curveTo(13.66f, 14f, 15f, 12.66f, 15f, 11f)
                verticalLineTo(5f)
                curveTo(15f, 3.34f, 13.66f, 2f, 12f, 2f)
                curveTo(10.34f, 2f, 9f, 3.34f, 9f, 5f)
                verticalLineTo(11f)
                curveTo(9f, 12.66f, 10.34f, 14f, 12f, 14f)
                close()
                moveTo(19f, 11f)
                curveTo(19f, 14.53f, 16.39f, 17.44f, 13f, 17.93f)
                verticalLineTo(21f)
                horizontalLineTo(11f)
                verticalLineTo(17.93f)
                curveTo(7.61f, 17.44f, 5f, 14.53f, 5f, 11f)
            }
        }.build()

    // ==================== BOOK OPEN ====================

    /**
     * Book open icon - Filled variant.
     * Used for reading exercises and lessons.
     */
    val BookOpenFilled: ImageVector
        get() = ImageVector.Builder(
            name = "BookOpenFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(21f, 5f)
                curveTo(19.89f, 4.65f, 18.67f, 4.5f, 17.5f, 4.5f)
                curveTo(15.55f, 4.5f, 13.45f, 4.9f, 12f, 6f)
                curveTo(10.55f, 4.9f, 8.45f, 4.5f, 6.5f, 4.5f)
                curveTo(5.33f, 4.5f, 4.11f, 4.65f, 3f, 5f)
                verticalLineTo(19.65f)
                curveTo(3f, 19.9f, 3.25f, 20.1f, 3.5f, 20f)
                curveTo(4.75f, 19.3f, 6.27f, 18.96f, 7.77f, 18.96f)
                curveTo(9.24f, 18.96f, 10.93f, 19.28f, 12f, 20f)
                curveTo(13.07f, 19.28f, 14.76f, 18.96f, 16.23f, 18.96f)
                curveTo(17.73f, 18.96f, 19.25f, 19.3f, 20.5f, 20f)
                curveTo(20.75f, 20.1f, 21f, 19.9f, 21f, 19.65f)
                verticalLineTo(5f)
                moveTo(12f, 18f)
                verticalLineTo(7f)
                curveTo(13.07f, 6.15f, 14.93f, 5.75f, 16.5f, 5.75f)
                curveTo(17.63f, 5.75f, 18.88f, 5.89f, 20f, 6.18f)
                verticalLineTo(17.63f)
                curveTo(18.88f, 17.33f, 17.63f, 17.19f, 16.5f, 17.19f)
                curveTo(14.93f, 17.19f, 13.07f, 17.58f, 12f, 18f)
                close()
            }
        }.build()

    /**
     * Book open icon - Outlined variant.
     * Used for reading exercises and lessons.
     */
    val BookOpenOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "BookOpenOutlined",
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
                moveTo(21f, 5f)
                curveTo(19.89f, 4.65f, 18.67f, 4.5f, 17.5f, 4.5f)
                curveTo(15.55f, 4.5f, 13.45f, 4.9f, 12f, 6f)
                curveTo(10.55f, 4.9f, 8.45f, 4.5f, 6.5f, 4.5f)
                curveTo(5.33f, 4.5f, 4.11f, 4.65f, 3f, 5f)
                verticalLineTo(19.65f)
                curveTo(3f, 19.9f, 3.25f, 20.1f, 3.5f, 20f)
                curveTo(4.75f, 19.3f, 6.27f, 18.96f, 7.77f, 18.96f)
                curveTo(9.24f, 18.96f, 10.93f, 19.28f, 12f, 20f)
                curveTo(13.07f, 19.28f, 14.76f, 18.96f, 16.23f, 18.96f)
                curveTo(17.73f, 18.96f, 19.25f, 19.3f, 20.5f, 20f)
                curveTo(20.75f, 20.1f, 21f, 19.9f, 21f, 19.65f)
                verticalLineTo(5f)
                close()
                moveTo(12f, 18f)
                verticalLineTo(7f)
                curveTo(13.07f, 6.15f, 14.93f, 5.75f, 16.5f, 5.75f)
                curveTo(17.63f, 5.75f, 18.88f, 5.89f, 20f, 6.18f)
                verticalLineTo(17.63f)
                curveTo(18.88f, 17.33f, 17.63f, 17.19f, 16.5f, 17.19f)
                curveTo(14.93f, 17.19f, 13.07f, 17.58f, 12f, 18f)
                close()
            }
        }.build()

    // ==================== PENCIL EDIT ====================

    /**
     * Pencil edit icon - Filled variant.
     * Used for writing exercises and editing.
     */
    val PencilEditFilled: ImageVector
        get() = ImageVector.Builder(
            name = "PencilEditFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(3f, 17.25f)
                verticalLineTo(21f)
                horizontalLineTo(6.75f)
                lineTo(17.81f, 9.94f)
                lineTo(14.06f, 6.19f)
                lineTo(3f, 17.25f)
                close()
                moveTo(20.71f, 7.04f)
                curveTo(21.1f, 6.65f, 21.1f, 6.02f, 20.71f, 5.63f)
                lineTo(18.37f, 3.29f)
                curveTo(17.98f, 2.9f, 17.35f, 2.9f, 16.96f, 3.29f)
                lineTo(15.13f, 5.12f)
                lineTo(18.88f, 8.87f)
                lineTo(20.71f, 7.04f)
                close()
            }
        }.build()

    /**
     * Pencil edit icon - Outlined variant.
     * Used for writing exercises and editing.
     */
    val PencilEditOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "PencilEditOutlined",
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
                moveTo(3f, 17.25f)
                verticalLineTo(21f)
                horizontalLineTo(6.75f)
                lineTo(17.81f, 9.94f)
                lineTo(14.06f, 6.19f)
                lineTo(3f, 17.25f)
                close()
                moveTo(20.71f, 7.04f)
                curveTo(21.1f, 6.65f, 21.1f, 6.02f, 20.71f, 5.63f)
                lineTo(18.37f, 3.29f)
                curveTo(17.98f, 2.9f, 17.35f, 2.9f, 16.96f, 3.29f)
                lineTo(15.13f, 5.12f)
                lineTo(18.88f, 8.87f)
                lineTo(20.71f, 7.04f)
                close()
            }
        }.build()

    // ==================== TROPHY STAR ====================

    /**
     * Trophy star icon - Filled variant.
     * Used for achievements and rewards.
     */
    val TrophyStarFilled: ImageVector
        get() = ImageVector.Builder(
            name = "TrophyStarFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(18f, 2f)
                curveTo(18f, 2f, 17f, 2.64f, 17f, 4f)
                curveTo(17f, 4.44f, 17.19f, 4.84f, 17.5f, 5.13f)
                curveTo(16.53f, 6.6f, 14.83f, 7.55f, 12.92f, 7.81f)
                curveTo(12.7f, 7.28f, 12.2f, 6.9f, 11.61f, 6.9f)
                curveTo(10.72f, 6.9f, 10f, 7.62f, 10f, 8.5f)
                curveTo(10f, 8.63f, 10.02f, 8.75f, 10.05f, 8.87f)
                curveTo(7.26f, 8.6f, 4.91f, 6.91f, 3.74f, 4.5f)
                curveTo(3.88f, 4.35f, 4f, 4.18f, 4f, 4f)
                curveTo(4f, 2.64f, 3f, 2f, 3f, 2f)
                horizontalLineTo(2f)
                verticalLineTo(5f)
                curveTo(2f, 8.87f, 4.69f, 12.1f, 8.25f, 13.06f)
                verticalLineTo(18f)
                horizontalLineTo(6f)
                verticalLineTo(20f)
                horizontalLineTo(10f)
                verticalLineTo(22f)
                horizontalLineTo(14f)
                verticalLineTo(20f)
                horizontalLineTo(18f)
                verticalLineTo(18f)
                horizontalLineTo(15.75f)
                verticalLineTo(13.06f)
                curveTo(19.31f, 12.1f, 22f, 8.87f, 22f, 5f)
                verticalLineTo(2f)
                horizontalLineTo(18f)
                close()
                moveTo(12f, 10f)
                lineTo(13.09f, 12.26f)
                lineTo(15.59f, 12.63f)
                lineTo(13.8f, 14.4f)
                lineTo(14.24f, 16.9f)
                lineTo(12f, 15.77f)
                lineTo(9.76f, 16.9f)
                lineTo(10.2f, 14.4f)
                lineTo(8.41f, 12.63f)
                lineTo(10.91f, 12.26f)
                lineTo(12f, 10f)
                close()
            }
        }.build()

    /**
     * Trophy star icon - Outlined variant.
     * Used for achievements and rewards.
     */
    val TrophyStarOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "TrophyStarOutlined",
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
                moveTo(18f, 2f)
                curveTo(18f, 2f, 17f, 2.64f, 17f, 4f)
                curveTo(17f, 4.44f, 17.19f, 4.84f, 17.5f, 5.13f)
                curveTo(16.53f, 6.6f, 14.83f, 7.55f, 12.92f, 7.81f)
                curveTo(12.7f, 7.28f, 12.2f, 6.9f, 11.61f, 6.9f)
                curveTo(10.72f, 6.9f, 10f, 7.62f, 10f, 8.5f)
                curveTo(10f, 8.63f, 10.02f, 8.75f, 10.05f, 8.87f)
                curveTo(7.26f, 8.6f, 4.91f, 6.91f, 3.74f, 4.5f)
                curveTo(3.88f, 4.35f, 4f, 4.18f, 4f, 4f)
                curveTo(4f, 2.64f, 3f, 2f, 3f, 2f)
                horizontalLineTo(2f)
                verticalLineTo(5f)
                curveTo(2f, 8.87f, 4.69f, 12.1f, 8.25f, 13.06f)
                verticalLineTo(18f)
                horizontalLineTo(6f)
                verticalLineTo(20f)
                horizontalLineTo(10f)
                verticalLineTo(22f)
                horizontalLineTo(14f)
                verticalLineTo(20f)
                horizontalLineTo(18f)
                verticalLineTo(18f)
                horizontalLineTo(15.75f)
                verticalLineTo(13.06f)
                curveTo(19.31f, 12.1f, 22f, 8.87f, 22f, 5f)
                verticalLineTo(2f)
                horizontalLineTo(18f)
                close()
                moveTo(12f, 10f)
                lineTo(13.09f, 12.26f)
                lineTo(15.59f, 12.63f)
                lineTo(13.8f, 14.4f)
                lineTo(14.24f, 16.9f)
                lineTo(12f, 15.77f)
                lineTo(9.76f, 16.9f)
                lineTo(10.2f, 14.4f)
                lineTo(8.41f, 12.63f)
                lineTo(10.91f, 12.26f)
                lineTo(12f, 10f)
                close()
            }
        }.build()

    // ==================== LIGHTNING BOLT ====================

    /**
     * Lightning bolt icon - Filled variant.
     * Used for XP boost and power-ups.
     */
    val LightningBoltFilled: ImageVector
        get() = ImageVector.Builder(
            name = "LightningBoltFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(11f, 21f)
                horizontalLineToRelative(-1f)
                lineToRelative(1f, -7f)
                horizontalLineTo(7.5f)
                curveTo(7.09f, 14f, 6.88f, 13.52f, 7.13f, 13.18f)
                lineTo(12f, 3f)
                horizontalLineToRelative(1f)
                lineToRelative(-1f, 7f)
                horizontalLineToRelative(3.5f)
                curveTo(16.91f, 10f, 17.12f, 10.48f, 16.87f, 10.82f)
                lineTo(12f, 21f)
                close()
            }
        }.build()

    /**
     * Lightning bolt icon - Outlined variant.
     * Used for XP boost and power-ups.
     */
    val LightningBoltOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "LightningBoltOutlined",
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
                moveTo(13f, 2f)
                lineTo(3f, 14f)
                horizontalLineTo(12f)
                lineTo(11f, 22f)
                lineTo(21f, 10f)
                horizontalLineTo(12f)
                lineTo(13f, 2f)
                close()
            }
        }.build()

    // ==================== BRAIN / HEAD ====================

    /**
     * Brain/Head icon - Filled variant.
     * Used for adaptive learning and cognitive features.
     */
    val BrainFilled: ImageVector
        get() = ImageVector.Builder(
            name = "BrainFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(13f, 3f)
                curveTo(9.23f, 3f, 6.19f, 5.95f, 6.02f, 9.66f)
                lineTo(4.08f, 9.05f)
                curveTo(3.45f, 8.86f, 3f, 9.5f, 3f, 10.09f)
                curveTo(3f, 10.86f, 3.5f, 11.5f, 4.24f, 11.68f)
                lineTo(6.08f, 12.14f)
                curveTo(6.03f, 12.42f, 6f, 12.7f, 6f, 13f)
                curveTo(6f, 16.87f, 9.13f, 20f, 13f, 20f)
                curveTo(16.87f, 20f, 20f, 16.87f, 20f, 13f)
                curveTo(20f, 9.13f, 16.87f, 6f, 13f, 6f)
                curveTo(12.7f, 6f, 12.42f, 6.03f, 12.14f, 6.08f)
                lineTo(11.68f, 4.24f)
                curveTo(11.5f, 3.5f, 10.86f, 3f, 10.09f, 3f)
                curveTo(9.5f, 3f, 8.86f, 3.45f, 9.05f, 4.08f)
                lineTo(9.66f, 6.02f)
                curveTo(10.08f, 5.42f, 10.71f, 5f, 11.44f, 5f)
                curveTo(11.78f, 5f, 12.1f, 5.09f, 12.38f, 5.25f)
                curveTo(14.32f, 5.85f, 15.8f, 7.63f, 15.8f, 9.75f)
                curveTo(15.8f, 12.26f, 13.76f, 14.3f, 11.25f, 14.3f)
                curveTo(10.35f, 14.3f, 9.5f, 14.03f, 8.78f, 13.54f)
                curveTo(8.3f, 13.2f, 7.65f, 13.31f, 7.31f, 13.79f)
                curveTo(6.97f, 14.27f, 7.08f, 14.92f, 7.56f, 15.26f)
                curveTo(8.62f, 16.02f, 9.91f, 16.43f, 11.25f, 16.43f)
                curveTo(14.93f, 16.43f, 17.93f, 13.43f, 17.93f, 9.75f)
                curveTo(17.93f, 6.07f, 14.93f, 3.07f, 11.25f, 3.07f)
                close()
                moveTo(13f, 8f)
                curveTo(12.45f, 8f, 12f, 8.45f, 12f, 9f)
                curveTo(12f, 9.55f, 12.45f, 10f, 13f, 10f)
                curveTo(13.55f, 10f, 14f, 9.55f, 14f, 9f)
                curveTo(14f, 8.45f, 13.55f, 8f, 13f, 8f)
                close()
                moveTo(10f, 10f)
                curveTo(9.45f, 10f, 9f, 10.45f, 9f, 11f)
                curveTo(9f, 11.55f, 9.45f, 12f, 10f, 12f)
                curveTo(10.55f, 12f, 11f, 11.55f, 11f, 11f)
                curveTo(11f, 10.45f, 10.55f, 10f, 10f, 10f)
                close()
            }
        }.build()

    /**
     * Brain/Head icon - Outlined variant.
     * Used for adaptive learning and cognitive features.
     */
    val BrainOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "BrainOutlined",
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
                moveTo(9.5f, 2f)
                arcTo(2.5f, 2.5f, 0f, false, false, 7f, 4.5f)
                verticalLineTo(5f)
                curveTo(4.46f, 5.67f, 2.5f, 8.14f, 2.5f, 11f)
                curveTo(2.5f, 13.03f, 3.44f, 14.87f, 4.89f, 16.12f)
                lineTo(6.5f, 22f)
                horizontalLineTo(17.5f)
                lineTo(19.11f, 16.12f)
                curveTo(20.56f, 14.87f, 21.5f, 13.03f, 21.5f, 11f)
                curveTo(21.5f, 8.14f, 19.54f, 5.67f, 17f, 5f)
                verticalLineTo(4.5f)
                arcTo(2.5f, 2.5f, 0f, false, false, 14.5f, 2f)
                horizontalLineTo(9.5f)
                close()
                moveTo(8f, 9f)
                arcTo(1f, 1f, 0f, false, true, 10f, 9f)
                arcTo(1f, 1f, 0f, false, true, 8f, 9f)
                close()
                moveTo(14f, 9f)
                arcTo(1f, 1f, 0f, false, true, 16f, 9f)
                arcTo(1f, 1f, 0f, false, true, 14f, 9f)
                close()
                moveTo(12f, 16f)
                curveTo(10.5f, 16f, 9.2f, 15.2f, 8.4f, 14f)
            }
        }.build()

    // ==================== TARGET ====================

    /**
     * Target icon - Filled variant.
     * Used for daily goals and objectives.
     */
    val TargetFilled: ImageVector
        get() = ImageVector.Builder(
            name = "TargetFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(12f, 20f)
                curveTo(7.59f, 20f, 4f, 16.41f, 4f, 12f)
                curveTo(4f, 7.59f, 7.59f, 4f, 12f, 4f)
                curveTo(16.41f, 4f, 20f, 7.59f, 20f, 12f)
                curveTo(20f, 16.41f, 16.41f, 20f, 12f, 20f)
                close()
                moveTo(12f, 6f)
                curveTo(8.69f, 6f, 6f, 8.69f, 6f, 12f)
                curveTo(6f, 15.31f, 8.69f, 18f, 12f, 18f)
                curveTo(15.31f, 18f, 18f, 15.31f, 18f, 12f)
                curveTo(18f, 8.69f, 15.31f, 6f, 12f, 6f)
                close()
                moveTo(12f, 16f)
                curveTo(9.79f, 16f, 8f, 14.21f, 8f, 12f)
                curveTo(8f, 9.79f, 9.79f, 8f, 12f, 8f)
                curveTo(14.21f, 8f, 16f, 9.79f, 16f, 12f)
                curveTo(16f, 14.21f, 14.21f, 16f, 12f, 16f)
                close()
            }
        }.build()

    /**
     * Target icon - Outlined variant.
     * Used for daily goals and objectives.
     */
    val TargetOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "TargetOutlined",
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
                moveTo(12f, 12f)
                moveTo(6f, 12f)
                arcTo(6f, 6f, 0f, false, true, 18f, 12f)
                arcTo(6f, 6f, 0f, false, true, 6f, 12f)
                close()
                moveTo(12f, 12f)
                moveTo(2f, 12f)
                arcTo(10f, 10f, 0f, false, true, 22f, 12f)
                arcTo(10f, 10f, 0f, false, true, 2f, 12f)
                close()
                moveTo(12f, 12f)
                moveTo(10f, 12f)
                arcTo(2f, 2f, 0f, false, true, 14f, 12f)
                arcTo(2f, 2f, 0f, false, true, 10f, 12f)
                close()
            }
        }.build()

    // ==================== STREAK FLAME ====================

    /**
     * Streak flame icon - Filled variant.
     * Used for learning streak tracking.
     */
    val StreakFlameFilled: ImageVector
        get() = ImageVector.Builder(
            name = "StreakFlameFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(13.5f, 0.67f)
                curveTo(13.5f, 0.67f, 13.5f, 0.67f, 13.5f, 0.67f)
                curveTo(13.5f, 0.67f, 13.5f, 0.67f, 13.5f, 0.67f)
                curveTo(12.65f, 2.52f, 11.83f, 4.73f, 11.5f, 7f)
                curveTo(11.5f, 7f, 11.5f, 7f, 11.5f, 7f)
                curveTo(11.5f, 7f, 11.5f, 7f, 11.5f, 7f)
                curveTo(10.5f, 5.25f, 9.5f, 3.5f, 8.5f, 2f)
                curveTo(8.5f, 2f, 8.5f, 2f, 8.5f, 2f)
                curveTo(8.5f, 2f, 8.5f, 2f, 8.5f, 2f)
                curveTo(6.5f, 4.5f, 5f, 7.5f, 5f, 11f)
                curveTo(5f, 16.19f, 8.95f, 20.45f, 14f, 20.93f)
                verticalLineTo(22f)
                horizontalLineTo(17f)
                verticalLineTo(20.94f)
                curveTo(21.35f, 20.53f, 24.83f, 16.85f, 24.83f, 12.5f)
                curveTo(24.83f, 9f, 23f, 6f, 20.5f, 4f)
                curveTo(20.5f, 4f, 20.5f, 4f, 20.5f, 4f)
                curveTo(20.5f, 4f, 20.5f, 4f, 20.5f, 4f)
                curveTo(20f, 5.5f, 19.5f, 7f, 19f, 8.5f)
                curveTo(19f, 8.5f, 19f, 8.5f, 19f, 8.5f)
                curveTo(19f, 8.5f, 19f, 8.5f, 19f, 8.5f)
                curveTo(17.5f, 6f, 16f, 3.5f, 13.5f, 0.67f)
                curveTo(13.5f, 0.67f, 13.5f, 0.67f, 13.5f, 0.67f)
                close()
            }
        }.build()

    /**
     * Streak flame icon - Outlined variant.
     * Used for learning streak tracking.
     */
    val StreakFlameOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "StreakFlameOutlined",
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
                moveTo(8.5f, 14.5f)
                curveTo(8.5f, 14.5f, 10f, 13f, 12f, 13f)
                curveTo(14f, 13f, 15.5f, 14.5f, 15.5f, 14.5f)
                moveTo(12f, 22f)
                curveTo(12f, 22f, 20f, 18f, 20f, 12f)
                curveTo(20f, 6f, 16f, 2f, 12f, 2f)
                curveTo(8f, 2f, 4f, 6f, 4f, 12f)
                curveTo(4f, 18f, 12f, 22f, 12f, 22f)
                close()
                moveTo(12f, 2f)
                lineTo(14f, 7f)
                lineTo(17f, 4f)
                curveTo(17f, 4f, 18f, 6f, 18f, 8f)
                curveTo(18f, 10f, 16f, 12f, 16f, 12f)
            }
        }.build()

    // ==================== STAR ====================

    /**
     * Star icon - Filled variant.
     * Used for ratings and favorites.
     */
    val StarFilled: ImageVector
        get() = ImageVector.Builder(
            name = "StarFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 17.27f)
                lineTo(18.18f, 21f)
                lineTo(16.54f, 13.97f)
                lineTo(22f, 9.24f)
                lineTo(14.81f, 8.62f)
                lineTo(12f, 2f)
                lineTo(9.19f, 8.62f)
                lineTo(2f, 9.24f)
                lineTo(7.45f, 13.97f)
                lineTo(5.82f, 21f)
                lineTo(12f, 17.27f)
                close()
            }
        }.build()

    /**
     * Star icon - Outlined variant.
     * Used for ratings and favorites.
     */
    val StarOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "StarOutlined",
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
                moveTo(12f, 17.27f)
                lineTo(18.18f, 21f)
                lineTo(16.54f, 13.97f)
                lineTo(22f, 9.24f)
                lineTo(14.81f, 8.62f)
                lineTo(12f, 2f)
                lineTo(9.19f, 8.62f)
                lineTo(2f, 9.24f)
                lineTo(7.45f, 13.97f)
                lineTo(5.82f, 21f)
                lineTo(12f, 17.27f)
                close()
            }
        }.build()

    // ==================== CHECK CIRCLE ====================

    /**
     * Check circle icon - Filled variant.
     * Used for success states and completions.
     */
    val CheckCircleFilled: ImageVector
        get() = ImageVector.Builder(
            name = "CheckCircleFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(10f, 17f)
                lineTo(5f, 12f)
                lineTo(6.41f, 10.59f)
                lineTo(10f, 14.17f)
                lineTo(17.59f, 6.58f)
                lineTo(19f, 8f)
                lineTo(10f, 17f)
                close()
            }
        }.build()

    /**
     * Check circle icon - Outlined variant.
     * Used for success states and completions.
     */
    val CheckCircleOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "CheckCircleOutlined",
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
                moveTo(22f, 11.08f)
                verticalLineTo(12f)
                arcTo(10f, 10f, 0f, true, true, 17.54f, 3.11f)
                moveTo(22f, 4f)
                lineTo(12f, 14.01f)
                lineTo(9f, 11.01f)
            }
        }.build()

    // ==================== CLOSE CIRCLE ====================

    /**
     * Close circle icon - Filled variant.
     * Used for error states and dismissals.
     */
    val CloseCircleFilled: ImageVector
        get() = ImageVector.Builder(
            name = "CloseCircleFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 2f)
                curveTo(6.47f, 2f, 2f, 6.47f, 2f, 12f)
                curveTo(2f, 17.53f, 6.47f, 22f, 12f, 22f)
                curveTo(17.53f, 22f, 22f, 17.53f, 22f, 12f)
                curveTo(22f, 6.47f, 17.53f, 2f, 12f, 2f)
                close()
                moveTo(17f, 15.59f)
                lineTo(15.59f, 17f)
                lineTo(12f, 13.41f)
                lineTo(8.41f, 17f)
                lineTo(7f, 15.59f)
                lineTo(10.59f, 12f)
                lineTo(7f, 8.41f)
                lineTo(8.41f, 7f)
                lineTo(12f, 10.59f)
                lineTo(15.59f, 7f)
                lineTo(17f, 8.41f)
                lineTo(13.41f, 12f)
                lineTo(17f, 15.59f)
                close()
            }
        }.build()

    /**
     * Close circle icon - Outlined variant.
     * Used for error states and dismissals.
     */
    val CloseCircleOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "CloseCircleOutlined",
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
                moveTo(12f, 22f)
                curveTo(17.523f, 22f, 22f, 17.523f, 22f, 12f)
                curveTo(22f, 6.477f, 17.523f, 2f, 12f, 2f)
                curveTo(6.477f, 2f, 2f, 6.477f, 2f, 12f)
                curveTo(2f, 17.523f, 6.477f, 22f, 12f, 22f)
                close()
                moveTo(15f, 9f)
                lineTo(9f, 15f)
                moveTo(9f, 9f)
                lineTo(15f, 15f)
            }
        }.build()

    // ==================== VOLUME UP ====================

    /**
     * Volume up icon - Filled variant.
     * Used for audio controls.
     */
    val VolumeUpFilled: ImageVector
        get() = ImageVector.Builder(
            name = "VolumeUpFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(3f, 9f)
                verticalLineTo(15f)
                horizontalLineTo(7f)
                lineTo(12f, 20f)
                verticalLineTo(4f)
                lineTo(7f, 9f)
                horizontalLineTo(3f)
                close()
                moveTo(16.5f, 12f)
                curveTo(16.5f, 10.23f, 15.48f, 8.71f, 14f, 7.97f)
                verticalLineTo(16.02f)
                curveTo(15.48f, 15.29f, 16.5f, 13.77f, 16.5f, 12f)
                close()
                moveTo(14f, 3.23f)
                verticalLineTo(5.29f)
                curveTo(16.89f, 6.15f, 19f, 8.83f, 19f, 12f)
                curveTo(19f, 15.17f, 16.89f, 17.85f, 14f, 18.71f)
                verticalLineTo(20.77f)
                curveTo(18.01f, 19.86f, 21f, 16.28f, 21f, 12f)
                curveTo(21f, 7.72f, 18.01f, 4.14f, 14f, 3.23f)
                close()
            }
        }.build()

    /**
     * Volume up icon - Outlined variant.
     * Used for audio controls.
     */
    val VolumeUpOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "VolumeUpOutlined",
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
                moveTo(11f, 5f)
                lineTo(6f, 9f)
                horizontalLineTo(2f)
                verticalLineTo(15f)
                horizontalLineTo(6f)
                lineTo(11f, 19f)
                verticalLineTo(5f)
                close()
                moveTo(15.54f, 8.46f)
                curveTo(16.48f, 9.4f, 17f, 10.63f, 17f, 12f)
                curveTo(17f, 13.37f, 16.48f, 14.6f, 15.54f, 15.54f)
                moveTo(19.07f, 4.93f)
                curveTo(20.95f, 6.81f, 22f, 9.28f, 22f, 12f)
                curveTo(22f, 14.72f, 20.95f, 17.19f, 19.07f, 19.07f)
            }
        }.build()

    // ==================== TRANSLATE ====================

    /**
     * Translate icon - Filled variant.
     * Used for translation features.
     */
    val TranslateFilled: ImageVector
        get() = ImageVector.Builder(
            name = "TranslateFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12.87f, 15.07f)
                lineTo(10.33f, 12.56f)
                lineTo(10.36f, 12.53f)
                curveTo(12.1f, 10.59f, 13.34f, 8.36f, 14.07f, 6f)
                horizontalLineTo(17f)
                verticalLineTo(4f)
                horizontalLineTo(10f)
                verticalLineTo(2f)
                horizontalLineTo(8f)
                verticalLineTo(4f)
                horizontalLineTo(1.01f)
                verticalLineTo(6f)
                horizontalLineTo(12.17f)
                curveTo(11.5f, 7.92f, 10.44f, 9.75f, 9f, 11.35f)
                curveTo(8.07f, 10.32f, 7.3f, 9.19f, 6.69f, 8f)
                horizontalLineTo(4.69f)
                curveTo(5.42f, 9.63f, 6.42f, 11.17f, 7.67f, 12.56f)
                lineTo(2.58f, 17.58f)
                lineTo(4f, 19f)
                lineTo(9f, 14f)
                lineTo(12.11f, 17.11f)
                lineTo(12.87f, 15.07f)
                close()
                moveTo(18.5f, 10f)
                horizontalLineTo(16.5f)
                lineTo(12f, 22f)
                horizontalLineTo(14f)
                lineTo(15.12f, 19f)
                horizontalLineTo(19.87f)
                lineTo(21f, 22f)
                horizontalLineTo(23f)
                lineTo(18.5f, 10f)
                close()
                moveTo(15.88f, 17f)
                lineTo(17.5f, 12.67f)
                lineTo(19.12f, 17f)
                horizontalLineTo(15.88f)
                close()
            }
        }.build()

    /**
     * Translate icon - Outlined variant.
     * Used for translation features.
     */
    val TranslateOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "TranslateOutlined",
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
                moveTo(5f, 8f)
                lineTo(10f, 8f)
                moveTo(7.5f, 8f)
                lineTo(7.5f, 2f)
                moveTo(7.5f, 8f)
                lineTo(3.5f, 15f)
                moveTo(7.5f, 8f)
                lineTo(11.5f, 15f)
                moveTo(10f, 12f)
                lineTo(2f, 12f)
                moveTo(18f, 12f)
                lineTo(18f, 22f)
                moveTo(18f, 12f)
                lineTo(14f, 22f)
                moveTo(18f, 12f)
                lineTo(22f, 22f)
                moveTo(16f, 19f)
                lineTo(20f, 19f)
            }
        }.build()

    // ==================== GRADUATION CAP ====================

    /**
     * Graduation cap icon - Filled variant.
     * Used for learning and courses.
     */
    val GraduationCapFilled: ImageVector
        get() = ImageVector.Builder(
            name = "GraduationCapFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 3f)
                lineTo(1f, 9f)
                lineTo(5f, 11.18f)
                verticalLineTo(17.18f)
                lineTo(12f, 21f)
                lineTo(19f, 17.18f)
                verticalLineTo(11.18f)
                lineTo(21f, 10.09f)
                verticalLineTo(17f)
                horizontalLineTo(23f)
                verticalLineTo(9f)
                lineTo(12f, 3f)
                close()
                moveTo(12f, 13.09f)
                lineTo(5.69f, 9.51f)
                lineTo(12f, 6.09f)
                lineTo(18.31f, 9.51f)
                lineTo(12f, 13.09f)
                close()
            }
        }.build()

    /**
     * Graduation cap icon - Outlined variant.
     * Used for learning and courses.
     */
    val GraduationCapOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "GraduationCapOutlined",
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
                moveTo(22f, 10f)
                verticalLineTo(16f)
                moveTo(22f, 10f)
                lineTo(12f, 5f)
                lineTo(2f, 10f)
                lineTo(12f, 15f)
                lineTo(22f, 10f)
                close()
                moveTo(6f, 12f)
                verticalLineTo(17f)
                curveTo(6f, 17f, 9f, 20f, 12f, 20f)
                curveTo(15f, 20f, 18f, 17f, 18f, 17f)
                verticalLineTo(12f)
            }
        }.build()
}
