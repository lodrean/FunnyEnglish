package com.funnyenglish.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.funnyenglish.app.di.appModule
import com.funnyenglish.app.screens.*
import com.funnyenglish.app.theme.FunnyEnglishTheme
import com.funnyenglish.app.viewmodel.*
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        FunnyEnglishTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                AppContent()
            }
        }
    }
}

@Composable
private fun AppContent() {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.state.collectAsState()

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

    when {
        !authState.isLoggedIn && currentScreen !is AppScreen.Register -> {
            LoginScreen(
                state = authState,
                onLogin = { email, password -> authViewModel.login(email, password) },
                onNavigateToRegister = { currentScreen = AppScreen.Register },
                onClearError = { authViewModel.clearError() }
            )
        }
        currentScreen is AppScreen.Register -> {
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
            MainAppContent(
                currentScreen = currentScreen,
                onNavigate = { currentScreen = it }
            )
        }
    }
}

@Composable
private fun MainAppContent(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    val homeViewModel: HomeViewModel = koinViewModel()
    val categoriesViewModel: CategoriesViewModel = koinViewModel()
    val testViewModel: TestViewModel = koinViewModel()
    val leaderboardViewModel: LeaderboardViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()

    when (currentScreen) {
        is AppScreen.Home -> {
            val state by homeViewModel.state.collectAsState()
            HomeScreen(
                state = state,
                onLoadData = { homeViewModel.loadHomeData() },
                onCategoryClick = { id -> onNavigate(AppScreen.CategoryTests(id)) },
                onTestClick = { id -> onNavigate(AppScreen.TestPlay(id)) },
                onViewAllCategories = { onNavigate(AppScreen.Categories) },
                onProfileClick = { onNavigate(AppScreen.Profile) }
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
        else -> {
            // Default to home
            onNavigate(AppScreen.Home)
        }
    }
}

sealed class AppScreen {
    data object Login : AppScreen()
    data object Register : AppScreen()
    data object Home : AppScreen()
    data object Categories : AppScreen()
    data class CategoryTests(val categoryId: String) : AppScreen()
    data class TestPlay(val testId: String) : AppScreen()
    data object Leaderboard : AppScreen()
    data object Profile : AppScreen()
    data object Achievements : AppScreen()
}

@Composable
fun CategoriesScreen(
    state: com.funnyenglish.app.viewmodel.CategoriesState,
    onLoad: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) { onLoad() }
    // Implement categories list
    com.funnyenglish.app.components.LoadingIndicator()
}

@Composable
fun CategoryTestsScreen(
    state: com.funnyenglish.app.viewmodel.CategoryTestsState,
    onLoad: () -> Unit,
    onTestClick: (String) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) { onLoad() }
    com.funnyenglish.app.components.LoadingIndicator()
}

@Composable
fun LeaderboardScreen(
    state: com.funnyenglish.app.viewmodel.LeaderboardState,
    onLoad: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) { onLoad() }
    com.funnyenglish.app.components.LoadingIndicator()
}
