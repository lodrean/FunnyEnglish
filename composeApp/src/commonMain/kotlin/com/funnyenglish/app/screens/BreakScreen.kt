package com.funnyenglish.app.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.designsystem.theme.funnyColors
import kotlinx.coroutines.delay

/**
 * Экран перерыва с дыхательным упражнением
 * 
 * Показывается после 10 минут обучения или 5 сегментов.
 * Включает анимированное дыхательное упражнение для снятия напряжения.
 */

@Composable
fun BreakScreen(
    breakDurationSeconds: Int = 30,
    streakDays: Int = 0,
    onResume: () -> Unit,
    onEndLesson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalReduceMotion.current
    var remainingSeconds by remember { mutableStateOf(breakDurationSeconds) }
    var isBreathingIn by remember { mutableStateOf(true) }
    
    // Countdown timer
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }
    
    // Breathing animation cycle
    LaunchedEffect(Unit) {
        while (true) {
            isBreathingIn = true
            delay(4000) // Breathe in for 4 seconds
            isBreathingIn = false
            delay(4000) // Breathe out for 4 seconds
        }
    }
    
    // Breathing animation
    val breathingAnimation by animateFloatAsState(
        targetValue = if (isBreathingIn) 1.5f else 1f,
        animationSpec = if (reduceMotion) {
            snap()
        } else {
            tween(4000, easing = EaseInOutCubic)
        },
        label = "breathing"
    )
    
    // Progress for the break
    val progress = 1f - (remainingSeconds.toFloat() / breakDurationSeconds)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceXxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpaceLg)
        ) {
            // Icon
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = "Break",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            // Title
            Text(
                text = "Время передохнуть!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            // Subtitle
            Text(
                text = "Сделай глубокий вдох и расслабься",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(SpaceXl))
            
            // Breathing animation circle
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer rings
                if (!reduceMotion) {
                    repeat(3) { index ->
                        val delay = index * 1000
                        val infiniteTransition = rememberInfiniteTransition(label = "ring_$index")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.8f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(4000, delayMillis = delay, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "ring_scale"
                        )
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(4000, delayMillis = delay, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "ring_alpha"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(scale)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                                    CircleShape
                                )
                        )
                    }
                }
                
                // Main breathing circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(if (reduceMotion) 1f else breathingAnimation)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBreathingIn) "Вдох" else "Выдох",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(SpaceXl))
            
            // Break progress
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // Remaining time
            Text(
                text = "Перерыв закончится через ${remainingSeconds} сек",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Streak motivation
            if (streakDays > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.funnyColors.streakContainer
                    ),
                    shape = CardShape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpaceMd),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥 Твоя серия: $streakDays дней! Отдохни и продолжай!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.funnyColors.streak,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(SpaceSm)
            ) {
                FunnyButton(
                    text = "Продолжить урок",
                    onClick = onResume,
                    type = FunnyButtonType.PRIMARY,
                    size = FunnyButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth()
                )
                
                FunnyButton(
                    text = "Завершить урок",
                    onClick = onEndLesson,
                    type = FunnyButtonType.TERTIARY,
                    size = FunnyButtonSize.MEDIUM,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Мини-версия перерыва для встраивания в урок
 */
@Composable
fun MiniBreakCard(
    onTakeBreak: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = CardShape
    ) {
        Column(
            modifier = Modifier.padding(SpaceLg),
            verticalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            Text(
                text = "💡 Небольшой совет",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                text = "Ты уже 10 минут учишься! Рекомендуем сделать короткий перерыв для лучшего запоминания.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpaceSm)
            ) {
                FunnyButton(
                    text = "Сделать перерыв",
                    onClick = onTakeBreak,
                    type = FunnyButtonType.SECONDARY,
                    size = FunnyButtonSize.SMALL,
                    modifier = Modifier.weight(1f)
                )
                
                FunnyButton(
                    text = "Продолжить",
                    onClick = onContinue,
                    type = FunnyButtonType.TERTIARY,
                    size = FunnyButtonSize.SMALL,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
