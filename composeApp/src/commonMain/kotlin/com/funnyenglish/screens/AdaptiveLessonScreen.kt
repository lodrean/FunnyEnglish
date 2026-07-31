package com.funnyenglish.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.funnyenglish.designsystem.animations.SuccessCelebration
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.components.cards.FunnyCard
import com.funnyenglish.designsystem.components.feedback.FunnyLinearProgress
import com.funnyenglish.designsystem.tokens.SpaceMd
import com.funnyenglish.designsystem.tokens.SpaceSm
import com.funnyenglish.viewmodel.AdaptiveLessonViewModel
import com.funnyenglish.viewmodel.LessonScreenState

/**
 * FunnyEnglish Adaptive Lesson Screen
 * 
 * Features:
 * - Question display with progress
 * - Answer selection
 * - Feedback after answer
 * - Break handling
 * - Completion celebration
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveLessonScreen(
    viewModel: AdaptiveLessonViewModel,
    onClose: () -> Unit = {},
    onComplete: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle completion
    LaunchedEffect(uiState.screenState) {
        if (uiState.screenState == LessonScreenState.COMPLETED) {
            kotlinx.coroutines.delay(1000)
            onComplete(uiState.xpEarned)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Урок") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    // Progress text
                    Text(
                        text = "${(uiState.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState.screenState) {
                LessonScreenState.LOADING -> {
                    // Loading state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Загрузка...")
                    }
                }
                
                LessonScreenState.QUESTION -> {
                    QuestionContent(
                        uiState = uiState,
                        onAnswerSelected = { answerId ->
                            viewModel.submitAnswer(answerId)
                        }
                    )
                }
                
                LessonScreenState.FEEDBACK -> {
                    FeedbackContent(
                        uiState = uiState,
                        onContinue = {
                            viewModel.nextQuestion()
                        }
                    )
                }
                
                LessonScreenState.BREAK -> {
                    BreakContent(
                        onResume = {
                            viewModel.resumeLesson()
                        }
                    )
                }
                
                LessonScreenState.COMPLETED -> {
                    // Show celebration overlay
                    SuccessCelebration(
                        isVisible = true,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Урок завершён!",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(SpaceMd))
                        Text(
                            text = "+${uiState.xpEarned} XP",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                LessonScreenState.ERROR -> {
                    ErrorContent(
                        error = uiState.error ?: "Unknown error",
                        onRetry = {
                            viewModel.clearError()
                        },
                        onClose = onClose
                    )
                }
                
                else -> {}
            }
        }
    }
}

@Composable
private fun QuestionContent(
    uiState: com.funnyenglish.viewmodel.LessonUiState,
    onAnswerSelected: (String) -> Unit
) {
    val question = uiState.currentQuestion ?: return
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpaceMd)
    ) {
        // Progress bar
        FunnyLinearProgress(
            progress = uiState.progress,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(SpaceMd))
        
        // Question text
        question.text?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(SpaceMd))
        }
        
        // Answers
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(SpaceSm)
        ) {
            items(question.answers) { answer ->
                FunnyCard(
                    onClick = { onAnswerSelected(answer.id) },
                    type = com.funnyenglish.designsystem.components.cards.FunnyCardType.OUTLINED
                ) {
                    answer.text?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackContent(
    uiState: com.funnyenglish.viewmodel.LessonUiState,
    onContinue: () -> Unit
) {
    val feedback = uiState.feedback ?: return
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpaceMd),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Correct/Incorrect indicator
        Text(
            text = if (feedback.isCorrect) "✅ Правильно!" else "❌ Неправильно",
            style = MaterialTheme.typography.headlineMedium,
            color = if (feedback.isCorrect) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(SpaceMd))
        
        // Explanation
        feedback.explanation?.let {
            FunnyCard {
                Column(modifier = Modifier.padding(SpaceMd)) {
                    Text(
                        text = "Объяснение:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(SpaceSm))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(SpaceMd))
        }
        
        // Grammar note
        feedback.grammarNote?.let {
            FunnyCard(
                type = com.funnyenglish.designsystem.components.cards.FunnyCardType.FILLED
            ) {
                Column(modifier = Modifier.padding(SpaceMd)) {
                    Text(
                        text = "💡 Подсказка:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(SpaceSm))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(SpaceMd))
        }
        
        // XP earned
        if (feedback.xpEarned > 0) {
            Text(
                text = "+${feedback.xpEarned} XP",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(SpaceMd))
        }
        
        // Continue button
        FunnyButton(
            text = "Продолжить",
            onClick = onContinue,
            type = FunnyButtonType.PRIMARY,
            size = FunnyButtonSize.LARGE,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BreakContent(
    onResume: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpaceMd),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "☕ Перерыв",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(SpaceMd))
        
        Text(
            text = "Отдохни 30 секунд перед продолжением",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(SpaceMd))
        
        FunnyButton(
            text = "Продолжить",
            onClick = onResume,
            type = FunnyButtonType.PRIMARY,
            size = FunnyButtonSize.LARGE,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpaceMd),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌ Ошибка",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(SpaceMd))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(SpaceMd))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            FunnyButton(
                text = "Повторить",
                onClick = onRetry,
                type = FunnyButtonType.PRIMARY
            )
            
            FunnyButton(
                text = "Закрыть",
                onClick = onClose,
                type = FunnyButtonType.SECONDARY
            )
        }
    }
}
