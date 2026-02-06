package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.app.accessibility.AccessibilityDescriptions
import com.funnyenglish.app.accessibility.AccessibilityUtils
import com.funnyenglish.app.components.*
import com.funnyenglish.app.theme.FunnyColors
import com.funnyenglish.app.viewmodel.HomeState
import com.funnyenglish.shared.model.Category
import com.funnyenglish.shared.model.StreakDayStatus
import com.funnyenglish.shared.model.TestListItem

@Composable
fun HomeScreen(
    state: HomeState,
    onLoadData: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onTestClick: (String) -> Unit,
    onViewAllCategories: () -> Unit,
    onProfileClick: () -> Unit,
    onContinueLearning: () -> Unit = {},
    onAdaptiveLessonClick: () -> Unit = {},
    onStreakClick: () -> Unit = {},
    onQuestsClick: () -> Unit = {},
    streakState: com.funnyenglish.app.viewmodel.StreakUiState? = null,
    questsState: com.funnyenglish.app.viewmodel.QuestsUiState? = null
) {
    LaunchedEffect(Unit) {
        onLoadData()
    }

    if (state.isLoading && state.userProfile == null) {
        LoadingIndicator()
        return
    }

    if (state.error != null && state.userProfile == null) {
        ErrorMessage(
            message = state.error,
            onRetry = onLoadData
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Navigation Bar
        item {
            TopNavBar(
                displayName = state.userProfile?.user?.displayName ?: "Друг",
                level = state.userProfile?.user?.level ?: 1,
                streak = state.userProfile?.user?.currentStreak ?: 0,
                points = state.userProfile?.user?.totalPoints ?: 0,
                onProfileClick = onProfileClick
            )
        }

        // Level Progress Hero Card
        item {
            LevelProgressCard(
                level = state.userProfile?.user?.level ?: 1,
                currentPoints = state.userProfile?.user?.totalPoints ?: 0,
                pointsToNextLevel = state.userProfile?.stats?.pointsToNextLevel ?: 100,
                onContinueLearning = onContinueLearning,
                onAdaptiveLessonClick = onAdaptiveLessonClick
            )
        }
        
        // Quick Actions Row
        item {
            QuickActionsRow(
                onAdaptiveLessonClick = onAdaptiveLessonClick,
                onContinueLearning = onContinueLearning
            )
        }
        
        // Gamification widgets row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streak Widget
                streakState?.let { streak ->
                    val weeklyProgress = streak.streakData?.weeklyCalendar?.map { 
                        it.status == StreakDayStatus.COMPLETED || it.status == StreakDayStatus.TODAY_COMPLETED
                    } ?: emptyList()
                    StreakWidget(
                        streak = streak.streakData?.currentStreak 
                            ?: state.userProfile?.user?.currentStreak ?: 0,
                        longestStreak = streak.streakData?.longestStreak ?: 0,
                        isAtRisk = streak.isStreakAtRisk,
                        weeklyProgress = weeklyProgress,
                        onClick = onStreakClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Daily Quests Widget
                questsState?.let { quests ->
                    DailyQuestsWidget(
                        quests = quests.dailyQuests.take(2),
                        onQuestClick = { /* TODO */ },
                        onClaimReward = { /* TODO */ },
                        onViewAll = onQuestsClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Categories section
        item {
            SectionHeader(
                title = "Категории",
                onViewAll = onViewAllCategories
            )
        }

        item {
            CategoriesRow(
                categories = state.categories,
                onCategoryClick = onCategoryClick
            )
        }

        // Recent tests section
        if (state.recentTests.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    title = "Недавние тесты",
                    onViewAll = null
                )
            }

            items(
                items = state.recentTests.take(3),
                key = { it.id },
                contentType = { "recentTest" }
            ) { test ->
                RecentTestCard(
                    test = test,
                    onClick = { onTestClick(test.id) }
                )
            }
        }
    }
}

@Composable
private fun TopNavBar(
    displayName: String,
    level: Int,
    streak: Int,
    points: Int,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar and greeting
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(
                    onClick = onProfileClick,
                    onClickLabel = AccessibilityDescriptions.NAVIGATE_TO_PROFILE
                )
                .semantics {
                    contentDescription = "Profile of $displayName, Level $level"
                }
        ) {
            Box {
                // Make avatar accessible
                Modifier.semantics {
                    contentDescription = AccessibilityDescriptions.USER_AVATAR
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FunnyColors.Primary.copy(alpha = 0.2f))
                        .border(2.dp, FunnyColors.Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.firstOrNull()?.toString() ?: "?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FunnyColors.Primary
                    )
                }
                // Level badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .background(FunnyColors.Secondary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "LVL $level",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White  // Keep white for level badge on colored background
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Привет!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$displayName!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Stats badges
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak
                Text(
                    text = "🔥",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = streak.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Points
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = FunnyColors.StarFilled,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = points.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LevelProgressCard(
    level: Int,
    currentPoints: Int,
    pointsToNextLevel: Int,
    onContinueLearning: () -> Unit,
    onAdaptiveLessonClick: () -> Unit
) {
    val totalForLevel = currentPoints + pointsToNextLevel
    val progress = if (totalForLevel > 0) currentPoints.toFloat() / totalForLevel else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular progress
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 8.dp
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = FunnyColors.Primary,
                    strokeWidth = 8.dp
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FunnyColors.Primary
                    )
                    Text(
                        text = "ПРОГРЕСС",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = FunnyColors.TextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Прогресс уровня $level",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Ещё $pointsToNextLevel XP до уровня ${level + 1}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Primary action - Continue Learning
            Button(
                onClick = onContinueLearning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FunnyColors.Primary
                )
            ) {
                Text(
                    text = "Продолжить обучение",
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Secondary action - Quick Adaptive Lesson
            OutlinedButton(
                onClick = onAdaptiveLessonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = FunnyColors.Secondary
                )
            ) {
                Text(
                    text = "⚡ Быстрый урок (5 мин)",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onAdaptiveLessonClick: () -> Unit,
    onContinueLearning: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Lesson Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .clickable(onClick = onAdaptiveLessonClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = FunnyColors.Secondary.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚡",
                    fontSize = 24.sp
                )
                Text(
                    text = "Быстрый урок",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = FunnyColors.Secondary
                )
                Text(
                    text = "5 минут",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Continue Learning Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .clickable(onClick = onContinueLearning),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = FunnyColors.Primary.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📚",
                    fontSize = 24.sp
                )
                Text(
                    text = "Продолжить",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = FunnyColors.Primary
                )
                Text(
                    text = "С последнего теста",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onViewAll: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (onViewAll != null) {
            TextButton(onClick = onViewAll) {
                Text(
                    text = "Все",
                    fontWeight = FontWeight.Bold,
                    color = FunnyColors.Primary
                )
            }
        }
    }
}

@Composable
private fun CategoriesRow(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = categories,
            key = { it.id },
            contentType = { "category" }
        ) { category ->
            CategoryChip(
                category = category,
                onClick = { onCategoryClick(category.id) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    onClick: () -> Unit
) {
    val (bgColor, iconColor, emoji) = when {
        category.name.contains("Животные", ignoreCase = true) -> Triple(
            FunnyColors.Purple.copy(alpha = 0.1f),
            FunnyColors.Purple,
            "🐾"
        )
        category.name.contains("Цвета", ignoreCase = true) -> Triple(
            FunnyColors.Secondary.copy(alpha = 0.1f),
            FunnyColors.Secondary,
            "🎨"
        )
        category.name.contains("Числа", ignoreCase = true) -> Triple(
            FunnyColors.Primary.copy(alpha = 0.1f),
            FunnyColors.Primary,
            "🔢"
        )
        category.name.contains("Еда", ignoreCase = true) -> Triple(
            FunnyColors.Orange.copy(alpha = 0.1f),
            FunnyColors.Orange,
            "🍎"
        )
        category.name.contains("Семья", ignoreCase = true) -> Triple(
            FunnyColors.Pink.copy(alpha = 0.1f),
            FunnyColors.Pink,
            "👨‍👩‍👧"
        )
        category.name.contains("Одежда", ignoreCase = true) -> Triple(
            FunnyColors.Indigo.copy(alpha = 0.1f),
            FunnyColors.Indigo,
            "👕"
        )
        else -> Triple(
            FunnyColors.Primary.copy(alpha = 0.1f),
            FunnyColors.Primary,
            "📚"
        )
    }

    val progress = if (category.testsCount > 0) {
        category.completedCount.toFloat() / category.testsCount
    } else 0f

    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(iconColor.copy(alpha = 0.2f), iconColor.copy(alpha = 0.2f)))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = iconColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(progress * 100).toInt()}% пройдено",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun RecentTestCard(
    test: TestListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = test.title.firstOrNull()?.toString() ?: "?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FunnyColors.Primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = test.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DifficultyBadge(difficulty = test.difficulty)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${test.questionsCount} вопросов",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stars or Play button
            test.userProgress?.let { progress ->
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    StarsDisplay(stars = progress.stars, size = 16)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${progress.percentage}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FunnyColors.Success
                    )
                }
            } ?: run {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = FunnyColors.Primary
                ) {
                    Text(
                        text = "Играть",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun TestListItemCard(
    test: TestListItem,
    onClick: () -> Unit
) {
    RecentTestCard(test = test, onClick = onClick)
}
