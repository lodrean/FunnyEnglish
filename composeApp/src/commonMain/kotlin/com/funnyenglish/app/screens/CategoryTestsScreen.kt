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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.funnyenglish.app.components.*
import com.funnyenglish.designsystem.theme.funnyColors
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
    val colors = MaterialTheme.colorScheme

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
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.tests,
                        key = { it.id },
                        contentType = { "test" }
                    ) { test ->
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
    val colors = MaterialTheme.colorScheme
    val isCompleted = test.userProgress != null
    val stars = test.userProgress?.stars ?: 0
    val bestPercentage = test.userProgress?.percentage

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Test thumbnail with fallback
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (test.difficulty) {
                            Difficulty.EASY -> MaterialTheme.funnyColors.success.copy(alpha = 0.15f)
                            Difficulty.MEDIUM -> colors.secondary.copy(alpha = 0.15f)
                            Difficulty.HARD -> colors.error.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!test.thumbnailUrl.isNullOrBlank()) {
                    // Show thumbnail image with fallback
                    SubcomposeAsyncImage(
                        model = test.thumbnailUrl,
                        contentDescription = test.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            // Fallback to text/icon on error
                            ThumbnailFallback(
                                title = test.title,
                                difficulty = test.difficulty,
                                isCompleted = isCompleted,
                                stars = stars
                            )
                        }
                    )
                } else {
                    // No thumbnail - show fallback
                    ThumbnailFallback(
                        title = test.title,
                        difficulty = test.difficulty,
                        isCompleted = isCompleted,
                        stars = stars
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
                    color = colors.onSurface
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
                        color = colors.onSurfaceVariant
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
                        tint = colors.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${test.pointsReward} points",
                        fontSize = 12.sp,
                        color = colors.secondary,
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
                        color = colors.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Not completed",
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Fallback composable when thumbnail fails to load or is not available
 */
@Composable
private fun ThumbnailFallback(
    title: String,
    difficulty: Difficulty,
    isCompleted: Boolean,
    stars: Int
) {
    if (isCompleted && stars == 3) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.funnyColors.success,
            modifier = Modifier.size(32.dp)
        )
    } else {
        Text(
            text = title.firstOrNull()?.toString() ?: "?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = when (difficulty) {
                Difficulty.EASY -> MaterialTheme.funnyColors.success
                Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
                Difficulty.HARD -> MaterialTheme.colorScheme.error
            }
        )
    }
}
