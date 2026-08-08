package com.sotospeak.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.sotospeak.app.di.appModule
import com.sotospeak.app.screens.LoginScreen
import com.sotospeak.app.screens.MessagesScreen
import com.sotospeak.app.screens.OnboardingScreen
import com.sotospeak.app.screens.RegisterScreen
import com.sotospeak.app.screens.SettingsScreen
import com.sotospeak.app.screens.SplashScreen
import com.sotospeak.app.screens.ProfileScreen
import com.sotospeak.app.screens.LibraryScreen
import com.sotospeak.app.screens.TopicsScreen
import com.sotospeak.app.screens.QuestionsScreen
import com.sotospeak.app.screens.VideoRoute
import com.sotospeak.app.screens.TrainingRoute
import com.sotospeak.app.screens.PracticeRoute
import com.sotospeak.app.screens.MySubmissionsScreen
import com.sotospeak.app.util.ObserveAsEvents
import com.sotospeak.designsystem.theme.ApplySystemBarStyle
import com.sotospeak.designsystem.theme.FunnyTheme
import com.sotospeak.designsystem.layout.MaxContentWidth
import com.sotospeak.app.viewmodel.*
import com.sotospeak.app.components.MergeProgressDialog
import com.sotospeak.shared.model.AuthMode
import org.koin.compose.KoinApplication
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext
import com.sotospeak.shared.platform.Settings
import kotlinx.coroutines.launch

@Composable
fun App() {
    // Грабля «KoinApplicationAlreadyStartedException»: процесс Android переживает
    // Activity, koin-compose 4.0 не всегда успевает stopKoin — повторная композиция
    // падает на старте. Если Koin уже жив — используем текущий контекст.
    if (GlobalContext.getOrNull() != null) {
        KoinContext { AppThemedContent() }
    } else {
        KoinApplication(application = {
            modules(appModule)
        }) {
            AppThemedContent()
        }
    }
}

@Composable
private fun AppThemedContent() {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settingsState by settingsViewModel.state.collectAsState()
    val useDarkTheme = when (settingsState.themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    ApplySystemBarStyle(darkTheme = useDarkTheme)

    // Клиентские логи WARN+ → backend (OpenSpec add-client-logging):
    // подключаем remote-очередь к логгеру и отправляем накопленное при старте
    val clientLogQueue: com.sotospeak.shared.util.ClientLogQueue = koinInject()
    val logUploader: com.sotospeak.app.util.LogUploader = koinInject()
    val appConfig: com.sotospeak.app.di.AppConfig = koinInject()
    val guestProgressRepository: com.sotospeak.shared.repository.GuestProgressRepository = koinInject()
    val logScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        com.sotospeak.shared.util.Logger.remoteQueue = clientLogQueue
        com.sotospeak.shared.util.Logger.remoteMeta = {
            com.sotospeak.shared.util.Logger.RemoteMeta(
                appVersion = appConfig.appVersion,
                anonymousId = guestProgressRepository.getAnonymousId()
            )
        }
        com.sotospeak.shared.util.Logger.onRemoteEnqueued = {
            logScope.launch { logUploader.flush() }
        }
        logUploader.flush()
    }

    FunnyTheme(darkTheme = useDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppContent(settingsViewModel, useDarkTheme = useDarkTheme)
        }
    }
}

