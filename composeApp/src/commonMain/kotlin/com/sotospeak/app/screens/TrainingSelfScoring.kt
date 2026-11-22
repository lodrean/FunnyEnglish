package com.sotospeak.app.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.components.PlaybackWaveform
import com.sotospeak.app.localization.LocalAppStrings
import com.sotospeak.app.subtitles.SubtitleCue
import com.sotospeak.app.subtitles.TranscriptPanel
import com.sotospeak.app.viewmodel.SelfScoringState
import com.sotospeak.app.viewmodel.TrainingState
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes

private const val MILLIS_PER_SECOND = 1000

/**
 * Карточка самооценки после попытки Training (спека Part 2 §5.4, bd h3l.5):
 * прослушивание своей записи + эталон-транскрипт видео топика + чеклист
 * 4 критерия по рубрике учителя. Компоненты экрана Training — вынесены из
 * TrainingScreen.kt (detekt TooManyFunctions).
 */

/** Карточка самооценки в списке (§5.4): связка state → [SelfScoringCard]. */
@Composable
internal fun SelfScoringItem(
    scoring: SelfScoringState,
    state: TrainingState,
    onPlayRecording: (String) -> Unit,
    onStopPlayback: () -> Unit,
    onToggleSelfCheck: (Int) -> Unit,
    onSaveSelfScore: () -> Unit,
    onSkipSelfScoring: () -> Unit
) {
    SelfScoringCard(
        scoring = scoring,
        durationMs = state.attempts
            .firstOrNull { it.filePath == scoring.filePath }?.durationMs ?: 0L,
        isPlaying = state.playingRecordingPath == scoring.filePath,
        subtitleCues = state.subtitleCues,
        onPlay = {
            if (state.playingRecordingPath == scoring.filePath) onStopPlayback()
            else onPlayRecording(scoring.filePath)
        },
        onToggleCheck = onToggleSelfCheck,
        onSave = onSaveSelfScore,
        onSkip = onSkipSelfScoring
    )
}

@Composable
private fun SelfScoringCard(
    scoring: SelfScoringState,
    durationMs: Long,
    isPlaying: Boolean,
    subtitleCues: List<SubtitleCue>,
    onPlay: () -> Unit,
    onToggleCheck: (Int) -> Unit,
    onSave: () -> Unit,
    onSkip: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    val checkLabels = listOf(
        strings.selfCheckGrammar,
        strings.selfCheckVocabulary,
        strings.selfCheckPronunciation,
        strings.selfCheckFluency
    )

    Card(
        shape = SpeakingShapes.Card,
        colors = CardDefaults.cardColors(containerColor = speaking.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("self_scoring_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                strings.selfScoreTitle(scoring.attemptNumber),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = speaking.text,
                modifier = Modifier.testTag("self_scoring_title")
            )

            SelfScorePlaybackRow(
                attemptLabel = strings.attemptNumber(scoring.attemptNumber),
                durationMs = durationMs,
                isPlaying = isPlaying,
                onPlay = onPlay
            )

            SelfScoringReference(cues = subtitleCues)

            SelfCheckList(
                checks = scoring.checks,
                labels = checkLabels,
                onToggle = onToggleCheck
            )

            SelfScoringCtas(onSave = onSave, onSkip = onSkip)
        }
    }
}

/** Эталонный ответ — транскрипт видео (скрыт, если субтитров нет, §5.4). */
@Composable
private fun SelfScoringReference(cues: List<SubtitleCue>) {
    if (cues.isEmpty()) return
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            strings.selfScoreReferenceTitle,
            style = MaterialTheme.typography.labelLarge,
            color = speaking.textMuted,
            modifier = Modifier.testTag("self_score_reference_title")
        )
        // Статичный текст для чтения: MAX_VALUE красит все слова в основной
        // цвет без karaoke-подсветки и автоскролла (синхронизация с записью
        // ученика невозможна — запись 30–80с ≠ тайминги видео)
        TranscriptPanel(
            cues = cues,
            positionMs = Long.MAX_VALUE,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 180.dp)
        )
    }
}

