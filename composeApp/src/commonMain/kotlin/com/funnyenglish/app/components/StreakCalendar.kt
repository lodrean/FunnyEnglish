package com.funnyenglish.app.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.designsystem.theme.funnyColors
import androidx.compose.material3.MaterialTheme
import com.funnyenglish.shared.model.*

/**
 * Визуальный календарь streaks с недельным видом
 */
@Composable
fun StreakCalendarView(
    streakData: StreakData,
    onMilestoneClick: (Int) -> Unit,
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
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок с текущим streak
            StreakHeader(
                currentStreak = streakData.currentStreak,
                longestStreak = streakData.longestStreak,
                nextMilestone = streakData.nextMilestone
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Календарь дней
            WeekCalendar(
                calendar = streakData.weeklyCalendar,
                isAtRisk = streakData.isAtRisk
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Информация о freeze и recovery
            StreakActions(
                freezesAvailable = streakData.streakFreezesAvailable,
                recoveryAvailable = streakData.recoveryChallengeAvailable,
                isAtRisk = streakData.isAtRisk
            )
        }
    }
}

@Composable
private fun StreakHeader(
    currentStreak: Int,
    longestStreak: Int,
    nextMilestone: Int
) {
    val isNewRecord = currentStreak > 0 && currentStreak == longestStreak
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Иконка огня с анимацией
        AnimatedVisibility(
            visible = currentStreak > 0,
            enter = scaleIn(animationSpec = tween(500)) + fadeIn()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                PulsingFireIcon()
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Текущий streak
        Text(
            text = "$currentStreak",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Text(
            text = if (currentStreak == 1) "день подряд" else "дней подряд",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        // До следующего milestone
        if (currentStreak > 0) {
            val daysToMilestone = nextMilestone - currentStreak
            Text(
                text = "${daysToMilestone} дней до рекорда! 🎯",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // New record badge
        if (isNewRecord && currentStreak > 1) {
            Surface(
                modifier = Modifier.padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.funnyColors.success.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "🏆 Новый рекорд!",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.funnyColors.success,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun PulsingFireIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "fire")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Icon(
        imageVector = Icons.Default.LocalFireDepartment,
        contentDescription = "Streak",
        modifier = Modifier.scale(scale),
        tint = MaterialTheme.colorScheme.secondary
    )
}

@Composable
private fun WeekCalendar(
    calendar: List<DayStatus>,
    isAtRisk: Boolean
) {
    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    
    Column {
        // Заголовки дней недели
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Дни с иконками статуса
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            calendar.forEach { dayStatus ->
                DayCell(
                    status = dayStatus.status,
                    xpEarned = dayStatus.xpEarned
                )
            }
        }
        
        // Предупреждение о риске
        if (isAtRisk) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.funnyColors.warning.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.funnyColors.warning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Серия под угрозой! Занимайся сегодня 🔥",
                        color = MaterialTheme.funnyColors.warning,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    status: StreakDayStatus,
    xpEarned: Int
) {
    val backgroundColor = when (status) {
        StreakDayStatus.COMPLETED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        StreakDayStatus.TODAY_COMPLETED -> MaterialTheme.colorScheme.secondary
        StreakDayStatus.FREEZE_USED -> MaterialTheme.funnyColors.info.copy(alpha = 0.2f)
        StreakDayStatus.MISSED -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    val borderColor = when (status) {
        StreakDayStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        StreakDayStatus.TODAY_COMPLETED -> MaterialTheme.colorScheme.secondary
        StreakDayStatus.TODAY_PENDING -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        StreakDayStatus.FREEZE_USED -> MaterialTheme.funnyColors.info
        else -> Color.Transparent
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(backgroundColor, CircleShape)
            .border(
                width = if (status == StreakDayStatus.TODAY_PENDING) 2.dp else if (borderColor != Color.Transparent) 1.dp else 0.dp,
                color = borderColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            StreakDayStatus.COMPLETED -> {
                Text(
                    text = "🔥",
                    fontSize = 20.sp
                )
            }
            StreakDayStatus.TODAY_COMPLETED -> {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Today",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            StreakDayStatus.FREEZE_USED -> {
                Icon(
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = "Freeze used",
                    tint = MaterialTheme.funnyColors.info,
                    modifier = Modifier.size(20.dp)
                )
            }
            StreakDayStatus.MISSED -> {
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 20.sp
                )
            }
            else -> {
                // Empty or pending - show placeholder
                if (status == StreakDayStatus.TODAY_PENDING) {
                    Text(
                        text = "⭕",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakActions(
    freezesAvailable: Int,
    recoveryAvailable: Boolean,
    isAtRisk: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Freeze info
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.funnyColors.info.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = null,
                    tint = MaterialTheme.funnyColors.info,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Заморозок: $freezesAvailable",
                    fontSize = 14.sp,
                    color = MaterialTheme.funnyColors.info,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Recovery info
        if (recoveryAvailable) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.funnyColors.success.copy(alpha = 0.1f),
                modifier = Modifier.clickable { /* TODO: Open recovery */ }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Восстановить 🔥",
                        fontSize = 14.sp,
                        color = MaterialTheme.funnyColors.success,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Компактный виджет streak для главного экрана
 */
@Composable
fun StreakWidget(
    currentStreak: Int,
    isAtRisk: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isAtRisk) 
            MaterialTheme.funnyColors.warning.copy(alpha = 0.1f) 
        else 
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak",
                tint = if (isAtRisk) MaterialTheme.funnyColors.warning else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = "$currentStreak",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAtRisk) MaterialTheme.funnyColors.warning else MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = if (isAtRisk) "Под угрозой!" else "дней подряд",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
