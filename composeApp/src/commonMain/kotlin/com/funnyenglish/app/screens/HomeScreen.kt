package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funnyenglish.app.components.ErrorMessage
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.app.components.DifficultyBadge
import com.funnyenglish.app.components.StarsDisplay
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.components.cards.FunnyCard
import com.funnyenglish.designsystem.components.cards.FunnyCardElevation
import com.funnyenglish.designsystem.components.cards.FunnyCardType
import com.funnyenglish.designsystem.components.gamification.FunnyLevelProgress
import com.funnyenglish.designsystem.components.gamification.FunnyStreakWidget
import com.funnyenglish.designsystem.components.gamification.FunnyXPCounter
import com.funnyenglish.designsystem.components.gamification.XPCounterSize
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.designsystem.theme.funnyColors
import com.funnyenglish.app.viewmodel.HomeState
import com.funnyenglish.shared.model.Category
import com.funnyenglish.shared.model.StreakDayStatus
import com.funnyenglish.shared.model.TestListItem

@Composable
fun HomeScreen(
    state: HomeState,
    isGuest: Boolean = false,
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
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 100.dp, top = SpaceMd)
    ) {
        // Top Navigation Bar
        item {
            TopNavBar(
                displayName = if (isGuest) "Гость" else (state.userProfile?.user?.displayName ?: "Друг"),
                level = state.userProfile?.user?.level ?: 1,
                streak = if (isGuest) 0 else (state.userProfile?.user?.currentStreak ?: 0),
                points = if (isGuest) 0 else (state.userProfile?.user?.totalPoints ?: 0),
                onProfileClick = onProfileClick
            )
        }

        // Level Progress Hero Card
        item {
            LevelProgressHero(
                level = state.userProfile?.user?.level ?: 1,
                currentPoints = state.userProfile?.user?.totalPoints ?: 0,
                pointsToNextLevel = state.userProfile?.stats?.pointsToNextLevel ?: 100,
                onContinueLearning = onContinueLearning,
                onAdaptiveLessonClick = onAdaptiveLessonClick
            )
        }
        
        // Gamification widgets row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpaceMd),
                horizontalArrangement = Arrangement.spacedBy(SpaceMd)
            ) {
                // Streak Widget
                streakState?.let { streak ->
                    val weeklyProgress = streak.streakData?.weeklyCalendar?.map { 
                        it.status == StreakDayStatus.COMPLETED || it.status == StreakDayStatus.TODAY_COMPLETED
                    } ?: emptyList()
                    
                    FunnyStreakWidget(
                        streak = streak.streakData?.currentStreak 
                            ?: state.userProfile?.user?.currentStreak ?: 0,
                        longestStreak = streak.streakData?.longestStreak,
                        isAtRisk = streak.isStreakAtRisk,
                        onClick = onStreakClick,
                        modifier = Modifier.weight(1f).testTag("streak_card")
                    )
                }
                
                // XP Widget
                FunnyCard(
                    type = FunnyCardType.ELEVATED,
                    elevation = FunnyCardElevation.FEATURED,
                    onClick = onProfileClick,
                    modifier = Modifier.weight(1f).testTag("xp_card")
                ) {
                    FunnyXPCounter(
                        currentXp = state.userProfile?.user?.totalPoints ?: 0,
                        showIcon = true,
                        size = XPCounterSize.MEDIUM
                    )
                }
            }
        }
        
        // Categories section
        item {
            SectionHeader(
                title = "Категории",
                onViewAll = onViewAllCategories,
                modifier = Modifier.testTag("categories_section")
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
                Spacer(modifier = Modifier.height(SpaceSm))
                SectionHeader(
                    title = "Недавние тесты",
                    onViewAll = null,
                    modifier = Modifier.testTag("recommended_tests")
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
            .padding(horizontal = SpaceMd, vertical = SpaceSm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar and greeting
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onProfileClick)
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // Level badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$level",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.testTag("level_badge")
                    )
                }
            }

            Spacer(modifier = Modifier.width(SpaceMd))

            Column {
                Text(
                    text = "Привет! 👋",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("greeting_text")
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("user_name")
                )
            }
        }

        // Stats badges
        FunnyCard(
            type = FunnyCardType.ELEVATED,
            elevation = FunnyCardElevation.DEFAULT
        ) {
            Row(
                modifier = Modifier.padding(horizontal = SpaceMd, vertical = SpaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak
                Text(
                    text = "🔥",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = streak.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.funnyColors.streak,
                    modifier = Modifier.testTag("streak_days")
                )

                Spacer(modifier = Modifier.width(SpaceMd))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Spacer(modifier = Modifier.width(SpaceMd))

                // Points
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.funnyColors.xp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = points.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.funnyColors.xp,
                    modifier = Modifier.testTag("xp_value")
                )
            }
        }
    }
}

