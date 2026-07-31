package com.funnyenglish.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.animations.AchievementGridSkeleton
import com.funnyenglish.designsystem.components.gamification.AchievementState
import com.funnyenglish.designsystem.components.gamification.FunnyAchievementBadge
import com.funnyenglish.designsystem.components.gamification.FunnyAchievementCard
import com.funnyenglish.designsystem.tokens.SpaceMd
import com.funnyenglish.viewmodel.AchievementsUiState
import com.funnyenglish.viewmodel.GamificationViewModel

/**
 * FunnyEnglish Achievements Screen
 * 
 * Grid of achievement badges with:
 * - Locked achievements (grayscale)
 * - Unlocked achievements (full color)
 * - Achievement details on tap
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: GamificationViewModel,
    onBack: () -> Unit = {}
) {
    val achievementsState by viewModel.achievementsState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Достижения") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (achievementsState.isLoading) {
            AchievementGridSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(SpaceMd)
            )
            return@Scaffold
        }
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SpaceMd),
            verticalArrangement = Arrangement.spacedBy(SpaceMd),
            horizontalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            items(achievementsState.achievements) { achievement ->
                FunnyAchievementBadge(
                    name = achievement.name,
                    description = achievement.description,
                    state = if (achievement.isHidden == true) 
                        AchievementState.LOCKED 
                    else 
                        AchievementState.UNLOCKED,
                    icon = {
                        // Use achievement icon from URL or default
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                )
            }
        }
        
        // Empty state
        if (achievementsState.achievements.isEmpty() && !achievementsState.isLoading) {
            Text(
                text = "Нет достижений",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(SpaceMd)
            )
        }
    }
}
