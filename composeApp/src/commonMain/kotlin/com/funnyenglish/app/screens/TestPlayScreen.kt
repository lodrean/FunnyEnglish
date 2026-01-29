package com.funnyenglish.app.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.funnyenglish.app.components.*
import com.funnyenglish.app.theme.FunnyColors
import com.funnyenglish.app.viewmodel.TestPlayState
import com.funnyenglish.shared.model.*
import com.funnyenglish.shared.platform.AudioPlayer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPlayScreen(
    state: TestPlayState,
    onBack: () -> Unit,
    onSelectAnswer: (String, String) -> Unit,
    onSetDragDropMatch: (String, String, String) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onGoToQuestion: (Int) -> Unit,
    onSubmit: () -> Unit,
    onShowResult: () -> Unit
) {
    val test = state.test

    if (state.isLoading || test == null) {
        LoadingIndicator()
        return
    }

    // Show result if available
    if (state.result != null) {
        TestResultScreen(
            result = state.result,
            testTitle = test.title,
            onContinue = onShowResult,
            onRetry = onShowResult
        )
        return
    }

    val currentQuestion = test.questions.getOrNull(state.currentQuestionIndex)
    val isLastQuestion = state.currentQuestionIndex == test.questions.size - 1
    val currentAnswer = currentQuestion?.let { state.answers[it.id] }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FunnyColors.Background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TestTopBar(
                title = "Вопрос ${state.currentQuestionIndex + 1} из ${test.questions.size}",
                progress = (state.currentQuestionIndex + 1).toFloat() / test.questions.size,
                timeElapsed = state.timeElapsed,
                onClose = onBack
            )

            // Question Progress Dots
            QuestionProgressDots(
                currentIndex = state.currentQuestionIndex,
                totalQuestions = test.questions.size,
                answeredQuestions = state.answers.keys,
                questions = test.questions,
                onQuestionClick = onGoToQuestion
            )

            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Question content
                currentQuestion?.let { question ->
                    AnimatedContent(
                        targetState = state.currentQuestionIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { it } + fadeIn() togetherWith
                                        slideOutHorizontally { -it } + fadeOut()
                            } else {
                                slideInHorizontally { -it } + fadeIn() togetherWith
                                        slideOutHorizontally { it } + fadeOut()
                            }
                        }
                    ) { _ ->
                        QuestionContent(
                            question = question,
                            selectedAnswerIds = currentAnswer?.selectedAnswerIds ?: emptyList(),
                            dragDropMatches = currentAnswer?.dragDropMatches ?: emptyMap(),
                            onSelectAnswer = { answerId -> onSelectAnswer(question.id, answerId) },
                            onSetDragDropMatch = { answerId, target ->
                                onSetDragDropMatch(question.id, answerId, target)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // Bottom Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            FunnyColors.Background,
                            FunnyColors.Background
                        )
                    )
                )
                .padding(24.dp)
        ) {
            if (isLastQuestion) {
                Button(
                    onClick = onSubmit,
                    enabled = !state.isSubmitting && currentAnswer != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FunnyColors.AccentPurple
                    )
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Завершить тест",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            } else {
                Button(
                    onClick = onNextQuestion,
                    enabled = currentAnswer != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FunnyColors.Primary
                    )
                ) {
                    Text(
                        text = "Проверить ответ",
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
private fun TestTopBar(
    title: String,
    progress: Float,
    timeElapsed: Int,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FunnyColors.Background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = FunnyColors.OnBackground
                )
            }

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

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
                    val minutes = timeElapsed / 60
                    val seconds = timeElapsed % 60
                    Text(
                        text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FunnyColors.Primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${(progress * 100).toInt()}% завершено",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FunnyColors.TextSecondary,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = FunnyColors.Primary,
            trackColor = FunnyColors.SurfaceVariant
        )
    }
}

