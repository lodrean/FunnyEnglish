package com.funnyenglish.app.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.funnyenglish.shared.model.Achievement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.designsystem.theme.funnyColors
import com.funnyenglish.shared.model.*

/**
 * Экран достижений
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    state: com.funnyenglish.app.viewmodel.AchievementsState,
    isGuest: Boolean = false,
    onLoad: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onRegisterClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        onLoad()
    }

    // Ачивки — только для зарегистрированных (гейтинг статистики)
    if (isGuest) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Достижения") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            com.funnyenglish.app.components.LockedFeature(
                title = "Достижения после регистрации",
                description = "Зарегистрируйтесь, чтобы открывать ачивки, выполнять квесты и отслеживать прогресс",
                onRegisterClick = onRegisterClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
        return
    }

    val userAchievements = state.achievements.map {
        UserAchievement(
            achievement = it,
            earnedAt = null,
            progress = if (isGuest) 0f else 1f, // Guests see all as locked
            isEarned = !isGuest // TODO: Check if earned for authenticated users
        )
    }

    AchievementScreenContent(
        achievements = userAchievements,
        isLoading = state.isLoading,
        onBack = onBack,
        onAchievementClick = { },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AchievementScreenContent(
    achievements: List<UserAchievement>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onAchievementClick: (Achievement) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Достижения") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            LoadingIndicator()
            return@Scaffold
        }
        
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .padding(16.dp)
        ) {
            // Статистика
            AchievementStats(achievements = achievements)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Фильтры по категориям
            CategoryFilter(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Список достижений
            val filteredAchievements = selectedCategory?.let { category ->
                achievements.filter { it.achievement.category == category }
            } ?: achievements
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredAchievements,
                    key = { it.achievement.id }
                ) { userAchievement ->
                    AchievementCard(
                        userAchievement = userAchievement,
                        onClick = { onAchievementClick(userAchievement.achievement) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementStats(achievements: List<UserAchievement>) {
    val earnedCount = achievements.count { it.isEarned }
    val totalCount = achievements.size
    val progress = if (totalCount > 0) earnedCount.toFloat() / totalCount else 0f
    
    val categoryCounts = achievements
        .filter { it.isEarned }
        .groupBy { it.achievement.category }
        .mapValues { it.value.size }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Твой прогресс",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Круговой прогресс
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 8.dp
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$earnedCount",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "/$totalCount",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Статистика по категориям
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CategoryStat(
                    icon = "📚",
                    count = categoryCounts[AchievementCategory.LEARNING] ?: 0,
                    label = "Обучение"
                )
                CategoryStat(
                    icon = "🔥",
                    count = categoryCounts[AchievementCategory.CONSISTENCY] ?: 0,
                    label = "Серия"
                )
                CategoryStat(
                    icon = "👥",
                    count = categoryCounts[AchievementCategory.SOCIAL] ?: 0,
                    label = "Социум"
                )
                CategoryStat(
                    icon = "🔍",
                    count = categoryCounts[AchievementCategory.EXPLORER] ?: 0,
                    label = "Исследования"
                )
            }
        }
    }
}

@Composable
private fun CategoryStat(icon: String, count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Text(
            text = "$count",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun CategoryFilter(
    selectedCategory: AchievementCategory?,
    onCategorySelected: (AchievementCategory?) -> Unit
) {
    val categories = listOf(
        null to "Все",
        AchievementCategory.LEARNING to "📚 Обучение",
        AchievementCategory.CONSISTENCY to "🔥 Серия",
        AchievementCategory.SOCIAL to "👥 Социум",
        AchievementCategory.EXPLORER to "🔍 Исследования",
        AchievementCategory.SECRET to "🎁 Секретные"
    )
    
    ScrollableTabRow(
        selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory },
        edgePadding = 0.dp,
        containerColor = Color.Transparent
    ) {
        categories.forEach { (category, label) ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = {
                    Text(
                        text = label,
                        fontSize = 13.sp
                    )
                }
            )
        }
    }
}

@Composable
private fun AchievementCard(
    userAchievement: UserAchievement,
    onClick: () -> Unit
) {
    val achievement = userAchievement.achievement
    val isEarned = userAchievement.isEarned
    val isHidden = (achievement.isHidden ?: false) && !isEarned
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEarned) 
                getRarityColor(achievement.rarity ?: Rarity.COMMON).copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isEarned) {
            CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        getRarityColor(achievement.rarity ?: Rarity.COMMON).copy(alpha = 0.5f),
                        getRarityColor(achievement.rarity ?: Rarity.COMMON).copy(alpha = 0.5f)
                    )
                )
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка достижения
            AchievementIcon(
                userAchievement = userAchievement,
                isHidden = isHidden
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (isHidden) {
                    Text(
                        text = "???",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Секретное достижение",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = achievement.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (isEarned) {
                            Spacer(modifier = Modifier.width(8.dp))
                            achievement.rarity?.let { RarityBadge(rarity = it) }
                        }
                    }
                    
                    Text(
                        text = achievement.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    
                    if (!isEarned) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { userAchievement.progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = getRarityColor(achievement.rarity ?: Rarity.COMMON),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${(userAchievement.progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            if (isEarned) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.funnyColors.success,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun AchievementIcon(
    userAchievement: UserAchievement,
    isHidden: Boolean
) {
    val achievement = userAchievement.achievement
    
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                color = if (userAchievement.isEarned)
                    getRarityColor(achievement.rarity ?: Rarity.COMMON).copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            )
            .border(
                width = if (userAchievement.isEarned) 2.dp else 0.dp,
                color = if (userAchievement.isEarned) 
                    getRarityColor(achievement.rarity ?: Rarity.COMMON) 
                else 
                    Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isHidden) {
            Text(
                text = "?",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold
            )
        } else {
            // Используем emoji или иконку на основе категории
            val icon = when (achievement.category) {
                AchievementCategory.LEARNING -> "📚"
                AchievementCategory.CONSISTENCY -> "🔥"
                AchievementCategory.SOCIAL -> "👥"
                AchievementCategory.EXPLORER -> "🔍"
                AchievementCategory.SECRET -> "🎁"
                null -> "🏆"
            }
            
            Text(
                text = icon,
                fontSize = 28.sp
            )
        }
    }
}

@Composable
private fun RarityBadge(rarity: Rarity) {
    val (text, colorValue) = when (rarity) {
        Rarity.COMMON -> "Обычное" to androidx.compose.ui.graphics.Color.Gray
        Rarity.UNCOMMON -> "Необычное" to androidx.compose.ui.graphics.Color(0xFF22C55E)
        Rarity.RARE -> "Редкое" to androidx.compose.ui.graphics.Color(0xFF3B82F6)
        Rarity.EPIC -> "Эпическое" to androidx.compose.ui.graphics.Color(0xFF8B5CF6)
        Rarity.LEGENDARY -> "Легендарное" to androidx.compose.ui.graphics.Color(0xFFF59E0B)
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = colorValue.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = colorValue
        )
    }
}

private fun getRarityColor(rarity: Rarity): androidx.compose.ui.graphics.Color {
    return when (rarity) {
        Rarity.COMMON -> androidx.compose.ui.graphics.Color.Gray
        Rarity.UNCOMMON -> androidx.compose.ui.graphics.Color(0xFF22C55E)
        Rarity.RARE -> androidx.compose.ui.graphics.Color(0xFF3B82F6)
        Rarity.EPIC -> androidx.compose.ui.graphics.Color(0xFF8B5CF6)
        Rarity.LEGENDARY -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
    }
}

/**
 * Celebration dialog при получении достижения
 */
@Composable
fun AchievementUnlockDialog(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        getRarityColor(achievement.rarity ?: Rarity.COMMON).copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏆",
                    fontSize = 40.sp
                )
            }
        },
        title = {
            Text(
                text = "Достижение разблокировано!",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = achievement.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = getRarityColor(achievement.rarity ?: Rarity.COMMON)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = achievement.description,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.funnyColors.warning.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💎")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${achievement.pointsReward} XP",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.funnyColors.warning
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Отлично!")
            }
        }
    )
}
