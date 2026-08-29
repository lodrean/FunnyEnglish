package com.sotospeak.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.LoadingIndicator
import com.sotospeak.app.components.SpeakingAppBar
import com.sotospeak.app.components.SpeakingGate
import com.sotospeak.app.components.questionsCountText
import com.sotospeak.app.viewmodel.QuestionsState
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.animations.speakingPressable
import com.sotospeak.designsystem.theme.SpeakingTextStyles

/**
 * Экран вопросов топика (спека Part 2 §2.4).
 * Два режима: Training (всем) и Practice (только авторизованным; гость → CTA логина).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionsScreen(
    state: QuestionsState,
    onStartTraining: () -> Unit,
    onStartPractice: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    libraryTitle: String = ""
) {
    val speaking = LocalSpeakingColors.current

    // Стрелки в аппбаре нет (мокап frame-questions) — системная кнопка/жест «назад»
    com.sotospeak.app.components.PlatformBackHandler(onBack = onBack)

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            // Мокап frame-questions: h1 «Вопросы», sub — «Тема · Топик · N вопросов», без стрелки
            SpeakingAppBar(
                title = "Вопросы",
                subtitle = listOfNotNull(
                    libraryTitle.ifBlank { null },
                    state.topicTitle.ifBlank { null },
                    if (state.questions.isNotEmpty()) questionsCountText(state.questions.size) else null
                ).joinToString(" · ").ifBlank { null }
            )
        },
        modifier = modifier.testTag("questions_screen")
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingIndicator()
                state.error != null -> ErrorMessage(message = state.error, onRetry = onRetry)
                else -> QuestionsContent(
                    state = state,
                    onStartTraining = onStartTraining,
                    onStartPractice = onStartPractice,
                    onLoginClick = onLoginClick,
                    onRegisterClick = onRegisterClick
                )
            }
        }
    }
}

@Composable
private fun QuestionsContent(
    state: QuestionsState,
    onStartTraining: () -> Unit,
    onStartPractice: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val speaking = LocalSpeakingColors.current

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(state.questions, key = { _, q -> q.id }) { index, question ->
                val isActive = index == 0
                // Мокап frame-questions: активный вопрос — hero-карточка с eyebrow-лейблом,
                // остальные — компактные строки с номером.
                if (isActive) {
                    ActiveQuestionCard(
                        index = index,
                        total = state.questions.size,
                        text = question.text
                    )
                } else {
                    CompactQuestionCard(
                        index = index,
                        text = question.text
                    )
                }
            }
        }

        // Режимы (для гостя с гейтом — scrollable, чтобы не клиппилось на маленьких экранах)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val trainingIsrc = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Button(
                onClick = onStartTraining,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .speakingPressable(trainingIsrc)
                    .testTag("mode_training_button"),
                colors = ButtonDefaults.buttonColors(containerColor = speaking.primaryStrong),
                interactionSource = trainingIsrc
            ) {
                Text("Тренировка · 3 попытки", fontWeight = FontWeight.SemiBold)
            }

            if (state.isGuest) {
                // Гейтинг гостя (PRD Story 3, frame-locked Playful Coach v1.1)
                SpeakingGate(
                    icon = SpeakingIcons.Lock,
                    title = "Ты почти у цели!",
                    text = "Отправка записи учителю доступна после регистрации",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onRegisterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .testTag("practice_locked_cta"),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Зарегистрироваться")
                    }
                    OutlinedButton(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("practice_locked_login"),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Войти")
                    }
                }
            } else if (state.hasSubmitted) {
                Button(
                    onClick = onStartPractice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("mode_practice_submitted_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = speaking.statusReviewedContainer)
                ) {
                    Icon(
                        imageVector = SpeakingIcons.CheckCircle,
                        contentDescription = null,
                        tint = speaking.statusReviewed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Отправлено · мои записи",
                        fontWeight = FontWeight.SemiBold,
                        color = speaking.statusReviewed
                    )
                }
            } else {
                val practiceIsrc = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                Button(
                    onClick = onStartPractice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .speakingPressable(practiceIsrc)
                        .testTag("mode_practice_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = speaking.record),
                    interactionSource = practiceIsrc
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = speaking.onRecord,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Практика · 30 сек",
                        fontWeight = FontWeight.SemiBold,
                        color = speaking.onRecord   // тёмный текст на record (WCAG AA)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveQuestionCard(
    index: Int,
    total: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("question_item_$index"),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
            Text(
                text = "ВОПРОС ${index + 1} ИЗ $total",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.08.em,
                color = speaking.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = SpeakingTextStyles.QuestionText,
                color = speaking.text
            )
        }
    }
}

@Composable
private fun CompactQuestionCard(
    index: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("question_item_$index"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = speaking.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = speaking.text
            )
        }
    }
}
