package com.funnyenglish.app.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.app.components.questions.ImageWordMatchQuestion
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.designsystem.theme.funnyColors
import com.funnyenglish.designsystem.layout.safeContentPadding
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.components.cards.FunnyCard
import com.funnyenglish.app.viewmodel.TestPlayState
import com.funnyenglish.shared.model.*
import com.funnyenglish.shared.platform.AudioPlayer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPlayScreen(
    state: TestPlayState,
    isGuest: Boolean = false,
    onBack: () -> Unit,
    onSelectAnswer: (String, String) -> Unit,
    onSetDragDropMatch: (String, String, String) -> Unit,
    onSetImageWordMatch: (String, String, String) -> Unit,  // questionId, wordId, hotspotId
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

    if (state.error != null) {
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
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = state.error ?: "Неизвестная ошибка",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onSubmit) {
                    Text("Повторить")
                }
                TextButton(onClick = onBack) {
                    Text("Вернуться")
                }
            }
        }
        return
    }

    // Show result if available
    if (state.result != null) {
        TestResultScreen(
            result = state.result,
            testTitle = test.title,
            isGuest = isGuest,
            onContinue = onShowResult,
            onRetry = onShowResult
        )
        return
    }

    val totalQuestions = test.questions.size
    if (totalQuestions == 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Тест пока пуст",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "В этом тесте еще нет вопросов.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onBack) {
                    Text("Вернуться")
                }
            }
        }
        return
    }

    val safeQuestionIndex = state.currentQuestionIndex.coerceIn(0, totalQuestions - 1)
    val currentQuestion = test.questions.getOrNull(safeQuestionIndex)
    val isLastQuestion = safeQuestionIndex == totalQuestions - 1
    val currentAnswer = currentQuestion?.let { state.answers[it.id] }
    val allQuestionsAnswered = state.answers.size >= totalQuestions

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .testTag("test_play_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TestTopBar(
                title = "Вопрос ${safeQuestionIndex + 1} из $totalQuestions",
                progress = (safeQuestionIndex + 1).toFloat() / totalQuestions,
                timeElapsed = state.timeElapsed,
                onClose = onBack
            )

            // Question Progress Dots
            QuestionProgressDots(
                currentIndex = safeQuestionIndex,
                totalQuestions = totalQuestions,
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
                        targetState = safeQuestionIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { it } + fadeIn() togetherWith
                                        slideOutHorizontally { -it } + fadeOut()
                            } else {
                                slideInHorizontally { -it } + fadeIn() togetherWith
                                        slideOutHorizontally { it } + fadeOut()
                            }
                        }
                    ) { questionIndex ->
                        QuestionContent(
                            question = question,
                            questionIndex = questionIndex + 1,
                            selectedAnswerIds = currentAnswer?.selectedAnswerIds ?: emptyList(),
                            dragDropMatches = currentAnswer?.dragDropMatches ?: emptyMap(),
                            imageWordMatches = currentAnswer?.imageWordMatches ?: emptyMap(),
                            onSelectAnswer = { answerId -> onSelectAnswer(question.id, answerId) },
                            onSetDragDropMatch = { answerId, target ->
                                onSetDragDropMatch(question.id, answerId, target)
                            },
                            onSetImageWordMatch = { wordId, hotspotId ->
                                onSetImageWordMatch(question.id, wordId, hotspotId)
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
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(24.dp)
        ) {
            if (isLastQuestion) {
                Button(
                    onClick = onSubmit,
                    enabled = !state.isSubmitting && allQuestionsAnswered,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.funnyColors.achievement,
                        disabledContainerColor = MaterialTheme.funnyColors.achievement.copy(alpha = 0.5f)
                    )
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        val buttonText = if (allQuestionsAnswered) {
                            "Завершить тест"
                        } else {
                            "Ответьте на все вопросы (${state.answers.size}/$totalQuestions)"
                        }
                        Text(
                            text = buttonText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (allQuestionsAnswered) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
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
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Далее",
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
            .background(MaterialTheme.colorScheme.background)
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
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("question_number")
            )

            // Timer
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
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
                        color = MaterialTheme.colorScheme.primary
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
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
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isAnswered -> MaterialTheme.funnyColors.success.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    )
                    .border(
                        width = if (isCurrent) 0.dp else 1.dp,
                        color = when {
                            isCurrent -> Color.Transparent
                            isAnswered -> MaterialTheme.funnyColors.success
                            else -> MaterialTheme.colorScheme.outline
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
                        tint = MaterialTheme.funnyColors.success
                    )
                } else {
                    Text(
                        text = (index + 1).toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionContent(
    question: Question,
    questionIndex: Int,
    selectedAnswerIds: List<String>,
    dragDropMatches: Map<String, String>,
    imageWordMatches: Map<String, String>,
    onSelectAnswer: (String) -> Unit,
    onSetDragDropMatch: (String, String) -> Unit,
    onSetImageWordMatch: (String, String) -> Unit  // wordId, hotspotId
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Question text with fallback - use dark color for guaranteed visibility
        val questionText = question.text?.takeIf { it.isNotBlank() } 
            ?: question.title?.takeIf { it.isNotBlank() }
            ?: "Вопрос ${questionIndex}"
        Text(
            text = questionText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("question_text"),
            lineHeight = 30.sp
        )

        question.audioUrl?.let {
            Spacer(modifier = Modifier.height(16.dp))
            AudioPlayerButton(url = it)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Question image (for IMAGE_SELECT type)
        if (question.type == QuestionType.IMAGE_SELECT && question.imageUrl != null) {
            SubcomposeAsyncImage(
                model = question.imageUrl,
                contentDescription = "Вопрос",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚠️ Ошибка загрузки изображения",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Answers based on question type
        when (question.type) {
            QuestionType.DRAG_DROP_IMAGE -> {
                DragDropQuestion(
                    answers = question.answers,
                    matches = dragDropMatches,
                    onMatch = onSetDragDropMatch
                )
            }
            QuestionType.IMAGE_SELECT -> {
                ImageAnswerOptions(
                    answers = question.answers,
                    selectedIds = selectedAnswerIds,
                    onSelect = onSelectAnswer
                )
            }
            QuestionType.IMAGE_WORD_MATCH -> {
                val content = question.imageWordMatchContent
                if (content != null) {
                    ImageWordMatchQuestion(
                        content = content,
                        currentMatches = imageWordMatches,
                        onMatch = { wordId, hotspotId ->
                            onSetImageWordMatch(wordId, hotspotId)
                        }
                    )
                } else {
                    ImageWordMatchPlaceholder()
                }
            }
            QuestionType.AUDIO_SELECT, QuestionType.TEXT_SELECT, QuestionType.FILL_BLANK -> {
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
private fun ImageWordMatchPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🖼️",
                fontSize = 48.sp
            )
            Text(
                text = "Image-Word Match",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Drag words to image areas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "(Implementation in progress)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AudioPlayerButton(url: String) {
    val sanitizedUrl = url.trim()
    if (sanitizedUrl.isEmpty()) {
        return
    }
    var isPlaying by remember(sanitizedUrl) { mutableStateOf(false) }
    val audioPlayer = remember(sanitizedUrl) { AudioPlayer() }
    val scope = rememberCoroutineScope()

    DisposableEffect(audioPlayer, sanitizedUrl) {
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
            val shouldPlay = !isPlaying
            if (shouldPlay) {
                audioPlayer.play(sanitizedUrl)
            } else {
                audioPlayer.pause()
            }
            isPlaying = shouldPlay
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isPlaying) "Пауза" else "Послушать",
            color = MaterialTheme.colorScheme.primary,
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
    if (answers.isEmpty()) {
        Text(
            text = "Нет вариантов ответа",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        answers.forEachIndexed { index, answer ->
            val isSelected = answer.id in selectedIds
            val displayText = answer.text?.trim()?.takeIf { it.isNotEmpty() }
                ?: "Вариант ${index + 1}"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(answer.id) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected)
                    CardDefaults.outlinedCardBorder().copy(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
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
                    Text(
                        text = displayText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageAnswerOptions(
    answers: List<Answer>,
    selectedIds: List<String>,
    onSelect: (String) -> Unit
) {
    if (answers.isEmpty()) {
        Text(
            text = "Нет вариантов ответа",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.heightIn(max = 400.dp)
    ) {
        items(answers.size) { index ->
            val answer = answers[index]
            val isSelected = answer.id in selectedIds
            val displayText = answer.text?.trim()?.takeIf { it.isNotEmpty() }
                ?: "Вариант ${index + 1}"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { onSelect(answer.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected)
                    CardDefaults.outlinedCardBorder().copy(
                        width = 3.dp,
                        brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
                    )
                else
                    CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
                    ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 4.dp else 1.dp
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    answer.imageUrl?.let { imageUrl ->
                        SubcomposeAsyncImage(
                            model = imageUrl,
                            contentDescription = answer.text,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        )
                    } ?: run {
                        Text(
                            text = displayText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(16.dp)
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
    if (answers.isEmpty()) {
        Text(
            text = "Нет элементов для сопоставления",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    val targets = answers
        .mapNotNull { it.matchTarget?.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

    if (targets.isEmpty()) {
        Text(
            text = "Нет доступных вариантов для сопоставления",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Соедините картинки со словами:",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", fontSize = 32.sp)
                }

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                var expanded by remember { mutableStateOf(false) }
                val selectedTarget = matches[answer.id]

                Surface(
                    modifier = Modifier
                        .width(140.dp)
                        .clickable { expanded = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTarget != null)
                        MaterialTheme.funnyColors.success.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surface,
                    border = if (selectedTarget != null)
                        CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(MaterialTheme.funnyColors.success, MaterialTheme.funnyColors.success))
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
    isGuest: Boolean,
    onContinue: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("results_view"),
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
                tint = if (result.stars >= 1) MaterialTheme.funnyColors.xp else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(56.dp)
            )
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = if (result.stars >= 2) MaterialTheme.funnyColors.xp else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(72.dp)
                    .offset(y = (-8).dp)
            )
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = if (result.stars >= 3) MaterialTheme.funnyColors.xp else MaterialTheme.colorScheme.surfaceVariant,
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
                result.percentage >= 80 -> MaterialTheme.funnyColors.success
                result.percentage >= 60 -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.error
            }
        )

        Text(
            text = testTitle,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isGuest) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Войдите, чтобы сохранить прогресс",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "В гостевом режиме результаты хранятся только на этом устройстве",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

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
                    color = MaterialTheme.colorScheme.primary
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
                            tint = MaterialTheme.funnyColors.success,
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
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.funnyColors.xp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${result.pointsEarned} XP",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (result.isNewBestScore) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "🏆 Рекорд!",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
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
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                containerColor = MaterialTheme.colorScheme.primary
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
                contentColor = MaterialTheme.colorScheme.primary
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
