package com.funnyenglish.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funnyenglish.app.components.ErrorMessage
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.app.viewmodel.QuestionsState
import com.funnyenglish.designsystem.theme.LocalSpeakingColors
import com.funnyenglish.designsystem.theme.SpeakingShapes
import com.funnyenglish.designsystem.theme.SpeakingTextStyles

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
                    onLoginClick = onLoginClick
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
    onLoginClick: () -> Unit
) {
    val speaking = LocalSpeakingColors.current

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(state.questions, key = { _, q -> q.id }) { index, question ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_item_$index"),
                    shape = SpeakingShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = speaking.surface)
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

        // Режимы
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onStartTraining,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("mode_training_button"),
                colors = ButtonDefaults.buttonColors(containerColor = speaking.primary)
            ) {
                Text("Тренировка · 3 попытки", fontWeight = FontWeight.SemiBold)
            }

            if (state.isGuest) {
                // Гейтинг гостя (PRD Story 3): замок + CTA логина
                OutlinedButton(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("practice_locked_cta"),
                    shape = SpeakingShapes.Chip
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Практика — войти / зарегистрироваться")
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
