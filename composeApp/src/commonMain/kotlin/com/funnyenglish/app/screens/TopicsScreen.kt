package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funnyenglish.app.components.ErrorMessage
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.app.viewmodel.TopicUiModel
import com.funnyenglish.app.viewmodel.TopicsState
import com.funnyenglish.designsystem.theme.LocalSpeakingColors
import com.funnyenglish.designsystem.theme.SpeakingShapes

/**
 * Экран топиков внутри темы (спека Part 2 §2.2).
 * Клик по топику — bottom-sheet выбора «С субтитрами / Без субтитров» + «Пропустить видео».
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsScreen(
    state: TopicsState,
    onTopicClick: (String) -> Unit,
    onSubtitleChoice: (topicId: String, withSubtitles: Boolean) -> Unit,
    onSkipVideo: (String) -> Unit,
    onDismissSubtitleChoice: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.libraryTitle.ifBlank { "Топики" },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = speaking.background)
            )
        },
        modifier = modifier.testTag("topics_screen")
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.topics.isEmpty() -> LoadingIndicator()
                state.error != null && state.topics.isEmpty() -> ErrorMessage(
                    message = state.error,
                    onRetry = onRetry
                )
                else -> TopicsList(
                    topics = state.topics,
                    onTopicClick = onTopicClick
                )
            }
        }
    }

    // Bottom-sheet выбора режима субтитров
    val choiceTopicId = state.subtitleChoiceTopicId
    if (choiceTopicId != null) {
        val topic = state.topics.firstOrNull { it.id == choiceTopicId }
        ModalBottomSheet(
            onDismissRequest = onDismissSubtitleChoice,
            shape = SpeakingShapes.Sheet,
            modifier = Modifier.testTag("subtitle_choice_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = topic?.title.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = speaking.text
                )
                if (topic?.hasSubtitles == true) {
                    Button(
                        onClick = { onSubtitleChoice(choiceTopicId, true) },
                        modifier = Modifier.fillMaxWidth().testTag("subtitle_with")
                    ) {
                        Text("С субтитрами")
                    }
                }
                OutlinedButton(
                    onClick = { onSubtitleChoice(choiceTopicId, false) },
                    modifier = Modifier.fillMaxWidth().testTag("subtitle_without")
                ) {
                    Text("Без субтитров")
                }
                TextButton(
                    onClick = { onSkipVideo(choiceTopicId) },
                    modifier = Modifier.fillMaxWidth().testTag("skip_video_button")
                ) {
                    Text("Пропустить видео — сразу к вопросам")
                }
            }
        }
    }
}

@Composable
private fun TopicsList(
    topics: List<TopicUiModel>,
    onTopicClick: (String) -> Unit
) {
    val speaking = LocalSpeakingColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(topics, key = { it.id }) { topic ->
            Card(
                onClick = { onTopicClick(topic.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("topic_item_${topic.id}"),
                shape = SpeakingShapes.Card,
                colors = CardDefaults.cardColors(containerColor = speaking.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = speaking.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = topic.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = speaking.text
                        )
                        Text(
                            text = formatDuration(topic.durationSeconds) +
                                if (topic.hasSubtitles) " · субтитры" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = speaking.textMuted
                        )
                    }
                    if (topic.isWatched) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Просмотрено",
                            tint = speaking.success,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (topic.hasLocalRecordings) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Есть записи",
                            tint = speaking.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

internal fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
