package com.sotospeak.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.sotospeak.app.di.mockSpeakingQuestions
import com.sotospeak.app.di.mockTrainingRecordingMetas
import com.sotospeak.app.recorder.MicPermissionState
import com.sotospeak.app.recorder.VoiceRecorderState
import com.sotospeak.app.screens.TrainingScreen
import com.sotospeak.app.viewmodel.RecorderUiState
import com.sotospeak.app.viewmodel.TrainingState
import com.sotospeak.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * UI тесты экрана Training (спека Part 2 §10.1).
 * Реальный composable [TrainingScreen] напрямую + моковый [TrainingState] +
 * [VoiceRecorderState.Idle] + captured callbacks.
 *
 * Сценарии:
 * 1. level_chip + training_questions_list + record_button
 * 2. record_button enabled при micPermission=Granted, disabled при Denied + mic_permission_rationale
 * 3. Recording-стейт: training_timer + stop_button
 * 4. Список попыток recording_item_<n> + attempt_check_<n> БЕЗ кнопок удаления
 * 5. isFinished → final_cta + final_go_practice/final_back_library/final_restart → callbacks
 */
@OptIn(ExperimentalTestApi::class)
class TrainingScreenTest : BaseUiTest() {

    // ============================================
    // 1. Базовый рендер (Ready, permission Granted)
    // ============================================

