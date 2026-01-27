package com.funnyenglish.app.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.app.components.*
import com.funnyenglish.app.theme.FunnyColors
import com.funnyenglish.app.viewmodel.TestPlayState
import com.funnyenglish.shared.model.*

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
        TestResultContent(
            result = state.result,
            testTitle = test.title,
            onContinue = onShowResult
        )
        return
    }

    val currentQuestion = test.questions.getOrNull(state.currentQuestionIndex)
    val isLastQuestion = state.currentQuestionIndex == test.questions.size - 1
    val currentAnswer = currentQuestion?.let { state.answers[it.id] }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(test.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Timer
                    TimerDisplay(seconds = state.timeElapsed)
                }
            )
        },
        bottomBar = {
            TestBottomBar(
                currentIndex = state.currentQuestionIndex,
                totalQuestions = test.questions.size,
                isLastQuestion = isLastQuestion,
                isSubmitting = state.isSubmitting,
                hasAnswer = currentAnswer != null,
                onPrevious = onPreviousQuestion,
                onNext = onNextQuestion,
                onSubmit = onSubmit
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Question progress indicators
            QuestionProgressIndicator(
                currentIndex = state.currentQuestionIndex,
                totalQuestions = test.questions.size,
                answeredQuestions = state.answers.keys,
                questions = test.questions,
                onQuestionClick = onGoToQuestion
            )

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
        }
    }
}

@Composable
private fun TimerDisplay(seconds: Int) {
    val minutes = seconds / 60
    val secs = seconds % 60
    val timeText = "${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = FunnyColors.SurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = FunnyColors.Primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = timeText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuestionProgressIndicator(
    currentIndex: Int,
    totalQuestions: Int,
    answeredQuestions: Set<String>,
    questions: List<Question>,
    onQuestionClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                            isAnswered -> FunnyColors.Success.copy(alpha = 0.3f)
                            else -> FunnyColors.SurfaceVariant
                        }
                    )
                    .then(
                        if (isCurrent) Modifier.border(2.dp, FunnyColors.Primary, CircleShape)
                        else Modifier
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Question text or audio
        question.text?.let { text ->
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        question.audioUrl?.let {
            // Audio player button
            AudioPlayerButton(url = it)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Answers based on question type
        when (question.type) {
            QuestionType.DRAG_DROP_IMAGE -> {
                DragDropQuestion(
                    answers = question.answers,
                    matches = dragDropMatches,
                    onMatch = onSetDragDropMatch
                )
            }
            QuestionType.AUDIO_SELECT, QuestionType.IMAGE_SELECT, QuestionType.TEXT_SELECT -> {
                SelectableAnswers(
                    answers = question.answers,
                    selectedIds = selectedAnswerIds,
                    onSelect = onSelectAnswer
                )
            }
            QuestionType.FILL_BLANK -> {
                // Fill blank implementation
                SelectableAnswers(
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

    Button(
        onClick = { isPlaying = !isPlaying },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Info else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Пауза" else "Воспроизвести"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isPlaying) "Пауза" else "Послушать")
    }
}

@Composable
private fun SelectableAnswers(
    answers: List<Answer>,
    selectedIds: List<String>,
    onSelect: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        answers.forEach { answer ->
            val isSelected = answer.id in selectedIds

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(answer.id) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        FunnyColors.Primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected)
                    androidx.compose.foundation.BorderStroke(2.dp, FunnyColors.Primary)
                else
                    null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Answer content
                    answer.imageUrl?.let {
                        // Image placeholder
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FunnyColors.SurfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    answer.text?.let { text ->
                        Text(
                            text = text,
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp
                        )
                    }

                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = FunnyColors.Primary
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
    // Simplified drag-drop - show as clickable pairs
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
                // Image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FunnyColors.SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("IMG", color = FunnyColors.TextSecondary)
                }

                // Arrow
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = FunnyColors.TextSecondary
                )

                // Target selection
                var expanded by remember { mutableStateOf(false) }
                val selectedTarget = matches[answer.id]

                Surface(
                    modifier = Modifier
                        .width(120.dp)
                        .clickable { expanded = true },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedTarget != null) FunnyColors.Success.copy(alpha = 0.1f) else FunnyColors.SurfaceVariant
                ) {
                    Text(
                        text = selectedTarget ?: "Выбрать",
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
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
private fun TestBottomBar(
    currentIndex: Int,
    totalQuestions: Int,
    isLastQuestion: Boolean,
    isSubmitting: Boolean,
    hasAnswer: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            OutlinedButton(
                onClick = onPrevious,
                enabled = currentIndex > 0
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Назад")
            }

            // Progress text
            Text(
                text = "${currentIndex + 1} / $totalQuestions",
                fontWeight = FontWeight.Medium
            )

            // Next/Submit button
            if (isLastQuestion) {
                FunnyButton(
                    text = if (isSubmitting) "..." else "Завершить",
                    onClick = onSubmit,
                    enabled = !isSubmitting,
                    modifier = Modifier.width(140.dp)
                )
            } else {
                Button(
                    onClick = onNext,
                    enabled = hasAnswer
                ) {
                    Text("Далее")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun TestResultContent(
    result: SubmitTestResult,
    testTitle: String,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Stars animation
        StarsDisplay(
            stars = result.stars,
            maxStars = 3,
            size = 48
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when {
                result.percentage >= 95 -> "Отлично!"
                result.percentage >= 80 -> "Хорошо!"
                result.percentage >= 60 -> "Неплохо!"
                else -> "Попробуй ещё!"
            },
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                result.percentage >= 80 -> FunnyColors.Success
                result.percentage >= 60 -> FunnyColors.Secondary
                else -> FunnyColors.Error
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = testTitle,
            fontSize = 16.sp,
            color = FunnyColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Score details
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${result.score} / ${result.maxScore}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = FunnyColors.Primary
                )
                Text(
                    text = "${result.percentage}% правильных ответов",
                    fontSize = 14.sp,
                    color = FunnyColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatChip(
                        icon = Icons.Default.Star,
                        value = "+${result.pointsEarned}",
                        label = "очков"
                    )

                    if (result.isNewBestScore) {
                        StatChip(
                            icon = Icons.Default.Star,
                            value = "Новый",
                            label = "рекорд!"
                        )
                    }
                }
            }
        }

        // Level up notification
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
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = FunnyColors.Secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Новый уровень!",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = levelUp.newTitle,
                            color = FunnyColors.TextSecondary
                        )
                    }
                }
            }
        }

        // New achievements
        if (result.newAchievements.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Новые достижения:",
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            result.newAchievements.forEach { achievement ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = FunnyColors.StarFilled
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(achievement.name, fontWeight = FontWeight.Medium)
                            Text(achievement.description, fontSize = 12.sp, color = FunnyColors.TextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        FunnyButton(
            text = "Продолжить",
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = FunnyColors.Primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(value, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(2.dp))
        Text(label, fontSize = 12.sp, color = FunnyColors.TextSecondary)
    }
}
