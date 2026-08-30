package com.sotospeak.app.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.dropbox.dropshots.Dropshots
import com.sotospeak.app.recorder.MicPermissionState
import com.sotospeak.app.recorder.VoiceRecorderState
import com.sotospeak.app.screens.LibraryScreen
import com.sotospeak.app.screens.LoginScreen
import com.sotospeak.app.screens.MySubmissionsScreen
import com.sotospeak.app.screens.OnboardingScreen
import com.sotospeak.app.screens.PracticeScreen
import com.sotospeak.app.screens.ProfileScreen
import com.sotospeak.app.screens.QuestionsScreen
import com.sotospeak.app.screens.RegisterScreen
import com.sotospeak.app.screens.TopicsScreen
import com.sotospeak.app.screens.TrainingScreen
import com.sotospeak.app.viewmodel.AuthState
import com.sotospeak.app.viewmodel.LibraryState
import com.sotospeak.app.viewmodel.MySubmissionsState
import com.sotospeak.app.viewmodel.PracticeState
import com.sotospeak.app.viewmodel.ProfileState
import com.sotospeak.app.viewmodel.QuestionsState
import com.sotospeak.app.viewmodel.TopicUiModel
import com.sotospeak.app.viewmodel.TopicsState
import com.sotospeak.app.viewmodel.TrainingState
import com.sotospeak.designsystem.theme.FunnyThemePreview
import com.sotospeak.shared.contracts.SpeakingGrade
import com.sotospeak.shared.contracts.SpeakingLibrary
import com.sotospeak.shared.contracts.SpeakingQuestion
import com.sotospeak.shared.contracts.SpeakingSubmission
import com.sotospeak.shared.contracts.User
import com.sotospeak.shared.contracts.UserProfile
import org.junit.Rule
import org.junit.Test

// Локальные моки (commonTest не виден из androidInstrumentedTest).
// Дублируют TestMocks.kt — при смене формата моделей править оба места.
private val mockLibraries = listOf(
    SpeakingLibrary(
        id = "lib-1",
        title = "Знакомство",
        description = "Базовые фразы для знакомства",
        coverUrl = null,
        topicCount = 3
    )
)

private val mockTopics = listOf(
    TopicUiModel(
        id = "topic-1",
        title = "Приветствие",
        durationSeconds = 95,
        questionCount = 5,
        hasSubtitles = true,
        isWatched = true,
        hasLocalRecordings = true
    ),
    TopicUiModel(
        id = "topic-2",
        title = "О себе",
        durationSeconds = 120,
        questionCount = 3,
        hasSubtitles = false,
        isWatched = false,
        hasLocalRecordings = false
    )
)

private val mockQuestions = listOf(
    SpeakingQuestion(id = "sq-1", text = "What is your name?", displayOrder = 1),
    SpeakingQuestion(id = "sq-2", text = "Where do you live?", displayOrder = 2),
    SpeakingQuestion(id = "sq-3", text = "What do you like to do?", displayOrder = 3)
)

private val mockSubmissions = listOf(
    SpeakingSubmission(
        id = "sub-1",
        topicId = "topic-1",
        topicTitle = "Приветствие",
        audioUrl = "https://media.example.com/sub-1.m4a",
        durationSec = 28,
        status = "NEW",
        grade = null,
        createdAt = "2026-07-31T10:00:00Z"
    ),
    SpeakingSubmission(
        id = "sub-2",
        topicId = "topic-2",
        topicTitle = "О себе",
        audioUrl = "https://media.example.com/sub-2.m4a",
        durationSec = 30,
        status = "REVIEWED",
        grade = SpeakingGrade(
            grammar = 8,
            vocabulary = 7,
            pronunciation = 9,
            fluency = 6,
            total = 7.5,
            comment = "Хорошая работа! Обрати внимание на артикли.",
            reviewerName = "Teacher Anna",
            createdAt = "2026-07-30T12:00:00Z",
            updatedAt = "2026-07-30T12:00:00Z"
        ),
        createdAt = "2026-07-29T09:00:00Z"
    )
)

/**
 * Golden screenshot-тесты (Dropshots) по ключевым экранам Speaking-тренажёра.
 *
 * Эталоны («blessed») — скриншоты, подтверждённые дизайн-аудитом DC-A1
 * (docs/qa/design-conformance/REPORT_ANDROID_2026-08-10.md), лежат в
 * `src/androidInstrumentedTest/screenshots/`.
 *
 * Запись/обновление эталонов (на эмуляторе Medium_Phone 1080x2400@420 — той же
 * конфигурации, что и гейт):
 *   ./gradlew :composeApp:recordDebugAndroidTestScreenshots
 * Проверка:
 *   ./gradlew :composeApp:connectedDebugAndroidTest
 */
class ScreenshotTest {

    @get:Rule(order = 0)
    val composeTestRule = createComposeRule()

