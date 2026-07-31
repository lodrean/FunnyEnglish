package com.funnyenglish.app.pages

import androidx.compose.ui.test.*
import androidx.compose.ui.test.ExperimentalTestApi

@OptIn(ExperimentalTestApi::class)

/**
 * Базовый класс для Page Object паттерна в UI тестах.
 * 
 * Page Object Pattern позволяет:
 * 1. Инкапсулировать детали UI (selectors, взаимодействия)
 * 2. Переиспользовать код между тестами
 * 3. Легко обновлять тесты при изменении UI
 * 
 * Пример использования:
 * ```kotlin
 * val loginPage = LoginPage(composeTestRule)
 * loginPage.enterEmail("user@test.com")
 * loginPage.enterPassword("password")
 * loginPage.clickLogin()
 * ```
 */
abstract class BasePage {
    abstract val compose: ComposeUiTest
    
    /**
     * Ждать пока элемент с текстом появится
     */
    fun waitForText(text: String, timeoutMillis: Long = 5000) {
        compose.waitUntil(timeoutMillis) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
    
    /**
     * Ждать пока элемент с тегом появится
     */
    fun waitForTag(tag: String, timeoutMillis: Long = 5000) {
        compose.waitUntil(timeoutMillis) {
            compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
    
    /**
     * Проверить что текст отображается
     */
    fun assertTextDisplayed(text: String) {
        compose.onNodeWithText(text).assertIsDisplayed()
    }
    
    /**
     * Проверить что элемент с тегом отображается.
     * Перед проверкой скроллит до элемента (если он в скроллящемся контейнере) —
     * иначе элементы «ниже сгиба» тестового окна падают с «not displayed».
     */
    fun assertTagDisplayed(tag: String) {
        val node = compose.onNodeWithTag(tag, useUnmergedTree = true)
        try {
            node.performScrollTo()
        } catch (_: Throwable) {
            // Не в скроллящемся контейнере (performScrollTo бросает AssertionError) — ок
        }
        node.assertIsDisplayed()
    }
    
    /**
     * Нажать на элемент с текстом
     */
    fun clickOnText(text: String) {
        compose.onNodeWithText(text).performClick()
    }
    
    /**
     * Нажать на элемент с тегом
     */
    fun clickOnTag(tag: String) {
        compose.onNodeWithTag(tag).performClick()
    }
    
    /**
     * Ввести текст в поле
     */
    fun enterText(tag: String, text: String) {
        compose.onNodeWithTag(tag).performTextInput(text)
    }
    
    /**
     * Очистить поле ввода
     */
    fun clearText(tag: String) {
        compose.onNodeWithTag(tag).performTextClearance()
    }
    
    /**
     * Проверить что элемент содержит текст
     */
    fun assertContainsText(tag: String, text: String) {
        compose.onNodeWithTag(tag).assertTextContains(text)
    }
    
    /**
     * Проверить что элемент отключен
     */
    fun assertIsDisabled(tag: String) {
        compose.onNodeWithTag(tag).assertIsNotEnabled()
    }
    
    /**
     * Проверить что элемент включен
     */
    fun assertIsEnabled(tag: String) {
        compose.onNodeWithTag(tag).assertIsEnabled()
    }
    
    /**
     * Свайп вниз (pull to refresh)
     */
    fun swipeDown(tag: String = "refresh") {
        compose.onNodeWithTag(tag).performTouchInput {
            swipeDown()
        }
    }
    
    /**
     * Нажать кнопку назад
     */
    fun pressBack() {
        compose.runOnIdle { }
        // В реальном приложении можно использовать LocalSoftwareKeyboardController
        // или специфичные для платформы методы
    }
}

/**
 * Расширение для ожидания условия с таймаутом
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.waitUntil(timeoutMillis: Long = 5000, condition: () -> Boolean) {
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < timeoutMillis) {
        if (condition()) return
        Thread.sleep(100)
    }
    throw AssertionError("Condition not met within ${timeoutMillis}ms")
}
