package com.funnyenglish.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.designsystem.theme.funnyColors

/**
 * Компактный виджет streak для главного экрана
 */

@Composable
fun StreakWidget(
    streak: Int,
    longestStreak: Int = 0,
    isAtRisk: Boolean = false,
    weeklyProgress: List<Boolean> = emptyList(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isAtRisk)
                MaterialTheme.funnyColors.warningContainer
            else
                MaterialTheme.funnyColors.streakContainer
        ),
        shape = RoundedCornerShape(CardRadius)
    ) {
        Column(
            modifier = Modifier.padding(SpaceMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak icon and count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpaceSm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (isAtRisk) MaterialTheme.funnyColors.warning.copy(alpha = 0.2f)
                                else MaterialTheme.funnyColors.streak.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = if (isAtRisk) MaterialTheme.funnyColors.warning else MaterialTheme.funnyColors.streak,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = "$streak",
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (isAtRisk) MaterialTheme.funnyColors.warning else MaterialTheme.funnyColors.streak,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (streak == 1) "день подряд" else "дней подряд",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // At risk warning or milestone
                if (isAtRisk) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpaceXs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "At risk",
                            tint = MaterialTheme.funnyColors.warning,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Под угрозой!",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.funnyColors.warning,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (longestStreak > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Рекорд",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$longestStreak",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Weekly progress dots
            if (weeklyProgress.isNotEmpty()) {
                Spacer(modifier = Modifier.height(SpaceSm))
                WeeklyProgressDots(
                    progress = weeklyProgress,
                    isAtRisk = isAtRisk
                )
            }
        }
    }
}

@Composable
private fun WeeklyProgressDots(
    progress: List<Boolean>,
    isAtRisk: Boolean
) {
    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, day ->
            val isCompleted = progress.getOrNull(index) ?: false
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpaceXs)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> if (isAtRisk) MaterialTheme.funnyColors.warning else MaterialTheme.funnyColors.streak
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                )
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) 
                        (if (isAtRisk) MaterialTheme.funnyColors.warning else MaterialTheme.funnyColors.streak)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
