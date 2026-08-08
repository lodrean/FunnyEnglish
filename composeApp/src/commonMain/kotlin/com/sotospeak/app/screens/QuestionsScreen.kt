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
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.LoadingIndicator
import com.sotospeak.app.components.SpeakingGate
import com.sotospeak.app.viewmodel.QuestionsState
import com.sotospeak.design.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors
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
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.topicTitle.ifBlank { "Вопросы" },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(state.questions, key = { _, q -> q.id }) { index, question ->
                // M3 FilledCard (A8): container surfaceContainerHigh, shape large(22)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_item_$index"),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Text(
                        text = question.text,
                        style = SpeakingTextStyles.QuestionText,
                        color = speaking.text,
                        modifier = Modifier.padding(20.dp)
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
            Button(
                onClick = onStartTraining,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("mode_training_button"),
                colors = ButtonDefaults.buttonColors(containerColor = speaking.primaryStrong)
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
                Button(
                    onClick = onStartPractice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("mode_practice_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = speaking.record)
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
