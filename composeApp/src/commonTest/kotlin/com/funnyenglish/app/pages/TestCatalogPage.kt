package com.funnyenglish.app.pages

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi

@OptIn(ExperimentalTestApi::class)

/**
 * Page Object для экрана каталога тестов.
 * 
 * Пользовательские сценарии:
 * - Просмотр списка категорий
 * - Выбор категории
 * - Просмотр списка тестов
 * - Поиск теста
 * - Пулл-ту-рефреш
 * - Переход к прохождению теста
 */
class TestCatalogPage(override val compose: ComposeUiTest) : BasePage() {
    
    companion object {
        const val TAG_SCREEN = "test_catalog_screen"
        const val TAG_CATEGORY_LIST = "category_list"
        const val TAG_TEST_LIST = "test_list"
        const val TAG_SEARCH_INPUT = "test_search_input"
        const val TAG_REFRESH_INDICATOR = "refresh_indicator"
        const val TAG_ERROR_VIEW = "error_view"
        const val TAG_EMPTY_VIEW = "empty_view"
        const val TAG_TEST_ITEM = "test_item_"
        const val TAG_CATEGORY_ITEM = "category_item_"
        const val TAG_USER_LEVEL = "user_level"
        const val TAG_USER_XP = "user_xp"
    }
    
    /**
     * Проверить что экран каталога отображается
     */
    fun assertScreenDisplayed() {
        assertTagDisplayed(TAG_SCREEN)
    }
    
    /**
     * Проверить что список категорий отображается
     */
    fun assertCategoriesDisplayed() {
        assertTagDisplayed(TAG_CATEGORY_LIST)
    }
    
    /**
     * Выбрать категорию по названию
     */
    fun selectCategory(categoryName: String) {
        clickOnText(categoryName)
    }
    
    /**
     * Выбрать категорию по ID
     */
    fun selectCategoryById(categoryId: String) {
        clickOnTag("${TAG_CATEGORY_ITEM}$categoryId")
    }
    
    /**
     * Проверить что тест отображается в списке
     */
    fun assertTestDisplayed(testName: String) {
        assertTextDisplayed(testName)
    }
    
    /**
     * Выбрать тест для прохождения
     */
    fun selectTest(testName: String) {
        clickOnText(testName)
    }
    
    /**
     * Выбрать тест по ID
     */
    fun selectTestById(testId: String) {
        clickOnTag("${TAG_TEST_ITEM}$testId")
    }
    
    /**
     * Выполнить поиск теста
     */
    fun searchTest(query: String) {
        enterText(TAG_SEARCH_INPUT, query)
    }
    
    /**
     * Очистить поиск
     */
    fun clearSearch() {
        clearText(TAG_SEARCH_INPUT)
    }
    
    /**
     * Pull-to-refresh
     */
    fun pullToRefresh() {
        swipeDown(TAG_CATEGORY_LIST)
    }
    
    /**
     * Проверить что отображается ошибка загрузки
     */
    fun assertErrorDisplayed() {
        assertTagDisplayed(TAG_ERROR_VIEW)
    }
    
    /**
     * Проверить что отображается пустое состояние
     */
    fun assertEmptyStateDisplayed() {
        assertTagDisplayed(TAG_EMPTY_VIEW)
    }
    
    /**
     * Проверить уровень пользователя
     */
    fun assertUserLevelDisplayed(level: String) {
        assertTagDisplayed(TAG_USER_LEVEL)
        assertContainsText(TAG_USER_LEVEL, level)
    }
    
    /**
     * Проверить XP пользователя
     */
    fun assertUserXpDisplayed(xp: String) {
        assertTagDisplayed(TAG_USER_XP)
        assertContainsText(TAG_USER_XP, xp)
    }
    
    /**
     * Сценарий: Просмотр тестов категории
     */
    fun viewTestsInCategory(categoryName: String) {
        assertCategoriesDisplayed()
        selectCategory(categoryName)
        waitForTag(TAG_TEST_LIST)
        assertTagDisplayed(TAG_TEST_LIST)
    }
    
    /**
     * Сценарий: Поиск теста
     */
    fun findTestBySearch(query: String, expectedTestName: String) {
        searchTest(query)
        waitForText(expectedTestName)
        assertTestDisplayed(expectedTestName)
    }
}