    @Test
    fun baseElementsAreVisible() = runTest(
        content = { TrainingScreenForTest() }
    ) {
        onNodeWithTag("training_screen").assertIsDisplayed()
        onNodeWithTag("level_chip", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("training_questions_list", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("question_item_0", useUnmergedTree = true).assertExists()
        try {
            onNodeWithTag("record_button", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("record_button", useUnmergedTree = true).assertExists()
    }

    // ============================================
    // 1a. DC-3: элементы мокапа (нумерация, idle-кольцо, подпись попытки, заголовок попыток)
    // ============================================

    @Test
    fun dc3MockupElementsAreVisible() = runTest(
        content = { TrainingScreenForTest() }
    ) {
        // T4: нумерация вопросов 1..N
        onNodeWithTag("question_number_0", useUnmergedTree = true).assertExists()
        // T2: idle-кольцо видно ДО записи
        try {
            onNodeWithTag("training_timer_idle", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("training_timer_idle", useUnmergedTree = true).assertExists()
        // T5: подпись попытки
        onNodeWithTag("attempt_hint", useUnmergedTree = true).assertExists()
        // При 0 попыток заголовок и пустой блок не показываются (mockups v2.0)
        onNodeWithTag("attempts_title", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun recordButtonIsEnabledWhenPermissionGranted() = runTest(
        content = {
            TrainingScreenForTest(state = mockTrainingState(micPermission = MicPermissionState.Granted))
        }
    ) {
        try {
            onNodeWithTag("record_button", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("record_button", useUnmergedTree = true).assertIsEnabled()
    }

    // ============================================
    // 2. Нет разрешения — disabled + rationale
    // ============================================

    @Test
    fun recordButtonIsDisabledAndRationaleShownWhenDenied() = runTest(
        content = {
            TrainingScreenForTest(state = mockTrainingState(micPermission = MicPermissionState.Denied))
        }
    ) {
        try {
            onNodeWithTag("record_button", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("record_button", useUnmergedTree = true).assertIsNotEnabled()
        onNodeWithTag("mic_permission_rationale", useUnmergedTree = true).assertExists()
    }

    // ============================================
    // 3. Recording-стейт: таймер + стоп
    // ============================================

    @Test
    fun recordingStateShowsTimerAndStopButton() = runTest(
        content = {
            TrainingScreenForTest(
                state = mockTrainingState(
                    recorder = RecorderUiState.Recording(startedAtMs = 0L),
                    remainingSeconds = 45
                )
            )
        }
    ) {
        try {
            onNodeWithTag("training_timer", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("training_timer", useUnmergedTree = true).assertExists()
        onNodeWithTag("stop_button", useUnmergedTree = true).assertExists()
        onNodeWithTag("record_button", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun stopButtonCallsOnStopRecording() = runTest(
        content = {
            TrainingScreenForTest(
                state = mockTrainingState(
                    recorder = RecorderUiState.Recording(startedAtMs = 0L),
                    remainingSeconds = 45
                )
            )
        }
    ) {
        try {
            onNodeWithTag("stop_button", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("stop_button", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(TrainingClicks.stopRecording, "onStopRecording должен быть вызван")
    }

    // ============================================
    // 4. Список попыток: прослушивание + авто-✅, БЕЗ удаления
    // ============================================

    @Test
    fun attemptsListShowsItemsWithCheckAndNoDelete() = runTest(
        content = {
            TrainingScreenForTest(
                state = mockTrainingState(
                    attempts = mockTrainingRecordingMetas,
                    attemptNumber = 3
                )
            )
        }
    ) {
        try {
            onNodeWithTag("recording_item_0", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("recording_item_0", useUnmergedTree = true).assertExists()
        onNodeWithTag("attempt_check_0", useUnmergedTree = true).assertExists()
        onNodeWithTag("attempts_title", useUnmergedTree = true).assertExists()
        try {
            onNodeWithTag("recording_item_1", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("recording_item_1", useUnmergedTree = true).assertExists()
        onNodeWithTag("attempt_check_1", useUnmergedTree = true).assertExists()
        // Удаления попыток нет по дизайну (дизайн v1.1: только прослушивание)
        onNodeWithTag("delete_recording_0", useUnmergedTree = true).assertDoesNotExist()
        onNodeWithTag("delete_recording_1", useUnmergedTree = true).assertDoesNotExist()
    }

    // ============================================
    // 5. Финальные CTA после 3-й попытки
    // ============================================

    @Test
    fun finishedStateShowsFinalCtaBlock() = runTest(
        content = { TrainingScreenForTest(state = mockFinishedState()) }
    ) {
        try {
            onNodeWithTag("final_cta", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("final_cta", useUnmergedTree = true).assertExists()
        onNodeWithTag("final_go_practice", useUnmergedTree = true).assertExists()
        onNodeWithTag("final_back_library", useUnmergedTree = true).assertExists()
        onNodeWithTag("final_restart", useUnmergedTree = true).assertExists()
        // Таймер/rec-кнопка скрыты после 3-й попытки
        onNodeWithTag("record_button", useUnmergedTree = true).assertDoesNotExist()
        onNodeWithTag("training_timer", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun finalGoPracticeCallsCallback() = runTest(
        content = { TrainingScreenForTest(state = mockFinishedState()) }
    ) {
        try {
            onNodeWithTag("final_go_practice", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("final_go_practice", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(TrainingClicks.goPractice, "onGoToPractice должен быть вызван")
    }

    @Test
    fun finalBackLibraryCallsCallback() = runTest(
        content = { TrainingScreenForTest(state = mockFinishedState()) }
    ) {
        try {
            onNodeWithTag("final_back_library", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("final_back_library", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(TrainingClicks.backLibrary, "onBackToLibrary должен быть вызван")
    }

    @Test
    fun finalRestartCallsCallback() = runTest(
        content = { TrainingScreenForTest(state = mockFinishedState()) }
    ) {
        try {
            onNodeWithTag("final_restart", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("final_restart", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(TrainingClicks.restart, "onRestartAttempts должен быть вызван")
    }
}

// ============================================
// Test fixtures
// ============================================

/** Captured callbacks */
private object TrainingClicks {
    var startRecording = false
    var stopRecording = false
    var goPractice = false
    var backLibrary = false
    var restart = false
}

private fun mockTrainingState(
    recorder: RecorderUiState = RecorderUiState.Idle,
    remainingSeconds: Int = 0,
    attempts: List<com.sotospeak.app.storage.RecordingMeta> = emptyList(),
    attemptNumber: Int = 1,
    isFinished: Boolean = false,
    micPermission: MicPermissionState = MicPermissionState.Granted
) = TrainingState(
    topicTitle = "Приветствие",
    questions = mockSpeakingQuestions,
    attempts = attempts,
    recorder = recorder,
    attemptNumber = attemptNumber,
    remainingSeconds = remainingSeconds,
    isFinished = isFinished,
    micPermission = micPermission
)

/** 3 попытки сделаны → финальные CTA */
private fun mockFinishedState() = mockTrainingState(
    attempts = mockTrainingRecordingMetas + mockTrainingRecordingMetas.last().copy(
        filePath = "/recordings/topic-1_training_3.m4a",
        attemptNumber = 3,
        timerLimitSeconds = 30,
        durationMs = 29_000
    ),
    attemptNumber = 3,
    isFinished = true
)

@androidx.compose.runtime.Composable
private fun TrainingScreenForTest(state: TrainingState = mockTrainingState()) {
    FunnyTheme {
        TrainingScreen(
            state = state,
            topicId = "topic-1",
            recorderState = VoiceRecorderState.Idle,
            micPermission = state.micPermission,
            onStartRecording = { TrainingClicks.startRecording = true },
            onStopRecording = { TrainingClicks.stopRecording = true },
            onPlayRecording = {},
            onStopPlayback = {},
            onGoToPractice = { TrainingClicks.goPractice = true },
            onRestartAttempts = { TrainingClicks.restart = true },
            onBackToLibrary = { TrainingClicks.backLibrary = true },
            onOpenSettings = {},
            onRetry = {},
            onBack = {}
        )
    }
}
