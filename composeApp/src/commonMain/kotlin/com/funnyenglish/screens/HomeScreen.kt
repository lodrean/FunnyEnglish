package com.funnyenglish.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.animations.HomeScreenSkeleton
import com.funnyenglish.designsystem.animations.SuccessCelebration
import com.funnyenglish.designsystem.animations.XPGainCelebration
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.components.gamification.FunnyQuestCard
import com.funnyenglish.designsystem.components.gamification.FunnyStreakWidget
import com.funnyenglish.designsystem.components.gamification.FunnyLevelProgress
import com.funnyenglish.designsystem.tokens.SpaceMd
import com.funnyenglish.designsystem.tokens.SpaceSm
import com.funnyenglish.viewmodel.GamificationViewModel

/**
 * FunnyEnglish Home Screen
 * 
 * Gamification-first layout:
 * 1. Streak Widget (top, prominent)
 * 2. Daily Quests grid
 * 3. Level Progress
 * 4. CTA Button (bottom)
 * 
 * Celebrations: XP gains, quest completion
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GamificationViewModel,
    onNavigateToLessons: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val streakState by viewModel.streakState.collectAsState()
    val xpState by viewModel.xpState.collectAsState()
    val questsState by viewModel.questsState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle errors
    LaunchedEffect(questsState.error) {
        questsState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrors()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FunnyEnglish") },
                actions = {
                    FunnyButton(
                        onClick = onNavigateToProfile,
                        icon = Icons.Default.Person,
                        type = FunnyButtonType.GHOST
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // Show loading skeleton while initial data loads
        if (streakState.isLoading && streakState.streakData == null) {
            HomeScreenSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
            return@Scaffold
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SpaceMd),
            verticalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            // Streak Widget
            item {
                streakState.streakData?.let { streakData ->
                    FunnyStreakWidget(
                        streak = streakData.currentStreak,
                        isAtRisk = streakData.isAtRisk,
                        longestStreak = streakData.longestStreak,
                        onClick = { viewModel.recordActivity() }
                    )
                }
            }
            
            // Daily Quests Section
            item {
                Text(
                    text = "Ежедневные квесты",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(SpaceSm))
            }
            
            items(questsState.dailyQuests) { quest ->
                FunnyQuestCard(
                    title = quest.title,
                    description = quest.description,
                    currentValue = quest.currentValue,
                    targetValue = quest.targetValue,
                    xpReward = quest.reward.xp,
                    gemReward = quest.reward.gems,
                    isCompleted = quest.isCompleted,
                    onClaim = { viewModel.claimQuestReward(quest.id) }
                )
            }
            
            // Show message if no quests
            if (questsState.dailyQuests.isEmpty() && !questsState.isLoading) {
                item {
                    Text(
                        text = "Нет активных квестов",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Level Progress
            item {
                Spacer(modifier = Modifier.height(SpaceMd))
                xpState.xpData?.let { xpData ->
                    FunnyLevelProgress(
                        currentLevel = xpData.currentLevel,
                        currentXp = xpData.currentXp,
                        xpForNextLevel = xpData.xpForNextLevel
                    )
                }
            }
            
            // CTA Button
            item {
                Spacer(modifier = Modifier.height(SpaceMd))
                FunnyButton(
                    text = "Начать урок",
                    onClick = onNavigateToLessons,
                    type = FunnyButtonType.PRIMARY,
                    size = FunnyButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // XP Celebration overlay
        if (xpState.showCelebration) {
            XPGainCelebration(
                isVisible = true,
                modifier = Modifier.fillMaxSize(),
                onComplete = { /* Animation complete */ }
            )
        }
        
        // Quest completion celebration
        streakState.celebration?.let {
            SuccessCelebration(
                isVisible = true,
                modifier = Modifier.fillMaxSize(),
                onComplete = { /* Animation complete */ }
            )
        }
    }
}
