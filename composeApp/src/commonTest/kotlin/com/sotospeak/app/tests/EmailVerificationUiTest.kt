package com.sotospeak.app.tests

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.sotospeak.app.screens.LoginScreen
import com.sotospeak.app.screens.RegisterScreen
import com.sotospeak.app.viewmodel.AuthState
import com.sotospeak.shared.model.AuthMode
import com.sotospeak.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI-тесты состояний email-верификации (OpenSpec add-email-verification):
 * RegisterScreen «Проверьте почту», LoginScreen плашка EMAIL_NOT_VERIFIED + resend.
 * Паттерн: реальные экраны + моковый state + captured callbacks (грабля №16).
 */
@OptIn(ExperimentalTestApi::class)
class EmailVerificationUiTest {

    private object Clicks {
        var resendTo: String? = null
        var toLogin = false
        fun reset() { resendTo = null; toLogin = false }
    }

    @Test
    fun registerShowsCheckEmailStateWhenVerificationSent() = runComposeUiTest {
        Clicks.reset()
        setContent {
            FunnyTheme {
                RegisterScreen(
                    state = AuthState(
                        mode = AuthMode.GUEST,
                        verificationEmailSentTo = "new@example.com"
                    ),
                    onRegister = { _, _, _ -> },
                    onNavigateToLogin = { Clicks.toLogin = true },
                    onClearError = {},
                    onResendVerification = { Clicks.resendTo = it }
                )
            }
        }

        // Состояние «Проверьте почту» вместо формы
        onNodeWithTag("check_email_screen", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Проверьте почту", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("new@example.com", substring = true, useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("register_button", useUnmergedTree = true).assertDoesNotExist()

        // Resend → callback с email
        onNodeWithTag("resend_verification_button", useUnmergedTree = true).performClick()
        assertEquals("new@example.com", Clicks.resendTo)

        // Ссылка на логин
        onNodeWithTag("check_email_login_link", useUnmergedTree = true).performClick()
        assertTrue(Clicks.toLogin)
    }

    @Test
    fun registerShowsResentConfirmation() = runComposeUiTest {
        Clicks.reset()
        setContent {
            FunnyTheme {
                RegisterScreen(
                    state = AuthState(
                        mode = AuthMode.GUEST,
                        verificationEmailSentTo = "new@example.com",
                        verificationResent = true
                    ),
                    onRegister = { _, _, _ -> },
                    onNavigateToLogin = {},
                    onClearError = {},
                    onResendVerification = {}
                )
            }
        }
        onNodeWithTag("verification_resent_message", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun loginShowsEmailNotVerifiedPanelWithResend() = runComposeUiTest {
        Clicks.reset()
        setContent {
            FunnyTheme {
                LoginScreen(
                    state = AuthState(mode = AuthMode.GUEST, emailNotVerified = true),
                    onLogin = { _, _ -> },
                    onNavigateToRegister = {},
                    onClearError = {},
                    onResendVerification = { Clicks.resendTo = it }
                )
            }
        }

        // Плашка скрыта до ввода email? Нет — плашка видна сразу, кнопка disabled без email
        onNodeWithTag("email_not_verified_panel", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Подтвердите почту", useUnmergedTree = true).assertIsDisplayed()

        // Вводим email → resend активна → callback
        onNode(
            hasAnyAncestor(hasTestTag("login_email_field")) and hasSetTextAction(),
            useUnmergedTree = true
        ).performTextInput("unverified@example.com")
        onNodeWithTag("login_resend_verification_button", useUnmergedTree = true).performClick()
        assertEquals("unverified@example.com", Clicks.resendTo)
    }

    @Test
    fun loginWithoutFlagShowsNoVerificationPanel() = runComposeUiTest {
        setContent {
            FunnyTheme {
                LoginScreen(
                    state = AuthState(mode = AuthMode.UNKNOWN),
                    onLogin = { _, _ -> },
                    onNavigateToRegister = {},
                    onClearError = {}
                )
            }
        }
        onNodeWithTag("email_not_verified_panel", useUnmergedTree = true).assertDoesNotExist()
    }
}
