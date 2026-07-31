package com.funnyenglish.app.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion
import com.funnyenglish.designsystem.animations.ConfettiAnimation
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.designsystem.theme.funnyColors
import com.funnyenglish.shared.model.Achievement
import com.funnyenglish.shared.model.Rarity
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Модальное окно при разблокировке достижения
 * 
 * Показывает 3D анимацию значка достижения с эффектом sparkle.
 */

@Composable
fun AchievementUnlockModal(
    achievement: Achievement,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val reduceMotion = LocalReduceMotion.current
    var showConfetti by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(2500)
        showConfetti = false
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        // Confetti for rare+ achievements
        if (achievement.rarity?.ordinal ?: 0 >= Rarity.RARE.ordinal) {
            ConfettiAnimation(
                modifier = Modifier.fillMaxSize(),
                isActive = showConfetti && !reduceMotion,
                particleCount = 80,
                duration = 2500
            )
        }
        
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
                // Achievement badge with 3D rotation
                AchievementBadge3D(
                    achievement = achievement,
                    reduceMotion = reduceMotion
                )
                
                Spacer(modifier = Modifier.height(SpaceMd))
                
                // Unlocked text
                Text(
                    text = "Достижение разблокировано!",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                // Achievement name
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                // Description
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                // Rarity badge
                achievement.rarity?.let { RarityBadge(rarity = it) }
                
                // XP reward
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.funnyColors.xpContainer
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
                            color = MaterialTheme.funnyColors.xp
                        )
                        Text(
                            text = achievement.pointsReward.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.funnyColors.xp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.funnyColors.xp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(SpaceLg))
                
                // Action buttons
                FunnyButton(
                    text = "Отлично!",
                    onClick = onDismiss,
                    type = FunnyButtonType.PRIMARY,
                    size = FunnyButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth()
                )
                
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

@Composable
private fun AchievementBadge3D(
    achievement: Achievement,
    reduceMotion: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_3d")
    
    // 3D rotation effect
    val rotationYAnim by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = if (!reduceMotion) 15f else -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation_y"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (!reduceMotion) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (!reduceMotion) 0.6f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = Modifier
            .size(140.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    getRarityColor(achievement.rarity ?: Rarity.COMMON).copy(alpha = glowAlpha),
                    CircleShape
                )
        )
        
        // Main badge with 3D rotation
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    rotationY = rotationYAnim
                    cameraDistance = 8f * density
                }
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            getRarityColor(achievement.rarity ?: Rarity.COMMON),
                            getRarityColor(achievement.rarity ?: Rarity.COMMON).copy(alpha = 0.7f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = achievement.iconUrl ?: "🏆",
                fontSize = 64.sp
            )
        }
        
        // Sparkle effects
        if (!reduceMotion) {
            repeat(4) { index ->
                val angle = (index * 90 + 45) * (PI / 180)
                val distance = 70
                val x = (cos(angle) * distance).dp
                val y = (sin(angle) * distance).dp
                
                Sparkle(
                    modifier = Modifier.offset(x, y),
                    delay = index * 200
                )
            }
        }
    }
}

@Composable
private fun Sparkle(
    modifier: Modifier = Modifier,
    delay: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_scale"
    )
    
    val alphaValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_alpha"
    )
    
    Box(
        modifier = modifier
            .size(12.dp)
            .scale(scale)
            .alpha(alphaValue)
            .background(MaterialTheme.funnyColors.xp, CircleShape)
    )
}

@Composable
private fun RarityBadge(rarity: Rarity) {
    val (text, color) = when (rarity) {
        Rarity.COMMON -> "Обычное" to MaterialTheme.colorScheme.outline
        Rarity.UNCOMMON -> "Необычное" to MaterialTheme.colorScheme.primary
        Rarity.RARE -> "Редкое" to AchievementPurple
        Rarity.EPIC -> "Эпическое" to GemTeal
        Rarity.LEGENDARY -> "Легендарное" to StreakOrange
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = SpaceMd, vertical = SpaceXs)
        )
    }
}

@Composable
private fun getRarityColor(rarity: Rarity): androidx.compose.ui.graphics.Color {
    return when (rarity) {
        Rarity.COMMON -> MaterialTheme.colorScheme.primary
        Rarity.UNCOMMON -> MaterialTheme.colorScheme.secondary
        Rarity.RARE -> AchievementPurple
        Rarity.EPIC -> GemTeal
        Rarity.LEGENDARY -> StreakOrange
    }
}
