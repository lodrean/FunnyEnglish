package com.funnyenglish.designsystem.components.gamification

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion
import com.funnyenglish.designsystem.components.cards.FunnyCard
import com.funnyenglish.designsystem.components.cards.FunnyCardElevation
import com.funnyenglish.designsystem.components.cards.FunnyCardType
import com.funnyenglish.designsystem.theme.funnyColors
import com.funnyenglish.designsystem.tokens.CardPadding
import com.funnyenglish.designsystem.tokens.CardShape

/**
 * FunnyEnglish Streak Widget
 *
 * Features:
 * - Fire icon with streak count
 * - At-risk state (color shift + warning)
 * - Flame pulse animation (Priority 3, Nice)
 *
 * Color: MaterialTheme.funnyColors.streak (Fire Orange family)
 */

@Composable
fun FunnyStreakWidget(
    streak: Int,
    modifier: Modifier = Modifier,
    isAtRisk: Boolean = false,
    longestStreak: Int? = null,
    onClick: (() -> Unit)? = null
) {
    val reduceMotion = LocalReduceMotion.current
    val colors = MaterialTheme.funnyColors

    // Flame pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (!reduceMotion) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    val flameColor = if (isAtRisk) colors.warning else colors.streak

    FunnyCard(
        modifier = modifier.fillMaxWidth(),
        type = if (isAtRisk) FunnyCardType.FILLED else FunnyCardType.ELEVATED,
        elevation = FunnyCardElevation.FEATURED,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Icon + Streak count
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fire icon with pulse animation
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(if (!reduceMotion) scale else 1f)
                        .background(flameColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        modifier = Modifier.size(32.dp),
                        tint = flameColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = streak.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = flameColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (streak == 1) "день"
                        else if (streak in 2..4) "дня"
                        else "дней",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right side: At-risk warning or Longest streak
            if (isAtRisk) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "At risk",
                        tint = colors.warning,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Серия под угрозой!",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.warning
                    )
                }
            } else if (longestStreak != null && longestStreak > streak) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Рекорд",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$longestStreak",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // At-risk message
        if (isAtRisk) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.warningContainer, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Потренируйся сегодня, чтобы сохранить серию! 🔥",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.warning
                )
            }
        }
    }
}

/**
 * Compact streak indicator for navigation/rail
 */
@Composable
fun FunnyStreakIndicator(
    streak: Int,
    modifier: Modifier = Modifier,
    isAtRisk: Boolean = false
) {
    val colors = MaterialTheme.funnyColors
    val flameColor = if (isAtRisk) colors.warning else colors.streak

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = "Streak",
            modifier = Modifier.size(20.dp),
            tint = flameColor
        )
        Text(
            text = streak.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = flameColor,
            fontWeight = FontWeight.Bold
        )
    }
}
