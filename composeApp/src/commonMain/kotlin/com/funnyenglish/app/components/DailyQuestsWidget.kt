package com.funnyenglish.app.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funnyenglish.app.viewmodel.QuestsViewModel
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.designsystem.theme.funnyColors
import com.funnyenglish.shared.model.DailyQuest
import com.funnyenglish.shared.model.QuestDifficulty
import com.funnyenglish.shared.model.QuestType

/**
 * Виджет ежедневных квестов для главного экрана
 */

@Composable
fun DailyQuestsWidget(
    quests: List<DailyQuest>,
    onQuestClick: (String) -> Unit,
    onClaimReward: (String) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(CardRadius)
    ) {
        Column(
            modifier = Modifier.padding(SpaceMd)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Ежедневные задания",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onViewAll) {
                    Text("Все")
                }
            }
            
            Spacer(modifier = Modifier.height(SpaceSm))
            
            // Quest list (show first 3)
            quests.take(3).forEach { quest ->
                QuestItem(
                    quest = quest,
                    onClick = { onQuestClick(quest.id) },
                    onClaim = { onClaimReward(quest.id) }
                )
                Spacer(modifier = Modifier.height(SpaceSm))
            }
            
            // Progress summary
            val completedCount = quests.count { it.isCompleted }
            val totalCount = quests.size
            
            if (completedCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.funnyColors.successContainer
                    ),
                    shape = RoundedCornerShape(CardRadius / 2)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpaceSm),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅ Выполнено $completedCount из $totalCount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.funnyColors.success
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestItem(
    quest: DailyQuest,
    onClick: () -> Unit,
    onClaim: () -> Unit
) {
    val progress = if (quest.targetValue > 0) {
        (quest.currentValue.toFloat() / quest.targetValue).coerceIn(0f, 1f)
    } else 0f
    
    // Note: isClaimed is not in the shared model, so we assume all completed quests are claimable
    // until claimed via API. The API should handle duplicate claim attempts gracefully.
    val isClaimed = false // This would need to be tracked separately or in the backend
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = !quest.isCompleted),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isClaimed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                quest.isCompleted -> MaterialTheme.funnyColors.successContainer
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpaceSm)
        ) {
            // Icon
            Text(
                text = getQuestIcon(quest.type),
                style = MaterialTheme.typography.titleMedium
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isClaimed) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                // Progress bar
                if (!quest.isCompleted) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = getDifficultyColor(quest.difficulty),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else if (!isClaimed) {
                    // Ready to claim
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpaceXs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.funnyColors.success,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Готово к получению!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.funnyColors.success
                        )
                    }
                }
            }
            
            // Reward or claim button
            if (quest.isCompleted && !isClaimed) {
                FunnyButton(
                    text = "Забрать",
                    onClick = onClaim,
                    type = FunnyButtonType.PRIMARY,
                    size = FunnyButtonSize.SMALL
                )
            } else {
                // Reward preview
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "+${quest.reward.xp}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.funnyColors.xp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.funnyColors.xp
                        )
                    }
                    if (quest.reward.gems > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "💎",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = quest.reward.gems.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = GemTeal
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getQuestIcon(type: QuestType): String {
    return when (type) {
        QuestType.COMPLETE_LESSONS -> "📚"
        QuestType.EARN_XP -> "⭐"
        QuestType.PRACTICE_STREAK -> "🔥"
        QuestType.REVIEW_WORDS -> "📝"
        QuestType.PERFECT_SCORE -> "💯"
        QuestType.TRY_NEW_CATEGORY -> "🆕"
        QuestType.SHARE_PROGRESS -> "📢"
        QuestType.PRACTICE_PRONUNCIATION -> "🎤"
    }
}

@Composable
private fun getDifficultyColor(difficulty: QuestDifficulty): androidx.compose.ui.graphics.Color {
    return when (difficulty) {
        QuestDifficulty.EASY -> MaterialTheme.funnyColors.success
        QuestDifficulty.MEDIUM -> MaterialTheme.funnyColors.warning
        QuestDifficulty.HARD -> MaterialTheme.colorScheme.error
    }
}
