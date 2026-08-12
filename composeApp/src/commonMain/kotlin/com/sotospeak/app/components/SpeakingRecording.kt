package com.sotospeak.app.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sotospeak.design.icons.SpeakingIcons
import com.sotospeak.designsystem.accessibility.LocalReduceMotion
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingElevation
import com.sotospeak.designsystem.theme.SpeakingMotion
import com.sotospeak.designsystem.theme.SpeakingShapes
import com.sotospeak.designsystem.theme.SpeakingTextStyles
import kotlin.math.abs
import kotlin.math.sin

/**
 * Компоненты записи Speaking Trainer (Playful Coach v1.1, .docs/design-system/mockups.html):
 * - [SpeakingTimerRing] — таймер-кольцо (.timer-ring);
 * - [RecIndicator] — REC-точка с пульсирующим кольцом (@keyframes recPulse);
 * - [RecordingWaveform] — анимированный waveform записи (.waveform.live);
 * - [PlaybackWaveform] — статичный waveform с «played»-закраской (.player-wave);
 * - [CheckPopAppear] — появление с checkPop (@keyframes checkPop).
 *
 * Все бесконечные/декоративные анимации гейтятся [LocalReduceMotion].
 */

/**
 * Таймер-кольцо: трек surfaceVariant, дуга цвета уровня (timerLevel80/50/30),
 * StrokeCap.Round, старт с -90°, прогресс оставшегося времени.
 * Дуга — animateFloatAsState 1000ms linear; цвет уровня — animateColorAsState 300ms EasingStandard.
 * Внутри — моноширинный таймер (testTag сохраняется для UI-тестов).
 */
@Composable
fun SpeakingTimerRing(
    remainingSeconds: Int,
    totalSeconds: Int,
    arcColor: Color,
    timeText: String,
    modifier: Modifier = Modifier,
    size: Dp = 176.dp,
    timerTextStyle: TextStyle = SpeakingTextStyles.TimerDisplay,
    caption: String? = null,
    timerTestTag: String? = null
) {
    val speaking = LocalSpeakingColors.current
    val reduceMotion = LocalReduceMotion.current
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

    val target = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
    } else {
        0f
    }
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = if (reduceMotion) snap() else tween(durationMillis = 1000, easing = LinearEasing),
        label = "timer_ring_progress"
    )
    val ringColor by animateColorAsState(
        targetValue = arcColor,
        animationSpec = if (reduceMotion) snap() else tween(SpeakingMotion.DurationMedium, easing = SpeakingMotion.EasingM3Standard),
        label = "timer_ring_color"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val diameter = this.size.minDimension - strokeWidth
            val topLeft = Offset(
                (this.size.width - diameter) / 2f,
                (this.size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)
            drawCircle(
                color = trackColor,
                radius = diameter / 2f,
                style = Stroke(width = strokeWidth)
            )
            if (progress > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                style = timerTextStyle,
                // Мокап .timer-label .tnum: цифры — основной текст (НЕ цвет дуги)
                color = speaking.text,
                modifier = if (timerTestTag != null) Modifier.testTag(timerTestTag) else Modifier
            )
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = speaking.textMuted
                )
            }
        }
    }
}

/**
 * REC-индикатор: точка record-цвета + пульсирующее кольцо
 * (1600ms, scale 0.9→1.25, alpha 0.7→0 — @keyframes recPulse).
 * При Reduce motion — статичная точка без кольца.
 */
@Composable
fun RecIndicator(
    modifier: Modifier = Modifier,
    label: String = "REC"
) {
    val speaking = LocalSpeakingColors.current
    val reduceMotion = LocalReduceMotion.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            if (!reduceMotion) {
                val transition = rememberInfiniteTransition(label = "rec_pulse")
                val t by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(SpeakingMotion.RecPulseMs, easing = SpeakingMotion.EasingStandard),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rec_pulse_t"
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = 0.9f + 0.35f * t
                            scaleY = 0.9f + 0.35f * t
                            alpha = 0.7f * (1f - t)
                        }
                        .border(2.dp, speaking.record, CircleShape)
                )
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(speaking.record)
            )
        }
        Text(
            text = label,
            color = speaking.record,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * Waveform записи (декоративный, .waveform.live): ряд баров с анимацией
 * scaleY 0.35→1.0, 1100ms, infinite Reverse, каскад задержек 45ms на бар.
 * При Reduce motion — статичные бары фиксированной высоты.
 */
@Composable
fun RecordingWaveform(
    modifier: Modifier = Modifier,
    barCount: Int = 24
) {
    val speaking = LocalSpeakingColors.current
    val reduceMotion = LocalReduceMotion.current

    Row(
        modifier = modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (reduceMotion) {
            repeat(barCount) { index ->
                WaveBar(
                    height = waveBarHeightDp(index, maxHeight = 48f, minHeight = 12f),
                    color = speaking.recordActive,
                    scaleY = 1f
                )
            }
        } else {
            val transition = rememberInfiniteTransition(label = "recording_waveform")
            repeat(barCount) { index ->
                val scaleY by transition.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1100, easing = SpeakingMotion.EasingStandard),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(index * 45)
                    ),
                    label = "wave_bar_$index"
                )
                WaveBar(
                    height = waveBarHeightDp(index, maxHeight = 48f, minHeight = 12f),
                    color = speaking.recordActive,
                    scaleY = scaleY
                )
            }
        }
    }
}

/**
 * Waveform воспроизведения (декоративный, .player-wave): статичные бары,
 * «played»-часть по прогрессу — waveformPlayback, остальное — surfaceVariant.
 *
 * @param playedFraction 0f..1f — доля прослушанного.
 */
