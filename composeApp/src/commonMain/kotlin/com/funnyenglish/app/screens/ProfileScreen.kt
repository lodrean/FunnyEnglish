package com.funnyenglish.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.app.components.ErrorMessage
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.app.components.ProgressBar
import com.funnyenglish.app.components.StarsDisplay
import com.funnyenglish.app.theme.FunnyColors
import com.funnyenglish.app.viewmodel.ProfileState
import com.funnyenglish.shared.model.CategoryProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onLoad: () -> Unit,
    onBack: () -> Unit,
    onAchievementsClick: () -> Unit
) {
    LaunchedEffect(Unit) { onLoad() }

    if (state.isLoading && state.userProfile == null) {
        LoadingIndicator()
        return
    }

    if (state.error != null && state.userProfile == null) {
        ErrorMessage(
            message = state.error,
            onRetry = onLoad
        )
        return
    }

    val profile = state.userProfile ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FunnyColors.SurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = profile.user.displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.user.email,
                            fontSize = 12.sp,
                            color = FunnyColors.TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LevelBadgeText(profile.user.level)
                            PointsBadgeText(profile.user.totalPoints)
                            StreakBadgeText(profile.user.currentStreak)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Статистика",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(value = profile.stats.testsCompleted.toString(), label = "Тестов")
                            StatItem(value = profile.stats.totalStars.toString(), label = "Звёзд")
                            StatItem(value = profile.stats.perfectScores.toString(), label = "Идеальных")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "До следующего уровня: ${profile.stats.pointsToNextLevel}",
                            fontSize = 12.sp,
                            color = FunnyColors.TextSecondary
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Прогресс по категориям",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val categories = state.progressSummary?.categoriesProgress ?: emptyList()
                        if (categories.isEmpty()) {
                            Text(
                                text = "Нет данных",
                                fontSize = 12.sp,
                                color = FunnyColors.TextSecondary
                            )
                        } else {
                            CategoryProgressList(categories = categories)
                        }
                    }
                }
            }

            if (profile.achievements.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Достижения",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            profile.achievements.take(5).forEach { achievement ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = FunnyColors.StarFilled
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(achievement.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            achievement.description,
                                            fontSize = 12.sp,
                                            color = FunnyColors.TextSecondary
                                        )
                                    }
                                }
                            }
                            if (profile.achievements.size > 5) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Показать все",
                                    color = FunnyColors.Primary,
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(top = 4.dp)
                                        .clickable { onAchievementsClick() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryProgressList(categories: List<CategoryProgress>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.forEach { category ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = category.categoryName, fontWeight = FontWeight.Medium)
                    Text(
                        text = "${category.completedCount}/${category.testsCount}",
                        fontSize = 12.sp,
                        color = FunnyColors.TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                ProgressBar(
                    progress = if (category.testsCount > 0) {
                        category.completedCount.toFloat() / category.testsCount
                    } else {
                        0f
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = FunnyColors.Primary,
                    trackColor = FunnyColors.SurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                StarsDisplay(
                    stars = category.totalStars,
                    maxStars = category.maxStars,
                    size = 12
                )
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = FunnyColors.Primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = FunnyColors.TextSecondary
        )
    }
}

@Composable
private fun LevelBadgeText(level: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Уровень",
            fontSize = 12.sp,
            color = FunnyColors.TextSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = level.toString(),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PointsBadgeText(points: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Очки",
            fontSize = 12.sp,
            color = FunnyColors.TextSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = points.toString(),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StreakBadgeText(streak: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Серия",
            fontSize = 12.sp,
            color = FunnyColors.TextSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = streak.toString(),
            fontWeight = FontWeight.Bold
        )
    }
}