@Composable
private fun LevelProgressHero(
    level: Int,
    currentPoints: Int,
    pointsToNextLevel: Int,
    onContinueLearning: () -> Unit,
    onAdaptiveLessonClick: () -> Unit
) {
    FunnyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceMd, vertical = SpaceSm),
        type = FunnyCardType.ELEVATED,
        elevation = FunnyCardElevation.FEATURED
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FunnyLevelProgress(
                currentLevel = level,
                currentXp = currentPoints,
                xpForNextLevel = currentPoints + pointsToNextLevel
            )
            
            Spacer(modifier = Modifier.height(SpaceMd))
            
            // Primary action - Continue Learning
            FunnyButton(
                text = "Продолжить обучение",
                onClick = onContinueLearning,
                type = FunnyButtonType.PRIMARY,
                size = FunnyButtonSize.LARGE,
                modifier = Modifier.fillMaxWidth().testTag("continue_learning")
            )
            
            Spacer(modifier = Modifier.height(SpaceSm))
            
            // Secondary action - Quick Adaptive Lesson
            FunnyButton(
                text = "⚡ Быстрый урок (5 мин)",
                onClick = onAdaptiveLessonClick,
                type = FunnyButtonType.SECONDARY,
                size = FunnyButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onViewAll: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceMd, vertical = SpaceSm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (onViewAll != null) {
            TextButton(onClick = onViewAll) {
                Text(
                    text = "Все",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
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
        contentPadding = PaddingValues(horizontal = SpaceMd),
        horizontalArrangement = Arrangement.spacedBy(SpaceMd)
    ) {
        items(
            items = categories,
            key = { it.id },
            contentType = { "category" }
        ) { category ->
            CategoryCard(
                category = category,
                onClick = { onCategoryClick(category.id) }
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    val (bgColor, iconColor, emoji) = when {
        category.name.contains("Животные", ignoreCase = true) -> Triple(
            MaterialTheme.funnyColors.achievementContainer,
            MaterialTheme.funnyColors.achievement,
            "🐾"
        )
        category.name.contains("Цвета", ignoreCase = true) -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.secondary,
            "🎨"
        )
        category.name.contains("Числа", ignoreCase = true) -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.primary,
            "🔢"
        )
        category.name.contains("Еда", ignoreCase = true) -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.tertiary,
            "🍎"
        )
        category.name.contains("Семья", ignoreCase = true) -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.error,
            "👨‍👩‍👧"
        )
        category.name.contains("Одежда", ignoreCase = true) -> Triple(
            MaterialTheme.funnyColors.infoContainer,
            MaterialTheme.funnyColors.info,
            "👕"
        )
        else -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.primary,
            "📚"
        )
    }

    val progress = if (category.testsCount > 0) {
        category.completedCount.toFloat() / category.testsCount
    } else 0f

    FunnyCard(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        type = FunnyCardType.FILLED
    ) {
        Column(
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
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(SpaceMd))

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(SpaceSm))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = iconColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(progress * 100).toInt()}% пройдено",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = iconColor
            )
        }
    }
}

@Composable
private fun RecentTestCard(
    test: TestListItem,
    onClick: () -> Unit
) {
    FunnyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceMd, vertical = 4.dp)
            .clickable(onClick = onClick),
        type = FunnyCardType.ELEVATED,
        elevation = FunnyCardElevation.DEFAULT
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = test.title.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(SpaceMd))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = test.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DifficultyBadge(difficulty = test.difficulty)
                    Spacer(modifier = Modifier.width(SpaceSm))
                    Text(
                        text = "${test.questionsCount} вопросов",
                        style = MaterialTheme.typography.bodySmall,
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
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.funnyColors.success
                    )
                }
            } ?: run {
                FunnyButton(
                    text = "Играть",
                    onClick = onClick,
                    type = FunnyButtonType.PRIMARY,
                    size = FunnyButtonSize.SMALL
                )
            }
        }
    }
}