/** Чеклист: 4 критерия по рубрике учителя (§5.4). */
@Composable
private fun SelfCheckList(
    checks: List<Boolean>,
    labels: List<String>,
    onToggle: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.testTag("self_check_list")
    ) {
        checks.forEachIndexed { index, checked ->
            SelfCheckRow(
                index = index,
                label = labels.getOrElse(index) { labels.last() },
                checked = checked,
                onToggle = { onToggle(index) }
            )
        }
    }
}

/** Прослушивание своей записи в карточке самооценки (§5.4). */
@Composable
private fun SelfScorePlaybackRow(
    attemptLabel: String,
    durationMs: Long,
    isPlaying: Boolean,
    onPlay: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    val playedFraction = rememberPlayedFraction(durationMs = durationMs, isPlaying = isPlaying)

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onPlay,
            modifier = Modifier.testTag("self_score_play")
        ) {
            Icon(
                imageVector = if (isPlaying) SpeakingIcons.Pause else SpeakingIcons.Play,
                contentDescription = if (isPlaying) strings.playbackStopDesc
                else strings.playbackListenDesc,
                tint = speaking.waveformPlayback
            )
        }
        Column {
            Text(
                attemptLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = speaking.text
            )
            Text(
                formatTimer((durationMs / MILLIS_PER_SECOND).toInt()),
                style = MaterialTheme.typography.bodySmall,
                color = speaking.textMuted
            )
            if (isPlaying) {
                Spacer(modifier = Modifier.height(6.dp))
                PlaybackWaveform(playedFraction = playedFraction)
            }
        }
    }
}

/** Строка критерия чеклиста самооценки (§5.4): тогл-тап, 48dp-таргет. */
@Composable
private fun SelfCheckRow(
    index: Int,
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpeakingShapes.Chip)
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("self_check_item_$index")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp)
        ) {
            if (checked) {
                Icon(
                    imageVector = SpeakingIcons.CheckCircle,
                    contentDescription = null,
                    tint = speaking.success,
                    modifier = Modifier
                        .size(22.dp)
                        .testTag("self_check_mark_$index")
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(2.dp, speaking.textMuted, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = speaking.text
        )
    }
}

/** CTA карточки самооценки (§5.4): «Готово» / «Пропустить». */
@Composable
private fun SelfScoringCtas(onSave: () -> Unit, onSkip: () -> Unit) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onSave,
            modifier = Modifier
                .weight(1f)
                .testTag("self_score_save"),
            colors = ButtonDefaults.buttonColors(containerColor = speaking.record)
        ) {
            Text(strings.selfScoreSave, color = speaking.onRecord)
        }
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier
                .weight(1f)
                .testTag("self_score_skip")
        ) {
            Text(strings.selfScoreSkip)
        }
    }
}

/** Сводка «N/4» на карточке попытки с сохранённой самооценкой (§5.4). */
@Composable
internal fun SelfScoreSummaryChip(summary: String, index: Int) {
    val speaking = LocalSpeakingColors.current
    Text(
        summary,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = speaking.textMuted,
        modifier = Modifier
            .clip(SpeakingShapes.Chip)
            .background(speaking.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("attempt_self_score_$index")
    )
}

/**
 * Декоративный прогресс воспроизведения записи (реальная позиция плеера
 * недоступна, симулируем как в mockups.html); сброс при остановке.
 * Общая для AttemptCard и карточки самооценки (§5.4).
 */
@Composable
internal fun rememberPlayedFraction(durationMs: Long, isPlaying: Boolean): Float {
    val playedMs = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying && durationMs > 0) {
            playedMs.snapTo(0f)
            playedMs.animateTo(
                targetValue = durationMs.toFloat(),
                animationSpec = tween(
                    durationMillis = durationMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    easing = LinearEasing
                )
            )
        } else {
            playedMs.snapTo(0f)
        }
    }
    return if (durationMs > 0) (playedMs.value / durationMs).coerceIn(0f, 1f) else 0f
}
