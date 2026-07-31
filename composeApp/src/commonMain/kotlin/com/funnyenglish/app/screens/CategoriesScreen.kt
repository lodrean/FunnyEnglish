package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.app.components.*
import com.funnyenglish.designsystem.theme.funnyColors
import com.funnyenglish.app.viewmodel.CategoriesState
import com.funnyenglish.shared.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreenContent(
    state: CategoriesState,
    onLoad: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        onLoad()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Categories",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
        when {
            state.isLoading -> {
                LoadingIndicator()
            }
            state.error != null -> {
                ErrorMessage(message = state.error, onRetry = onLoad)
            }
            state.categories.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет доступных категорий")
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(padding).testTag("categories_grid"),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.categories) { category ->
                        CategoryGridCard(
                            category = category,
                            onClick = { onCategoryClick(category.id) },
                            modifier = Modifier.testTag("category_card_${category.id}")
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun CategoryGridCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
        listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
        listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary),
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.funnyColors.achievement),
        listOf(MaterialTheme.funnyColors.achievement, MaterialTheme.colorScheme.primary),
        listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
    )
    val colorPair = colors[category.id.hashCode().mod(colors.size)]

    val categoryEmoji = when {
        category.name.contains("Животные", ignoreCase = true) -> "🐾"
        category.name.contains("Цвета", ignoreCase = true) -> "🎨"
        category.name.contains("Числа", ignoreCase = true) -> "🔢"
        category.name.contains("Еда", ignoreCase = true) -> "🍎"
        category.name.contains("Семья", ignoreCase = true) -> "👨‍👩‍👧"
        category.name.contains("Одежда", ignoreCase = true) -> "👕"
        else -> "📚"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(colorPair),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Emoji icon
                Text(
                    text = categoryEmoji,
                    fontSize = 40.sp
                )

                Column {
                    // Category name
                    Text(
                        text = category.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tests count
                    Text(
                        text = "${category.testsCount} тестов",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress
                    if (category.testsCount > 0) {
                        val progress = category.completedCount.toFloat() / category.testsCount

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Stars
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.funnyColors.xp,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${category.totalStars} / ${category.testsCount * 3}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}
