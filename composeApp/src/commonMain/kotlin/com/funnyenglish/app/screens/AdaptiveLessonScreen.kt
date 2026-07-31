package com.funnyenglish.app.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.funnyenglish.app.components.*
import com.funnyenglish.app.viewmodel.AdaptiveLessonUiState
import com.funnyenglish.app.viewmodel.AdaptiveLessonViewModel
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion
import com.funnyenglish.designsystem.animations.ConfettiAnimation
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.components.cards.FunnyCard
import com.funnyenglish.designsystem.components.cards.FunnyCardType
import com.funnyenglish.designsystem.components.gamification.FunnyXPCounter
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.designsystem.theme.funnyColors
import com.funnyenglish.designsystem.layout.safeContentPadding
import org.koin.compose.viewmodel.koinViewModel

/**
 * Основной экран адаптивного микро-урока
 * 
 * Управляет отображением вопросов, перерывов и завершения урока.
 * Интегрируется с DifficultyEngine для динамической сложности.
 */

@Composable
fun AdaptiveLessonScreen(
    categoryId: String? = null,
    targetDurationMinutes: Int = 5,
    isGuest: Boolean = false,
    onLessonComplete: (xpEarned: Int) -> Unit,
    onLessonExit: () -> Unit,
    viewModel: AdaptiveLessonViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val reduceMotion = LocalReduceMotion.current
    
    // Start lesson on first composition
    LaunchedEffect(Unit) {
        if (uiState.lesson == null && !uiState.isLoading) {
            viewModel.startLesson(categoryId, targetDurationMinutes, isGuest)
        }
    }
    
    // Handle lesson completion
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onLessonComplete(uiState.earnedXp)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
    ) {
        when {
            // Loading state
            uiState.isLoading -> {
                LoadingLessonState()
            }
            
            // Error state
            uiState.error != null -> {
                ErrorLessonState(
                    error = uiState.error!!,
                    onRetry = { viewModel.startLesson(categoryId, targetDurationMinutes) },
                    onExit = onLessonExit
                )
            }
            
            // Break screen
            uiState.showBreak -> {
                BreakScreen(
                    breakDurationSeconds = 30,
                    streakDays = 0, // TODO: Get from user profile
                    onResume = { viewModel.resumeLesson() },
                    onEndLesson = { 
                        viewModel.completeLesson()
                        onLessonExit()
                    }
                )
            }
            
            // Lesson complete
            uiState.isComplete -> {
                LessonCompleteScreen(
                    xpEarned = uiState.earnedXp,
                    totalCorrect = uiState.totalCorrect,
                    totalAnswered = uiState.totalAnswered,
                    weakAreasImproved = uiState.weakAreasImproved,
                    onContinue = { onLessonComplete(uiState.earnedXp) },
                    reduceMotion = reduceMotion
                )
            }
            
            // Active lesson
            else -> {
                ActiveLessonContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    onExit = onLessonExit
                )
            }
        }
    }
}

@Composable
private fun ActiveLessonContent(
    uiState: AdaptiveLessonUiState,
    viewModel: AdaptiveLessonViewModel,
    onExit: () -> Unit
) {
    val lesson = uiState.lesson ?: return
    val currentSegment = lesson.segments.getOrNull(lesson.currentSegmentIndex)
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar with progress and controls
        LessonTopBar(
            currentSegment = lesson.currentSegmentIndex,
            totalSegments = lesson.totalSegments,
            segmentProgress = viewModel.getSegmentProgress(),
            overallProgress = viewModel.getOverallProgress(),
            remainingSeconds = viewModel.getRemainingTimeSeconds(),
            onExit = onExit
        )
        
        // Main content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            currentSegment?.let { segment ->
                uiState.currentQuestion?.let { question ->
                    AnimatedContent(
                        targetState = question.id,
                        transitionSpec = {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                        },
                        label = "question_transition"
                    ) { _ ->
                        MicroExerciseView(
                            question = question,
                            questionNumber = uiState.currentQuestionIndex + 1,
                            totalQuestions = segment.questions.size,
                            feedback = uiState.feedback,
                            onAnswerSelected = { answerId ->
                                viewModel.submitAnswer(answerId)
                            },
                            onNext = { viewModel.moveToNextQuestion() },
                            onSkip = { viewModel.skipQuestion() },
                            isSubmitting = uiState.isSubmitting,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        
        // Bottom progress bar
        LessonBottomBar(
            segmentLabel = currentSegment?.type?.name ?: "",
            learningObjective = currentSegment?.learningObjective ?: ""
        )
    }
}

@Composable
private fun LessonTopBar(
    currentSegment: Int,
    totalSegments: Int,
    segmentProgress: Float,
    overallProgress: Float,
    remainingSeconds: Int,
    onExit: () -> Unit
) {
    val segmentLabels = listOf("Intro", "Practice 1", "Practice 2", "Challenge", "Review")
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = ElevationSmall
    ) {
        Column(
            modifier = Modifier.padding(SpaceMd)
        ) {
            // Controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit button
                IconButton(onClick = onExit) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit lesson",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Timer
                val isWarning = remainingSeconds < 60
                LessonTimer(
                    remainingSeconds = remainingSeconds,
                    isWarning = isWarning
                )
                
                // Pause button (placeholder)
                IconButton(onClick = { /* TODO: Pause */ }) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(SpaceSm))
            
            // Segmented progress
            SegmentedProgressBar(
                totalSegments = totalSegments,
                currentSegmentIndex = currentSegment,
                segmentProgress = segmentProgress,
                overallProgress = overallProgress,
                segmentLabels = segmentLabels.take(totalSegments),
                showPercentage = false
            )
        }
    }
}

