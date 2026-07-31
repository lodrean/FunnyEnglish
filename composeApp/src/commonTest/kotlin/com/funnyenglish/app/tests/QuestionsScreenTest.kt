package com.funnyenglish.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.funnyenglish.app.di.mockSpeakingQuestions
import com.funnyenglish.app.screens.QuestionsScreen
import com.funnyenglish.app.viewmodel.QuestionsState
import com.funnyenglish.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * UI тесты экрана вопросов топика (спека Part 2 §10.1).
 * Реальный [QuestionsScreen] + моковый [QuestionsState] + captured callbacks.
 *
 * Сценарии:
 * 1. Список вопросов (question_item_<n>)
 * 2. Гость: practice_locked_cta вместо mode_practice_button, клик → onLoginClick
 * 3. Авторизованный: mode_practice_button → onStartPractice
 * 4. mode_training_button → onStartTraining (всем)
 */
@OptIn(ExperimentalTestApi::class)
class QuestionsScreenTest : BaseUiTest() {

    // ============================================
    // 1. Список вопросов
    // ============================================

    @Test
    fun questionItemsAreVisible() = runTest(
        content = { QuestionsScreenForTest() }
    ) {
        onNodeWithTag("questions_screen").assertIsDisplayed()
        onNodeWithTag("question_item_0", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("question_item_1", useUnmergedTree = true).assertExists()
        // Третий вопрос может быть за пределами viewport — скроллим (грабля №16)
        try {
            onNodeWithTag("question_item_2", useUnmergedTree = true).performScrollTo()
        } catch (e: Throwable) { /* уже виден */ }
        onNodeWithTag("question_item_2", useUnmergedTree = true).assertExists()
    }

    // ============================================
    // 2. Гость: Practice заблокирован
    // ============================================

    @Test
    fun guestSeesLockedPracticeCta() = runTest(
        content = { QuestionsScreenForTest(state = mockQuestionsState(isGuest = true)) }
    ) {
        onNodeWithTag("practice_locked_cta", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("mode_practice_button", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun guestClickOnLockedCtaCallsOnLoginClick() = runTest(
        content = { QuestionsScreenForTest(state = mockQuestionsState(isGuest = true)) }
    ) {
        onNodeWithTag("practice_locked_cta", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(QuestionsClicks.login, "onLoginClick должен быть вызван")
    }

    // ============================================
    // 3. Авторизованный: Practice доступна
    // ============================================

    @Test
    fun authorizedUserSeesPracticeButton() = runTest(
        content = { QuestionsScreenForTest(state = mockQuestionsState(isGuest = false)) }
    ) {
        onNodeWithTag("mode_practice_button", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("practice_locked_cta", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun clickOnPracticeButtonCallsOnStartPractice() = runTest(
        content = { QuestionsScreenForTest(state = mockQuestionsState(isGuest = false)) }
    ) {
        onNodeWithTag("mode_practice_button", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(QuestionsClicks.practice, "onStartPractice должен быть вызван")
    }

    // ============================================
    // 4. Training доступен всем
    // ============================================

    @Test
    fun clickOnTrainingButtonCallsOnStartTraining() = runTest(
        content = { QuestionsScreenForTest(state = mockQuestionsState(isGuest = true)) }
    ) {
        onNodeWithTag("mode_training_button", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(QuestionsClicks.training, "onStartTraining должен быть вызван")
    }
}

// ============================================
// Test fixtures
// ============================================

/** Captured callbacks */
private object QuestionsClicks {
    var training = false
    var practice = false
    var login = false
}

private fun mockQuestionsState(isGuest: Boolean = false) = QuestionsState(
    topicTitle = "Приветствие",
    questions = mockSpeakingQuestions,
    isGuest = isGuest
)

@androidx.compose.runtime.Composable
private fun QuestionsScreenForTest(state: QuestionsState = mockQuestionsState()) {
    FunnyTheme {
        QuestionsScreen(
            state = state,
            onStartTraining = { QuestionsClicks.training = true },
            onStartPractice = { QuestionsClicks.practice = true },
            onLoginClick = { QuestionsClicks.login = true },
            onRetry = {},
            onBack = {}
        )
    }
}
