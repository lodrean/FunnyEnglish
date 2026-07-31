package com.funnyenglish.designsystem.animations

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion
import com.funnyenglish.designsystem.tokens.CardPadding
import com.funnyenglish.designsystem.tokens.CardRadius
import com.funnyenglish.designsystem.tokens.SpaceMd
import com.funnyenglish.designsystem.tokens.SpaceSm

/**
 * FunnyEnglish Loading Skeleton
 * 
 * Priority 5 (Must-have)
 * Animation: Shimmer effect
 * Purpose: Perceived performance, engagement retention
 * 
 * Shimmer: Gradient that moves across the skeleton
 */

@Composable
fun SkeletonContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(CardRadius),
    content: @Composable () -> Unit
) {
    val reduceMotion = LocalReduceMotion.current
    
    // Shimmer animation
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = if (reduceMotion) {
            infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        },
        label = "shimmer_translate"
    )
    
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200, 0f),
        end = Offset(translateAnim, 0f)
    )
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    ) {
        content()
    }
}

/**
 * Text skeleton (single line)
 */
@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f
) {
    SkeletonContainer(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(16.dp)
    ) {}
}

/**
 * Title skeleton
 */
@Composable
fun SkeletonTitle(
    modifier: Modifier = Modifier,
    widthFraction: Float = 0.7f
) {
    SkeletonContainer(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(24.dp)
    ) {}
}

/**
 * Circle skeleton (for avatars/icons)
 */
@Composable
fun SkeletonCircle(
    size: Int = 48,
    modifier: Modifier = Modifier
) {
    SkeletonContainer(
        modifier = modifier.size(size.dp),
        shape = CircleShape
    ) {}
}

/**
 * Card skeleton
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    lines: Int = 3,
    showImage: Boolean = true
) {
    SkeletonContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardRadius)
    ) {
        Column(
            modifier = Modifier.padding(CardPadding),
            verticalArrangement = Arrangement.spacedBy(SpaceSm)
        ) {
            if (showImage) {
                SkeletonContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(CardRadius / 2)
                ) {}
                Spacer(modifier = Modifier.height(SpaceSm))
            }
            
            SkeletonTitle(widthFraction = 0.8f)
            
            repeat(lines) {
                SkeletonText(
                    widthFraction = if (it == lines - 1) 0.6f else 1f
                )
            }
        }
    }
}

/**
 * List item skeleton
 */
@Composable
fun SkeletonListItem(
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpaceMd),
        horizontalArrangement = Arrangement.spacedBy(SpaceMd)
    ) {
        if (showIcon) {
            SkeletonCircle(size = 48)
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(SpaceSm)
        ) {
            SkeletonTitle(widthFraction = 0.7f)
            SkeletonText(widthFraction = 0.9f)
        }
    }
}

/**
 * Quest card skeleton
 */
@Composable
fun SkeletonQuestCard(
    modifier: Modifier = Modifier
) {
    SkeletonContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardRadius)
    ) {
        Column(
            modifier = Modifier.padding(CardPadding),
            verticalArrangement = Arrangement.spacedBy(SpaceSm)
        ) {
            // Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpaceSm),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                SkeletonCircle(size = 24)
                SkeletonTitle(widthFraction = 0.5f)
            }
            
            Spacer(modifier = Modifier.height(SpaceSm))
            
            // Progress bar skeleton
            SkeletonContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                shape = RoundedCornerShape(4.dp)
            ) {}
            
            Spacer(modifier = Modifier.height(SpaceSm))
            
            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonText(widthFraction = 0.3f)
                SkeletonContainer(
                    modifier = Modifier
                        .width(80.dp)
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {}
            }
        }
    }
}

/**
 * Full page skeleton for home screen
 */
@Composable
fun HomeScreenSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(SpaceMd),
        verticalArrangement = Arrangement.spacedBy(SpaceMd)
    ) {
        // Streak widget skeleton
        SkeletonContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(CardRadius)
        ) {}
        
        // Section title
        SkeletonTitle(widthFraction = 0.4f)
        
        // Quest cards
        repeat(3) {
            SkeletonQuestCard()
        }
        
        // CTA button skeleton
        SkeletonContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {}
    }
}

/**
 * Achievement grid skeleton
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementGridSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 6
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpaceMd),
        verticalArrangement = Arrangement.spacedBy(SpaceMd),
        maxItemsInEachRow = 3
    ) {
        repeat(itemCount) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpaceSm)
            ) {
                SkeletonCircle(size = 72)
                SkeletonText(widthFraction = 0.8f)
            }
        }
    }
}

/**
 * Generic shimmer brush for custom usage
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val reduceMotion = LocalReduceMotion.current
    val transition = rememberInfiniteTransition(label = "shimmer_brush")
    
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (reduceMotion) 3000 else 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_brush_translate"
    )
    
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )
    
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200, 0f),
        end = Offset(translateAnim, 0f)
    )
}
