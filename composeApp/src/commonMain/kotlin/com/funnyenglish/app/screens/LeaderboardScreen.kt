package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funnyenglish.app.components.ErrorMessage
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.designsystem.components.cards.FunnyCard
import com.funnyenglish.designsystem.components.cards.FunnyCardType
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.app.viewmodel.LeaderboardState
import com.funnyenglish.designsystem.theme.funnyColors
import com.funnyenglish.shared.model.LeaderboardEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreenContent(
    state: LeaderboardState,
    isGuest: Boolean = false,
    onLoad: () -> Unit,
    onBack: () -> Unit,
    onRegisterClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        onLoad()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Рейтинг",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            // Рейтинг — только для зарегистрированных (гейтинг статистики)
            isGuest -> {
                com.funnyenglish.app.components.LockedFeature(
                    title = "Рейтинг после регистрации",
                    description = "Зарегистрируйтесь, чтобы видеть рейтинг и соревноваться с другими учениками",
                    onRegisterClick = onRegisterClick,
                    modifier = Modifier.padding(padding)
                )
            }
            state.isLoading -> {
                LoadingIndicator()
            }
            state.error != null -> {
                ErrorMessage(message = state.error, onRetry = onLoad)
            }
            state.leaderboard == null || state.leaderboard.entries.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏆",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(SpaceMd))
                        Text(
                            text = "Рейтинг пуст",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Пройди тесты, чтобы попасть в рейтинг!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                val entries = state.leaderboard.entries

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
                    contentPadding = PaddingValues(SpaceMd)
                ) {
                    // Top 3 podium
                    if (entries.size >= 3) {
                        item {
                            TopThreePodium(
                                first = entries[0],
                                second = entries[1],
                                third = entries[2]
                            )
                            Spacer(modifier = Modifier.height(SpaceLg))
                        }
                    }

                    // User's rank if available (only for authenticated users)
                    if (!isGuest) {
                    state.leaderboard.userRank?.let { userRank ->
                        item {
                            FunnyCard(
                                type = FunnyCardType.FILLED,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(SpaceMd),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Твоё место:",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(SpaceSm))
                                    Text(
                                        text = "#$userRank",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(SpaceMd))
                        }
                    }
                    }

                    // Full list (skip first 3 if showing podium)
                    val startIndex = if (entries.size >= 3) 3 else 0
                    itemsIndexed(entries.drop(startIndex)) { index, entry ->
                        LeaderboardEntryCard(
                            entry = entry,
                            rank = startIndex + index + 1,
                            isCurrentUser = state.leaderboard.userRank == entry.rank
                        )
                        Spacer(modifier = Modifier.height(SpaceSm))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopThreePodium(
    first: LeaderboardEntry,
    second: LeaderboardEntry,
    third: LeaderboardEntry
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // Second place
        PodiumItem(
            entry = second,
            rank = 2,
            height = 100.dp,
            color = SilverLight
        )

        // First place (tallest)
        PodiumItem(
            entry = first,
            rank = 1,
            height = 130.dp,
            color = MaterialTheme.funnyColors.xp
        )

        // Third place
        PodiumItem(
            entry = third,
            rank = 3,
            height = 80.dp,
            color = BronzeLight
        )
    }
}

@Composable
private fun PodiumItem(
    entry: LeaderboardEntry,
    rank: Int,
    height: androidx.compose.ui.unit.Dp,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.displayName.firstOrNull()?.toString() ?: "?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(SpaceSm))

        // Name
        Text(
            text = entry.displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        // Points
        Text(
            text = "${entry.totalPoints}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(SpaceSm))

        // Podium
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .clip(MaterialTheme.shapes.small)
                .background(color),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> rank.toString()
                },
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = SpaceSm)
            )
        }
    }
}

@Composable
private fun LeaderboardEntryCard(
    entry: LeaderboardEntry,
    rank: Int,
    isCurrentUser: Boolean
) {
    FunnyCard(
        modifier = Modifier.fillMaxWidth(),
        type = if (isCurrentUser) FunnyCardType.ELEVATED else FunnyCardType.FILLED
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp)
            )

            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.displayName.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(SpaceMd))

            // Name and level
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Уровень ${entry.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Points
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.funnyColors.xp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${entry.totalPoints}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.funnyColors.xp
                )
            }
        }
    }
}