@Composable
private fun LessonBottomBar(
    segmentLabel: String,
    learningObjective: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = ElevationSmall
    ) {
        Column(
            modifier = Modifier.padding(SpaceMd)
        ) {
            // Current segment type
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpaceSm)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                )
                Text(
                    text = segmentLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Learning objective
            if (learningObjective.isNotEmpty()) {
                Text(
                    text = learningObjective,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoadingLessonState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Подготовка урока...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorLessonState(
    error: String,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpaceLg),
        contentAlignment = Alignment.Center
    ) {
        FunnyCard(
            type = FunnyCardType.FILLED,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(SpaceLg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpaceMd)
            ) {
                Text(
                    text = "😕",
                    style = MaterialTheme.typography.displayMedium
                )
                
                Text(
                    text = "Что-то пошло не так",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(SpaceMd))
                
                FunnyButton(
                    text = "Попробовать снова",
                    onClick = onRetry,
                    type = FunnyButtonType.PRIMARY,
                    size = FunnyButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth()
                )
                
                FunnyButton(
                    text = "Вернуться назад",
                    onClick = onExit,
                    type = FunnyButtonType.TERTIARY,
                    size = FunnyButtonSize.MEDIUM,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun LessonCompleteScreen(
    xpEarned: Int,
    totalCorrect: Int,
    totalAnswered: Int,
    weakAreasImproved: List<com.funnyenglish.shared.model.SkillGap>,
    onContinue: () -> Unit,
    reduceMotion: Boolean
) {
    val accuracy = if (totalAnswered > 0) (totalCorrect * 100 / totalAnswered) else 0
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Confetti animation
        var showConfetti by remember { mutableStateOf(!reduceMotion) }
        LaunchedEffect(Unit) {
            delay(3000)
            showConfetti = false
        }
        ConfettiAnimation(
            modifier = Modifier.fillMaxSize(),
            isActive = showConfetti,
            particleCount = 100,
            duration = 3000
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceXxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpaceLg)
        ) {
            // Success icon
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayLarge
            )
            
            // Title
            Text(
                text = "Урок завершён!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            // XP earned with animation
            var displayedXp by remember { mutableStateOf(0) }
            LaunchedEffect(xpEarned) {
                if (!reduceMotion) {
                    val steps = 30
                    val stepValue = xpEarned / steps
                    repeat(steps) {
                        displayedXp = (displayedXp + stepValue).coerceAtMost(xpEarned)
                        delay(50)
                    }
                }
                displayedXp = xpEarned
            }
            FunnyXPCounter(
                currentXp = displayedXp,
                animateChanges = !reduceMotion
            )
            
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    value = "$accuracy%",
                    label = "Точность",
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    value = "$totalCorrect/$totalAnswered",
                    label = "Правильно",
                    color = MaterialTheme.funnyColors.success
                )
            }
            
            // Skills improved
            if (weakAreasImproved.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.funnyColors.successContainer
                    ),
                    shape = CardShape
                ) {
                    Column(
                        modifier = Modifier.padding(SpaceMd),
                        verticalArrangement = Arrangement.spacedBy(SpaceSm)
                    ) {
                        Text(
                            text = "📈 Улучшенные навыки:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.funnyColors.success,
                            fontWeight = FontWeight.Medium
                        )
                        weakAreasImproved.forEach { skill ->
                            Text(
                                text = "• ${skill.skillType}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
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
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = CardShape
    ) {
        Column(
            modifier = Modifier.padding(horizontal = SpaceLg, vertical = SpaceMd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
