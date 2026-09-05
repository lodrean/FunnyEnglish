package com.sotospeak.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.sotospeak.app.components.PlatformBackHandler
import com.sotospeak.app.di.appModule
import com.sotospeak.app.localization.LocalAppStrings
import com.sotospeak.app.localization.Strings
import com.sotospeak.app.navigation.AppBackStack
import com.sotospeak.app.navigation.AppScaffold
import com.sotospeak.app.navigation.AppScreen
import com.sotospeak.app.navigation.rememberScreenTransition
import com.sotospeak.app.screens.LoginScreen
import com.sotospeak.app.screens.OnboardingScreen
import com.sotospeak.app.screens.RegisterScreen
import com.sotospeak.app.screens.SplashScreen
import com.sotospeak.app.viewmodel.AppThemeMode
import com.sotospeak.app.viewmodel.AuthViewModel
import com.sotospeak.app.viewmodel.SettingsViewModel
import com.sotospeak.app.components.MergeProgressDialog
import com.sotospeak.designsystem.theme.ApplySystemBarStyle
import com.sotospeak.designsystem.theme.FunnyTheme
import com.sotospeak.shared.contracts.AuthMode
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
    val recordingStore: com.sotospeak.app.storage.RecordingStore = koinInject()
    val logScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        // Чистка записей Speaking Trainer (bd 5tf.7): сиротские файлы/метаданные,
        // TRAINING-записи старше TTL. Pending PRACTICE (offline-retry) не трогается.
        recordingStore.prune()
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

    CompositionLocalProvider(LocalAppStrings provides Strings.get(settingsState.language)) {
        FunnyTheme(darkTheme = useDarkTheme) {
            Surface(modifier = Modifier.fillMaxSize()) {
                AppContent(settingsViewModel, useDarkTheme = useDarkTheme)
            }
        }
    }
}

// Guest-first (Playful Coach v1.1): возвращающийся пользователь без сессии сразу
// попадает в библиотеку как гость; логин показывается только при входе в
// авторизованную зону (Practice-гейт, профиль).
// Вынесено из suspend-лямбды LaunchedEffect: `when` по sealed-типам внутри
// coroutine state machine генерировал невалидный wasm (CompileError при
// instantiateStreaming, bd qbq.8) — обходим баг кодогена Kotlin/Wasm 2.1.0.
private fun resolveSplashTarget(
    onboardingCompleted: Boolean,
    mode: AuthMode,
    startGuest: () -> Unit
): AppScreen = when {
    !onboardingCompleted -> AppScreen.Onboarding
    mode == AuthMode.AUTHENTICATED || mode == AuthMode.GUEST -> AppScreen.Library
    else -> {
        // startGuestSession() обновляет mode синхронно — мигания Login нет.
        startGuest()
        AppScreen.Library
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

    val backStack = rememberSaveable(saver = AppBackStack.Saver) { AppBackStack(AppScreen.Splash) }

    // App-level системный «назад» (bd 5tf.3): экраны со своим PlatformBackHandler
    // (Topics/Video/Questions/Training/Practice) composируются позже и перехватывают
    // событие первыми; сюда событие доходит только с экранов без собственного back.
    PlatformBackHandler(enabled = backStack.canGoBack) { backStack.pop() }

    AnimatedContent(
        targetState = backStack.current,
        transitionSpec = rememberScreenTransition(),
        label = "app_screen_transition"
    ) { screen ->
    when (screen) {
        is AppScreen.Splash -> {
            SplashScreen(isLoading = authState.isLoading, isDarkTheme = useDarkTheme)
            LaunchedEffect(authState.isLoading, authState.mode) {
                if (!authState.isLoading) {
                    kotlinx.coroutines.delay(1200)
                    // reset: из Splash «назад» не возвращается никуда
                    val target = resolveSplashTarget(
                        onboardingCompleted = onboardingCompleted,
                        mode = authState.mode,
                        startGuest = { authViewModel.startGuestSession() }
                    )
                    backStack.reset(target)
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
                    backStack.reset(AppScreen.Library)
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
                    backStack.reset(AppScreen.Library)
                }
            }
            LoginScreen(
                state = authState,
                onLogin = { email, password -> authViewModel.login(email, password) },
                onNavigateToRegister = { backStack.push(AppScreen.Register) },
                onClearError = { authViewModel.clearError() },
                onContinueAsGuest = {
                    authViewModel.startGuestSession()
                    backStack.reset(AppScreen.Library)
                },
                onResendVerification = { email -> authViewModel.resendVerificationEmail(email) }
            )
        }
        is AppScreen.Register -> {
            val initialMode = remember { authState.mode }
            LaunchedEffect(authState.mode) {
                if (authState.mode != initialMode && authState.mode == AuthMode.AUTHENTICATED) {
                    backStack.reset(AppScreen.Library)
                }
            }
            RegisterScreen(
                state = authState,
                onRegister = { email, password, name ->
                    authViewModel.register(email, password, name)
                },
                onNavigateToLogin = { backStack.popOrPush(AppScreen.Login) },
                onClearError = { authViewModel.clearError() },
                onResendVerification = { email -> authViewModel.resendVerificationEmail(email) }
            )
        }
        else -> {
            when (authState.mode) {
                AuthMode.UNKNOWN -> {
                    if (authState.isLoading) {
                        // Процессная смерть на глубоком экране: стек восстановился,
                        // а сессия ещё инициализируется — показываем Splash, не логин.
                        SplashScreen(isLoading = true, isDarkTheme = useDarkTheme)
                    } else {
                        LoginScreen(
                            state = authState,
                            onLogin = { email, password -> authViewModel.login(email, password) },
                            onNavigateToRegister = { backStack.push(AppScreen.Register) },
                            onClearError = { authViewModel.clearError() },
                            onContinueAsGuest = {
                                authViewModel.startGuestSession()
                                backStack.reset(AppScreen.Library)
                            }
                        )
                    }
                }
                AuthMode.GUEST, AuthMode.AUTHENTICATED -> {
                    AppScaffold(
                        currentScreen = screen,
                        authMode = authState.mode,
                        settingsViewModel = settingsViewModel,
                        onNavigate = { backStack.push(it) },
                        onBack = { backStack.pop() },
                        onLogout = {
                            authViewModel.logout()
                            backStack.reset(AppScreen.Login)
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

private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
