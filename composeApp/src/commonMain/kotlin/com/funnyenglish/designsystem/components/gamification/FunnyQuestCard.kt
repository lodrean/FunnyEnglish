package com.funnyenglish.designsystem.components.gamification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.components.cards.FunnyCard
import com.funnyenglish.designsystem.components.cards.FunnyCardType
import com.funnyenglish.designsystem.components.feedback.FunnyLinearProgress
import com.funnyenglish.designsystem.theme.funnyColors
import com.funnyenglish.designsystem.tokens.CardPadding
import com.funnyenglish.designsystem.tokens.SpaceSm

/**
 * FunnyEnglish Quest Card
 *
 * Features:
 * - Title + description
 * - Progress indicator
 * - Reward preview (XP + gems)
 * - Claim action when complete
 *
 * Color: MaterialTheme.colorScheme.tertiary (Orange family)
 */

@Composable
fun FunnyQuestCard(
    title: String,
    description: String,
    currentValue: Int,
    targetValue: Int,
    xpReward: Int,
    modifier: Modifier = Modifier,
    gemReward: Int? = null,
    isCompleted: Boolean = false,
    isClaimed: Boolean = false,
    onClaim: () -> Unit = {}
) {
    val progress = (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
    val colors = MaterialTheme.funnyColors
    val tertiary = MaterialTheme.colorScheme.tertiary

    val cardType = when {
        isClaimed -> FunnyCardType.FILLED
        isCompleted -> FunnyCardType.ELEVATED
        else -> FunnyCardType.OUTLINED
    }

    FunnyCard(
        modifier = modifier.fillMaxWidth(),
        type = cardType
    ) {
        Column {
            // Header: Icon + Title + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpaceSm)
                ) {
                    // Quest icon
                    Icon(
                        imageVector = if (isClaimed)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (isClaimed)
                            MaterialTheme.colorScheme.primary
                        else
                            tertiary,
                        modifier = Modifier.size(24.dp)
                    )

                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (isClaimed)
                            TextDecoration.LineThrough
                        else
                            TextDecoration.None,
                        color = if (isClaimed)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                // Status badge
                AnimatedVisibility(
                    visible = isCompleted && !isClaimed,
                    enter = fadeIn() + scaleIn()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Description
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            FunnyLinearProgress(
                progress = progress,
                color = tertiary,
                showLabel = true,
                label = "$currentValue / $targetValue"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Rewards + Claim button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rewards
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // XP reward
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = colors.xp,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "+$xpReward",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.xp
                        )
                    }

                    // Gem reward (if any)
                    gemReward?.let { gems ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = colors.gem,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "+$gems",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.gem
                            )
                        }
                    }
                }

                // Claim button
                AnimatedVisibility(
                    visible = isCompleted && !isClaimed,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    FunnyButton(
                        text = "Забрать",
                        onClick = onClaim,
                        type = FunnyButtonType.TERTIARY,
                        size = FunnyButtonSize.SMALL
                    )
                }
            }
        }
    }
}

/**
 * Compact quest item for lists
 */
@Composable
fun FunnyQuestItem(
    title: String,
    currentValue: Int,
    targetValue: Int,
    modifier: Modifier = Modifier,
    xpReward: Int = 0,
    isCompleted: Boolean = false
) {
    val progress = (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
    val colors = MaterialTheme.funnyColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox indicator
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (isCompleted)
                        TextDecoration.LineThrough
                    else
                        TextDecoration.None
                )

                // Progress
                if (!isCompleted) {
                    Text(
                        text = "$currentValue / $targetValue",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // XP reward
        if (xpReward > 0) {
            Text(
                text = "+$xpReward XP",
                style = MaterialTheme.typography.labelLarge,
                color = colors.xp
            )
        }
    }
}
