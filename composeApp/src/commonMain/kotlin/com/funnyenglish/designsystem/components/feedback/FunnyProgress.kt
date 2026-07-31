package com.funnyenglish.designsystem.components.feedback

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion
import com.funnyenglish.designsystem.tokens.CardRadius
import com.funnyenglish.designsystem.tokens.SpaceSm
import com.funnyenglish.designsystem.tokens.SpaceXs

/**
 * FunnyEnglish Progress Indicators
 * 
 * Types: LINEAR, CIRCULAR, SEGMENTED
 * Animation: Smooth interpolation (300ms, NORMAL duration)
 * Must-have for perceived performance
 */

enum class FunnyProgressType {
    LINEAR,    // Horizontal bar
    CIRCULAR,  // Rotating circle
    SEGMENTED  // Discrete steps
}

enum class FunnyProgressSize {
    SMALL,
    MEDIUM,
    LARGE
}

/**
 * Linear progress indicator
 */
@Composable
fun FunnyLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showLabel: Boolean = false,
    label: String? = null
) {
    val reduceMotion = LocalReduceMotion.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (reduceMotion) {
            tween(durationMillis = 0)
        } else {
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        },
        label = "linear_progress"
    )
    
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )
        
        if (showLabel) {
            Text(
                text = label ?: "${(animatedProgress * 100).toInt()}%",
                modifier = Modifier
                    .padding(top = SpaceXs)
                    .fillMaxWidth(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Indeterminate linear progress (loading state)
 */
@Composable
fun FunnyLinearProgressIndeterminate(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    LinearProgressIndicator(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = color,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round
    )
}

/**
 * Circular progress indicator
 */
@Composable
fun FunnyCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: FunnyProgressSize = FunnyProgressSize.MEDIUM,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Float = 4f
) {
    val reduceMotion = LocalReduceMotion.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (reduceMotion) {
            tween(durationMillis = 0)
        } else {
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        },
        label = "circular_progress"
    )
    
    val sizeDp = when (size) {
        FunnyProgressSize.SMALL -> 24.dp
        FunnyProgressSize.MEDIUM -> 40.dp
        FunnyProgressSize.LARGE -> 64.dp
    }
    
    val stroke = when (size) {
        FunnyProgressSize.SMALL -> 2f
        FunnyProgressSize.MEDIUM -> 4f
        FunnyProgressSize.LARGE -> 6f
    }
    
    CircularProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier.size(sizeDp),
        color = color,
        trackColor = trackColor,
        strokeWidth = stroke.dp,
        strokeCap = StrokeCap.Round
    )
}

/**
 * Indeterminate circular progress
 */
@Composable
fun FunnyCircularProgressIndeterminate(
    modifier: Modifier = Modifier,
    size: FunnyProgressSize = FunnyProgressSize.MEDIUM,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val sizeDp = when (size) {
        FunnyProgressSize.SMALL -> 24.dp
        FunnyProgressSize.MEDIUM -> 40.dp
        FunnyProgressSize.LARGE -> 64.dp
    }
    
    val stroke = when (size) {
        FunnyProgressSize.SMALL -> 2.dp
        FunnyProgressSize.MEDIUM -> 4.dp
        FunnyProgressSize.LARGE -> 6.dp
    }
    
    CircularProgressIndicator(
        modifier = modifier.size(sizeDp),
        color = color,
        trackColor = trackColor,
        strokeWidth = stroke,
        strokeCap = StrokeCap.Round
    )
}

/**
 * Segmented progress indicator (for multi-step processes)
 */
@Composable
fun FunnySegmentedProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    incompleteColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val reduceMotion = LocalReduceMotion.current
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(incompleteColor)
    ) {
        val progress = (currentStep.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = if (reduceMotion) {
                tween(durationMillis = 0)
            } else {
                tween(durationMillis = 300, easing = FastOutSlowInEasing)
            },
            label = "segmented_progress"
        )
        
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(color)
        )
    }
}

/**
 * Loading skeleton with shimmer effect
 * Priority 5 (Must-have)
 */
@Composable
fun FunnyLoadingSkeleton(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    // Shimmer effect implementation would go here
    // For now using a simple placeholder
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CardRadius))
            .background(color)
    )
}