    @get:Rule(order = 1)
    val dropshots = Dropshots()

    @OptIn(ExperimentalTestApi::class)
    private fun snap(name: String, content: @Composable () -> Unit) {
        composeTestRule.setContent {
            FunnyThemePreview(darkTheme = false, content = content)
        }
        composeTestRule.waitForIdle()
        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        dropshots.assertSnapshot(bitmap, name = name)
    }

    @Test
    fun onboarding() = snap("Onboarding") {
        OnboardingScreen(onFinish = {})
    }

    @Test
    fun login() = snap("Login") {
        LoginScreen(
            state = AuthState(),
            onLogin = { _, _ -> },
            onNavigateToRegister = {},
            onClearError = {},
            onContinueAsGuest = {}
        )
    }

    @Test
    fun register() = snap("Register") {
        RegisterScreen(
            state = AuthState(),
            onRegister = { _, _, _ -> },
            onNavigateToLogin = {},
            onClearError = {}
        )
    }

    @Test
    fun library() = snap("Library") {
        LibraryScreen(
            state = LibraryState(libraries = mockLibraries),
            onLoad = {},
            onLibraryClick = {}
        )
    }

    @Test
    fun topics() = snap("Topics") {
        TopicsScreen(
            state = TopicsState(libraryTitle = "Разговорный английский", topics = mockTopics),
            onTopicClick = {},
            onRetry = {},
            onBack = {},
            libraryTitle = "Разговорный английский"
        )
    }

    @Test
    fun questionsAuth() = snap("Questions_auth") {
        QuestionsScreen(
            state = QuestionsState(
                topicTitle = "Знакомство",
                questions = mockQuestions,
                isGuest = false
            ),
            onStartTraining = {},
            onStartPractice = {},
            onLoginClick = {},
            onRegisterClick = {},
            onRetry = {},
            onBack = {},
            libraryTitle = "Разговорный английский"
        )
    }

    @Test
    fun questionsGuestGate() = snap("Questions_guest_gate") {
        QuestionsScreen(
            state = QuestionsState(
                topicTitle = "Знакомство",
                questions = mockQuestions,
                isGuest = true
            ),
            onStartTraining = {},
            onStartPractice = {},
            onLoginClick = {},
            onRegisterClick = {},
            onRetry = {},
            onBack = {},
            libraryTitle = "Разговорный английский"
        )
    }

    @Test
    fun trainingIdle() = snap("Training_idle") {
        TrainingScreen(
            state = TrainingState(
                topicTitle = "Знакомство",
                questions = mockQuestions,
                attemptNumber = 1,
                remainingSeconds = 80,
                micPermission = MicPermissionState.Granted
            ),
            topicId = "topic-1",
            recorderState = VoiceRecorderState.Idle,
            micPermission = MicPermissionState.Granted,
            onStartRecording = {},
            onStopRecording = {},
            onPlayRecording = {},
            onStopPlayback = {},
            onGoToPractice = {},
            onRestartAttempts = {},
            onBackToLibrary = {},
            onOpenSettings = {},
            onRetry = {},
            onBack = {},
            libraryTitle = "Разговорный английский"
        )
    }

    @Test
    fun practiceReady() = snap("Practice_ready") {
        PracticeScreen(
            state = PracticeState(
                topicTitle = "Знакомство",
                questions = mockQuestions,
                remainingSeconds = PracticeState.PRACTICE_LIMIT_SECONDS,
                micPermission = MicPermissionState.Granted
            ),
            onStart = {},
            onStopEarly = {},
            onRetryUpload = {},
            onBackToLibrary = {},
            onRetry = {},
            onBack = {},
            libraryTitle = "Разговорный английский"
        )
    }

    @Test
    fun mySubmissions() = snap("MySubmissions") {
        MySubmissionsScreen(
            state = MySubmissionsState(submissions = mockSubmissions),
            onRefresh = {},
            onRetryPending = {},
            onPlayAudio = {},
            onStopAudio = {}
        )
    }

    @Test
    fun profileGuest() = snap("Profile_guest") {
        ProfileScreen(
            state = ProfileState(),
            isGuest = true,
            onThemeSelected = {},
            onLoad = {},
            onLogout = {},
            onLoginClick = {}
        )
    }

    @Test
    fun profileAuth() = snap("Profile_auth") {
        ProfileScreen(
            state = ProfileState(
                userProfile = UserProfile(
                    user = User(
                        id = "1",
                        email = "anna@smirnova.ru",
                        displayName = "Анна Смирнова",
                        level = 1,
                        totalPoints = 0,
                        currentStreak = 0,
                        role = "USER",
                        createdAt = "2026-01-01T00:00:00Z"
                    )
                )
            ),
            isGuest = false,
            submissionsCount = 3,
            topicsCompleted = 2,
            onThemeSelected = {},
            onLoad = {},
            onLogout = {}
        )
    }
}
