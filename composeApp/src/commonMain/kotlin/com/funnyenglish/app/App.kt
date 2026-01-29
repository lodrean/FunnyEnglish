package com.funnyenglish.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.funnyenglish.app.di.appModule
import com.funnyenglish.app.screens.CategoriesScreenContent
import com.funnyenglish.app.screens.CategoryTestsScreenContent
import com.funnyenglish.app.screens.HomeScreen
import com.funnyenglish.app.screens.LeaderboardScreenContent
import com.funnyenglish.app.screens.LoginScreen
import com.funnyenglish.app.screens.RegisterScreen
import com.funnyenglish.app.screens.SettingsScreen
import com.funnyenglish.app.screens.SplashScreen
import com.funnyenglish.app.screens.TestPlayScreen
import com.funnyenglish.app.screens.ProfileScreen
import com.funnyenglish.app.screens.AchievementsScreen
import com.funnyenglish.app.theme.FunnyEnglishTheme
import com.funnyenglish.app.viewmodel.*
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        val settingsViewModel: SettingsViewModel = koinViewModel()
        val settingsState by settingsViewModel.state.collectAsState()
        val useDarkTheme = when (settingsState.themeMode) {
            AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            AppThemeMode.DARK -> true
            AppThemeMode.LIGHT -> false
        }

        FunnyEnglishTheme(darkTheme = useDarkTheme) {
            Surface(modifier = Modifier.fillMaxSize()) {
                AppContent(settingsViewModel)
            }
        }
    }
}

@Composable
private fun AppContent(settingsViewModel: SettingsViewModel) {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.state.collectAsState()

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }

    when (currentScreen) {
        is AppScreen.Splash -> {
            SplashScreen(isLoading = authState.isLoading)
            LaunchedEffect(authState.isLoading, authState.isLoggedIn) {
                if (!authState.isLoading) {
                    kotlinx.coroutines.delay(1200)
                    currentScreen = if (authState.isLoggedIn) {
                        AppScreen.Home
                    } else {
                        AppScreen.Login
                    }
                }
            }
        }
        is AppScreen.Register -> {
            RegisterScreen(
                state = authState,
                onRegister = { email, password, name ->
                    authViewModel.register(email, password, name)
                },
                onNavigateToLogin = { currentScreen = AppScreen.Login },
                onClearError = { authViewModel.clearError() }
            )
        }
        else -> {
            if (!authState.isLoggedIn) {
                LoginScreen(
                    state = authState,
                    onLogin = { email, password -> authViewModel.login(email, password) },
                    onNavigateToRegister = { currentScreen = AppScreen.Register },
                    onClearError = { authViewModel.clearError() }
                )
            } else {
                MainAppContent(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it },
                    settingsViewModel = settingsViewModel,
                    onLogout = {
                        authViewModel.logout()
                        currentScreen = AppScreen.Login
                    }
                )
            }
        }
    }
}

