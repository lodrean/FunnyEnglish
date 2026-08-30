package com.sotospeak.app.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sotospeak.app.components.LockedFeature
import com.sotospeak.app.screens.DebugMenuScreen
import com.sotospeak.app.screens.LibraryScreen
import com.sotospeak.app.screens.MessagesScreen
import com.sotospeak.app.screens.MySubmissionsScreen
import com.sotospeak.app.screens.PracticeRoute
import com.sotospeak.app.screens.ProfileScreen
import com.sotospeak.app.screens.QuestionsScreen
import com.sotospeak.app.screens.SettingsScreen
import com.sotospeak.app.screens.TopicsScreen
import com.sotospeak.app.screens.TrainingRoute
import com.sotospeak.app.screens.VideoRoute
import com.sotospeak.app.util.ObserveAsEvents
import com.sotospeak.app.util.routeViewModel
import com.sotospeak.app.viewmodel.LibraryAction
import com.sotospeak.app.viewmodel.LibraryEvent
import com.sotospeak.app.viewmodel.LibraryViewModel
import com.sotospeak.app.viewmodel.MessagesViewModel
import com.sotospeak.app.viewmodel.MySubmissionsAction
import com.sotospeak.app.viewmodel.MySubmissionsEvent
import com.sotospeak.app.viewmodel.MySubmissionsViewModel
import com.sotospeak.app.viewmodel.ProfileViewModel
import com.sotospeak.app.viewmodel.QuestionsAction
import com.sotospeak.app.viewmodel.QuestionsEvent
import com.sotospeak.app.viewmodel.QuestionsViewModel
import com.sotospeak.app.viewmodel.SettingsViewModel
import com.sotospeak.app.viewmodel.TopicsAction
import com.sotospeak.app.viewmodel.TopicsEvent
import com.sotospeak.app.viewmodel.TopicsViewModel
import com.sotospeak.shared.contracts.AuthMode
import org.koin.compose.koinInject

/**
 * Диспетчер контентных экранов внутри основного Scaffold (AppScaffold):
 * AnimatedContent с M3-переходом + when по AppScreen.
 * Экраны верхнего уровня флоу (Splash/Onboarding/Login/Register) живут в App.kt.
 */
