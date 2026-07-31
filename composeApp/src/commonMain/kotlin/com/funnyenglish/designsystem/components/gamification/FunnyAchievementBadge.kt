package com.funnyenglish.designsystem.components.gamification

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion
import com.funnyenglish.designsystem.components.cards.FunnyCard
import com.funnyenglish.designsystem.tokens.AchievementPurple
import com.funnyenglish.designsystem.tokens.CardPadding
import com.funnyenglish.designsystem.tokens.SpaceSm
import kotlinx.coroutines.delay

/**
 * FunnyEnglish Achievement Badge
 * 
 * States:
 * - Locked: Grayscale, reduced opacity, lock overlay
 * - Unlocking: 3D rotation + sparkle (1000ms, CELEBRATION)
 * - Unlocked: Full color, subtle glow
 * 
 * Color: #9B7EDE (Purple)
 */

enum class AchievementState {
    LOCKED,
    UNLOCKING,
    UNLOCKED
}

@Composable
fun FunnyAchievementBadge(
    name: String,
    description: String,
    state: AchievementState,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
    },
    rarity: AchievementRarity = AchievementRarity.COMMON,
    onUnlockAnimationEnd: () -> Unit = {}
) {
    val reduceMotion = LocalReduceMotion.current
    var hasAnimated by remember { mutableStateOf(false) }
    
    // 3D rotation animation for unlocking
    val rotation by animateFloatAsState(
        targetValue = when {
            state == AchievementState.UNLOCKING && !reduceMotion -> 360f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "achievement_rotation"
    )
    
    // Scale animation
    val scale by animateFloatAsState(
        targetValue = when {
            state == AchievementState.UNLOCKING -> 1.1f
            state == AchievementState.UNLOCKED -> 1.05f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 300),
        label = "achievement_scale"
    )
    
    // Trigger callback after unlock animation
    LaunchedEffect(state) {
        if (state == AchievementState.UNLOCKING && !hasAnimated) {
            hasAnimated = true
            delay(1000)
            onUnlockAnimationEnd()
        }
    }
    
    val (backgroundColor, contentColor) = when (state) {
        AchievementState.LOCKED -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        AchievementState.UNLOCKING, AchievementState.UNLOCKED -> Pair(
            rarity.color.copy(alpha = 0.15f),
            rarity.color
        )
    }
    
    val alpha = if (state == AchievementState.LOCKED) 0.6f else 1f
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Badge icon container
        Box(
            modifier = Modifier
                .size(72.dp)
                .alpha(alpha)
                .scale(scale)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon()
            
            // Lock overlay for locked state
            if (state == AchievementState.LOCKED) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Sparkle effect for unlocked
            if (state == AchievementState.UNLOCKED && !reduceMotion) {
                // Sparkle animation would go here
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Name
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = if (state == AchievementState.LOCKED) 
                MaterialTheme.colorScheme.onSurfaceVariant 
            else 
                MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Description (only for unlocked)
        if (state != AchievementState.LOCKED) {
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

/**
 * Achievement rarity levels
 */
enum class AchievementRarity(
    val color: Color
) {
    COMMON(Color(0xFFB0BEC5)),      // Gray
    UNCOMMON(Color(0xFF81C784)),    // Green
    RARE(Color(0xFF64B5F6)),        // Blue
    EPIC(AchievementPurple),         // Purple
    LEGENDARY(Color(0xFFFFD54F))    // Gold
}

/**
 * Achievement card with details
 */
@Composable
fun FunnyAchievementCard(
    name: String,
    description: String,
    state: AchievementState,
    modifier: Modifier = Modifier,
    rarity: AchievementRarity = AchievementRarity.COMMON,
    earnedAt: String? = null,
    points: Int = 0
) {
    FunnyCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (state == AchievementState.LOCKED) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            rarity.color.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (state == AchievementState.LOCKED) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        rarity.color,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state == AchievementState.LOCKED) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Rarity indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(rarity.color, CircleShape)
                    )
                }
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                earnedAt?.let {
                    Text(
                        text = "Получено: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Points
            if (points > 0) {
                Text(
                    text = "+$points XP",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (state == AchievementState.LOCKED) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