@Composable
private fun MainAppContent(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    settingsViewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    val homeViewModel: HomeViewModel = koinViewModel()
    val categoriesViewModel: CategoriesViewModel = koinViewModel()
    val testViewModel: TestViewModel = koinViewModel()
    val leaderboardViewModel: LeaderboardViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()

    val showBottomNav = currentScreen is AppScreen.Home ||
        currentScreen is AppScreen.Categories ||
        currentScreen is AppScreen.Leaderboard ||
        currentScreen is AppScreen.Profile

    androidx.compose.material3.Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = onNavigate
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (currentScreen) {
                is AppScreen.Home -> {
                    val state by homeViewModel.state.collectAsState()
                    HomeScreen(
                        state = state,
                        onLoadData = { homeViewModel.loadHomeData() },
                        onCategoryClick = { id -> onNavigate(AppScreen.CategoryTests(id)) },
                        onTestClick = { id -> onNavigate(AppScreen.TestPlay(id)) },
                        onViewAllCategories = { onNavigate(AppScreen.Categories) },
                        onProfileClick = { onNavigate(AppScreen.Profile) },
                        onContinueLearning = {
                            // Navigate to first incomplete test or categories
                            val incompleteTest = state.recentTests.firstOrNull { it.userProgress == null }
                            if (incompleteTest != null) {
                                onNavigate(AppScreen.TestPlay(incompleteTest.id))
                            } else {
                                onNavigate(AppScreen.Categories)
                            }
                        }
                    )
                }
                is AppScreen.Categories -> {
                    val state by categoriesViewModel.categoriesState.collectAsState()
                    CategoriesScreen(
                        state = state,
                        onLoad = { categoriesViewModel.loadCategories() },
                        onCategoryClick = { id -> onNavigate(AppScreen.CategoryTests(id)) },
                        onBack = { onNavigate(AppScreen.Home) }
                    )
                }
                is AppScreen.CategoryTests -> {
                    val state by categoriesViewModel.categoryTestsState.collectAsState()
                    CategoryTestsScreen(
                        state = state,
                        onLoad = { categoriesViewModel.loadCategoryTests(currentScreen.categoryId) },
                        onTestClick = { id -> onNavigate(AppScreen.TestPlay(id)) },
                        onBack = { onNavigate(AppScreen.Categories) }
                    )
                }
                is AppScreen.TestPlay -> {
                    val state by testViewModel.state.collectAsState()
                    LaunchedEffect(currentScreen.testId) {
                        testViewModel.loadTest(currentScreen.testId)
                    }
                    TestPlayScreen(
                        state = state,
                        onBack = {
                            testViewModel.resetTest()
                            onNavigate(AppScreen.Home)
                        },
                        onSelectAnswer = testViewModel::selectAnswer,
                        onSetDragDropMatch = testViewModel::setDragDropMatch,
                        onNextQuestion = testViewModel::goToNextQuestion,
                        onPreviousQuestion = testViewModel::goToPreviousQuestion,
                        onGoToQuestion = testViewModel::goToQuestion,
                        onSubmit = testViewModel::submitTest,
                        onShowResult = {
                            testViewModel.resetTest()
                            onNavigate(AppScreen.Home)
                        }
                    )
                }
                is AppScreen.Leaderboard -> {
                    val state by leaderboardViewModel.state.collectAsState()
                    LeaderboardScreen(
                        state = state,
                        onLoad = { leaderboardViewModel.loadLeaderboard() },
                        onBack = { onNavigate(AppScreen.Home) }
                    )
                }
                is AppScreen.Profile -> {
                    val state by profileViewModel.profileState.collectAsState()
                    ProfileScreen(
                        state = state,
                        onLoad = { profileViewModel.loadProfile() },
                        onBack = { onNavigate(AppScreen.Home) },
                        onSettingsClick = { onNavigate(AppScreen.Settings) },
                        onAchievementsClick = { onNavigate(AppScreen.Achievements) }
                    )
                }
                is AppScreen.Achievements -> {
                    val state by profileViewModel.achievementsState.collectAsState()
                    AchievementsScreen(
                        state = state,
                        onLoad = { profileViewModel.loadAchievements() },
                        onBack = { onNavigate(AppScreen.Profile) }
                    )
                }
                is AppScreen.Settings -> {
                    val state by settingsViewModel.state.collectAsState()
                    SettingsScreen(
                        state = state,
                        onBack = { onNavigate(AppScreen.Profile) },
                        onToggleNotifications = settingsViewModel::setNotificationsEnabled,
                        onToggleSound = settingsViewModel::setSoundEnabled,
                        onToggleHaptics = settingsViewModel::setHapticsEnabled,
                        onToggleAutoPlay = settingsViewModel::setAutoPlayAudio,
                        onLanguageSelected = settingsViewModel::setLanguage,
                        onThemeSelected = settingsViewModel::setThemeMode,
                        onLogout = onLogout
                    )
                }
                else -> {
                    // Default to home
                    onNavigate(AppScreen.Home)
                }
            }
        }
    }
}

private data class BottomNavItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector
)

@Composable
private fun BottomNavigationBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    val items = listOf(
        BottomNavItem(AppScreen.Home, "Главная", Icons.Default.Home),
        BottomNavItem(AppScreen.Categories, "Категории", Icons.Default.Category),
        BottomNavItem(AppScreen.Leaderboard, "Рейтинг", Icons.Default.EmojiEvents),
        BottomNavItem(AppScreen.Profile, "Профиль", Icons.Default.Person)
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

sealed class AppScreen {
    data object Splash : AppScreen()
    data object Login : AppScreen()
    data object Register : AppScreen()
    data object Home : AppScreen()
    data object Categories : AppScreen()
    data class CategoryTests(val categoryId: String) : AppScreen()
    data class TestPlay(val testId: String) : AppScreen()
    data object Leaderboard : AppScreen()
    data object Profile : AppScreen()
    data object Achievements : AppScreen()
    data object Settings : AppScreen()
}

@Composable
fun CategoriesScreen(
    state: com.funnyenglish.app.viewmodel.CategoriesState,
    onLoad: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onBack: () -> Unit
) {
    CategoriesScreenContent(
        state = state,
        onLoad = onLoad,
        onCategoryClick = onCategoryClick,
        onBack = onBack
    )
}

@Composable
fun CategoryTestsScreen(
    state: com.funnyenglish.app.viewmodel.CategoryTestsState,
    onLoad: () -> Unit,
    onTestClick: (String) -> Unit,
    onBack: () -> Unit
) {
    CategoryTestsScreenContent(
        state = state,
        onLoad = onLoad,
        onTestClick = onTestClick,
        onBack = onBack
    )
}

@Composable
fun LeaderboardScreen(
    state: com.funnyenglish.app.viewmodel.LeaderboardState,
    onLoad: () -> Unit,
    onBack: () -> Unit
) {
    LeaderboardScreenContent(
        state = state,
        onLoad = onLoad,
        onBack = onBack
    )
}
