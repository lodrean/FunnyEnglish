package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.app.components.*
import com.funnyenglish.app.theme.FunnyColors
import com.funnyenglish.app.viewmodel.HomeState
import com.funnyenglish.shared.model.Category
import com.funnyenglish.shared.model.TestListItem

@Composable
fun HomeScreen(
    state: HomeState,
    onLoadData: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onTestClick: (String) -> Unit,
    onViewAllCategories: () -> Unit
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
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // User greeting card
        item {
            UserGreetingCard(
                displayName = state.userProfile?.user?.displayName ?: "Друг",
                level = state.userProfile?.user?.level ?: 1,
                totalPoints = state.userProfile?.user?.totalPoints ?: 0,
                currentStreak = state.userProfile?.user?.currentStreak ?: 0
            )
        }

        // Progress summary
        state.userProfile?.let { profile ->
            item {
                ProgressSummaryCard(
                    testsCompleted = profile.stats.testsCompleted.toInt(),
                    totalStars = profile.stats.totalStars,
                    pointsToNextLevel = profile.stats.pointsToNextLevel
                )
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
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category.id) }
                    )
                }
            }
        }

        // Recent tests section
        if (state.recentTests.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Продолжить обучение",
                    onViewAll = null
                )
            }

            items(state.recentTests) { test ->
                TestListItemCard(
                    test = test,
                    onClick = { onTestClick(test.id) }
                )
            }
        }
    }
}

@Composable
private fun UserGreetingCard(
    displayName: String,
    level: Int,
    totalPoints: Int,
    currentStreak: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(FunnyColors.Primary, FunnyColors.Cyan)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Привет, $displayName!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Готов к новым знаниям?",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    LevelBadge(level = level)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatBadge(
                        icon = Icons.Filled.Star,
                        value = "$currentStreak дней",
                        label = "Серия"
                    )
                    PointsBadge(points = totalPoints)
                }
            }
        }
    }
}

@Composable
private fun StatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FunnyColors.Secondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ProgressSummaryCard(
    testsCompleted: Int,
    totalStars: Int,
    pointsToNextLevel: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = testsCompleted.toString(), label = "Тестов")
                StatItem(value = totalStars.toString(), label = "Звёзд")
                StatItem(value = pointsToNextLevel.toString(), label = "До уровня")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
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
private fun SectionHeader(
    title: String,
    onViewAll: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (onViewAll != null) {
            TextButton(onClick = onViewAll) {
                Text("Все")
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    val colors = listOf(
        listOf(FunnyColors.Pink, FunnyColors.Purple),
        listOf(FunnyColors.Green, FunnyColors.Cyan),
        listOf(FunnyColors.Secondary, FunnyColors.Yellow),
        listOf(FunnyColors.Primary, FunnyColors.PrimaryDark)
    )
    val colorPair = colors[category.id.hashCode().mod(colors.size)]

    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(colorPair),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${category.testsCount} тестов",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (category.completedCount > 0) {
                    ProgressBar(
                        progress = category.completedCount.toFloat() / category.testsCount,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StarsDisplay(
                        stars = category.totalStars,
                        maxStars = category.testsCount * 3,
                        size = 12
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(FunnyColors.SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = test.title.firstOrNull()?.toString() ?: "?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = FunnyColors.Primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = test.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DifficultyBadge(difficulty = test.difficulty)
                    Text(
                        text = "${test.questionsCount} вопросов",
                        fontSize = 12.sp,
                        color = FunnyColors.TextSecondary
                    )
                }
            }

            test.userProgress?.let { progress ->
                StarsDisplay(stars = progress.stars, size = 20)
            }
        }
    }
}
