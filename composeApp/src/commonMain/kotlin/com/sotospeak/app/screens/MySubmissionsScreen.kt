package com.sotospeak.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.LoadingIndicator
import com.sotospeak.app.viewmodel.MySubmissionsState
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes
import com.sotospeak.shared.model.SpeakingGrade
import com.sotospeak.shared.model.SpeakingSubmission

/**
 * Экран «Мои записи» (спека Part 2 §2.7, §7): статусы NEW/REVIEWED,
 * карточка оценки по рубрике (4 критерия 1–10 + total + комментарий),
 * секция «Не отправлено» с retry, empty state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySubmissionsScreen(
    state: MySubmissionsState,
    onRefresh: () -> Unit,
    onRetryPending: (String) -> Unit,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            TopAppBar(
                title = { Text("Мои записи", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = speaking.background)
            )
        },
        modifier = modifier.testTag("my_submissions_screen")
    ) { padding ->
        when {
            state.isLoading && state.submissions.isEmpty() ->
                LoadingIndicator(modifier = Modifier.padding(padding))
            state.error != null && state.submissions.isEmpty() && state.pendingUploads.isEmpty() ->
                ErrorMessage(message = state.error, onRetry = onRefresh, modifier = Modifier.padding(padding))
            state.submissions.isEmpty() && state.pendingUploads.isEmpty() ->
                SubmissionsEmptyState(modifier = Modifier.padding(padding))
            else -> SubmissionsList(
                state = state,
                onRetryPending = onRetryPending,
                onPlayAudio = onPlayAudio,
                onStopAudio = onStopAudio,
                modifier = Modifier.padding(padding)
            )
        }
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
        contentPadding = PaddingValues(16.dp),
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
                Card(
                    shape = SpeakingShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = speaking.statusNewContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_upload_item")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = speaking.statusNew
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Запись ждёт отправки",
                            modifier = Modifier.weight(1f),
                            color = speaking.text
                        )
                        TextButton(
                            onClick = { onRetryPending(pending.filePath) },
                            colors = ButtonDefaults.textButtonColors(contentColor = speaking.primary)
                        ) {
                            Text("Повторить")
                        }
                    }
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

    Card(
        shape = SpeakingShapes.Card,
        colors = CardDefaults.cardColors(containerColor = speaking.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("submission_item_${submission.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        submission.topicTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = speaking.text
                    )
                    Text(
                        "${formatTimer(submission.durationSec)} · ${submission.createdAt?.take(10).orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = speaking.textMuted
                    )
                }
                // Статус-чип
                Surface(
                    shape = SpeakingShapes.StatusPill,
                    color = if (isReviewed) speaking.statusReviewedContainer
                    else speaking.statusNewContainer,
                    modifier = Modifier.testTag("submission_status_${submission.id}")
                ) {
                    Text(
                        if (isReviewed) "Проверено" else "На проверке",
                        color = if (isReviewed) speaking.statusReviewed else speaking.statusNew,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            submission.grade?.let { grade ->
                Spacer(modifier = Modifier.height(12.dp))
                GradeCard(grade = grade, submissionId = submission.id)
            }
        }
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