@Composable
private fun AppContent(settingsViewModel: SettingsViewModel, useDarkTheme: Boolean) {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.state.collectAsState()

    // Онбординг первого запуска
    val appSettings = koinInject<Settings>()
    var onboardingCompleted by remember {
        mutableStateOf(appSettings.getString(KEY_ONBOARDING_COMPLETED, null) == "true")
    }

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }

    // M3 Emphasized-переходы между экранами верхнего уровня (спека §2 motion);
    // при Reduce motion — мгновенная смена без анимации
    val reduceMotion = com.sotospeak.designsystem.accessibility.LocalReduceMotion.current
    val screenTransition: androidx.compose.animation.AnimatedContentTransitionScope<AppScreen>.() -> androidx.compose.animation.ContentTransform = {
        if (reduceMotion) {
            androidx.compose.animation.ContentTransform(
                androidx.compose.animation.EnterTransition.None,
                androidx.compose.animation.ExitTransition.None
            )
        } else {
            androidx.compose.animation.fadeIn(
                androidx.compose.animation.core.tween(300, easing = com.sotospeak.designsystem.theme.SpeakingMotion.EasingM3Emphasized)
            ) + androidx.compose.animation.scaleIn(
                initialScale = 0.96f,
                animationSpec = androidx.compose.animation.core.tween(300, easing = com.sotospeak.designsystem.theme.SpeakingMotion.EasingM3Emphasized)
            ) togetherWith androidx.compose.animation.fadeOut(
                androidx.compose.animation.core.tween(200, easing = com.sotospeak.designsystem.theme.SpeakingMotion.EasingM3Standard)
            )
        }
    }

    androidx.compose.animation.AnimatedContent(
        targetState = currentScreen,
        transitionSpec = screenTransition,
        label = "app_screen_transition"
    ) { screen ->
    when (screen) {
        is AppScreen.Splash -> {
            SplashScreen(isLoading = authState.isLoading, isDarkTheme = useDarkTheme)
            LaunchedEffect(authState.isLoading, authState.mode) {
                if (!authState.isLoading) {
                    kotlinx.coroutines.delay(1200)
                    currentScreen = when {
                        !onboardingCompleted -> AppScreen.Onboarding
                        authState.mode == AuthMode.AUTHENTICATED || authState.mode == AuthMode.GUEST -> AppScreen.Library
                        else -> {
                            // Guest-first (Playful Coach v1.1): возвращающийся пользователь без
                            // сессии сразу попадает в библиотеку как гость; логин показывается
                            // только при входе в авторизованную зону (Practice-гейт, профиль).
                            // startGuestSession() обновляет mode синхронно — мигания Login нет.
                            authViewModel.startGuestSession()
                            AppScreen.Library
                        }
                    }
                }
            }
        }
        is AppScreen.Onboarding -> {
            // Регистрации на онбординге нет (frame-onboarding): «Начать» → гость в библиотеку
            OnboardingScreen(
                onFinish = {
                    appSettings.putString(KEY_ONBOARDING_COMPLETED, "true")
                    onboardingCompleted = true
                    authViewModel.startGuestSession()
                    // Без явной навигации пользователь остался бы на онбординге —
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
                onContinueAsGuest = {
                    authViewModel.startGuestSession()
                    currentScreen = AppScreen.Library
                },
                onResendVerification = { email -> authViewModel.resendVerificationEmail(email) }
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
                onClearError = { authViewModel.clearError() },
                onResendVerification = { email -> authViewModel.resendVerificationEmail(email) }
            )
        }
        else -> {
            when (authState.mode) {
                AuthMode.UNKNOWN -> {
                    LoginScreen(
                        state = authState,
                        onLogin = { email, password -> authViewModel.login(email, password) },
                        onNavigateToRegister = { currentScreen = AppScreen.Register },
                        onClearError = { authViewModel.clearError() },
                        onContinueAsGuest = {
                            authViewModel.startGuestSession()
                            currentScreen = AppScreen.Library
                        }
                    )
                }
                AuthMode.GUEST, AuthMode.AUTHENTICATED -> {
                    MainAppContent(
                        currentScreen = screen,
                        onNavigate = { currentScreen = it },
                        settingsViewModel = settingsViewModel,
                        authMode = authState.mode,
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
}

@Composable
private fun MainAppContent(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    settingsViewModel: SettingsViewModel,
    authMode: AuthMode,
    onLogout: () -> Unit
) {
    val settingsState by settingsViewModel.state.collectAsState()
    val profileViewModel: ProfileViewModel = koinViewModel()
    val messagesViewModel: MessagesViewModel = koinViewModel()

    val showBottomNav = currentScreen is AppScreen.Library ||
        currentScreen is AppScreen.MySubmissions ||
        currentScreen is AppScreen.Profile

    // M3-адаптивность (спека §5, Q4): compact → NavigationBar, medium/expanded → NavigationRail
    androidx.compose.foundation.layout.BoxWithConstraints {
        val useRail = showBottomNav &&
            com.sotospeak.designsystem.layout.calculateWindowWidthSizeClass(maxWidth) !=
            com.sotospeak.designsystem.layout.WindowWidthSizeClass.COMPACT

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail) {
                SpeakingNavigationRail(
                    currentScreen = currentScreen,
                    onNavigate = onNavigate
                )
            }
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    if (showBottomNav && !useRail) {
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
            // M3 Emphasized-переходы между экранами внутри основного контента
            val reduceMotion = com.sotospeak.designsystem.accessibility.LocalReduceMotion.current
            val innerTransition: androidx.compose.animation.AnimatedContentTransitionScope<AppScreen>.() -> androidx.compose.animation.ContentTransform = {
                if (reduceMotion) {
                    androidx.compose.animation.ContentTransform(
                        androidx.compose.animation.EnterTransition.None,
                        androidx.compose.animation.ExitTransition.None
                    )
                } else {
                    androidx.compose.animation.fadeIn(
                        androidx.compose.animation.core.tween(300, easing = com.sotospeak.designsystem.theme.SpeakingMotion.EasingM3Emphasized)
                    ) + androidx.compose.animation.scaleIn(
                        initialScale = 0.96f,
                        animationSpec = androidx.compose.animation.core.tween(300, easing = com.sotospeak.designsystem.theme.SpeakingMotion.EasingM3Emphasized)
                    ) togetherWith androidx.compose.animation.fadeOut(
                        androidx.compose.animation.core.tween(200, easing = com.sotospeak.designsystem.theme.SpeakingMotion.EasingM3Standard)
                    )
                }
            }
            androidx.compose.animation.AnimatedContent(
                targetState = currentScreen,
                transitionSpec = innerTransition,
                label = "main_screen_transition"
            ) { currentScreen ->
            when (currentScreen) {
                is AppScreen.Profile -> {
                    val state by profileViewModel.profileState.collectAsState()
                    val isGuest = authMode == AuthMode.GUEST
                    // Статистика stat-карточек — из существующего MySubmissionsViewModel
                    val submissionsVm: MySubmissionsViewModel = koinViewModel()
                    val submissionsState by submissionsVm.state.collectAsState()
                    // debug-меню (qa/debug): вход — 7 тапов по версии внизу профиля
                    val appConfig: com.sotospeak.app.di.AppConfig = koinInject()
                    LaunchedEffect(isGuest) {
                        if (!isGuest) submissionsVm.onAction(MySubmissionsAction.OnRefresh)
                    }
                    ProfileScreen(
                        state = state,
                        isGuest = isGuest,
                        submissionsCount = submissionsState.submissions.size,
                        topicsCompleted = submissionsState.submissions.map { it.topicId }.distinct().size,
                        themeMode = settingsState.themeMode,
                        onThemeSelected = settingsViewModel::setThemeMode,
                        onLoad = { profileViewModel.loadProfile() },
                        onLogout = onLogout,
                        onRegisterClick = { onNavigate(AppScreen.Register) },
                        onLoginClick = if (isGuest) {
                            { onNavigate(AppScreen.Login) }
                        } else null,
                        versionLabel = if (appConfig.debugToolsEnabled) {
                            "So to speak v${appConfig.appVersion}"
                        } else null,
                        onDebugMenuOpen = { onNavigate(AppScreen.DebugMenu) }
                    )
                }
                is AppScreen.DebugMenu -> {
                    val appConfig: com.sotospeak.app.di.AppConfig = koinInject()
                    val logUploader: com.sotospeak.app.util.LogUploader = koinInject()
                    com.sotospeak.app.screens.DebugMenuScreen(
                        appConfig = appConfig,
                        logUploader = logUploader,
                        onBack = { onNavigate(AppScreen.Profile) }
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
                            is QuestionsEvent.NavigateToMySubmissions ->
                                onNavigate(AppScreen.MySubmissions)
                            is QuestionsEvent.NavigateBack ->
                                onNavigate(AppScreen.Topics(currentScreen.libraryId))
                        }
                    }
                    QuestionsScreen(
                        state = state,
                        onStartTraining = { vm.onAction(QuestionsAction.OnStartTraining) },
                        onStartPractice = { vm.onAction(QuestionsAction.OnStartPractice) },
                        onLoginClick = { onNavigate(AppScreen.Login) },
                        onRegisterClick = { onNavigate(AppScreen.Register) },
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
                        com.sotospeak.app.components.LockedFeature(
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
        }
    }
}

private data class BottomNavItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector
)

// Лейблы и иконки по мокапу Playful Coach v1.1 (bottomnav: home/send/user, аудит 2026-08-01)
private val mainNavItems = listOf(
    BottomNavItem(AppScreen.Library, "Темы", com.sotospeak.design.icons.SpeakingIcons.Home),
    BottomNavItem(AppScreen.MySubmissions, "Отправки", com.sotospeak.design.icons.SpeakingIcons.Send),
    BottomNavItem(AppScreen.Profile, "Профиль", com.sotospeak.design.icons.SpeakingIcons.User)
)

@Composable
internal fun BottomNavigationBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    // M3-дефолты (DSM-5 §4 «Навигация»): container surfaceContainer, pill-индикатор
    // primaryContainer, selected icon onPrimaryContainer, текст активного onSurface,
    // неактивные — onSurfaceVariant
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        mainNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

/** M3 NavigationRail для wide/desktop (medium/expanded) — те же пункты, что у bottom nav */
@Composable
private fun SpeakingNavigationRail(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Spacer(modifier = Modifier.weight(1f))
        mainNavItems.forEach { item ->
            NavigationRailItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

sealed class AppScreen {
    data object Splash : AppScreen()
    data object Onboarding : AppScreen()
    data object Login : AppScreen()
    data object Register : AppScreen()
    data object Profile : AppScreen()
    data object Settings : AppScreen()
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

    /** Скрытое debug-меню (QA/debug-сборки): вход — 7 тапов по версии в профиле */
    data object DebugMenu : AppScreen()
}
