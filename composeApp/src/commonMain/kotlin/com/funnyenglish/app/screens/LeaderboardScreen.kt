package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.app.components.*
import com.funnyenglish.app.theme.FunnyColors
import com.funnyenglish.app.viewmodel.LeaderboardState
import com.funnyenglish.shared.model.LeaderboardEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreenContent(
    state: LeaderboardState,
    onLoad: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        onLoad()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Рейтинг") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
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
            state.leaderboard == null || state.leaderboard.entries.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏆",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Рейтинг пуст",
                            fontSize = 16.sp,
                            color = FunnyColors.TextSecondary
                        )
                        Text(
                            text = "Пройди тесты, чтобы попасть в рейтинг!",
                            fontSize = 14.sp,
                            color = FunnyColors.TextSecondary
                        )
                    }
                }
            }
            else -> {
                val entries = state.leaderboard.entries

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Top 3 podium
                    if (entries.size >= 3) {
                        item {
                            TopThreePodium(
                                first = entries[0],
                                second = entries[1],
                                third = entries[2]
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // User's rank if available
                    state.leaderboard.userRank?.let { userRank ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = FunnyColors.Primary.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Твоё место:",
                                        color = FunnyColors.TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "#$userRank",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FunnyColors.Primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
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
                        Spacer(modifier = Modifier.height(8.dp))
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
            color = Color(0xFFC0C0C0) // Silver
        )

        // First place (tallest)
        PodiumItem(
            entry = first,
            rank = 1,
            height = 130.dp,
            color = Color(0xFFFFD700) // Gold
        )

        // Third place
        PodiumItem(
            entry = third,
            rank = 3,
            height = 80.dp,
            color = Color(0xFFCD7F32) // Bronze
        )
    }
}

@Composable
private fun PodiumItem(
    entry: LeaderboardEntry,
    rank: Int,
    height: androidx.compose.ui.unit.Dp,
    color: Color
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
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Name
        Text(
            text = entry.displayName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        // Points
        Text(
            text = "${entry.totalPoints}",
            fontSize = 12.sp,
            color = FunnyColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Podium
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
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
                fontSize = 32.sp,
                modifier = Modifier.padding(top = 8.dp)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser)
                FunnyColors.Primary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = "#$rank",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = FunnyColors.TextSecondary,
                modifier = Modifier.width(40.dp)
            )

            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(FunnyColors.SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.displayName.firstOrNull()?.toString() ?: "?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FunnyColors.Primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and level
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Уровень ${entry.level}",
                    fontSize = 12.sp,
                    color = FunnyColors.TextSecondary
                )
            }

            // Points
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = FunnyColors.Secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${entry.totalPoints}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = FunnyColors.Secondary
                )
            }
        }
    }
}