@Composable
fun MainNavHost(
    currentScreen: AppScreen,
    authMode: AuthMode,
    settingsViewModel: SettingsViewModel,
    onNavigate: (AppScreen) -> Unit,
    onLogout: () -> Unit
) {
    val settingsState by settingsViewModel.state.collectAsState()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = rememberScreenTransition(),
        label = "main_screen_transition"
    ) { screen ->
        when (screen) {
            is AppScreen.Profile -> {
                // VM со скоупом маршрута (К3): создаются при входе, очищаются при уходе
                val profileViewModel: ProfileViewModel = routeViewModel(screen)
                val state by profileViewModel.profileState.collectAsState()
                val isGuest = authMode == AuthMode.GUEST
                // Статистика stat-карточек — из существующего MySubmissionsViewModel
                val submissionsVm: MySubmissionsViewModel = routeViewModel(screen)
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
                DebugMenuScreen(
                    appConfig = appConfig,
                    logUploader = logUploader,
                    onBack = { onNavigate(AppScreen.Profile) }
                )
            }
            is AppScreen.Messages -> {
                val messagesViewModel: MessagesViewModel = routeViewModel(screen)
                val state by messagesViewModel.state.collectAsState()
                MessagesScreen(
                    state = state,
                    onLoad = { messagesViewModel.loadMessages() },
                    onMarkAsRead = { messagesViewModel.markAsRead(it) },
                    onBack = { onNavigate(AppScreen.Profile) }
                )
            }
            is AppScreen.Settings -> {
                SettingsScreen(
                    state = settingsState,
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
                val vm: LibraryViewModel = routeViewModel(screen)
                val state by vm.state.collectAsState()
                ObserveAsEvents(vm.events) { event ->
                    when (event) {
                        is LibraryEvent.NavigateToTopics ->
                            onNavigate(AppScreen.Topics(event.libraryId, event.libraryTitle))
                    }
                }
                LibraryScreen(
                    state = state,
                    onLoad = { vm.onAction(LibraryAction.OnRefresh) },
                    onLibraryClick = { id -> vm.onAction(LibraryAction.OnLibraryClick(id)) }
                )
            }
            is AppScreen.Topics -> {
                val vm: TopicsViewModel = routeViewModel(screen)
                val state by vm.state.collectAsState()
                LaunchedEffect(screen.libraryId) {
                    vm.onAction(TopicsAction.OnLoad(screen.libraryId))
                }
                ObserveAsEvents(vm.events) { event ->
                    when (event) {
                        is TopicsEvent.NavigateToVideo ->
                            onNavigate(
                                AppScreen.Video(
                                    event.topicId,
                                    screen.libraryId,
                                    event.withSubtitles,
                                    screen.libraryTitle
                                )
                            )
                        is TopicsEvent.NavigateToQuestions ->
                            onNavigate(
                                AppScreen.Questions(
                                    event.topicId,
                                    screen.libraryId,
                                    screen.libraryTitle
                                )
                            )
                        is TopicsEvent.NavigateBack -> onNavigate(AppScreen.Library)
                    }
                }
                TopicsScreen(
                    state = state,
                    libraryTitle = screen.libraryTitle,
                    onTopicClick = { id -> vm.onAction(TopicsAction.OnTopicClick(id)) },
                    onRetry = { vm.onAction(TopicsAction.OnRefresh) },
                    onBack = { vm.onAction(TopicsAction.OnBack) }
                )
            }
            is AppScreen.Video -> {
                VideoRoute(
                    topicId = screen.topicId,
                    withSubtitles = screen.withSubtitles,
                    libraryTitle = screen.libraryTitle,
                    onNavigateToQuestions = {
                        onNavigate(
                            AppScreen.Questions(
                                screen.topicId,
                                screen.libraryId,
                                screen.libraryTitle
                            )
                        )
                    },
                    onNavigateBack = {
                        onNavigate(AppScreen.Topics(screen.libraryId, screen.libraryTitle))
                    }
                )
            }
            is AppScreen.Questions -> {
                val vm: QuestionsViewModel = routeViewModel(screen)
                val state by vm.state.collectAsState()
                val isGuest = authMode == AuthMode.GUEST
                LaunchedEffect(screen.topicId, isGuest) {
                    vm.onAction(QuestionsAction.OnLoad(screen.topicId, isGuest))
                }
                ObserveAsEvents(vm.events) { event ->
                    when (event) {
                        is QuestionsEvent.NavigateToTraining ->
                            onNavigate(
                                AppScreen.Training(
                                    event.topicId,
                                    screen.libraryId,
                                    screen.libraryTitle
                                )
                            )
                        is QuestionsEvent.NavigateToPractice ->
                            onNavigate(
                                AppScreen.Practice(
                                    event.topicId,
                                    screen.libraryId,
                                    screen.libraryTitle
                                )
                            )
                        is QuestionsEvent.ShowLoginCta -> onNavigate(AppScreen.Login)
                        is QuestionsEvent.NavigateToMySubmissions ->
                            onNavigate(AppScreen.MySubmissions)
                        is QuestionsEvent.NavigateBack ->
                            onNavigate(AppScreen.Topics(screen.libraryId, screen.libraryTitle))
                    }
                }
                QuestionsScreen(
                    state = state,
                    libraryTitle = screen.libraryTitle,
                    onStartTraining = { vm.onAction(QuestionsAction.OnStartTraining) },
                    onStartPractice = { vm.onAction(QuestionsAction.OnStartPractice) },
                    onLoginClick = { onNavigate(AppScreen.Login) },
                    onRegisterClick = { onNavigate(AppScreen.Register) },
                    onRetry = { vm.onAction(QuestionsAction.OnLoad(screen.topicId, isGuest)) },
                    onBack = { vm.onAction(QuestionsAction.OnBack) }
                )
            }
            is AppScreen.Training -> {
                TrainingRoute(
                    topicId = screen.topicId,
                    libraryTitle = screen.libraryTitle,
                    onNavigateToPractice = {
                        onNavigate(
                            AppScreen.Practice(
                                screen.topicId,
                                screen.libraryId,
                                screen.libraryTitle
                            )
                        )
                    },
                    onNavigateToLibrary = { onNavigate(AppScreen.Library) },
                    onNavigateBack = {
                        onNavigate(
                            AppScreen.Questions(
                                screen.topicId,
                                screen.libraryId,
                                screen.libraryTitle
                            )
                        )
                    }
                )
            }
            is AppScreen.Practice -> {
                PracticeRoute(
                    topicId = screen.topicId,
                    libraryTitle = screen.libraryTitle,
                    onNavigateToMySubmissions = { onNavigate(AppScreen.MySubmissions) },
                    onNavigateToLibrary = { onNavigate(AppScreen.Library) },
                    onNavigateBack = {
                        onNavigate(
                            AppScreen.Questions(
                                screen.topicId,
                                screen.libraryId,
                                screen.libraryTitle
                            )
                        )
                    }
                )
            }
            is AppScreen.MySubmissions -> {
                if (authMode == AuthMode.GUEST) {
                    // Гость: заглушка с CTA логина (спека §7)
                    LockedFeature(
                        title = "Отправки доступны после входа",
                        description = "Войдите или зарегистрируйтесь, чтобы отправлять записи учителю и видеть оценки",
                        onRegisterClick = { onNavigate(AppScreen.Register) }
                    )
                } else {
                    val vm: MySubmissionsViewModel = routeViewModel(screen)
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
                        onStopAudio = { vm.onAction(MySubmissionsAction.OnStopAudio) }
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
