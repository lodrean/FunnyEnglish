package com.sotospeak.designsystem.components.gamification

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.designsystem.accessibility.LocalReduceMotion
import com.sotospeak.designsystem.theme.funnyColors
import com.sotospeak.designsystem.tokens.SpaceSm
import kotlinx.coroutines.delay

/**
 * So to Speak XP Counter
 *
 * Features:
 * - Animated count-up (1000ms, CELEBRATION duration)
 * - Easing: EaseOutCubic or EaseOutBounce
 * - Confetti trigger for gains 100+ XP
 *
 * Color: MaterialTheme.funnyColors.xp (Gold family)
 */

@Composable
fun FunnyXPCounter(
    currentXp: Int,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    size: XPCounterSize = XPCounterSize.MEDIUM,
    animateChanges: Boolean = true
) {
    val reduceMotion = LocalReduceMotion.current
    val colors = MaterialTheme.funnyColors
    var displayedXp by remember { mutableIntStateOf(if (animateChanges) 0 else currentXp) }

    // Animate from 0 to current on first load or changes
    LaunchedEffect(currentXp) {
        if (!reduceMotion && animateChanges) {
            displayedXp = 0
            delay(100)
            displayedXp = currentXp
        } else {
            displayedXp = currentXp
        }
    }

    val animatedXp by animateIntAsState(
        targetValue = displayedXp,
        animationSpec = if (reduceMotion || !animateChanges) {
            tween(durationMillis = 0)
        } else {
            tween(durationMillis = 1000, easing = EaseOutCubic)
        },
        label = "xp_count"
    )

    val (textStyle, iconSize) = when (size) {
        XPCounterSize.SMALL -> Pair(MaterialTheme.typography.labelLarge, 16.dp)
        XPCounterSize.MEDIUM -> Pair(MaterialTheme.typography.titleLarge, 24.dp)
        XPCounterSize.LARGE -> Pair(MaterialTheme.typography.headlineMedium, 32.dp)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpaceSm)
    ) {
        if (showIcon) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .background(colors.xpContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "XP",
                    modifier = Modifier.size(iconSize * 0.6f),
                    tint = colors.xp
                )
            }
        }

        Text(
            text = "$animatedXp",
            style = textStyle,
            color = colors.xp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "XP",
            style = textStyle,
            color = colors.xp.copy(alpha = 0.7f)
        )
    }
}

enum class XPCounterSize {
    SMALL,
    MEDIUM,
    LARGE
}

/**
 * XP Gain animation with +X popup
 */
@Composable
fun FunnyXPGain(
    amount: Int,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    val reduceMotion = LocalReduceMotion.current
    val colors = MaterialTheme.funnyColors

    Box(
        modifier = modifier
            .background(colors.xp.copy(alpha = 0.9f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$amount",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }

    // Trigger confetti for large gains
    if (amount >= 100 && !reduceMotion) {
        // Confetti trigger would go here
    }
}

/**
 * XP Progress bar showing progress to next level
 */
@Composable
fun FunnyXPProgressBar(
    currentXpInLevel: Int,
    xpForNextLevel: Int,
    modifier: Modifier = Modifier,
    currentLevel: Int? = null
) {
    val colors = MaterialTheme.funnyColors
    val progress = if (xpForNextLevel > 0) {
        (currentXpInLevel.toFloat() / xpForNextLevel.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Level indicator
        currentLevel?.let { level ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(colors.xpContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.xp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Progress bar
        com.sotospeak.designsystem.components.feedback.FunnyLinearProgress(
            progress = progress,
            modifier = Modifier.weight(1f),
            color = colors.xp,
            showLabel = true,
            label = "$currentXpInLevel / $xpForNextLevel XP"
        )
    }
}
