package com.sotospeak.app.screens

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
import com.sotospeak.app.components.EmptyState
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.SpeakingAppBar
import com.sotospeak.app.localization.LocalAppStrings
import com.sotospeak.app.viewmodel.TopicUiModel
import com.sotospeak.app.viewmodel.TopicsState
import com.sotospeak.designsystem.animations.ListSkeleton
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes
import com.sotospeak.designsystem.icons.SpeakingIcons

/**
 * Экран топиков внутри темы (спека Part 2 §2.2).
 * Клик по топику — сразу экран видео; режим субтитров переключается чипами там
 * (DC-5/V2: bottom-sheet выбора убран по мокапу frame-video).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsScreen(
    state: TopicsState,
    onTopicClick: (String) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    libraryTitle: String = ""
) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current

    // Стрелки в аппбаре нет (мокап) — системная кнопка/жест «назад»
    com.sotospeak.app.components.PlatformBackHandler(onBack = onBack)

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            // Мокап frame-topics: h1 — название темы, sub — «N топиков · выбери и начни говорить»,
            // БЕЗ стрелки «назад» (назад — системная кнопка/жест)
            SpeakingAppBar(
                title = libraryTitle.ifBlank { state.libraryTitle }.ifBlank { strings.topicsTitle },
                subtitle = if (state.topics.isNotEmpty()) {
                    strings.topicsSubtitle(state.topics.size)
                } else null
            )
        },
        modifier = modifier.testTag("topics_screen")
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.topics.isEmpty() -> ListSkeleton()
                state.error != null && state.topics.isEmpty() -> ErrorMessage(
                    message = state.error,
                    onRetry = onRetry
                )
                state.topics.isEmpty() -> EmptyState(
                    icon = SpeakingIcons.Play,
                    title = strings.topicsEmptyTitle,
                    subtitle = strings.topicsEmptySubtitle,
                    ctaLabel = strings.ctaRefresh,
                    onCtaClick = onRetry,
                    modifier = Modifier.testTag("topics_empty")
                )
                else -> TopicsList(
                    topics = state.topics,
                    onTopicClick = onTopicClick
                )
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
    val strings = LocalAppStrings.current

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
                // M3 ListItem (A6) по мокапу frame-topics (.li): leading — play,
                // headline — название (fade вместо ellipsis), supporting — «N вопросов · видео m:ss»,
                // trailing — чип статуса + chevron
                ListItem(
                    headlineContent = {
                        com.sotospeak.app.components.FadingEdgeText(
                            text = topic.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = speaking.text,
                            maxLines = 3,
                            fadeColor = speaking.surface
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "${strings.questionsCount(topic.questionCount)} · ${strings.videoLabel} ${formatDuration(topic.durationSeconds)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = speaking.textMuted
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = speaking.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Чип статуса по мокапу (.chip-done/.chip-new), токены status* — dark-safe
                            val done = topic.isWatched || topic.hasLocalRecordings
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = if (done) strings.topicStatusDone else strings.topicStatusNew,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                },
                                shape = MaterialTheme.shapes.small,
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (done) speaking.statusReviewedContainer
                                    else speaking.statusNewContainer,
                                    labelColor = if (done) speaking.statusReviewed else speaking.statusNew
                                ),
                                border = null,
                                modifier = Modifier.testTag("topic_chip_${topic.id}")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = SpeakingIcons.ChevronRight,
                                contentDescription = null,
                                tint = speaking.textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }
}

internal fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
