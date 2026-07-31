package com.funnyenglish.app.tests

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.funnyenglish.app.di.mockAchievements
import com.funnyenglish.app.screens.AchievementScreen
import com.funnyenglish.app.viewmodel.AchievementsState
import com.funnyenglish.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * UI тесты экрана достижений на МОКОВЫХ данных.
 *
 * Рендерится РЕАЛЬНЫЙ [AchievementScreen] с моковым [AchievementsState]
 * (mockAchievements: First Steps / Streak Master / Word Wizard).
 *
 * Сценарии:
 * 1. Список достижений
 * 2. Статистика прогресса (для авторизованного / гостя)
 * 3. Фильтрация по категориям
 * 4. Редкость (RarityBadge)
 * 5. Состояние загрузки, кнопка «Назад»
 */
@OptIn(ExperimentalTestApi::class)
class AchievementUserFlowTest : BaseUiTest() {

    @Test
    fun userCanViewAchievements() = runTest(
        content = { AchievementsScreenForTest() }
    ) {
        onNodeWithText("Достижения").assertIsDisplayed()
        onNodeWithText("First Steps").assertIsDisplayed()
        onNodeWithText("Streak Master").assertIsDisplayed()
    }

    @Test
    fun authenticatedUserSeesEarnedStats() = runTest(
        content = { AchievementsScreenForTest() }
    ) {
        // Для авторизованного все моковые ачивки earned (isEarned = !isGuest)
        onNodeWithText("Твой прогресс").assertIsDisplayed()
        onNodeWithText("/3", substring = true).assertIsDisplayed()
    }

    @Test
    fun guestSeesLockedAchievements() = runTest(
        content = { AchievementsScreenForTest(isGuest = true) }
    ) {
        // Гость: ачивки закрыты гейтингом (LockedFeature) с CTA регистрации
        onNodeWithTag("locked_feature").assertIsDisplayed()
        onNodeWithTag("locked_feature_register_button").assertIsDisplayed()
        onNodeWithText("First Steps").assertDoesNotExist()
    }

    @Test
    fun userCanFilterByCategory() = runTest(
        content = { AchievementsScreenForTest() }
    ) {
        // Фильтр «Серия» (CONSISTENCY): остаётся только Streak Master
        onNodeWithText("🔥 Серия").performClick()
        waitForIdle()
        onNodeWithText("Streak Master").assertIsDisplayed()
        onNodeWithText("Word Wizard").assertDoesNotExist()
    }

    @Test
    fun rarityBadgeIsDisplayed() = runTest(
        content = { AchievementsScreenForTest() }
    ) {
        // Streak Master имеет rarity RARE (бейдж локализован)
        onNodeWithText("Редкое").assertIsDisplayed()
    }

    @Test
    fun loadingStateShowsIndicator() = runTest(
        content = { AchievementsScreenForTest(state = AchievementsState(isLoading = true)) }
    ) {
        onNodeWithText("First Steps").assertDoesNotExist()
    }

    @Test
    fun backButtonCallsCallback() = runTest(
        content = { AchievementsScreenForTest() }
    ) {
        onNodeWithContentDescription("Назад").performClick()
        waitForIdle()
        assertTrue(AchievementClicks.back, "onBack должен быть вызван")
    }
}

// ============================================
// Test fixtures
// ============================================

private object AchievementClicks {
    var back = false
}

/** Реальный AchievementScreen на моковых данных */
@Composable
fun AchievementsScreenForTest(
    state: AchievementsState = AchievementsState(achievements = mockAchievements),
    isGuest: Boolean = false
) {
    FunnyTheme {
        AchievementScreen(
            state = state,
            isGuest = isGuest,
            onLoad = {},
            onBack = { AchievementClicks.back = true }
        )
    }
}
