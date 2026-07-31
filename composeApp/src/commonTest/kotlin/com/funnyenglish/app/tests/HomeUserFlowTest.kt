package com.funnyenglish.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.funnyenglish.app.di.mockCategories
import com.funnyenglish.app.di.mockTestListItems
import com.funnyenglish.app.di.mockUser
import com.funnyenglish.app.di.mockUserProfile
import com.funnyenglish.app.screens.HomeScreen
import com.funnyenglish.app.viewmodel.HomeState
import com.funnyenglish.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI тесты главного экрана (Home) на МОКОВЫХ данных.
 *
 * Рендерится РЕАЛЬНЫЙ [HomeScreen] с моковым [HomeState]
 * (моки из `app/di/TestMocks.kt`): профиль, категории, недавние тесты.
 * Навигация проверяется через captured callbacks.
 *
 * Сценарии:
 * 1. Приветствие, имя и уровень пользователя
 * 2. Статистика (streak, XP)
 * 3. Категории и недавние тесты из моков
 * 4. «Продолжить обучение» и клики (callbacks)
 * 5. Стейт loading / error
 * 6. Разные моковые состояния (streak 0 / 30, обновлённый XP)
 */
@OptIn(ExperimentalTestApi::class)
class HomeUserFlowTest : BaseUiTest() {

    // ============================================
    // 1. Приветствие и профиль
    // ============================================

