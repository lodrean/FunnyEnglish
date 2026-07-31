package com.funnyenglish.app.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import kotlinx.coroutines.delay

/**
 * Модальное окно при повышении уровня
 * 
 * Показывает анимацию повышения уровня с конфетти и информацией
 * о новых разблокированных возможностях.
 */

@Composable
fun LevelUpModal(
    oldLevel: Int,
    newLevel: Int,
    unlockedFeatures: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onShare: (() -> Unit)? = null
) {
    val reduceMotion = LocalReduceMotion.current
    var showConfetti by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(3000)
        showConfetti = false
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        // Confetti animation
        ConfettiAnimation(
            modifier = Modifier.fillMaxSize(),
            isActive = showConfetti && !reduceMotion,
            particleCount = 100,
            duration = 3000
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(SpaceLg),
            shape = RoundedCornerShape(CardRadius),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(SpaceXl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpaceMd)
            ) {
                // Level badge with animation
                LevelBadge(
                    level = newLevel,
                    reduceMotion = reduceMotion
                )
                
                Spacer(modifier = Modifier.height(SpaceMd))
                
                // Title
                Text(
                    text = "Новый уровень!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Level transition text
                Text(
                    text = "$oldLevel → $newLevel",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.funnyColors.xp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(SpaceSm))
                
                // Unlocked features
                if (unlockedFeatures.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(CardRadius / 2)
                    ) {
                        Column(
                            modifier = Modifier.padding(SpaceMd),
                            verticalArrangement = Arrangement.spacedBy(SpaceSm)
                        ) {
                            Text(
                                text = "🔓 Разблокировано:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            unlockedFeatures.forEach { feature ->
                                Text(
                                    text = "• $feature",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(SpaceLg))
                
                // Action buttons
                FunnyButton(
                    text = "Продолжить",
                    onClick = onDismiss,
                    type = FunnyButtonType.PRIMARY,
                    size = FunnyButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (onShare != null) {
                    FunnyButton(
                        text = "Поделиться",
                        onClick = onShare,
                        type = FunnyButtonType.TERTIARY,
                        size = FunnyButtonSize.MEDIUM,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelBadge(
    level: Int,
    reduceMotion: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "level_pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (!reduceMotion) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_pulse"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = if (!reduceMotion) 5f else -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_rotation"
    )
    
    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.funnyColors.xpContainer, CircleShape)
        )
        
        // Main badge
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(MaterialTheme.funnyColors.xp, MaterialTheme.funnyColors.xp.copy(alpha = 0.8f))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = level.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Компактный индикатор уровня для профиля
 */
@Composable
fun XpProgressBar(
    currentLevel: Int,
    currentXp: Int,
    xpForNextLevel: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (xpForNextLevel > 0) {
        currentXp.toFloat() / xpForNextLevel
    } else 1f
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpaceXs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Уровень $currentLevel",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$currentXp / $xpForNextLevel XP",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Плавающий виджет XP при заработке
 */
@Composable
fun FloatingXpWidget(
    xpAmount: Int,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalReduceMotion.current
    
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (!reduceMotion) -10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )
    
    Card(
        modifier = modifier
            .offset(y = offsetY.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.funnyColors.xp
        ),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SpaceMd, vertical = SpaceSm),
            horizontalArrangement = Arrangement.spacedBy(SpaceXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = xpAmount.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "XP",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
