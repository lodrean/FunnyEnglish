package com.funnyenglish.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.animations.PageTransition
import com.funnyenglish.designsystem.animations.PageTransitionDirection
import com.funnyenglish.designsystem.tokens.SidebarWidth
import com.funnyenglish.designsystem.tokens.SpaceMd
import com.funnyenglish.designsystem.tokens.SpaceSm
import com.funnyenglish.screens.AchievementsScreen
import com.funnyenglish.screens.HomeScreen
import com.funnyenglish.screens.LessonsScreen
import com.funnyenglish.screens.ProfileScreen
import com.funnyenglish.viewmodel.GamificationViewModel

/**
 * FunnyEnglish Sidebar Navigation (Desktop)
 * 
 * 200dp fixed sidebar with navigation items
 * Main content area with page transitions
 */

@Composable
fun FunnyEnglishApp(
    gamificationViewModel: GamificationViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var showLesson by remember { mutableStateOf(false) }
    
    Row(modifier = modifier.fillMaxSize()) {
        // Sidebar
        Sidebar(
            currentScreen = currentScreen,
            onScreenSelected = { screen ->
                currentScreen = screen
                showLesson = false
            }
        )
        
        // Main Content
        Surface(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showLesson) {
                // Lesson screen (full screen, no sidebar)
                com.funnyenglish.screens.AdaptiveLessonScreen(
                    viewModel = com.funnyenglish.di.AppModule.provideAdaptiveLessonViewModel(),
                    onClose = { showLesson = false },
                    onComplete = { xp ->
                        showLesson = false
                        // Show XP celebration
                        gamificationViewModel.showXpCelebration(xp)
                    }
                )
            } else {
                // Regular screens with transition
                PageTransition(
                    targetState = currentScreen,
                    direction = PageTransitionDirection.RIGHT
                ) { screen ->
                    when (screen) {
                        Screen.HOME -> HomeScreen(
                            viewModel = gamificationViewModel,
                            onNavigateToLessons = { currentScreen = Screen.LESSONS },
                            onNavigateToAchievements = { currentScreen = Screen.ACHIEVEMENTS },
                            onNavigateToProfile = { currentScreen = Screen.PROFILE }
                        )
                        
                        Screen.LESSONS -> LessonsScreen(
                            onBack = { currentScreen = Screen.HOME },
                            onStartLesson = { showLesson = true }
                        )
                        
                        Screen.ACHIEVEMENTS -> AchievementsScreen(
                            viewModel = gamificationViewModel,
                            onBack = { currentScreen = Screen.HOME }
                        )
                        
                        Screen.PROFILE -> ProfileScreen(
                            onBack = { currentScreen = Screen.HOME }
                        )
                        
                        else -> HomeScreen(
                            viewModel = gamificationViewModel,
                            onNavigateToLessons = { currentScreen = Screen.LESSONS },
                            onNavigateToAchievements = { currentScreen = Screen.ACHIEVEMENTS },
                            onNavigateToProfile = { currentScreen = Screen.PROFILE }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Sidebar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(SidebarWidth)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = SpaceMd)
        ) {
            // Logo / App Name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpaceMd, vertical = SpaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(SpaceSm))
                Text(
                    text = "FunnyEnglish",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(SpaceMd))
            Divider()
            Spacer(modifier = Modifier.height(SpaceMd))
            
            // Navigation Items
            val mainScreens = listOf(
                Screen.HOME,
                Screen.LESSONS,
                Screen.ACHIEVEMENTS
            )
            
            mainScreens.forEach { screen ->
                NavigationDrawerItem(
                    icon = { Icon(screen.icon, contentDescription = null) },
                    label = { Text(screen.title) },
                    selected = currentScreen == screen,
                    onClick = { onScreenSelected(screen) },
                    modifier = Modifier.padding(horizontal = SpaceSm),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Divider()
            Spacer(modifier = Modifier.height(SpaceMd))
            
            // Settings / Profile at bottom
            NavigationDrawerItem(
                icon = { Icon(Screen.PROFILE.icon, contentDescription = null) },
                label = { Text(Screen.PROFILE.title) },
                selected = currentScreen == Screen.PROFILE,
                onClick = { onScreenSelected(Screen.PROFILE) },
                modifier = Modifier.padding(horizontal = SpaceSm)
            )
        }
    }
}

/**
 * Compact navigation (for medium screens)
 */
@Composable
fun RailNavigation(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    // Implementation for rail navigation (icon-only sidebar)
    // For screens between 600dp and 1200dp
}

/**
 * Bottom navigation (for mobile/compact screens)
 */
@Composable
fun BottomNavigation(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    // Implementation for bottom navigation
    // For screens < 600dp
}
