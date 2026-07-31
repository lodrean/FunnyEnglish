package com.funnyenglish.app.tests

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.funnyenglish.app.screens.LoginScreen
import com.funnyenglish.shared.model.AuthMode
import com.funnyenglish.app.viewmodel.AuthState
import com.funnyenglish.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI тесты экрана логина на МОКОВЫХ данных.
 *
 * Рендерится РЕАЛЬНЫЙ [LoginScreen] с моковым [AuthState];
 * «API» заменено captured callbacks (onLogin и др.).
 *
 * Сценарии:
 * 1. Вход с валидными credentials (callback получает email/password)
 * 2. Отображение ошибки из state
 * 3. Валидация пустых полей (кнопка отключена/включена)
 * 4. Индикатор загрузки (isLoading)
 * 5. Гостевой режим и переход к регистрации
 */
@OptIn(ExperimentalTestApi::class)
class LoginUserFlowTest : BaseUiTest() {

    @Test
    fun userCanLoginWithValidCredentials() = runTest(
        content = { LoginScreenForTest() }
    ) {
        onNodeWithTag("login_email_field").assertIsDisplayed()
        enterTextByTag("login_email_field", TestData.TEST_USER_EMAIL)
        enterTextByTag("login_password_field", TestData.TEST_USER_PASSWORD)
        onNodeWithTag("login_button").performClick()
        waitForIdle()

        assertEquals(TestData.TEST_USER_EMAIL, LoginClicks.email)
        assertEquals(TestData.TEST_USER_PASSWORD, LoginClicks.password)
    }

    @Test
    fun userSeesErrorFromState() = runTest(
        content = {
            LoginScreenForTest(state = AuthState(error = "Invalid email or password"))
        }
    ) {
        onNodeWithText("Invalid email or password").assertIsDisplayed()
    }

    @Test
    fun loginButtonDisabledWhenFieldsEmpty() = runTest(
        content = { LoginScreenForTest() }
    ) {
        onNodeWithTag("login_button").assertIsNotEnabled()
    }

    @Test
    fun loginButtonEnabledWhenFieldsFilled() = runTest(
        content = { LoginScreenForTest() }
    ) {
        enterTextByTag("login_email_field", TestData.TEST_USER_EMAIL)
        enterTextByTag("login_password_field", TestData.TEST_USER_PASSWORD)
        onNodeWithTag("login_button").assertIsEnabled()
    }

    @Test
    fun loadingStateDisablesButtonAndChangesText() = runTest(
        content = { LoginScreenForTest(state = AuthState(isLoading = true)) }
    ) {
        // Кнопка отключена и показывает «Вход...» во время загрузки
        onNodeWithTag("login_button").assertIsNotEnabled()
        onNodeWithText("Вход...").assertIsDisplayed()
    }

    @Test
    fun userCanContinueAsGuest() = runTest(
        content = { LoginScreenForTest() }
    ) {
        // Кнопка внизу скроллящегося экрана — скроллим до неё перед кликом
        onNodeWithText("Продолжить без регистрации", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        waitForIdle()
        assertTrue(LoginClicks.guest, "onContinueAsGuest должен быть вызван")
    }

    @Test
    fun userCanNavigateToRegister() = runTest(
        content = { LoginScreenForTest() }
    ) {
        onNodeWithTag("register_link", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        waitForIdle()
        assertTrue(LoginClicks.register, "onNavigateToRegister должен быть вызван")
    }

    @Test
    fun completeLoginFlowTest() = runTest(
        content = { LoginScreenForTest() }
    ) {
        // 1. Начальное состояние — кнопка отключена
        onNodeWithTag("login_button").assertIsNotEnabled()

        // 2. Только email — всё ещё отключена
        enterTextByTag("login_email_field", TestData.TEST_USER_EMAIL)
        onNodeWithTag("login_button").assertIsNotEnabled()

        // 3. Заполняем пароль — включена
        enterTextByTag("login_password_field", TestData.TEST_USER_PASSWORD)
        onNodeWithTag("login_button").assertIsEnabled()

        // 4. Вход — callback получает credentials
        onNodeWithTag("login_button").performClick()
        waitForIdle()
        assertEquals(TestData.TEST_USER_EMAIL, LoginClicks.email)
    }
}

// ============================================
// Test fixtures
// ============================================

/** Captured callbacks LoginScreen */
private object LoginClicks {
    var email: String? = null
    var password: String? = null
    var register = false
    var guest = false
}

/** Ввод текста в FunnyTextField: testTag стоит на обёртке,
 *  сам editable — потомок с SetText action */
@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.enterTextByTag(tag: String, text: String) {
    onNode(hasAnyAncestor(hasTestTag(tag)) and hasSetTextAction(), useUnmergedTree = true)
        .performTextInput(text)
}

/** Реальный LoginScreen на моковых данных */
@Composable
fun LoginScreenForTest(state: AuthState = AuthState(mode = AuthMode.UNKNOWN)) {
    FunnyTheme {
        LoginScreen(
            state = state,
            onLogin = { email, password ->
                LoginClicks.email = email
                LoginClicks.password = password
            },
            onNavigateToRegister = { LoginClicks.register = true },
            onClearError = {},
            onContinueAsGuest = { LoginClicks.guest = true }
        )
    }
}
