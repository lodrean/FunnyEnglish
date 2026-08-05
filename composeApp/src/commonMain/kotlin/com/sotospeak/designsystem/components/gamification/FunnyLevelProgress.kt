package com.sotospeak.designsystem.components.gamification

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.designsystem.accessibility.LocalReduceMotion
import com.sotospeak.designsystem.components.cards.FunnyCard
import com.sotospeak.designsystem.components.feedback.FunnyLinearProgress
import com.sotospeak.designsystem.theme.funnyColors
import com.sotospeak.designsystem.tokens.SpaceSm

/**
 * So to Speak Level Progress
 *
 * Features:
 * - Current level display (large number)
 * - XP progress bar toward next level
 * - Next level threshold
 *
 * Color: MaterialTheme.funnyColors.achievement (Purple accent)
 */

@Composable
fun FunnyLevelProgress(
    currentLevel: Int,
    currentXp: Int,
    xpForNextLevel: Int,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalReduceMotion.current
    val colors = MaterialTheme.funnyColors
    val xpInLevel = currentXp - xpForCurrentLevel(currentLevel)
    val xpNeeded = xpForNextLevel - xpForCurrentLevel(currentLevel)
    val progress = if (xpNeeded > 0) {
        (xpInLevel.toFloat() / xpNeeded.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (reduceMotion) {
            androidx.compose.animation.core.snap()
        } else {
            tween(durationMillis = 500)
        },
        label = "level_progress"
    )

    FunnyCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current level
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(colors.achievementContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentLevel.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.achievement,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Уровень $currentLevel",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = getLevelTitle(currentLevel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Next level indicator
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Следующий",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = (currentLevel + 1).toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar
        FunnyLinearProgress(
            progress = animatedProgress,
            color = colors.achievement,
            showLabel = true,
            label = "$xpInLevel / $xpNeeded XP"
        )
    }
}

/**
 * Compact level indicator for navigation
 */
@Composable
fun FunnyLevelIndicator(
    level: Int,
    modifier: Modifier = Modifier,
    showXp: Boolean = false,
    currentXp: Int = 0,
    xpForNextLevel: Int = 0
) {
    val colors = MaterialTheme.funnyColors

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(colors.achievementContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = colors.achievement,
                fontWeight = FontWeight.Bold
            )
        }

        if (showXp && xpForNextLevel > 0) {
            val xpInLevel = currentXp - xpForCurrentLevel(level)
            val xpNeeded = xpForNextLevel - xpForCurrentLevel(level)
            Text(
                text = "$xpInLevel / $xpNeeded XP",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Get XP required for a specific level
 */
fun xpForLevel(level: Int): Int {
    return when (level) {
        1 -> 0
        2 -> 100
        3 -> 250
        4 -> 450
        5 -> 700
        6 -> 1000
        7 -> 1400
        8 -> 1900
        9 -> 2500
        10 -> 3200
        else -> 3200 + (level - 10) * 1000
    }
}

fun xpForCurrentLevel(level: Int): Int {
    return xpForLevel(level)
}

fun calculateLevel(totalXp: Int): Int {
    var level = 1
    while (level < 50 && totalXp >= xpForLevel(level + 1)) {
        level++
    }
    return level
}

/**
 * Get title for level
 */
fun getLevelTitle(level: Int): String {
    return when (level) {
        1 -> "Новичок"
        2 -> "Ученик"
        3 -> "Стажёр"
        4 -> "Практикант"
        5 -> "Знаток"
        6 -> "Эксперт"
        7 -> "Мастер"
        8 -> "Гуру"
        9 -> "Профессор"
        10 -> "Легенда"
        else -> "Магистр $level"
    }
}
