package com.sotospeak.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.sotospeak.app.di.mockSpeakingQuestions
import com.sotospeak.app.screens.QuestionsScreen
import com.sotospeak.app.viewmodel.QuestionsState
import com.sotospeak.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * UI тесты экрана вопросов топика (спека Part 2 §10.1).
 * Реальный [QuestionsScreen] + моковый [QuestionsState] + captured callbacks.
 *
 * Сценарии:
 * 1. Список вопросов (question_item_<n>)
 * 2. Гость: гейт frame-locked («Ты почти у цели!») вместо mode_practice_button,
 *    «Зарегистрироваться» → onRegisterClick, «Войти» → onLoginClick
 * 3. Авторизованный: mode_practice_button → onStartPractice
 * 4. mode_training_button → onStartTraining (всем)
 */
@OptIn(ExperimentalTestApi::class)
class QuestionsScreenTest : BaseUiTest() {

    // ============================================
    // 1. Список вопросов
    // ============================================

    @Test
    fun activeQuestionShowsEyebrow() = runTest(
        content = { QuestionsScreenForTest() }
    ) {
        onNodeWithText("ВОПРОС 1 ИЗ 3", useUnmergedTree = true).assertExists()
    }

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
    fun guestSeesLockedPracticeGate() = runTest(
        content = { QuestionsScreenForTest(state = mockQuestionsState(isGuest = true)) }
    ) {
        // Гейт по frame-locked: заголовок, текст и обе кнопки
        onNodeWithText("Ты почти у цели!", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText(
            "Отправка записи учителю доступна после регистрации",
            useUnmergedTree = true
        ).assertIsDisplayed()
        onNodeWithTag("practice_locked_cta", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("practice_locked_login", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("mode_practice_button", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun guestClickOnRegisterCtaCallsOnRegisterClick() = runTest(
        content = { QuestionsScreenForTest(state = mockQuestionsState(isGuest = true)) }
    ) {
        onNodeWithTag("practice_locked_cta", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(QuestionsClicks.register, "onRegisterClick должен быть вызван")
    }

    @Test
    fun guestClickOnLoginButtonCallsOnLoginClick() = runTest(
        content = { QuestionsScreenForTest(state = mockQuestionsState(isGuest = true)) }
    ) {
        onNodeWithTag("practice_locked_login", useUnmergedTree = true).performClick()
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
    var register = false
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
            onRegisterClick = { QuestionsClicks.register = true },
            onRetry = {},
            onBack = {}
        )
    }
}
