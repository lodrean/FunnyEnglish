package com.funnyenglish.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.funnyenglish.app.di.mockSpeakingQuestions
import com.funnyenglish.app.recorder.MicPermissionState
import com.funnyenglish.app.screens.PracticeScreen
import com.funnyenglish.app.viewmodel.PracticePhase
import com.funnyenglish.app.viewmodel.PracticeState
import com.funnyenglish.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * UI тесты экрана Practice (спека Part 2 §10.1).
 * Реальный [PracticeScreen] + моковый [PracticeState] + captured callbacks.
 *
 * Сценарии:
 * 1. Фаза Ready: practice_start_button + practice_auto_send_note
 * 2. Фаза Recording: practice_timer + practice_stop_button
 * 3. Фаза Uploading: upload_panel
 * 4. Фаза Sent: sent_panel + sent_back_button → callback
 * 5. uploadError → upload_retry_button → callback
 * 6. «Назад» в фазе Recording → диалог «Прервать запись?» (onBack НЕ вызывается сразу)
 */
@OptIn(ExperimentalTestApi::class)
class PracticeScreenTest : BaseUiTest() {

    // ============================================
    // 1. Фаза Ready
    // ============================================

    @Test
    fun readyPhaseShowsStartButtonAndAutoSendNote() = runTest(
        content = { PracticeScreenForTest() }
    ) {
        onNodeWithTag("practice_screen").assertIsDisplayed()
        onNodeWithTag("practice_questions_list", useUnmergedTree = true).assertExists()
        onNodeWithTag("practice_auto_send_note", useUnmergedTree = true).assertIsDisplayed()
        try {
            onNodeWithTag("practice_start_button", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("practice_start_button", useUnmergedTree = true).assertExists()
    }

    @Test
    fun startButtonCallsOnStart() = runTest(
        content = { PracticeScreenForTest() }
    ) {
        try {
            onNodeWithTag("practice_start_button", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("practice_start_button", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(PracticeClicks.start, "onStart должен быть вызван")
    }

    // ============================================
    // 2. Фаза Recording
    // ============================================

    @Test
    fun recordingPhaseShowsTimerAndStopButton() = runTest(
        content = {
            PracticeScreenForTest(
                state = mockPracticeState(phase = PracticePhase.Recording, remainingSeconds = 20)
            )
        }
    ) {
        try {
            onNodeWithTag("practice_timer", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("practice_timer", useUnmergedTree = true).assertExists()
        onNodeWithTag("practice_stop_button", useUnmergedTree = true).assertExists()
        onNodeWithTag("practice_start_button", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun stopButtonCallsOnStopEarly() = runTest(
        content = {
            PracticeScreenForTest(
                state = mockPracticeState(phase = PracticePhase.Recording, remainingSeconds = 20)
            )
        }
    ) {
        try {
            onNodeWithTag("practice_stop_button", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("practice_stop_button", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(PracticeClicks.stopEarly, "onStopEarly должен быть вызван")
    }

    // ============================================
    // 3. Фаза Uploading
    // ============================================

    @Test
    fun uploadingPhaseShowsUploadPanel() = runTest(
        content = {
            PracticeScreenForTest(
                state = mockPracticeState(phase = PracticePhase.Uploading, uploadProgress = 40)
            )
        }
    ) {
        try {
            onNodeWithTag("upload_panel", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("upload_panel", useUnmergedTree = true).assertExists()
    }

    // ============================================
    // 4. Фаза Sent
    // ============================================

    @Test
    fun sentPhaseShowsSentPanelAndBackButton() = runTest(
        content = {
            PracticeScreenForTest(state = mockPracticeState(phase = PracticePhase.Sent))
        }
    ) {
        try {
            onNodeWithTag("sent_panel", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("sent_panel", useUnmergedTree = true).assertExists()
        onNodeWithTag("sent_back_button", useUnmergedTree = true).assertExists()
    }

    @Test
    fun sentBackButtonCallsOnBackToLibrary() = runTest(
        content = {
            PracticeScreenForTest(state = mockPracticeState(phase = PracticePhase.Sent))
        }
    ) {
        try {
            onNodeWithTag("sent_back_button", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("sent_back_button", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(PracticeClicks.backToLibrary, "onBackToLibrary должен быть вызван")
    }

    // ============================================
    // 5. Ошибка отправки → retry (файл не теряется)
    // ============================================

    @Test
    fun uploadErrorShowsRetryButton() = runTest(
        content = {
            PracticeScreenForTest(
                state = mockPracticeState(phase = PracticePhase.Ready, uploadError = true)
            )
        }
    ) {
        try {
            onNodeWithTag("upload_retry_button", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("upload_retry_button", useUnmergedTree = true).assertExists()
        onNodeWithTag("upload_retry_button", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(PracticeClicks.retryUpload, "onRetryUpload должен быть вызван")
    }

    // ============================================
    // 6. «Назад» в фазе Recording → диалог подтверждения
    // ============================================

    @Test
    fun backInRecordingPhaseShowsConfirmDialog() = runTest(
        content = {
            PracticeScreenForTest(
                state = mockPracticeState(phase = PracticePhase.Recording, remainingSeconds = 20)
            )
        }
    ) {
        onNodeWithContentDescription("Назад").performClick()
        waitForIdle()
        // Диалог подтверждения вместо мгновенного выхода
        onNodeWithText("Прервать запись?").assertIsDisplayed()
        // Подтверждение «Выйти» → onBack
        onNodeWithText("Выйти").performClick()
        waitForIdle()
        assertTrue(PracticeClicks.back, "onBack должен быть вызван после «Выйти»")
    }

    @Test
    fun backInReadyPhaseCallsOnBackDirectly() = runTest(
        content = { PracticeScreenForTest() }
    ) {
        onNodeWithContentDescription("Назад").performClick()
        waitForIdle()
        assertTrue(PracticeClicks.back, "onBack в фазе Ready вызывается без диалога")
    }
}

// ============================================
// Test fixtures
// ============================================

/** Captured callbacks */
private object PracticeClicks {
    var start = false
    var stopEarly = false
    var retryUpload = false
    var backToLibrary = false
    var back = false
}

private fun mockPracticeState(
    phase: PracticePhase = PracticePhase.Ready,
    remainingSeconds: Int = PracticeState.PRACTICE_LIMIT_SECONDS,
    uploadProgress: Int = 0,
    uploadError: Boolean = false,
    micPermission: MicPermissionState = MicPermissionState.Granted
) = PracticeState(
    topicTitle = "Приветствие",
    questions = mockSpeakingQuestions,
    phase = phase,
    remainingSeconds = remainingSeconds,
    uploadProgress = uploadProgress,
    uploadError = uploadError,
    micPermission = micPermission
)

@androidx.compose.runtime.Composable
private fun PracticeScreenForTest(state: PracticeState = mockPracticeState()) {
    FunnyTheme {
        PracticeScreen(
            state = state,
            onStart = { PracticeClicks.start = true },
            onStopEarly = { PracticeClicks.stopEarly = true },
            onRetryUpload = { PracticeClicks.retryUpload = true },
            onBackToLibrary = { PracticeClicks.backToLibrary = true },
            onRetry = {},
            onBack = { PracticeClicks.back = true }
        )
    }
}
