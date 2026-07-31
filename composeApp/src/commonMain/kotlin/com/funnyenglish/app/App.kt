package com.funnyenglish.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.funnyenglish.app.di.appModule
import com.funnyenglish.app.screens.CategoriesScreenContent
import com.funnyenglish.app.screens.CategoryTestsScreenContent
import com.funnyenglish.app.screens.HomeScreen
import com.funnyenglish.app.screens.LeaderboardScreenContent
import com.funnyenglish.app.screens.LoginScreen
import com.funnyenglish.app.screens.MessagesScreen
import com.funnyenglish.app.screens.OnboardingScreen
import com.funnyenglish.app.screens.RegisterScreen
import com.funnyenglish.app.screens.SettingsScreen
import com.funnyenglish.app.screens.SplashScreen
import com.funnyenglish.app.screens.TestPlayScreen
import com.funnyenglish.app.screens.ProfileScreen
import com.funnyenglish.app.screens.AchievementScreen
import com.funnyenglish.app.screens.AdaptiveLessonScreen
import com.funnyenglish.app.screens.LibraryScreen
import com.funnyenglish.app.screens.TopicsScreen
import com.funnyenglish.app.screens.QuestionsScreen
import com.funnyenglish.app.screens.VideoRoute
import com.funnyenglish.app.screens.TrainingRoute
import com.funnyenglish.app.screens.PracticeRoute
import com.funnyenglish.app.screens.MySubmissionsScreen
import com.funnyenglish.app.util.ObserveAsEvents
import com.funnyenglish.screens.GroupsScreen
import com.funnyenglish.screens.GroupDetailScreen
import com.funnyenglish.designsystem.theme.FunnyTheme
import com.funnyenglish.designsystem.layout.MaxContentWidth
import com.funnyenglish.app.viewmodel.*
import com.funnyenglish.app.components.StreakWidget
import com.funnyenglish.app.components.DailyQuestsWidget
import com.funnyenglish.app.components.MergeProgressDialog
import com.funnyenglish.shared.model.AuthMode
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import com.funnyenglish.shared.platform.Settings

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

        FunnyTheme(darkTheme = useDarkTheme) {
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

    // Онбординг первого запуска
    val appSettings = koinInject<Settings>()
    var onboardingCompleted by remember {
        mutableStateOf(appSettings.getString(KEY_ONBOARDING_COMPLETED, null) == "true")
    }

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }

    when (currentScreen) {
        is AppScreen.Splash -> {
            SplashScreen(isLoading = authState.isLoading)
            LaunchedEffect(authState.isLoading, authState.mode) {
                if (!authState.isLoading) {
                    kotlinx.coroutines.delay(1200)
                    currentScreen = when {
                        !onboardingCompleted -> AppScreen.Onboarding
                        authState.mode == AuthMode.AUTHENTICATED || authState.mode == AuthMode.GUEST -> AppScreen.Library
                        else -> AppScreen.Login
                    }
                }
            }
        }
        is AppScreen.Onboarding -> {
            OnboardingScreen(
                onRegister = {
                    appSettings.putString(KEY_ONBOARDING_COMPLETED, "true")
                    onboardingCompleted = true
                    currentScreen = AppScreen.Register
                },
                onContinueAsGuest = {
                    appSettings.putString(KEY_ONBOARDING_COMPLETED, "true")
                    onboardingCompleted = true
                    authViewModel.startGuestSession()
                    // BUG: без явной навигации пользователь оставался на «Как начнём?» —
                    // LaunchedEffect с переходом по mode существует только в ветке Splash.
                    currentScreen = AppScreen.Library
                }
            )
        }
        is AppScreen.Login -> {
            // Грабля «залипший экран»: после успешного логина/гостя mode меняется,
            // но currentScreen остаётся Login — переходим в Library явно.
            // Переход только при СМЕНЕ mode: гость, зашедший на логин (конверсия), не должен отскочить обратно.
            val initialMode = remember { authState.mode }
            LaunchedEffect(authState.mode) {
                if (authState.mode != initialMode &&
                    (authState.mode == AuthMode.AUTHENTICATED || authState.mode == AuthMode.GUEST)
                ) {
                    currentScreen = AppScreen.Library
                }
            }
            LoginScreen(
                state = authState,
                onLogin = { email, password -> authViewModel.login(email, password) },
                onNavigateToRegister = { currentScreen = AppScreen.Register },
                onClearError = { authViewModel.clearError() },
                onContinueAsGuest = { authViewModel.startGuestSession() }
            )
        }
        is AppScreen.Register -> {
            val initialMode = remember { authState.mode }
            LaunchedEffect(authState.mode) {
                if (authState.mode != initialMode && authState.mode == AuthMode.AUTHENTICATED) {
                    currentScreen = AppScreen.Library
                }
            }
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
            val homeViewModel: HomeViewModel = koinViewModel()

            // Reload home data after successful merge
            LaunchedEffect(authState.mergeCompleted) {
                if (authState.mergeCompleted) {
                    homeViewModel.loadHomeData(isGuest = false)
                    authViewModel.onMergeCompletedHandled()
                }
            }

            when (authState.mode) {
                AuthMode.UNKNOWN -> {
                    LoginScreen(
                        state = authState,
                        onLogin = { email, password -> authViewModel.login(email, password) },
                        onNavigateToRegister = { currentScreen = AppScreen.Register },
                        onClearError = { authViewModel.clearError() },
                        onContinueAsGuest = { authViewModel.startGuestSession() }
                    )
                }
                AuthMode.GUEST, AuthMode.AUTHENTICATED -> {
                    MainAppContent(
                        currentScreen = currentScreen,
                        onNavigate = { currentScreen = it },
                        settingsViewModel = settingsViewModel,
                        authMode = authState.mode,
                        homeViewModel = homeViewModel,
                        onLogout = {
                            authViewModel.logout()
                            currentScreen = AppScreen.Login
                        }
                    )

                    if (authState.hasPendingGuestProgress) {
                        MergeProgressDialog(
                            error = authState.error,
                            onMerge = {
                                authViewModel.clearError()
                                authViewModel.mergeGuestProgress()
                            },
                            onDismiss = {
                                authViewModel.clearError()
                                authViewModel.markGuestProgressMerged()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainAppContent(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    settingsViewModel: SettingsViewModel,
    authMode: AuthMode,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit
) {
    val categoriesViewModel: CategoriesViewModel = koinViewModel()
    val testViewModel: TestViewModel = koinViewModel()
    val leaderboardViewModel: LeaderboardViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()
    val messagesViewModel: MessagesViewModel = koinViewModel()

    val showBottomNav = currentScreen is AppScreen.Home ||
        currentScreen is AppScreen.Categories ||
        currentScreen is AppScreen.Groups ||
        currentScreen is AppScreen.Leaderboard ||
        currentScreen is AppScreen.Library ||
        currentScreen is AppScreen.MySubmissions ||
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
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.fillMaxSize().widthIn(max = MaxContentWidth)
            ) {
            when (currentScreen) {
                is AppScreen.Home -> {
                    val state by homeViewModel.state.collectAsState()
                    val streakViewModel: StreakViewModel = koinViewModel()
                    val questsViewModel: QuestsViewModel = koinViewModel()
                    val streakState by streakViewModel.uiState.collectAsState()
                    val questsState by questsViewModel.uiState.collectAsState()
                    val isGuest = authMode == AuthMode.GUEST
                    
                    HomeScreen(
                        state = state,
                        isGuest = isGuest,
                        onLoadData = { homeViewModel.loadHomeData(isGuest) },
                        onCategoryClick = { id -> onNavigate(AppScreen.CategoryTests(id)) },
                        onTestClick = { id -> onNavigate(AppScreen.TestPlay(id)) },
                        onViewAllCategories = { onNavigate(AppScreen.Categories) },
                        onProfileClick = { onNavigate(AppScreen.Profile) },
                        onContinueLearning = {
                            val incompleteTest = state.recentTests.firstOrNull { it.userProgress == null }
                            if (incompleteTest != null) {
                                onNavigate(AppScreen.TestPlay(incompleteTest.id))
                            } else {
                                onNavigate(AppScreen.Categories)
                            }
                        },
                        onAdaptiveLessonClick = {
                            onNavigate(AppScreen.AdaptiveLesson(categoryId = null, durationMinutes = 5))
                        },
                        onStreakClick = { /* TODO: Navigate to streak detail */ },
                        onQuestsClick = { /* TODO: Navigate to quests screen */ },
                        streakState = streakState,
                        questsState = questsState
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
                    val isGuest = authMode == AuthMode.GUEST
                    LaunchedEffect(currentScreen.testId) {
                        testViewModel.loadTest(currentScreen.testId, isGuest)
                    }
                    TestPlayScreen(
                        state = state,
                        isGuest = isGuest,
                        onBack = {
                            testViewModel.resetTest()
                            onNavigate(AppScreen.Home)
                        },
                        onSelectAnswer = testViewModel::selectAnswer,
                        onSetDragDropMatch = testViewModel::setDragDropMatch,
                        onSetImageWordMatch = testViewModel::setImageWordMatch,
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
                        isGuest = authMode == AuthMode.GUEST,
                        onLoad = { leaderboardViewModel.loadLeaderboard() },
                        onBack = { onNavigate(AppScreen.Home) },
                        onRegisterClick = { onNavigate(AppScreen.Register) }
                    )
                }
                is AppScreen.Profile -> {
                    val state by profileViewModel.profileState.collectAsState()
                    val unreadMessages by messagesViewModel.unreadCount.collectAsState()
                    val isGuest = authMode == AuthMode.GUEST
                    LaunchedEffect(isGuest) {
                        if (!isGuest) messagesViewModel.loadUnreadCount()
                    }
                    ProfileScreen(
                        state = state,
                        isGuest = isGuest,
                        onLoad = { profileViewModel.loadProfile() },
                        onBack = { onNavigate(AppScreen.Home) },
                        onSettingsClick = { onNavigate(AppScreen.Settings) },
                        onAchievementsClick = { onNavigate(AppScreen.Achievements) },
                        onMessagesClick = { onNavigate(AppScreen.Messages) },
                        unreadMessages = unreadMessages,
                        onLoginClick = if (isGuest) {
                            { onNavigate(AppScreen.Login) }
                        } else null
                    )
                }
                is AppScreen.Messages -> {
                    val state by messagesViewModel.state.collectAsState()
                    MessagesScreen(
                        state = state,
                        onLoad = { messagesViewModel.loadMessages() },
                        onMarkAsRead = { messagesViewModel.markAsRead(it) },
                        onBack = { onNavigate(AppScreen.Profile) }
                    )
                }
                is AppScreen.Achievements -> {
                    val state by profileViewModel.achievementsState.collectAsState()
                    AchievementScreen(
                        state = state,
                        isGuest = authMode == AuthMode.GUEST,
                        onLoad = { profileViewModel.loadAchievements() },
                        onBack = { onNavigate(AppScreen.Profile) },
                        onRegisterClick = { onNavigate(AppScreen.Register) }
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
                is AppScreen.AdaptiveLesson -> {
                    AdaptiveLessonScreen(
                        categoryId = currentScreen.categoryId,
                        targetDurationMinutes = currentScreen.durationMinutes,
                        isGuest = authMode == AuthMode.GUEST,
                        onLessonComplete = { xpEarned ->
                            // Show completion and navigate back
                            onNavigate(AppScreen.Home)
                        },
                        onLessonExit = {
                            onNavigate(AppScreen.Home)
                        }
                    )
                }
                is AppScreen.Groups -> {
                    GroupsScreen(
                        isGuest = authMode == AuthMode.GUEST,
                        onNavigate = onNavigate,
                        onNavigateToGroupDetail = { groupId ->
                            onNavigate(AppScreen.GroupDetail(groupId))
                        },
                        onNavigateBack = { onNavigate(AppScreen.Home) }
                    )
                }
                is AppScreen.GroupDetail -> {
                    GroupDetailScreen(
                        groupId = currentScreen.groupId,
                        onNavigateBack = { onNavigate(AppScreen.Groups) }
                    )
                }
                // ==================== Speaking-тренажёр ====================
                is AppScreen.Library -> {
                    val vm: LibraryViewModel = koinViewModel()
                    val state by vm.state.collectAsState()
                    ObserveAsEvents(vm.events) { event ->
                        when (event) {
                            is LibraryEvent.NavigateToTopics ->
                                onNavigate(AppScreen.Topics(event.libraryId))
                        }
                    }
                    LibraryScreen(
                        state = state,
                        onLoad = { vm.onAction(LibraryAction.OnRefresh) },
                        onLibraryClick = { id -> vm.onAction(LibraryAction.OnLibraryClick(id)) }
                    )
                }
                is AppScreen.Topics -> {
                    val vm: TopicsViewModel = koinViewModel()
                    val state by vm.state.collectAsState()
                    LaunchedEffect(currentScreen.libraryId) {
                        vm.onAction(TopicsAction.OnLoad(currentScreen.libraryId))
                    }
                    ObserveAsEvents(vm.events) { event ->
                        when (event) {
                            is TopicsEvent.NavigateToVideo ->
                                onNavigate(AppScreen.Video(event.topicId, currentScreen.libraryId, event.withSubtitles))
                            is TopicsEvent.NavigateToQuestions ->
                                onNavigate(AppScreen.Questions(event.topicId, currentScreen.libraryId))
                            is TopicsEvent.NavigateBack -> onNavigate(AppScreen.Library)
                        }
                    }
                    TopicsScreen(
                        state = state,
                        onTopicClick = { id -> vm.onAction(TopicsAction.OnTopicClick(id)) },
                        onSubtitleChoice = { id, withSubs ->
                            vm.onAction(TopicsAction.OnSubtitleChoice(id, withSubs))
                        },
                        onSkipVideo = { id -> vm.onAction(TopicsAction.OnSkipVideo(id)) },
                        onDismissSubtitleChoice = { vm.onAction(TopicsAction.OnDismissSubtitleChoice) },
                        onRetry = { vm.onAction(TopicsAction.OnRefresh) },
                        onBack = { vm.onAction(TopicsAction.OnBack) }
                    )
                }
                is AppScreen.Video -> {
                    VideoRoute(
                        topicId = currentScreen.topicId,
                        withSubtitles = currentScreen.withSubtitles,
                        onNavigateToQuestions = {
                            onNavigate(AppScreen.Questions(currentScreen.topicId, currentScreen.libraryId))
                        },
                        onNavigateBack = { onNavigate(AppScreen.Topics(currentScreen.libraryId)) }
                    )
                }
                is AppScreen.Questions -> {
                    val vm: QuestionsViewModel = koinViewModel()
                    val state by vm.state.collectAsState()
                    val isGuest = authMode == AuthMode.GUEST
                    LaunchedEffect(currentScreen.topicId, isGuest) {
                        vm.onAction(QuestionsAction.OnLoad(currentScreen.topicId, isGuest))
                    }
                    ObserveAsEvents(vm.events) { event ->
                        when (event) {
                            is QuestionsEvent.NavigateToTraining ->
                                onNavigate(AppScreen.Training(event.topicId, currentScreen.libraryId))
                            is QuestionsEvent.NavigateToPractice ->
                                onNavigate(AppScreen.Practice(event.topicId, currentScreen.libraryId))
                            is QuestionsEvent.ShowLoginCta -> onNavigate(AppScreen.Login)
                            is QuestionsEvent.NavigateBack ->
                                onNavigate(AppScreen.Topics(currentScreen.libraryId))
                        }
                    }
                    QuestionsScreen(
                        state = state,
                        onStartTraining = { vm.onAction(QuestionsAction.OnStartTraining) },
                        onStartPractice = { vm.onAction(QuestionsAction.OnStartPractice) },
                        onLoginClick = { onNavigate(AppScreen.Login) },
                        onRetry = { vm.onAction(QuestionsAction.OnLoad(currentScreen.topicId, isGuest)) },
                        onBack = { vm.onAction(QuestionsAction.OnBack) }
                    )
                }
                is AppScreen.Training -> {
                    TrainingRoute(
                        topicId = currentScreen.topicId,
                        onNavigateToPractice = {
                            onNavigate(AppScreen.Practice(currentScreen.topicId, currentScreen.libraryId))
                        },
                        onNavigateToLibrary = { onNavigate(AppScreen.Library) },
                        onNavigateBack = {
                            onNavigate(AppScreen.Questions(currentScreen.topicId, currentScreen.libraryId))
                        }
                    )
                }
                is AppScreen.Practice -> {
                    PracticeRoute(
                        topicId = currentScreen.topicId,
                        onNavigateToMySubmissions = { onNavigate(AppScreen.MySubmissions) },
                        onNavigateToLibrary = { onNavigate(AppScreen.Library) },
                        onNavigateBack = {
                            onNavigate(AppScreen.Questions(currentScreen.topicId, currentScreen.libraryId))
                        }
                    )
                }
                is AppScreen.MySubmissions -> {
                    if (authMode == AuthMode.GUEST) {
                        // Гость: заглушка с CTA логина (спека §7)
                        com.funnyenglish.app.components.LockedFeature(
                            title = "Мои записи доступны после входа",
                            description = "Войдите или зарегистрируйтесь, чтобы отправлять записи учителю и видеть оценки",
                            onRegisterClick = { onNavigate(AppScreen.Register) }
                        )
                    } else {
                        val vm: MySubmissionsViewModel = koinViewModel()
                        val state by vm.state.collectAsState()
                        LaunchedEffect(Unit) {
                            vm.onAction(MySubmissionsAction.OnRefresh)
                        }
                        ObserveAsEvents(vm.events) { event ->
                            when (event) {
                                is MySubmissionsEvent.NavigateBack -> onNavigate(AppScreen.Library)
                                is MySubmissionsEvent.ShowMessage -> { /* snackbar в T12 */ }
                            }
                        }
                        MySubmissionsScreen(
                            state = state,
                            onRefresh = { vm.onAction(MySubmissionsAction.OnRefresh) },
                            onRetryPending = { path ->
                                vm.onAction(MySubmissionsAction.OnRetryPending(path))
                            },
                            onPlayAudio = { url ->
                                vm.onAction(MySubmissionsAction.OnPlayAudio(url))
                            },
                            onStopAudio = { vm.onAction(MySubmissionsAction.OnStopAudio) },
                            onBack = { vm.onAction(MySubmissionsAction.OnBack) }
                        )
                    }
                }
                else -> {
                    // Default: библиотека speaking-тренажёра (пивот навигации)
                    onNavigate(AppScreen.Library)
                }
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
        BottomNavItem(AppScreen.Library, "Библиотека", Icons.AutoMirrored.Filled.MenuBook),
        BottomNavItem(AppScreen.MySubmissions, "Мои записи", Icons.Default.Mic),
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

private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

sealed class AppScreen {
    data object Splash : AppScreen()
    data object Onboarding : AppScreen()
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
    data class AdaptiveLesson(val categoryId: String? = null, val durationMinutes: Int = 5) : AppScreen()
    data object Groups : AppScreen()
    data class GroupDetail(val groupId: String) : AppScreen()
    data object Messages : AppScreen()

    // Speaking-тренажёр (спека Part 2 §1.2).
    // libraryId пробрасывается по цепочке — back stack отсутствует, onBack явный.
    data object Library : AppScreen()
    data class Topics(val libraryId: String) : AppScreen()
    data class Video(val topicId: String, val libraryId: String, val withSubtitles: Boolean) : AppScreen()
    data class Questions(val topicId: String, val libraryId: String) : AppScreen()
    data class Training(val topicId: String, val libraryId: String) : AppScreen()
    data class Practice(val topicId: String, val libraryId: String) : AppScreen()
    data object MySubmissions : AppScreen()
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
    isGuest: Boolean = false,
    onLoad: () -> Unit,
    onBack: () -> Unit,
    onRegisterClick: () -> Unit = {}
) {
    LeaderboardScreenContent(
        state = state,
        isGuest = isGuest,
        onLoad = onLoad,
        onBack = onBack,
        onRegisterClick = onRegisterClick
    )
}