@Composable
private fun QuestionProgressDots(
    currentIndex: Int,
    totalQuestions: Int,
    answeredQuestions: Set<String>,
    questions: List<Question>,
    onQuestionClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(questions) { index, question ->
            val isAnswered = question.id in answeredQuestions
            val isCurrent = index == currentIndex

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrent -> FunnyColors.Primary
                            isAnswered -> FunnyColors.Success.copy(alpha = 0.2f)
                            else -> Color.White
                        }
                    )
                    .border(
                        width = if (isCurrent) 0.dp else 1.dp,
                        color = when {
                            isCurrent -> Color.Transparent
                            isAnswered -> FunnyColors.Success
                            else -> FunnyColors.Border
                        },
                        shape = CircleShape
                    )
                    .clickable { onQuestionClick(index) },
                contentAlignment = Alignment.Center
            ) {
                if (isAnswered && !isCurrent) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = FunnyColors.Success
                    )
                } else {
                    Text(
                        text = (index + 1).toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) Color.White else FunnyColors.OnSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionContent(
    question: Question,
    selectedAnswerIds: List<String>,
    dragDropMatches: Map<String, String>,
    onSelectAnswer: (String) -> Unit,
    onSetDragDropMatch: (String, String) -> Unit
) {
    Column {
        // Question text
        question.text?.let { text ->
            Text(
                text = text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 30.sp
            )
        }

        question.audioUrl?.let {
            Spacer(modifier = Modifier.height(16.dp))
            AudioPlayerButton(url = it)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Answers based on question type
        when (question.type) {
            QuestionType.DRAG_DROP_IMAGE -> {
                DragDropQuestion(
                    answers = question.answers,
                    matches = dragDropMatches,
                    onMatch = onSetDragDropMatch
                )
            }
            QuestionType.AUDIO_SELECT, QuestionType.IMAGE_SELECT, QuestionType.TEXT_SELECT, QuestionType.FILL_BLANK -> {
                AnswerOptions(
                    answers = question.answers,
                    selectedIds = selectedAnswerIds,
                    onSelect = onSelectAnswer
                )
            }
        }
    }
}

@Composable
private fun AudioPlayerButton(url: String) {
    var isPlaying by remember { mutableStateOf(false) }
    val audioPlayer = remember { AudioPlayer() }
    val scope = rememberCoroutineScope()

    DisposableEffect(audioPlayer) {
        audioPlayer.setOnCompletionListener {
            scope.launch {
                isPlaying = false
            }
        }
        onDispose {
            audioPlayer.release()
        }
    }

    Button(
        onClick = {
            if (isPlaying) {
                audioPlayer.pause()
            } else {
                audioPlayer.play(url)
            }
            isPlaying = !isPlaying
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FunnyColors.Primary.copy(alpha = 0.1f)
        )
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
            tint = FunnyColors.Primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isPlaying) "Пауза" else "Послушать",
            color = FunnyColors.Primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AnswerOptions(
    answers: List<Answer>,
    selectedIds: List<String>,
    onSelect: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        answers.forEachIndexed { index, answer ->
            val isSelected = answer.id in selectedIds

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(answer.id) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        Color.White
                    else
                        Color.White
                ),
                border = if (isSelected)
                    CardDefaults.outlinedCardBorder().copy(
                        width = 3.dp,
                        brush = Brush.linearGradient(listOf(FunnyColors.AccentPurple, FunnyColors.AccentPurple))
                    )
                else
                    CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 4.dp else 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    answer.text?.let { text ->
                        Text(
                            text = text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) FunnyColors.AccentPurple else FunnyColors.OnBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DragDropQuestion(
    answers: List<Answer>,
    matches: Map<String, String>,
    onMatch: (String, String) -> Unit
) {
    val targets = answers.mapNotNull { it.matchTarget }.distinct()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Соедините картинки со словами:",
            fontSize = 14.sp,
            color = FunnyColors.TextSecondary
        )

        answers.forEach { answer ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FunnyColors.SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", fontSize = 32.sp)
                }

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = FunnyColors.TextSecondary
                )

                var expanded by remember { mutableStateOf(false) }
                val selectedTarget = matches[answer.id]

                Surface(
                    modifier = Modifier
                        .width(140.dp)
                        .clickable { expanded = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTarget != null)
                        FunnyColors.Success.copy(alpha = 0.1f)
                    else
                        Color.White,
                    border = if (selectedTarget != null)
                        CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(FunnyColors.Success, FunnyColors.Success))
                        )
                    else null
                ) {
                    Text(
                        text = selectedTarget ?: "Выбрать",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = if (selectedTarget != null) FontWeight.Bold else FontWeight.Normal
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    targets.forEach { target ->
                        DropdownMenuItem(
                            text = { Text(target) },
                            onClick = {
                                onMatch(answer.id, target)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TestResultScreen(
    result: SubmitTestResult,
    testTitle: String,
    onContinue: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FunnyColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Stars
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = if (result.stars >= 1) FunnyColors.StarFilled else FunnyColors.StarEmpty,
                modifier = Modifier.size(56.dp)
            )
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = if (result.stars >= 2) FunnyColors.StarFilled else FunnyColors.StarEmpty,
                modifier = Modifier
                    .size(72.dp)
                    .offset(y = (-8).dp)
            )
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = if (result.stars >= 3) FunnyColors.StarFilled else FunnyColors.StarEmpty,
                modifier = Modifier.size(56.dp)
            )
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
            color = FunnyColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Score Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ваш результат",
                    fontSize = 14.sp,
                    color = FunnyColors.TextSecondary
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
                    color = FunnyColors.SurfaceVariant
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

                    if (result.isNewBestScore) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FunnyColors.Secondary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "🏆 Рекорд!",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                color = FunnyColors.Secondary
                            )
                        }
                    }
                }
            }
        }

        // Level up
        result.levelUp?.let { levelUp ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FunnyColors.Secondary.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎉", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Новый уровень!",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = levelUp.newTitle,
                            color = FunnyColors.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Achievements
        if (result.newAchievements.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Новые достижения:",
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            result.newAchievements.forEach { achievement ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🏅", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = achievement.name,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = achievement.description,
                                fontSize = 12.sp,
                                color = FunnyColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action buttons
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FunnyColors.Primary
            )
        ) {
            Text(
                text = "На главную",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = FunnyColors.Primary
            )
        ) {
            Text(
                text = "Попробовать снова",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
