package com.funnyenglish.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.app.components.ErrorMessage
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.app.viewmodel.ProfileState
import com.funnyenglish.designsystem.components.cards.FunnyCard
import com.funnyenglish.designsystem.components.cards.FunnyCardType
import com.funnyenglish.designsystem.components.gamification.FunnyLevelProgress
import com.funnyenglish.designsystem.components.gamification.FunnyStreakWidget
import com.funnyenglish.designsystem.components.gamification.FunnyXPCounter
import com.funnyenglish.designsystem.tokens.SpaceMd
import com.funnyenglish.designsystem.tokens.SpaceSm
import com.funnyenglish.shared.model.CategoryProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    isGuest: Boolean = false,
    onLoad: () -> Unit,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onLoginClick: (() -> Unit)? = null,
    onMessagesClick: () -> Unit = {},
    unreadMessages: Int = 0
) {
    if (!isGuest) {
        LaunchedEffect(Unit) { onLoad() }
    }

    if (isGuest) {
        GuestProfileStub(
            guestSession = state.guestSession,
            onBack = onBack,
            onLoginClick = onLoginClick
        )
        return
    }

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Профиль",
                        style = MaterialTheme.typography.titleLarge,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = SpaceMd,
                vertical = SpaceSm
            ),
            verticalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            // User Info Card with Design System
            item {
                FunnyCard(
                    type = FunnyCardType.ELEVATED,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(SpaceMd)) {
                        Text(
                            text = profile.user.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(SpaceMd))

                        // Stats Row with Design System components
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(SpaceSm)
                        ) {
                            // Level Progress
                            FunnyLevelProgress(
                                currentLevel = profile.user.level,
                                currentXp = profile.user.totalPoints,
                                xpForNextLevel = profile.user.totalPoints + profile.stats.pointsToNextLevel,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(SpaceSm))
                            
                            // Streak and XP Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                FunnyStreakWidget(
                                    streak = profile.user.currentStreak,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Spacer(modifier = Modifier.width(SpaceSm))
                                
                                FunnyXPCounter(
                                    currentXp = profile.user.totalPoints,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Statistics Card
            item {
                FunnyCard(
                    type = FunnyCardType.FILLED,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(SpaceMd)) {
                        Text(
                            text = "Статистика",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(SpaceMd))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(
                                value = profile.stats.testsCompleted.toString(),
                                label = "Тестов"
                            )
                            StatItem(
                                value = profile.stats.totalStars.toString(),
                                label = "Звёзд"
                            )
                            StatItem(
                                value = profile.stats.perfectScores.toString(),
                                label = "Идеальных"
                            )
                        }
                        Spacer(modifier = Modifier.height(SpaceSm))
                        Text(
                            text = "До следующего уровня: ${profile.stats.pointsToNextLevel} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Category Progress Card
            item {
                FunnyCard(
                    type = FunnyCardType.FILLED,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(SpaceMd)) {
                        Text(
                            text = "Прогресс по категориям",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(SpaceSm))
                        val categories = state.progressSummary?.categoriesProgress ?: emptyList()
                        if (categories.isEmpty()) {
                            Text(
                                text = "Нет данных",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            CategoryProgressList(categories = categories)
                        }
                    }
                }
            }

            // Achievements Card
            if (profile.achievements.isNotEmpty()) {
                item {
                    FunnyCard(
                        type = FunnyCardType.FILLED,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(SpaceMd)) {
                            Text(
                                text = "Достижения",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(SpaceSm))
                            profile.achievements.take(5).forEach { achievement ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (achievement.earned) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(SpaceSm))
                                    Column {
                                        Text(
                                            achievement.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            achievement.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            if (profile.achievements.size > 5) {
                                Spacer(modifier = Modifier.height(SpaceSm))
                                Text(
                                    text = "Показать все (${profile.achievements.size})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .clickable { onAchievementsClick() },
                                )
                            }
                        }
                    }
                }
            }

            // Messages Card (inbox от учителя) — только для авторизованных
            if (!isGuest) {
                item {
                    FunnyCard(
                        type = FunnyCardType.OUTLINED,
                        onClick = onMessagesClick,
                        modifier = Modifier.fillMaxWidth().testTag("messages_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(SpaceMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MailOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(SpaceSm))
                            Text(
                                text = "Сообщения",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            if (unreadMessages > 0) {
                                Badge {
                                    Text(unreadMessages.toString())
                                }
                                Spacer(modifier = Modifier.width(SpaceSm))
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Settings Card
            item {
                FunnyCard(
                    type = FunnyCardType.OUTLINED,
                    onClick = onSettingsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpaceMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(SpaceSm))
                        Text(
                            text = "Настройки",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryProgressList(categories: List<CategoryProgress>) {
    Column(verticalArrangement = Arrangement.spacedBy(SpaceMd)) {
        categories.forEach { category ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${category.completedCount}/${category.testsCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Progress indicator
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { 
                        if (category.testsCount > 0) {
                            category.completedCount.toFloat() / category.testsCount
                        } else {
                            0f
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${category.totalStars} ★ / ${category.maxStars} макс",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuestProfileStub(
    guestSession: com.funnyenglish.shared.model.GuestSession?,
    onBack: () -> Unit,
    onLoginClick: (() -> Unit)?
) {
    val testsCount = guestSession?.testProgress?.size ?: 0
    val totalStars = guestSession?.testProgress?.sumOf { it.stars } ?: 0
    val totalXp = guestSession?.totalXpEarned ?: 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Профиль",
                        style = MaterialTheme.typography.titleLarge,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .padding(SpaceMd),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "👤",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(SpaceMd))
            Text(
                text = "Гостевой режим",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(SpaceSm))
            Text(
                text = "Войдите, чтобы сохранить прогресс в облаке",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (testsCount > 0) {
                Spacer(modifier = Modifier.height(SpaceMd))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpaceMd),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(value = testsCount.toString(), label = "Тестов")
                        StatItem(value = totalStars.toString(), label = "Звёзд")
                        StatItem(value = totalXp.toString(), label = "XP")
                    }
                }
            }

            Spacer(modifier = Modifier.height(SpaceMd))
            onLoginClick?.let {
                androidx.compose.material3.Button(onClick = it) {
                    Text("Войти или зарегистрироваться")
                }
            }
        }
    }
}
