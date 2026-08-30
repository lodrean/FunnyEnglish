package com.sotospeak.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.LoadingIndicator
import com.sotospeak.app.viewmodel.MySubmissionsState
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes
import com.sotospeak.shared.contracts.SpeakingGrade
import com.sotospeak.shared.contracts.SpeakingSubmission

/**
 * Экран «Отправки» (мокап frame-submissions, спека Part 2 §2.7, §7):
 * заголовок + подзаголовок (без стрелки назад — экран в bottom nav),
 * 2-строчные карточки со статусом NEW/REVIEWED и grade-chip,
 * карточка оценки по рубрике (4 критерия 1–10 + total + комментарий),
 * секция «Не отправлено» с retry, explainer о запрете повторной отправки, empty state.
 */
@Composable
fun MySubmissionsScreen(
    state: MySubmissionsState,
    onRefresh: () -> Unit,
    onRetryPending: (String) -> Unit,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    Scaffold(
        containerColor = speaking.background,
        modifier = modifier.testTag("my_submissions_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SubmissionsHeader()
            when {
                state.isLoading && state.submissions.isEmpty() ->
                    LoadingIndicator()
                state.error != null && state.submissions.isEmpty() && state.pendingUploads.isEmpty() ->
                    ErrorMessage(message = state.error.orEmpty(), onRetry = onRefresh)
                state.submissions.isEmpty() && state.pendingUploads.isEmpty() ->
                    SubmissionsEmptyState()
                else -> SubmissionsList(
                    state = state,
                    onRetryPending = onRetryPending,
                    onPlayAudio = onPlayAudio,
                    onStopAudio = onStopAudio
                )
            }
        }
    }
}

/** Заголовок по мокапу frame-submissions: «Отправки» + подзаголовок (стрелка назад избыточна — экран в bottom nav). */
@Composable
private fun SubmissionsHeader() {
    val speaking = LocalSpeakingColors.current
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)) {
        Text(
            text = "Отправки",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = speaking.text,
            modifier = Modifier.testTag("submissions_title")
        )
        Text(
            text = "Записи, отправленные учителю",
            style = MaterialTheme.typography.bodyMedium,
            color = speaking.textMuted,
            modifier = Modifier.testTag("submissions_subtitle")
        )
    }
}

@Composable
private fun SubmissionsList(
    state: MySubmissionsState,
    onRetryPending: (String) -> Unit,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Секция «Не отправлено» (offline retry, спека §6.4)
        if (state.pendingUploads.isNotEmpty()) {
            item {
                Text(
                    "Не отправлено",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.statusNew
                )
            }
            items(state.pendingUploads, key = { it.filePath }) { pending ->
                // M3: Card + ListItem (A11, как TopicsScreen)
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = speaking.statusNewContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_upload_item")
                ) {
                    ListItem(
                        headlineContent = {
                            Text("Запись ждёт отправки", color = speaking.text)
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = speaking.statusNew
                            )
                        },
                        trailingContent = {
                            TextButton(
                                onClick = { onRetryPending(pending.filePath) },
                                colors = ButtonDefaults.textButtonColors(contentColor = speaking.primary)
                            ) {
                                Text("Повторить")
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }

        items(state.submissions, key = { it.id }) { submission ->
            SubmissionCard(
                submission = submission,
                isPlaying = state.playingAudioUrl == submission.audioUrl,
                onPlay = {
                    if (state.playingAudioUrl == submission.audioUrl) onStopAudio()
                    else onPlayAudio(submission.audioUrl)
                }
            )
        }

        // Explainer мокапа (MS2): правило DUPLICATE_SUBMISSION
        item {
            Text(
                "Повторная отправка по топику запрещена — после REVIEWED топик можно только переиграть в Training",
                style = MaterialTheme.typography.bodySmall,
                color = speaking.textMuted,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .testTag("submissions_explainer")
            )
        }
    }
}

@Composable
private fun SubmissionCard(
    submission: SpeakingSubmission,
    isPlaying: Boolean,
    onPlay: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    val isReviewed = submission.status == "REVIEWED"

    // M3: Card + ListItem (A11, как TopicsScreen); 2 строки по мокапу: тема + «дата, время · длительность»
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = speaking.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("submission_item_${submission.id}")
    ) {
        Column {
            ListItem(
                headlineContent = {
                    Text(
                        submission.topicTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = speaking.text
                    )
                },
                supportingContent = {
                    val meta = listOfNotNull(
                        formatSubmissionDate(submission.createdAt).takeIf { it.isNotBlank() },
                        formatTimer(submission.durationSec)
                    ).joinToString(" · ")
                    Text(
                        meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = speaking.textMuted
                    )
                },
                leadingContent = {
                    IconButton(
                        onClick = onPlay,
                        modifier = Modifier.testTag("play_submission_${submission.id}")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Стоп" else "Прослушать",
                            tint = speaking.waveformPlayback
                        )
                    }
                },
                trailingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        submission.grade?.let { grade ->
                            GradeTotalChip(total = grade.total)
                        }
                        SubmissionStatusChip(
                            isReviewed = isReviewed,
                            modifier = Modifier.testTag("submission_status_${submission.id}")
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            submission.grade?.let { grade ->
                Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    GradeCard(grade = grade, submissionId = submission.id)
                }
            }
        }
    }
}

/** Статус-чип NEW/REVIEWED (терминология мокапа) — M3 AssistChip: container + тёмный текст (AA). */
@Composable
private fun SubmissionStatusChip(isReviewed: Boolean, modifier: Modifier = Modifier) {
    val speaking = LocalSpeakingColors.current
    AssistChip(
        onClick = {},
        modifier = modifier,
        label = {
            Text(
                if (isReviewed) "REVIEWED" else "NEW",
                style = MaterialTheme.typography.labelMedium
            )
        },
        shape = MaterialTheme.shapes.small,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isReviewed) speaking.statusReviewedContainer
            else speaking.statusNewContainer,
            labelColor = if (isReviewed) speaking.statusReviewed
            else speaking.statusNew
        ),
        border = null
    )
}

