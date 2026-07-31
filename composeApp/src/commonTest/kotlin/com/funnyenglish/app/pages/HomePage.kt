package com.funnyenglish.app.pages

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi

@OptIn(ExperimentalTestApi::class)

/**
 * Page Object для главного экрана (Home/Dashboard).
 * 
 * Пользовательские сценарии:
 * - Просмотр приветствия
 * - Просмотр статистики (streak, XP)
 * - Быстрый доступ к тестам
 * - Просмотр рекомендаций
 * - Навигация к профилю
 * - Навигация к ачивкам
 */
class HomePage(override val compose: ComposeUiTest) : BasePage() {
    
    companion object {
        const val TAG_SCREEN = "home_screen"
        const val TAG_GREETING = "greeting_text"
        const val TAG_USER_NAME = "user_name"
        const val TAG_STREAK_CARD = "streak_card"
        const val TAG_STREAK_DAYS = "streak_days"
        const val TAG_XP_CARD = "xp_card"
        const val TAG_XP_VALUE = "xp_value"
        const val TAG_LEVEL_BADGE = "level_badge"
        const val TAG_RECOMMENDED_TESTS = "recommended_tests"
        const val TAG_CONTINUE_LEARNING = "continue_learning"
        const val TAG_LEADERBOARD_PREVIEW = "leaderboard_preview"
        const val TAG_ACHIEVEMENT_PREVIEW = "achievement_preview"
        const val TAG_BOTTOM_NAV = "bottom_navigation"
        const val NAV_TESTS = "nav_tests"
        const val NAV_PROFILE = "nav_profile"
        const val NAV_LEADERBOARD = "nav_leaderboard"
        const val NAV_SETTINGS = "nav_settings"
        const val TAG_SETTINGS_BUTTON = "settings_button"
        const val TAG_NOTIFICATIONS_BUTTON = "notifications_button"
    }
    
    /**
     * Проверить что главный экран отображается
     */
    fun assertScreenDisplayed() {
        assertTagDisplayed(TAG_SCREEN)
    }
    
    /**
     * Проверить приветствие
     */
    fun assertGreetingDisplayed() {
        assertTagDisplayed(TAG_GREETING)
    }
    
    /**
     * Проверить имя пользователя
     */
    fun assertUserNameDisplayed(name: String) {
        assertTagDisplayed(TAG_USER_NAME)
        assertContainsText(TAG_USER_NAME, name)
    }
    
    /**
     * Проверить карточку streak
     */
    fun assertStreakCardDisplayed() {
        assertTagDisplayed(TAG_STREAK_CARD)
    }
    
    /**
     * Проверить количество дней streak
     */
    fun assertStreakDays(days: String) {
        assertTagDisplayed(TAG_STREAK_DAYS)
        assertContainsText(TAG_STREAK_DAYS, days)
    }
    
    /**
     * Проверить карточку XP
     */
    fun assertXpCardDisplayed() {
        assertTagDisplayed(TAG_XP_CARD)
    }
    
    /**
     * Проверить значение XP
     */
    fun assertXpValue(xp: String) {
        assertTagDisplayed(TAG_XP_VALUE)
        assertContainsText(TAG_XP_VALUE, xp)
    }
    
    /**
     * Проверить бейдж уровня
     */
    fun assertLevelBadgeDisplayed(level: String) {
        assertTagDisplayed(TAG_LEVEL_BADGE)
        assertContainsText(TAG_LEVEL_BADGE, level)
    }
    
    /**
     * Проверить секцию рекомендуемых тестов
     */
    fun assertRecommendedTestsDisplayed() {
        assertTagDisplayed(TAG_RECOMMENDED_TESTS)
    }
    
    /**
     * Выбрать рекомендуемый тест
     */
    fun selectRecommendedTest(testName: String) {
        clickOnText(testName)
    }
    
    /**
     * Проверить секцию "Продолжить обучение"
     */
    fun assertContinueLearningDisplayed() {
        assertTagDisplayed(TAG_CONTINUE_LEARNING)
    }
    
    /**
     * Продолжить незавершенный тест/урок
     */
    fun clickContinueLearning() {
        clickOnTag(TAG_CONTINUE_LEARNING)
    }
    
    /**
     * Проверить превью лидерборда
     */
    fun assertLeaderboardPreviewDisplayed() {
        assertTagDisplayed(TAG_LEADERBOARD_PREVIEW)
    }
    
    /**
     * Нажать на превью лидерборда (перейти к полному)
     */
    fun clickLeaderboardPreview() {
        clickOnTag(TAG_LEADERBOARD_PREVIEW)
    }
    
    /**
     * Проверить превью ачивок
     */
    fun assertAchievementPreviewDisplayed() {
        assertTagDisplayed(TAG_ACHIEVEMENT_PREVIEW)
    }
    
    /**
     * Нажать на превью ачивок
     */
    fun clickAchievementPreview() {
        clickOnTag(TAG_ACHIEVEMENT_PREVIEW)
    }
    
    /**
     * Проверить нижнюю навигацию
     */
    fun assertBottomNavDisplayed() {
        assertTagDisplayed(TAG_BOTTOM_NAV)
    }
    
    /**
     * Перейти на вкладку Тесты
     */
    fun navigateToTests() {
        clickOnTag(NAV_TESTS)
    }
    
    /**
     * Перейти на вкладку Профиль
     */
    fun navigateToProfile() {
        clickOnTag(NAV_PROFILE)
    }
    
    /**
     * Перейти на вкладку Лидерборд
     */
    fun navigateToLeaderboard() {
        clickOnTag(NAV_LEADERBOARD)
    }
    
    /**
     * Перейти на вкладку Настройки
     */
    fun navigateToSettings() {
        clickOnTag(NAV_SETTINGS)
    }
    
    /**
     * Нажать на кнопку настроек
     */
    fun clickSettings() {
        clickOnTag(TAG_SETTINGS_BUTTON)
    }
    
    /**
     * Нажать на кнопку уведомлений
     */
    fun clickNotifications() {
        clickOnTag(TAG_NOTIFICATIONS_BUTTON)
    }
    
    /**
     * Сценарий: Проверка статистики пользователя
     */
    fun verifyUserStats(name: String, streak: String, xp: String, level: String) {
        assertUserNameDisplayed(name)
        assertStreakDays(streak)
        assertXpValue(xp)
        assertLevelBadgeDisplayed(level)
    }
    
    /**
     * Сценарий: Навигация через bottom bar
     */
    fun navigateThroughTabs() {
        navigateToTests()
        navigateToProfile()
        navigateToLeaderboard()
        navigateToSettings()
    }
}
