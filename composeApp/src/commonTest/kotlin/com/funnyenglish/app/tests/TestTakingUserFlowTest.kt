package com.funnyenglish.app.tests

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.funnyenglish.app.di.mockAchievements
import com.funnyenglish.app.di.mockTestDetail
import com.funnyenglish.app.screens.TestPlayScreen
import com.funnyenglish.app.viewmodel.TestPlayState
import com.funnyenglish.designsystem.theme.FunnyTheme
import com.funnyenglish.shared.model.SubmitAnswer
import com.funnyenglish.shared.model.SubmitTestResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI тесты прохождения теста на МОКОВЫХ данных.
 *
 * Рендерится РЕАЛЬНЫЙ [TestPlayScreen] с моковым [TestPlayState]
 * (mockTestDetail из `app/di/TestMocks.kt` — 2 TEXT_SELECT вопроса).
 * «API» заменено captured callbacks.
 *
 * Сценарии:
 * 1. Отображение вопроса и вариантов ответа
 * 2. Выбор ответа (callback onSelectAnswer)
 * 3. Навигация по вопросам (Далее / счётчик)
 * 4. Отправка последнего вопроса (Завершить тест)
 * 5. Экран результатов (мок SubmitTestResult)
 * 6. Состояния loading / error
 */
@OptIn(ExperimentalTestApi::class)
class TestTakingUserFlowTest : BaseUiTest() {

    @Test
    fun questionAndAnswersAreDisplayed() = runTest(
        content = { TestPlayScreenForTest() }
    ) {
        onNodeWithText("Вопрос 1 из 2", substring = true).assertIsDisplayed()
        onNodeWithText("I _____ to school every day.", substring = true).assertIsDisplayed()
        onNodeWithText("go").assertIsDisplayed()
        onNodeWithText("goes").assertIsDisplayed()
    }

    @Test
    fun userCanSelectAnswer() = runTest(
        content = { TestPlayScreenForTest() }
    ) {
        onNodeWithText("goes").performClick()
        waitForIdle()
        assertEquals("q-1", TestPlayClicks.questionId)
        assertEquals("a-2", TestPlayClicks.answerId)
    }

    @Test
    fun userCanNavigateToNextQuestion() = runTest(
        content = {
            // «Далее» активна только при выбранном ответе
            TestPlayScreenForTest(
                state = TestPlayState(
                    test = mockTestDetail,
                    answers = mapOf("q-1" to SubmitAnswer("q-1", listOf("a-2")))
                )
            )
        }
    ) {
        onNodeWithText("Далее").performClick()
        waitForIdle()
        assertTrue(TestPlayClicks.next, "onNextQuestion должен быть вызван")
    }

    @Test
    fun secondQuestionShowsSubmitButton() = runTest(
        content = {
            TestPlayScreenForTest(
                state = TestPlayState(
                    test = mockTestDetail,
                    currentQuestionIndex = 1,
                    answers = mapOf(
                        "q-1" to SubmitAnswer("q-1", listOf("a-1")),
                        "q-2" to SubmitAnswer("q-2", listOf("a-6"))
                    )
                )
            )
        }
    ) {
        onNodeWithText("Вопрос 2 из 2", substring = true).assertIsDisplayed()
        onNodeWithText("Завершить тест").assertIsDisplayed()
        onNodeWithText("Завершить тест").performClick()
        waitForIdle()
        assertTrue(TestPlayClicks.submit, "onSubmit должен быть вызван")
    }

    @Test
    fun userSeesResultsAfterSubmit() = runTest(
        content = {
            TestPlayScreenForTest(
                state = TestPlayState(
                    test = mockTestDetail,
                    result = mockSubmitResult(percentage = 100, stars = 3)
                )
            )
        }
    ) {
        onNodeWithText("Ваш результат").assertIsDisplayed()
        onNodeWithText("100%").assertIsDisplayed()
        onNodeWithText("+50 XP", substring = true).assertIsDisplayed()
    }

    @Test
    fun perfectScoreShowsAchievement() = runTest(
        content = {
            TestPlayScreenForTest(
                state = TestPlayState(
                    test = mockTestDetail,
                    result = mockSubmitResult(
                        percentage = 100,
                        stars = 3,
                        newAchievements = listOf(mockAchievements[0])
                    )
                )
            )
        }
    ) {
        onNodeWithText("Ваш результат").assertIsDisplayed()
        // Секция новых достижений
        onNodeWithText("Новые достижения:", substring = true).assertIsDisplayed()
        onNodeWithText("First Steps").assertIsDisplayed()
    }

    @Test
    fun loadingStateShowsIndicator() = runTest(
        content = { TestPlayScreenForTest(state = TestPlayState(isLoading = true)) }
    ) {
        onNodeWithText("Далее").assertDoesNotExist()
    }

    @Test
    fun errorStateShowsMessage() = runTest(
        content = {
            // error показывается только когда тест загружен (иначе ветка loading)
            TestPlayScreenForTest(state = TestPlayState(test = mockTestDetail, error = "Network error"))
        }
    ) {
        onNodeWithText("Ошибка").assertIsDisplayed()
        onNodeWithText("Network error").assertIsDisplayed()
        onNodeWithText("Вернуться").performClick()
        waitForIdle()
        assertTrue(TestPlayClicks.back, "onBack должен быть вызван")
    }
}

// ============================================
// Test fixtures
// ============================================

private object TestPlayClicks {
    var questionId: String? = null
    var answerId: String? = null
    var next = false
    var submit = false
    var back = false
}

private fun mockSubmitResult(
    percentage: Int,
    stars: Int,
    newAchievements: List<com.funnyenglish.shared.model.Achievement> = emptyList()
) = SubmitTestResult(
    score = 4,
    maxScore = 5,
    percentage = percentage,
    stars = stars,
    pointsEarned = 50,
    isNewBestScore = false,
    newAchievements = newAchievements,
    levelUp = null
)

/** Реальный TestPlayScreen на моковых данных */
@Composable
fun TestPlayScreenForTest(
    state: TestPlayState = TestPlayState(test = mockTestDetail)
) {
    FunnyTheme {
        TestPlayScreen(
            state = state,
            isGuest = false,
            onBack = { TestPlayClicks.back = true },
            onSelectAnswer = { qId, aId ->
                TestPlayClicks.questionId = qId
                TestPlayClicks.answerId = aId
            },
            onSetDragDropMatch = { _, _, _ -> },
            onSetImageWordMatch = { _, _, _ -> },
            onNextQuestion = { TestPlayClicks.next = true },
            onPreviousQuestion = {},
            onGoToQuestion = {},
            onSubmit = { TestPlayClicks.submit = true },
            onShowResult = {}
        )
    }
}
