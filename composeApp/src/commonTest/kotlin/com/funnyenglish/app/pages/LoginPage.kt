package com.funnyenglish.app.pages

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi

@OptIn(ExperimentalTestApi::class)

/**
 * Page Object для экрана логина.
 * 
 * Пользовательские сценарии:
 * - Вход с валидными credentials
 * - Вход с невалидными credentials  
 * - Валидация полей (пустые поля, невалидный email)
 * - Переход к регистрации
 */
class LoginPage(override val compose: ComposeUiTest) : BasePage() {
    
    // Test Tags - должны совпадать с тем что в composable
    companion object {
        const val TAG_EMAIL_INPUT = "login_email_input"
        const val TAG_PASSWORD_INPUT = "login_password_input"
        const val TAG_LOGIN_BUTTON = "login_button"
        const val TAG_ERROR_MESSAGE = "login_error_message"
        const val TAG_LOADING_INDICATOR = "login_loading"
        const val TAG_REGISTER_LINK = "login_register_link"
        const val TAG_LOGO = "login_logo"
    }
    
    /**
     * Проверить что экран логина отображается
     */
    fun assertScreenDisplayed() {
        assertTagDisplayed(TAG_LOGO)
        assertTagDisplayed(TAG_EMAIL_INPUT)
        assertTagDisplayed(TAG_PASSWORD_INPUT)
        assertTagDisplayed(TAG_LOGIN_BUTTON)
    }
    
    /**
     * Ввести email
     */
    fun enterEmail(email: String) {
        enterText(TAG_EMAIL_INPUT, email)
    }
    
    /**
     * Ввести пароль
     */
    fun enterPassword(password: String) {
        enterText(TAG_PASSWORD_INPUT, password)
    }
    
    /**
     * Нажать кнопку входа
     */
    fun clickLogin() {
        clickOnTag(TAG_LOGIN_BUTTON)
    }
    
    /**
     * Полный сценарий входа
     */
    fun login(email: String, password: String) {
        enterEmail(email)
        enterPassword(password)
        clickLogin()
    }
    
    /**
     * Проверить что показывается ошибка
     */
    fun assertErrorDisplayed(errorMessage: String) {
        waitForText(errorMessage)
        assertTextDisplayed(errorMessage)
    }
    
    /**
     * Проверить что кнопка входа отключена (при пустых полях)
     */
    fun assertLoginButtonDisabled() {
        assertIsDisabled(TAG_LOGIN_BUTTON)
    }
    
    /**
     * Проверить что кнопка входа включена
     */
    fun assertLoginButtonEnabled() {
        assertIsEnabled(TAG_LOGIN_BUTTON)
    }
    
    /**
     * Проверить что индикатор загрузки отображается
     */
    fun assertLoadingDisplayed() {
        assertTagDisplayed(TAG_LOADING_INDICATOR)
    }
    
    /**
     * Нажать на ссылку регистрации
     */
    fun clickRegisterLink() {
        clickOnTag(TAG_REGISTER_LINK)
    }
    
    /**
     * Очистить поля
     */
    fun clearFields() {
        clearText(TAG_EMAIL_INPUT)
        clearText(TAG_PASSWORD_INPUT)
    }
    
    /**
     * Сценарий: Попытка входа с пустыми полями
     */
    fun attemptLoginWithEmptyFields() {
        clearFields()
        assertLoginButtonDisabled()
    }
    
    /**
     * Сценарий: Попытка входа с невалидным email
     */
    fun attemptLoginWithInvalidEmail() {
        enterEmail("invalid-email")
        enterPassword("password123")
        // Кнопка должна быть отключена или показать ошибку валидации
    }
}
