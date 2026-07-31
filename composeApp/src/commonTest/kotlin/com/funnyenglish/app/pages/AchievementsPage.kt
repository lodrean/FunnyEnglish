package com.funnyenglish.app.pages

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi

@OptIn(ExperimentalTestApi::class)

/**
 * Page Object для экрана ачивок.
 * 
 * Пользовательские сценарии:
 * - Просмотр списка ачивок
 * - Просмотр деталей ачивки
 * - Просмотр прогресса ачивки
 * - Фильтрация по категориям
 * - Просмотр редкости ачивки
 */
class AchievementsPage(override val compose: ComposeUiTest) : BasePage() {
    
    companion object {
        const val TAG_SCREEN = "achievements_screen"
        const val TAG_ACHIEVEMENT_LIST = "achievement_list"
        const val TAG_ACHIEVEMENT_ITEM = "achievement_item_"
        const val TAG_ACHIEVEMENT_NAME = "achievement_name"
        const val TAG_ACHIEVEMENT_DESCRIPTION = "achievement_description"
        const val TAG_ACHIEVEMENT_ICON = "achievement_icon"
        const val TAG_ACHIEVEMENT_PROGRESS = "achievement_progress"
        const val TAG_ACHIEVEMENT_RARITY = "achievement_rarity"
        const val TAG_CATEGORY_FILTER = "category_filter"
        const val TAG_EARNED_BADGE = "earned_badge"
        const val TAG_LOCKED_OVERLAY = "locked_overlay"
        const val TAG_TOTAL_POINTS = "total_points"
        const val TAG_EARNED_COUNT = "earned_count"
        const val TAG_SHARE_BUTTON = "share_button"
        const val TAG_BACK_BUTTON = "back_button"
    }
    
    /**
     * Проверить что экран ачивок отображается
     */
    fun assertScreenDisplayed() {
        assertTagDisplayed(TAG_SCREEN)
    }
    
    /**
     * Проверить что список ачивок отображается
     */
    fun assertAchievementListDisplayed() {
        assertTagDisplayed(TAG_ACHIEVEMENT_LIST)
    }
    
    /**
     * Проверить что ачивка отображается
     */
    fun assertAchievementDisplayed(achievementName: String) {
        assertTextDisplayed(achievementName)
    }
    
    /**
     * Выбрать ачивку по названию
     */
    fun selectAchievement(achievementName: String) {
        clickOnText(achievementName)
    }
    
    /**
     * Выбрать ачивку по ID
     */
    fun selectAchievementById(achievementId: String) {
        clickOnTag("${TAG_ACHIEVEMENT_ITEM}$achievementId")
    }
    
    /**
     * Проверить описание ачивки
     */
    fun assertAchievementDescription(description: String) {
        assertTextDisplayed(description)
    }
    
    /**
     * Проверить прогресс ачивки
     */
    fun assertAchievementProgress(achievementId: String, progress: String) {
        assertTagDisplayed("${TAG_ACHIEVEMENT_PROGRESS}$achievementId")
        assertContainsText("${TAG_ACHIEVEMENT_PROGRESS}$achievementId", progress)
    }
    
    /**
     * Проверить редкость ачивки
     */
    fun assertAchievementRarity(rarity: String) {
        assertTagDisplayed(TAG_ACHIEVEMENT_RARITY)
        assertContainsText(TAG_ACHIEVEMENT_RARITY, rarity)
    }
    
    /**
     * Проверить что ачивка получена (есть бейдж)
     */
    fun assertAchievementEarned(achievementId: String) {
        assertTagDisplayed("${TAG_EARNED_BADGE}$achievementId")
    }
    
    /**
     * Проверить что ачивка заблокирована (есть оверлей)
     */
    fun assertAchievementLocked(achievementId: String) {
        assertTagDisplayed("${TAG_LOCKED_OVERLAY}$achievementId")
    }
    
    /**
     * Фильтровать по категории
     */
    fun filterByCategory(category: String) {
        clickOnTag(TAG_CATEGORY_FILTER)
        clickOnText(category)
    }
    
    /**
     * Проверить общее количество очков
     */
    fun assertTotalPoints(points: String) {
        assertTagDisplayed(TAG_TOTAL_POINTS)
        assertContainsText(TAG_TOTAL_POINTS, points)
    }
    
    /**
     * Проверить количество полученных ачивок
     */
    fun assertEarnedCount(count: String) {
        assertTagDisplayed(TAG_EARNED_COUNT)
        assertContainsText(TAG_EARNED_COUNT, count)
    }
    
    /**
     * Нажать кнопку поделиться ачивкой
     */
    fun clickShare() {
        clickOnTag(TAG_SHARE_BUTTON)
    }
    
    /**
     * Нажать назад
     */
    fun clickBack() {
        clickOnTag(TAG_BACK_BUTTON)
    }
    
    /**
     * Сценарий: Просмотр деталей ачивки
     */
    fun viewAchievementDetails(achievementName: String) {
        selectAchievement(achievementName)
        waitForTag(TAG_ACHIEVEMENT_DESCRIPTION)
        assertAchievementDescription(achievementName)
    }
    
    /**
     * Сценарий: Проверка прогресса по категории
     */
    fun checkCategoryProgress(category: String) {
        filterByCategory(category)
        waitForTag(TAG_ACHIEVEMENT_LIST)
        assertAchievementListDisplayed()
    }
}
