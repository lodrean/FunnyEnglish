package com.funnyenglish.app.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion
import com.funnyenglish.designsystem.animations.ConfettiAnimation
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.designsystem.theme.funnyColors
import com.funnyenglish.shared.model.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage

/**
 * Компонент микро-упражнения для адаптивных уроков
 * 
 * Показывает вопрос с вариантами ответов и обратную связь.
 * Оптимизирован для быстрых сессий 1-2 минуты.
 */

@Composable
fun MicroExerciseView(
    question: Question,
    questionNumber: Int,
    totalQuestions: Int,
    feedback: FeedbackResponse?,
    selectedAnswerId: String? = null,
    onAnswerSelected: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    isSubmitting: Boolean = false,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalReduceMotion.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpaceMd),
        verticalArrangement = Arrangement.spacedBy(SpaceMd)
    ) {
        // Question header with number
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Вопрос $questionNumber из $totalQuestions",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Skip button
            if (feedback == null) {
                FunnyButton(
                    text = "Пропустить",
                    onClick = onSkip,
                    type = FunnyButtonType.TERTIARY,
                    size = FunnyButtonSize.SMALL
                )
            }
        }
        
        // Question text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = CardShape
        ) {
            Text(
                text = question.text ?: "Вопрос",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpaceLg)
            )
        }
        
        // Question image if available
        question.imageUrl?.let { imageUrl ->
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(CardRadius))
            )
        }
        
        Spacer(modifier = Modifier.height(SpaceSm))
        
        // Answer options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SpaceSm)
        ) {
            question.answers.forEach { answer ->
                val isSelected = selectedAnswerId == answer.id
                val isAnswered = feedback != null
                val showCorrect = feedback?.isCorrect == true && isSelected
                val showWrong = feedback?.isCorrect == false && isSelected
                
                AnswerOption(
                    answer = answer,
                    isSelected = isSelected,
                    showCorrect = showCorrect,
                    showWrong = showWrong,
                    isEnabled = !isAnswered && !isSubmitting,
                    onClick = { onAnswerSelected(answer.id) },
                    reduceMotion = reduceMotion
                )
            }
        }
        
        // Feedback section
        AnimatedVisibility(
            visible = feedback != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            feedback?.let { fb ->
                FeedbackCard(
                    feedback = fb,
                    onNext = onNext,
                    reduceMotion = reduceMotion
                )
            }
        }
        
        // Confetti for correct answer
        if (feedback?.isCorrect == true && !reduceMotion) {
            ConfettiAnimation(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                isActive = true,
                particleCount = 50,
                duration = 1500
            )
        }
    }
}

@Composable
private fun AnswerOption(
    answer: Answer,
    isSelected: Boolean,
    showCorrect: Boolean,
    showWrong: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    reduceMotion: Boolean
) {
    val backgroundColor = when {
        showCorrect -> MaterialTheme.funnyColors.success.copy(alpha = 0.2f)
        showWrong -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when {
        showCorrect -> MaterialTheme.funnyColors.success
        showWrong -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected && !reduceMotion) 1.02f else 1f,
        animationSpec = tween(150),
        label = "answer_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = borderColor
        ),
        shape = ButtonShape,
        onClick = onClick,
        enabled = isEnabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceMd),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Answer content
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(SpaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Answer image if available
                answer.imageUrl?.let { imageUrl ->
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                
                Text(
                    text = answer.text ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        showCorrect -> MaterialTheme.funnyColors.success
                        showWrong -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            
            // Status icon
            when {
                showCorrect -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Correct",
                    tint = MaterialTheme.funnyColors.success,
                    modifier = Modifier.size(24.dp)
                )
                showWrong -> Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Wrong",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    feedback: FeedbackResponse,
    onNext: () -> Unit,
    reduceMotion: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = SpaceMd),
        colors = CardDefaults.cardColors(
            containerColor = if (feedback.isCorrect)
                MaterialTheme.funnyColors.successContainer
            else
                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        ),
        shape = CardShape
    ) {
        Column(
            modifier = Modifier.padding(SpaceLg),
            verticalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            // Feedback header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpaceSm)
            ) {
                Icon(
                    imageVector = if (feedback.isCorrect) 
                        Icons.Default.CheckCircle 
                    else 
                        Icons.Default.Close,
                    contentDescription = if (feedback.isCorrect) "Correct" else "Wrong",
                    tint = if (feedback.isCorrect) MaterialTheme.funnyColors.success else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                
                Text(
                    text = if (feedback.isCorrect) "Правильно!" else "Почти получилось",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (feedback.isCorrect) MaterialTheme.funnyColors.success else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // XP earned
            if (feedback.xpEarned > 0) {
                Text(
                    text = "+${feedback.xpEarned} XP",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.funnyColors.xp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Explanation
            feedback.explanation?.let { explanation ->
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Grammar note
            feedback.grammarNote?.let { grammarNote ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.funnyColors.infoContainer
                    ),
                    shape = RoundedCornerShape(CardRadius / 2)
                ) {
                    Text(
                        text = "💡 $grammarNote",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.funnyColors.info,
                        modifier = Modifier.padding(SpaceMd)
                    )
                }
            }
            
            // Weak area identified
            feedback.weakAreaIdentified?.let { weakArea ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.funnyColors.achievementContainer
                    ),
                    shape = RoundedCornerShape(CardRadius / 2)
                ) {
                    Text(
                        text = "📚 Эта тема требует дополнительной практики: ${weakArea.skillType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.funnyColors.achievement,
                        modifier = Modifier.padding(SpaceMd)
                    )
                }
            }
            
            // Next button
            FunnyButton(
                text = "Дальше",
                onClick = onNext,
                type = FunnyButtonType.PRIMARY,
                size = FunnyButtonSize.LARGE,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


