package com.sotospeak.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sotospeak.app.components.CheckPopAppear
import com.sotospeak.app.localization.LocalAppStrings
import com.sotospeak.app.storage.RecordingMeta
import com.sotospeak.app.viewmodel.TrainingState
import com.sotospeak.app.viewmodel.TrainingViewModel
import com.sotospeak.shared.contracts.SpeakingQuestion
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes

/**
 * Секции экрана Training (спека Part 2 §5.3/§5.4): заголовок уровня, список вопросов,
 * список попыток, privacy-note, финальные CTA + LazyListScope-расширения.
 * Вынесены из TrainingScreen.kt (detekt TooManyFunctions/LongMethod).
 */

/** Цвет таймера по номеру попытки (80/50/30 — уровень сложности, дизайн v1.1). */
@Composable
internal fun timerColorFor(attemptNumber: Int): Color {
    val speaking = LocalSpeakingColors.current
    return when (attemptNumber) {
        1 -> speaking.timerLevel80
        2 -> speaking.timerLevel50
        else -> speaking.timerLevel30
    }
}

/** Сводка «N/4» для попытки с сохранённой самооценкой (§5.4), иначе null. */
@Composable
internal fun attemptSelfScoreSummary(attempt: RecordingMeta): String? {
    val strings = LocalAppStrings.current
    return attempt.selfScore?.let { score ->
        strings.selfScoreSummary(score.checks.count { it }, TrainingViewModel.SELF_CHECK_COUNT)
    }
}

/** Level-chip «Уровень N · X сек» + индикаторы попыток (§5.3). */
@Composable
internal fun LevelHeader(state: TrainingState, limit: Int, timerColor: Color) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // M3 AssistChip (A9): timer-цвета из LocalSpeakingColors
        AssistChip(
            onClick = {},
            modifier = Modifier.testTag("level_chip"),
            label = {
                Text(
                    strings.levelChip(state.attemptNumber, limit),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = SpeakingIcons.Clock,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            },
            shape = MaterialTheme.shapes.small,
            colors = AssistChipDefaults.assistChipColors(
                containerColor = timerColor.copy(alpha = 0.15f),
                labelColor = timerColor,
                leadingIconContentColor = timerColor
            ),
            border = null
        )
        AttemptProgressBar(
            completedCount = state.attempts.size,
            currentIndex = state.attemptNumber - 1,
            total = TrainingViewModel.MAX_ATTEMPTS,
            completedColor = speaking.success,
            currentColor = timerColor,
            upcomingColor = speaking.surfaceVariant
        )
    }
}

/** Весь список вопросов — отвечаем на все одной записью (нумерация .pq-item). */
@Composable
internal fun TrainingQuestionsList(questions: List<com.sotospeak.shared.contracts.SpeakingQuestion>) {
    val speaking = LocalSpeakingColors.current
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("training_questions_list")
    ) {
        questions.forEachIndexed { index, question ->
            Card(
                shape = SpeakingShapes.Chip,
                colors = CardDefaults.cardColors(containerColor = speaking.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("question_item_$index")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = speaking.primary,
                        modifier = Modifier.testTag("question_number_$index")
                    )
                    Text(
                        question.text,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified,
                        color = speaking.text
                    )
                }
            }
        }
    }
}

/** Заголовок списка попыток «Попытки · N из 3» (§5.3). */
@Composable
private fun AttemptsSectionTitle(attemptsCount: Int) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    Text(
        strings.attemptsTitle(attemptsCount, TrainingViewModel.MAX_ATTEMPTS),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = speaking.text,
        modifier = Modifier.testTag("attempts_title")
    )
}

/** Privacy-note (mockups.html .privacy-note — shield-иконка). */
@Composable
internal fun PrivacyNote() {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = SpeakingIcons.Shield,
                contentDescription = null,
                tint = speaking.secondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                strings.privacyNote,
                style = MaterialTheme.typography.bodySmall,
                color = speaking.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}


/** Колбэки карточки самооценки (§5.4) — бандл против LongParameterList. */
internal class SelfScoringActions(
    val onPlayRecording: (String) -> Unit,
    val onStopPlayback: () -> Unit,
    val onToggleCheck: (Int) -> Unit,
    val onSave: () -> Unit,
    val onSkip: () -> Unit
)

/** Карточка самооценки после попытки (§5.4): между rec-зоной и списком попыток. */
internal fun LazyListScope.selfScoringSection(state: TrainingState, actions: SelfScoringActions) {
    state.selfScoring?.let { scoring ->
        item(key = "self_scoring") {
            SelfScoringItem(
                scoring = scoring,
                state = state,
                onPlayRecording = actions.onPlayRecording,
                onStopPlayback = actions.onStopPlayback,
                onToggleSelfCheck = actions.onToggleCheck,
                onSaveSelfScore = actions.onSave,
                onSkipSelfScoring = actions.onSkip
            )
        }
    }
}

/** Список попыток: только прослушивание, ✅ автоматически, удаления нет (§5.3). */
internal fun LazyListScope.attemptsSection(
    state: TrainingState,
    onPlayRecording: (String) -> Unit,
    onStopPlayback: () -> Unit,
    onOpenSelfScoring: (String) -> Unit
) {
    if (state.attempts.isEmpty()) return
    item { AttemptsSectionTitle(attemptsCount = state.attempts.size) }
    itemsIndexed(state.attempts, key = { _, a -> a.filePath }) { index, attempt ->
        AttemptCard(
            index = index,
            durationMs = attempt.durationMs,
            isPlaying = state.playingRecordingPath == attempt.filePath,
            selfScoreSummary = attemptSelfScoreSummary(attempt),
            onPlay = {
                if (state.playingRecordingPath == attempt.filePath) onStopPlayback()
                else onPlayRecording(attempt.filePath)
            },
            onOpenSelfScoring = { onOpenSelfScoring(attempt.filePath) }
        )
    }
}

@Composable
internal fun FinalCtaBlock(
    onGoToPractice: () -> Unit,
    onBackToLibrary: () -> Unit,
    onRestartAttempts: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    // Появление финальных CTA — fadeIn + scale с EasingBounce (motion-токены)
    CheckPopAppear(initialScale = 0.9f) {
    Card(
        shape = SpeakingShapes.Card,
        colors = CardDefaults.cardColors(containerColor = speaking.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("final_cta")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                strings.allAttemptsDone,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = speaking.text
            )
            Button(
                onClick = onGoToPractice,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("final_go_practice"),
                colors = ButtonDefaults.buttonColors(containerColor = speaking.record)
            ) {
                Text(strings.goToPractice, color = speaking.onRecord)
            }
            OutlinedButton(
                onClick = onBackToLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("final_back_library")
            ) {
                Text(strings.backToLibrary)
            }
            TextButton(
                onClick = onRestartAttempts,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("final_restart")
            ) {
                Text(strings.restartFromAttempt1)
            }
        }
    }
    }
}

@Composable
private fun AttemptProgressBar(
    completedCount: Int,
    currentIndex: Int,
    total: Int,
    completedColor: androidx.compose.ui.graphics.Color,
    currentColor: androidx.compose.ui.graphics.Color,
    upcomingColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val color = when {
                index < completedCount -> completedColor
                index == currentIndex -> currentColor
                else -> upcomingColor
            }
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(6.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

