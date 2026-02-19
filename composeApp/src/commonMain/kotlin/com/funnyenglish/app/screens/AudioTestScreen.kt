package com.funnyenglish.app.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.funnyenglish.app.components.ModernAudioPlayer // TEMPORARILY DISABLED
import com.funnyenglish.app.theme.FunnyColors
import com.funnyenglish.app.viewmodel.AudioTestScreenState
import com.funnyenglish.shared.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTestScreen(
    state: AudioTestScreenState,
    onBack: () -> Unit,
    onSelectAnswer: (String, String) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onGoToQuestion: (Int) -> Unit,
    onSubmit: () -> Unit,
    onPlayStarted: () -> Unit,
    onPlayCompleted: () -> Unit
) {
    val audioTest = state.audioTest

    if (state.isLoading || audioTest == null) {
        LoadingIndicator()
        return
    }

    if (state.error != null) {
        ErrorView(
            error = state.error,
            onRetry = onBack,
            onBack = onBack
        )
        return
    }

    if (state.result != null) {
        AudioTestResultScreen(
            result = state.result,
            testTitle = audioTest.title,
            onContinue = onBack
        )
        return
    }

    val totalQuestions = audioTest.questions.size
    val currentQuestion = audioTest.questions.getOrNull(state.currentQuestionIndex)
    val isLastQuestion = state.currentQuestionIndex == totalQuestions - 1
    val currentAnswer = currentQuestion?.let { state.answers[it.id] }
    val playsRemaining = (audioTest.playsLimit ?: Int.MAX_VALUE) - state.playsUsed

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = audioTest.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Вопрос ${state.currentQuestionIndex + 1} из $totalQuestions",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Timer
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = FunnyColors.Primary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val minutes = state.timeSpentSeconds / 60
                            val seconds = state.timeSpentSeconds % 60
                            Text(
                                text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FunnyColors.Primary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(
                isLastQuestion = isLastQuestion,
                hasAnswer = currentAnswer != null,
                onNext = onNextQuestion,
                onSubmit = onSubmit
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Audio Player (TEMPORARILY DISABLED - ModernAudioPlayer has compilation issues)
            /*
            ModernAudioPlayer(
                audioUrl = audioTest.audioFileUrl,
                playsRemaining = playsRemaining,
                onPlayStarted = onPlayStarted,
                onPlayCompleted = onPlayCompleted,
                allowPause = true,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            */
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Audio Player (Disabled)",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Question Progress Dots
            QuestionProgressDots(
                currentIndex = state.currentQuestionIndex,
                totalQuestions = totalQuestions,
                answeredQuestions = state.answers.keys,
                onQuestionClick = onGoToQuestion
            )

            // Question Content
            currentQuestion?.let { question ->
                QuestionCard(
                    question = question,
                    selectedOptionId = currentAnswer,
                    questionNumber = state.currentQuestionIndex + 1,
                    onSelectOption = { optionId ->
                        onSelectAnswer(question.id, optionId)
                    }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun QuestionCard(
    question: AudioTestQuestion,
    selectedOptionId: String?,
    questionNumber: Int,
    onSelectOption: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Question number badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FunnyColors.Primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "Вопрос $questionNumber",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FunnyColors.Primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question text
            val questionText = question.text
            Text(
                text = questionText ?: "",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Points badge
            Text(
                text = "${question.points} ${getPointsWord(question.points)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Options
            question.answers.forEach { answer ->
                AnswerItem(
                    answer = answer,
                    isSelected = answer.id == selectedOptionId,
                    onClick = { onSelectOption(answer.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AnswerItem(
    answer: com.funnyenglish.shared.model.AudioTestAnswer,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> FunnyColors.Primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val borderColor = when {
        isSelected -> FunnyColors.Primary
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> FunnyColors.Primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, borderColor)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) FunnyColors.Primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .background(if (isSelected) FunnyColors.Primary else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = answer.text,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
private fun QuestionProgressDots(
    currentIndex: Int,
    totalQuestions: Int,
    answeredQuestions: Set<String>,
    onQuestionClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        repeat(totalQuestions) { index ->
            val isCurrent = index == currentIndex
            val isAnswered = index < answeredQuestions.size

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 40.dp else 32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrent -> FunnyColors.Primary
                            isAnswered -> FunnyColors.Success
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable { onQuestionClick(index) }
                    .border(
                        width = if (isCurrent) 0.dp else 1.dp,
                        color = if (isAnswered) FunnyColors.Success else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (index + 1).toString(),
                    fontSize = if (isCurrent) 16.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isCurrent || isAnswered -> Color.White
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    isLastQuestion: Boolean,
    hasAnswer: Boolean,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (isLastQuestion) {
                Button(
                    onClick = onSubmit,
                    enabled = hasAnswer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FunnyColors.AccentPurple
                    )
                ) {
                    Text(
                        text = "Завершить тест",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            } else {
                Button(
                    onClick = onNext,
                    enabled = hasAnswer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FunnyColors.Primary
                    )
                ) {
                    Text(
                        text = "Следующий вопрос",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ошибка",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = FunnyColors.Error
            )
            Text(
                text = error,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("Повторить")
            }
            TextButton(onClick = onBack) {
                Text("Вернуться")
            }
        }
    }
}

@Composable
private fun AudioTestResultScreen(
    result: SubmitAudioTestResult,
    testTitle: String,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Stars
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(3) { index ->
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = if (result.stars > index) FunnyColors.StarFilled else FunnyColors.StarEmpty,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when {
                result.percentage >= 95 -> "Отлично!"
                result.percentage >= 80 -> "Хорошо!"
                result.percentage >= 60 -> "Неплохо!"
                else -> "Попробуй ещё!"
            },
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = when {
                result.percentage >= 80 -> FunnyColors.Success
                result.percentage >= 60 -> FunnyColors.Secondary
                else -> FunnyColors.Error
            }
        )

        Text(
            text = testTitle,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Score Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ваш результат",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${result.percentage}%",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FunnyColors.Primary
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = FunnyColors.Success,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${result.score} / ${result.maxScore} правильно",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FunnyColors.Primary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = FunnyColors.StarFilled,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${result.pointsEarned} XP",
                                fontWeight = FontWeight.Bold,
                                color = FunnyColors.Primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Продолжить",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getPointsWord(points: Int): String {
    return when {
        points % 10 == 1 && points % 100 != 11 -> "балл"
        points % 10 in 2..4 && points % 100 !in 12..14 -> "балла"
        else -> "баллов"
    }
}
