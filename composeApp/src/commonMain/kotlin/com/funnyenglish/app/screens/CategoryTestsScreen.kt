package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.app.components.*
import com.funnyenglish.app.theme.FunnyColors
import com.funnyenglish.app.theme.FunnyTheme
import com.funnyenglish.app.viewmodel.CategoryTestsState
import com.funnyenglish.shared.model.Difficulty
import com.funnyenglish.shared.model.TestListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTestsScreenContent(
    state: CategoryTestsState,
    onLoad: () -> Unit,
    onTestClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val colors = FunnyTheme.colors

    LaunchedEffect(Unit) {
        onLoad()
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.categoryName.ifEmpty { "Tests" },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingIndicator()
            }
            state.error != null -> {
                ErrorMessage(message = state.error, onRetry = onLoad)
            }
            state.tests.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📝",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tests available",
                            fontSize = 16.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.tests) { test ->
                        TestCard(
                            test = test,
                            onClick = { onTestClick(test.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TestCard(
    test: TestListItem,
    onClick: () -> Unit
) {
    val colors = FunnyTheme.colors
    val isCompleted = test.userProgress != null
    val stars = test.userProgress?.stars ?: 0
    val bestPercentage = test.userProgress?.percentage

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (colors.isDark) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Test icon/thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (test.difficulty) {
                            Difficulty.EASY -> FunnyColors.Success.copy(alpha = 0.15f)
                            Difficulty.MEDIUM -> FunnyColors.Secondary.copy(alpha = 0.15f)
                            Difficulty.HARD -> FunnyColors.Error.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted && stars == 3) {
                    // Perfect score indicator
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = FunnyColors.Success,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text(
                        text = test.title.firstOrNull()?.toString() ?: "?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (test.difficulty) {
                            Difficulty.EASY -> FunnyColors.Success
                            Difficulty.MEDIUM -> FunnyColors.Secondary
                            Difficulty.HARD -> FunnyColors.Error
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Test info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = test.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Difficulty badge
                    DifficultyBadge(difficulty = test.difficulty)

                    // Questions count
                    Text(
                        text = "${test.questionsCount} questions",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Points reward
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = FunnyColors.Secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${test.pointsReward} points",
                        fontSize = 12.sp,
                        color = FunnyColors.Secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Stars and progress
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Stars display
                StarsDisplay(stars = stars, size = 20)

                Spacer(modifier = Modifier.height(4.dp))

                // Best score
                if (bestPercentage != null) {
                    Text(
                        text = "Best: $bestPercentage%",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                } else {
                    Text(
                        text = "Not completed",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}
