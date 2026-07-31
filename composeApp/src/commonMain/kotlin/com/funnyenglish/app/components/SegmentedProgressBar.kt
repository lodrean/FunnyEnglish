package com.funnyenglish.app.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion
import com.funnyenglish.designsystem.tokens.SpaceMd
import com.funnyenglish.designsystem.tokens.SpaceSm
import com.funnyenglish.designsystem.tokens.SpaceXs

/**
 * Сегментированный прогресс-бар для адаптивных уроков
 */

@Composable
fun SegmentedProgressBar(
    totalSegments: Int,
    currentSegmentIndex: Int,
    segmentProgress: Float,
    overallProgress: Float,
    segmentLabels: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true
) {
    val reduceMotion = LocalReduceMotion.current
    
    val animatedSegmentProgress by animateFloatAsState(
        targetValue = segmentProgress.coerceIn(0f, 1f),
        animationSpec = if (reduceMotion) snap() else tween(300, easing = FastOutSlowInEasing),
        label = "segment_progress"
    )
    
    val animatedOverallProgress by animateFloatAsState(
        targetValue = overallProgress.coerceIn(0f, 1f),
        animationSpec = if (reduceMotion) snap() else tween(500, easing = FastOutSlowInEasing),
        label = "overall_progress"
    )
    
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedOverallProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        
        Spacer(modifier = Modifier.height(SpaceSm))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSegments) { index ->
                val isCompleted = index < currentSegmentIndex
                val isCurrent = index == currentSegmentIndex
                
                SegmentIndicator(
                    index = index,
                    label = segmentLabels.getOrNull(index) ?: "${index + 1}",
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    progress = if (isCurrent) animatedSegmentProgress else if (isCompleted) 1f else 0f,
                    modifier = Modifier.weight(1f)
                )
                
                if (index < totalSegments - 1) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(2.dp)
                            .background(
                                if (isCompleted) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }
        
        if (showPercentage) {
            Spacer(modifier = Modifier.height(SpaceXs))
            Text(
                text = "${(animatedOverallProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SegmentIndicator(
    index: Int,
    label: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalReduceMotion.current
    
    val infiniteTransition = rememberInfiniteTransition(label = "segment_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCurrent && !reduceMotion) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (isCurrent) 28.dp else 24.dp)
                .scale(if (isCurrent) pulseScale else 1f)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            } else if (isCurrent && progress > 0) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        
        Spacer(modifier = Modifier.height(SpaceXs))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = when {
                isCompleted || isCurrent -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun CompactSegmentedProgress(
    currentSegment: Int,
    totalSegments: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSegments) { index ->
            val isCompleted = index < currentSegment
            val isCurrent = index == currentSegment
            
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            )
        }
    }
}

@Composable
fun LessonTimer(
    remainingSeconds: Int,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val secondsStr = if (seconds < 10) "0$seconds" else "$seconds"
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${minutes}:${secondsStr}",
            style = MaterialTheme.typography.labelLarge,
            color = if (isWarning) MaterialTheme.colorScheme.error
                   else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
