package com.funnyenglish.app.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.designsystem.theme.funnyColors
import androidx.compose.material3.MaterialTheme
import com.funnyenglish.shared.model.*
// Date/time handling

/**
 * Панель ежедневных заданий
 */
@Composable
fun DailyQuestsPanel(
    quests: List<DailyQuest>,
    onQuestComplete: (DailyQuest) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Заголовок
            QuestsHeader(
                completedCount = quests.count { it.isCompleted },
                totalCount = quests.size
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Список квестов
            quests.forEachIndexed { index, quest ->
                QuestItem(
                    quest = quest,
                    onComplete = { onQuestComplete(quest) },
                    isLast = index == quests.size - 1
                )
            }
        }
    }
}

@Composable
private fun QuestsHeader(
    completedCount: Int,
    totalCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Ежедневные задания",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Выполняй задания, получай награды!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        // Прогресс кругом
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { completedCount.toFloat() / totalCount },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.funnyColors.success,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round
            )
            
            Text(
                text = "$completedCount/$totalCount",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuestItem(
    quest: DailyQuest,
    onComplete: () -> Unit,
    isLast: Boolean
) {
    val progress = quest.currentValue.toFloat() / quest.targetValue
    
    AnimatedVisibility(
        visible = !quest.isCompleted,
        exit = shrinkVertically() + fadeOut()
    ) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = when (quest.difficulty) {
                    QuestDifficulty.EASY -> MaterialTheme.funnyColors.success.copy(alpha = 0.05f)
                    QuestDifficulty.MEDIUM -> MaterialTheme.funnyColors.warning.copy(alpha = 0.05f)
                    QuestDifficulty.HARD -> MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Иконка типа квеста
                    QuestTypeIcon(type = quest.type)
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Контент
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = quest.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Бейдж сложности
                            DifficultyBadge(difficulty = quest.difficulty)
                        }
                        
                        Text(
                            text = quest.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Прогресс бар
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when (quest.difficulty) {
                                QuestDifficulty.EASY -> MaterialTheme.funnyColors.success
                                QuestDifficulty.MEDIUM -> MaterialTheme.funnyColors.warning
                                QuestDifficulty.HARD -> MaterialTheme.colorScheme.error
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${quest.currentValue}/${quest.targetValue}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Награда
                    QuestRewardBadge(reward = quest.reward)
                }
            }
            
            if (!isLast) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
    
    // Показываем completed состояние компактно
    if (quest.isCompleted) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.funnyColors.success.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.funnyColors.success,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = quest.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.funnyColors.success,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "+${quest.reward.xp} XP",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.funnyColors.success
                )
            }
        }
    }
}

@Composable
private fun QuestTypeIcon(type: QuestType) {
    val (icon, backgroundColor) = when (type) {
        QuestType.COMPLETE_LESSONS -> Icons.Default.MenuBook to MaterialTheme.colorScheme.primary
        QuestType.EARN_XP -> Icons.Default.Star to MaterialTheme.funnyColors.warning
        QuestType.PRACTICE_STREAK -> Icons.Default.LocalFireDepartment to MaterialTheme.colorScheme.secondary
        QuestType.REVIEW_WORDS -> Icons.Default.Refresh to MaterialTheme.funnyColors.info
        QuestType.PERFECT_SCORE -> Icons.Default.EmojiEvents to MaterialTheme.colorScheme.primary
        QuestType.TRY_NEW_CATEGORY -> Icons.Default.Explore to MaterialTheme.colorScheme.tertiary
        QuestType.SHARE_PROGRESS -> Icons.Default.Share to MaterialTheme.funnyColors.success
        QuestType.PRACTICE_PRONUNCIATION -> Icons.Default.Mic to MaterialTheme.colorScheme.secondary
    }
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(backgroundColor.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = backgroundColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun DifficultyBadge(difficulty: QuestDifficulty) {
    val (text, color) = when (difficulty) {
        QuestDifficulty.EASY -> "Легко" to MaterialTheme.funnyColors.success
        QuestDifficulty.MEDIUM -> "Средне" to MaterialTheme.funnyColors.warning
        QuestDifficulty.HARD -> "Сложно" to MaterialTheme.colorScheme.error
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun QuestRewardBadge(reward: QuestReward) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.funnyColors.warning.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💎",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${reward.gems}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.funnyColors.warning
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "+${reward.xp} XP",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Компактный виджет квестов для главного экрана
 */
@Composable
fun CompactQuestsWidget(
    quests: List<DailyQuest>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = quests.count { it.isCompleted }
    val totalCount = quests.size
    
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Task,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Ежедневные задания",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = { completedCount.toFloat() / totalCount },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.funnyColors.success,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "$completedCount/$totalCount",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