/** Grade-chip мокапа (.grade-chip): итоговый балл на secondaryContainer, pill, extrabold. */
@Composable
private fun GradeTotalChip(total: Double, modifier: Modifier = Modifier) {
    val speaking = LocalSpeakingColors.current
    Surface(
        shape = SpeakingShapes.StatusPill,
        color = speaking.secondaryContainer,
        modifier = modifier
    ) {
        Text(
            // KMP: String.format недоступен (WASM) — округление вручную
            (kotlin.math.round(total * 10) / 10).toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = speaking.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun GradeCard(grade: SpeakingGrade, submissionId: String) {
    val speaking = LocalSpeakingColors.current
    Card(
        shape = SpeakingShapes.Chip,
        colors = CardDefaults.cardColors(containerColor = speaking.statusReviewedContainer),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("grade_card_$submissionId")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Оценка учителя",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.text
                )
                Text(
                    // KMP: String.format недоступен (WASM) — округление вручную
                    (kotlin.math.round(grade.total * 10) / 10).toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = speaking.statusReviewed
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            GradeBar("Грамматика", grade.grammar)
            GradeBar("Словарный запас", grade.vocabulary)
            GradeBar("Произношение", grade.pronunciation)
            GradeBar("Беглость", grade.fluency)
            grade.comment?.takeIf { it.isNotBlank() }?.let { comment ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = speaking.text
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Проверил: ${grade.reviewerName}",
                style = MaterialTheme.typography.bodySmall,
                color = speaking.textMuted
            )
        }
    }
}

@Composable
private fun GradeBar(label: String, value: Int) {
    val speaking = LocalSpeakingColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = speaking.text,
            modifier = Modifier.width(130.dp)
        )
        LinearProgressIndicator(
            progress = { (value / 10f).coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
            color = speaking.primary,
            trackColor = speaking.surfaceVariant
        )
        Text(
            "$value",
            style = MaterialTheme.typography.labelMedium,
            color = speaking.textMuted,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun SubmissionsEmptyState(modifier: Modifier = Modifier) {
    val speaking = LocalSpeakingColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("submissions_empty"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "У вас пока нет отправленных записей",
            style = MaterialTheme.typography.titleMedium,
            color = speaking.textMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Пройдите практику в любом топике библиотеки",
            style = MaterialTheme.typography.bodyMedium,
            color = speaking.textMuted
        )
    }
}

/** Дата карточки по мокапу: ISO «2026-07-29T09:00:00Z» → «29.07.2026, 09:00». */
private fun formatSubmissionDate(iso: String?): String {
    if (iso == null || iso.length < 16) return iso.orEmpty()
    val ymd = iso.substring(0, 10).split("-")
    if (ymd.size != 3) return iso.take(10)
    return "${ymd[2]}.${ymd[1]}.${ymd[0]}, ${iso.substring(11, 16)}"
}
