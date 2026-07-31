package com.funnyenglish.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.funnyenglish.app.di.mockPendingUploads
import com.funnyenglish.app.di.mockSpeakingSubmissions
import com.funnyenglish.app.screens.MySubmissionsScreen
import com.funnyenglish.app.viewmodel.MySubmissionsState
import com.funnyenglish.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * UI тесты экрана «Мои записи» (спека Part 2 §10.1).
 * Реальный [MySubmissionsScreen] + моковый [MySubmissionsState] + captured callbacks.
 *
 * Сценарии:
 * 1. Список submission_item_<id> + статусы («На проверке» / «Проверено»)
 * 2. grade_card_<id>: 4 критерия рубрики + total
 * 3. pending_upload_item с «Повторить» → callback с filePath
 * 4. Empty state (submissions_empty)
 */
@OptIn(ExperimentalTestApi::class)
class MySubmissionsScreenTest : BaseUiTest() {

    // ============================================
    // 1. Список и статусы
    // ============================================

    @Test
    fun submissionItemsAndStatusesAreVisible() = runTest(
        content = { MySubmissionsScreenForTest() }
    ) {
        onNodeWithTag("my_submissions_screen").assertIsDisplayed()
        onNodeWithTag("submission_item_sub-1", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("submission_status_sub-1", useUnmergedTree = true).assertExists()
        onNode(
            hasAnyAncestor(hasTestTag("submission_status_sub-1")) and hasText("На проверке"),
            useUnmergedTree = true
        ).assertExists()

        try {
            onNodeWithTag("submission_item_sub-2", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("submission_item_sub-2", useUnmergedTree = true).assertExists()
        onNode(
            hasAnyAncestor(hasTestTag("submission_status_sub-2")) and hasText("Проверено"),
            useUnmergedTree = true
        ).assertExists()
    }

    // ============================================
    // 2. Карточка оценки (рубрика)
    // ============================================

    @Test
    fun gradeCardShowsAllCriteriaAndTotal() = runTest(
        content = { MySubmissionsScreenForTest() }
    ) {
        try {
            onNodeWithTag("grade_card_sub-2", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("grade_card_sub-2", useUnmergedTree = true).assertExists()
        // 4 критерия рубрики + итоговый балл (7.5) + комментарий
        onNodeWithText("Грамматика", useUnmergedTree = true).assertExists()
        onNodeWithText("Словарный запас", useUnmergedTree = true).assertExists()
        onNodeWithText("Произношение", useUnmergedTree = true).assertExists()
        onNodeWithText("Беглость", useUnmergedTree = true).assertExists()
        onNodeWithText("7.5", useUnmergedTree = true).assertExists()
        onNodeWithText("Хорошая работа! Обрати внимание на артикли.", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun newSubmissionHasNoGradeCard() = runTest(
        content = { MySubmissionsScreenForTest() }
    ) {
        onNodeWithTag("submission_item_sub-1", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("grade_card_sub-1", useUnmergedTree = true).assertDoesNotExist()
    }

    // ============================================
    // 3. Неотправленные записи (offline retry)
    // ============================================

    @Test
    fun pendingUploadItemWithRetryCallback() = runTest(
        content = {
            MySubmissionsScreenForTest(
                state = MySubmissionsState(pendingUploads = mockPendingUploads)
            )
        }
    ) {
        onNodeWithTag("pending_upload_item", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Повторить").performClick()
        waitForIdle()
        assertEquals(
            "/recordings/topic-2_practice_0.m4a",
            MySubmissionsClicks.retryPath,
            "onRetryPending должен получить filePath записи"
        )
    }

    // ============================================
    // 4. Empty state
    // ============================================

    @Test
    fun emptyStateIsShownWhenNoSubmissions() = runTest(
        content = { MySubmissionsScreenForTest(state = MySubmissionsState()) }
    ) {
        onNodeWithTag("submissions_empty", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("У вас пока нет отправленных записей").assertIsDisplayed()
    }
}

// ============================================
// Test fixtures
// ============================================

/** Captured callbacks */
private object MySubmissionsClicks {
    var retryPath: String? = null
}

@androidx.compose.runtime.Composable
private fun MySubmissionsScreenForTest(
    state: MySubmissionsState = MySubmissionsState(submissions = mockSpeakingSubmissions)
) {
    FunnyTheme {
        MySubmissionsScreen(
            state = state,
            onRefresh = {},
            onRetryPending = { MySubmissionsClicks.retryPath = it },
            onPlayAudio = {},
            onStopAudio = {},
            onBack = {}
        )
    }
}