@Composable
fun PlaybackWaveform(
    playedFraction: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 32
) {
    val speaking = LocalSpeakingColors.current
    val playedBars = (playedFraction.coerceIn(0f, 1f) * barCount).toInt()

    Row(
        modifier = modifier.height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(barCount) { index ->
            WaveBar(
                height = waveBarHeightDp(index, maxHeight = 34f, minHeight = 6f),
                color = if (index < playedBars) speaking.waveformPlayback else speaking.surfaceVariant,
                scaleY = 1f,
                barWidth = 3.dp
            )
        }
    }
}

/**
 * Появление с checkPop (@keyframes checkPop): scale 0.3→(overshoot 1.18)→1.0 + fadeIn,
 * 500ms, overshoot через EasingBounce. При Reduce motion — сразу видимый контент.
 */
@Composable
fun CheckPopAppear(
    modifier: Modifier = Modifier,
    initialScale: Float = 0.3f,
    content: @Composable () -> Unit
) {
    val reduceMotion = LocalReduceMotion.current
    val scale = remember { Animatable(if (reduceMotion) 1f else initialScale) }
    val alpha = remember { Animatable(if (reduceMotion) 1f else 0f) }

    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            scale.animateTo(1f, SpeakingMotion.tweenBounce())
        }
    }
    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            alpha.animateTo(1f, tween(SpeakingMotion.DurationMedium, easing = LinearEasing))
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
        }
    ) {
        content()
    }
}

@Composable
private fun WaveBar(
    height: Dp,
    color: Color,
    scaleY: Float,
    barWidth: Dp = 4.dp
) {
    Box(
        modifier = Modifier
            .width(barWidth)
            .height(height)
            .graphicsLayer {
                this.scaleY = scaleY
                transformOrigin = TransformOrigin.Center
            }
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

/** Детерминированные высоты баров как в mockups.html: 12 + 36*|sin(i*0.7)|*(0.5 + rnd*0.5) */
private fun waveBarHeightDp(index: Int, maxHeight: Float, minHeight: Float): Dp {
    val pseudoRandom = abs(sin(index * 1.618f))
    val h = minHeight + (maxHeight - minHeight) * abs(sin(index * 0.7f)) * (0.5f + pseudoRandom * 0.5f)
    return h.dp
}

/**
 * Record-кнопка (вариант B, аудит 2026-08-01): squircle 22dp 72×72 (НЕ круг, tokens.json),
 * record #FF9F6B, жёсткая «оттопыренная» тень 0 4px 0 recordShadow (tokens.json elevation.fab),
 * при нажатии тень схлопывается до 1dp + scale(.94) (mockups .rec-btn:active).
 * Иконка mic/stop 30dp, тёмная на record (WCAG AA).
 *
 * Общий компонент Training/Practice (DC-3/DC-4). При recording — пульс recPulse вокруг кнопки.
 */
@Composable
fun SpeakingRecordButton(
    isRecording: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    contentDescription: String = if (isRecording) "Остановить запись" else "Начать запись"
) {
    val speaking = LocalSpeakingColors.current
    val reduceMotion = LocalReduceMotion.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    // M3 state layers (tokens v1.3.0 state.*): оверлей onRecord 8% hover / 12% focus+pressed
    val stateLayerAlpha = when {
        !enabled -> 0f
        isPressed || isFocused -> 0.12f
        isHovered -> 0.08f
        else -> 0f
    }
    val shadowOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) {
            SpeakingElevation.RecorderShadowPressedOffsetY
        } else {
            SpeakingElevation.RecorderShadowOffsetY
        },
        animationSpec = if (reduceMotion) snap() else SpeakingMotion.tweenFast(),
        label = "rec_shadow_offset"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = if (reduceMotion) snap() else SpeakingMotion.tweenFast(),
        label = "rec_press_scale"
    )

    Box(modifier = modifier.size(88.dp), contentAlignment = Alignment.Center) {
        // recPulse-кольцо при записи (mockups .rec-btn.recording::before)
        if (isRecording && !reduceMotion) {
            val transition = rememberInfiniteTransition(label = "rec_btn_pulse")
            val t by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(SpeakingMotion.RecPulseMs, easing = SpeakingMotion.EasingStandard),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rec_btn_pulse_t"
            )
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = 1f + 0.25f * t
                        scaleY = 1f + 0.25f * t
                        alpha = 0.6f * (1f - t)
                    }
                    .border(3.dp, speaking.record, SpeakingShapes.Recorder)
            )
        }
        // Тень (recordShadow), верхний squircle (record); disabled — M3: onSurface 12%/38%
        Box(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .clip(SpeakingShapes.Recorder)
                .background(
                    if (enabled) speaking.recordShadow
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = shadowOffset)
                    .fillMaxSize()
                    .clip(SpeakingShapes.Recorder)
                    .background(
                        if (enabled) speaking.record
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true),
                        enabled = enabled,
                        onClick = onClick
                    )
                    .testTag(testTag),
                contentAlignment = Alignment.Center
            ) {
                // M3 state layer (hover/focus/pressed) — оверлей onRecord поверх record
                if (stateLayerAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(speaking.onRecord.copy(alpha = stateLayerAlpha))
                    )
                }
                Icon(
                    imageVector = if (isRecording) SpeakingIcons.Stop else SpeakingIcons.Mic,
                    contentDescription = contentDescription,
                    tint = if (enabled) speaking.onRecord
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