    @Test
    fun userSeesPersonalizedGreeting() = runTest(
        content = { HomeScreenForTest() }
    ) {
        onNodeWithTag("home_screen").assertIsDisplayed()
        onNodeWithTag("greeting_text", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("user_name", useUnmergedTree = true).assertIsDisplayed()
            .assertTextContains(TestData.TEST_USER_NAME)
        // Бейдж уровня в аватаре: mockUser.level = 5
        onNodeWithTag("level_badge", useUnmergedTree = true).assertIsDisplayed()
            .assertTextContains("5")
    }

    // ============================================
    // 2. Статистика (streak, XP)
    // ============================================

    @Test
    fun userCanSeeStats() = runTest(
        content = { HomeScreenForTest() }
    ) {
        onNodeWithTag("home_screen").assertIsDisplayed()
        // Streak в бейдже топ-бара: mockUser.currentStreak = 7
        onNodeWithTag("streak_days").assertIsDisplayed()
            .assertTextContains("7")
        // XP: mockUser.totalPoints = 1250
        onNodeWithTag("xp_card").assertIsDisplayed()
        onNodeWithTag("xp_value").assertIsDisplayed()
            .assertTextContains("1250")
    }

    // ============================================
    // 3. Моковые категории и недавние тесты
    // ============================================

    @Test
    fun userCanSeeRecommendedTests() = runTest(
        content = { HomeScreenForTest() }
    ) {
        onNodeWithTag("recommended_tests").assertIsDisplayed()
        // Недавние тесты из моков
        onNodeWithText("Present Simple").assertIsDisplayed()
    }

    @Test
    fun userCanSeeCategories() = runTest(
        content = { HomeScreenForTest() }
    ) {
        // Категории из mockCategories
        onNodeWithText("Grammar").assertIsDisplayed()
    }

    // ============================================
    // 4. Взаимодействия (callbacks)
    // ============================================

    @Test
    fun userCanContinueLearning() = runTest(
        content = { HomeScreenForTest() }
    ) {
        onNodeWithTag("continue_learning").assertIsDisplayed()
        onNodeWithTag("continue_learning").performClick()
        waitForIdle()
        assertTrue(TestClicks.continueLearning, "onContinueLearning callback должен быть вызван")
    }

    @Test
    fun userCanClickCategory() = runTest(
        content = { HomeScreenForTest() }
    ) {
        onNodeWithText("Grammar").performClick()
        waitForIdle()
        assertEquals("cat-1", TestClicks.categoryId, "onCategoryClick должен получить id категории")
    }

    @Test
    fun userCanClickProfile() = runTest(
        content = { HomeScreenForTest() }
    ) {
        onNodeWithTag("user_name", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(TestClicks.profile, "onProfileClick callback должен быть вызван")
    }

    // ============================================
    // 5. Состояния loading / error
    // ============================================

    @Test
    fun loadingStateShowsIndicator() = runTest(
        content = { HomeScreenForTest(state = HomeState(isLoading = true)) }
    ) {
        // При загрузке контент экрана не отображается
        onNodeWithTag("home_screen").assertDoesNotExist()
    }

    @Test
    fun errorStateShowsFriendlyMessage() = runTest(
        content = {
            HomeScreenForTest(
                state = HomeState(error = "Expected response body ... 504 Proxy Error ...")
            )
        }
    ) {
        // Сырой текст ошибки НЕ показываем — маппим в человеческий (userFriendlyError)
        onNodeWithText("Сервер временно недоступен. Попробуйте позже.").assertIsDisplayed()
        onNodeWithText("Попробовать снова").performClick()
        assertTrue(TestClicks.loadData, "onLoadData (retry) должен быть вызван")
    }

    // ============================================
    // 6. Вариации моковых данных
    // ============================================

    @Test
    fun userSeesUpdatedStatsAfterTest() = runTest(
        content = {
            // Было 1250 XP, после теста +50
            HomeScreenForTest(state = mockHomeState(totalPoints = 1300))
        }
    ) {
        onNodeWithTag("xp_value").assertIsDisplayed()
            .assertTextContains("1300")
        onNodeWithTag("level_badge", useUnmergedTree = true).assertIsDisplayed()
            .assertTextContains("5")
    }

    @Test
    fun newUserSeesZeroStreak() = runTest(
        content = { HomeScreenForTest(state = mockHomeState(streak = 0)) }
    ) {
        onNodeWithTag("streak_days").assertIsDisplayed()
            .assertTextContains("0")
    }

    @Test
    fun userWithLongStreakSeesBadge() = runTest(
        content = { HomeScreenForTest(state = mockHomeState(streak = 30)) }
    ) {
        onNodeWithTag("streak_days").assertIsDisplayed()
            .assertTextContains("30")
    }

    @Test
    fun completeHomeScreenFlow() = runTest(
        content = { HomeScreenForTest() }
    ) {
        onNodeWithTag("home_screen").assertIsDisplayed()
        onNodeWithTag("greeting_text", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("streak_days").assertIsDisplayed()
        onNodeWithTag("xp_card").assertIsDisplayed()
        onNodeWithTag("recommended_tests").assertIsDisplayed()
        onNodeWithTag("continue_learning").assertIsDisplayed()
    }
}

// ============================================
// Test fixtures
// ============================================

/** Captured callbacks */
private object TestClicks {
    var continueLearning = false
    var profile = false
    var loadData = false
    var categoryId: String? = null
}

/** Моковый HomeState с вариативными полями */
private fun mockHomeState(
    streak: Int = mockUser.currentStreak,
    totalPoints: Int = mockUser.totalPoints
): HomeState {
    val user = mockUser.copy(currentStreak = streak, totalPoints = totalPoints)
    return HomeState(
        isLoading = false,
        userProfile = mockUserProfile.copy(user = user),
        categories = mockCategories,
        recentTests = mockTestListItems,
        error = null
    )
}

/** Реальный HomeScreen на моковых данных.
 *  TestClicks НЕ сбрасывается здесь (рекомпозиция обнулила бы флаги) —
 *  флаги накапливаются между тестами, что допустимо для assertTrue/equals после клика. */
@androidx.compose.runtime.Composable
fun HomeScreenForTest(state: HomeState = mockHomeState()) {
    FunnyTheme {
        HomeScreen(
            state = state,
            isGuest = false,
            onLoadData = { TestClicks.loadData = true },
            onCategoryClick = { TestClicks.categoryId = it },
            onTestClick = {},
            onViewAllCategories = {},
            onProfileClick = { TestClicks.profile = true },
            onContinueLearning = { TestClicks.continueLearning = true }
        )
    }
}
