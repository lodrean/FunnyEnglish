package com.sotospeak.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.app.components.SpeakingField
import com.sotospeak.app.viewmodel.AuthState
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Экран регистрации по мокапу frame-register (Playful Coach v1.1).
 * Loading: кнопка disabled + «Создаём…», поля формы disabled.
 */
@Composable
fun RegisterScreen(
    state: AuthState,
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onClearError: () -> Unit,
    onResendVerification: (String) -> Unit = {}
) {
    val speaking = LocalSpeakingColors.current
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Email-верификация (flag=on): после регистрации — «Проверьте почту» вместо auto-login
    val sentTo = state.verificationEmailSentTo
    if (sentTo != null) {
        CheckEmailContent(
            email = sentTo,
            isLoading = state.isLoading,
            resent = state.verificationResent,
            onResend = { onResendVerification(sentTo) },
            onNavigateToLogin = onNavigateToLogin
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(speaking.background)
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeContent)
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 24.dp)
            .testTag("register_screen")
    ) {
        Text(
            text = "Создай аккаунт",
            fontSize = 31.sp,
            fontWeight = FontWeight.ExtraBold,
            color = speaking.text
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Чтобы отправлять записи учителю и получать оценки",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = speaking.textMuted,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        SpeakingField(
            value = displayName,
            onValueChange = {
                if (state.error != null) onClearError()
                displayName = it
            },
            label = "Имя",
            placeholder = "Как тебя зовут?",
            enabled = !state.isLoading,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            modifier = Modifier.testTag("register_name_field"),
            inputModifier = Modifier.testTag("register_name_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        SpeakingField(
            value = email,
            onValueChange = {
                if (state.error != null) onClearError()
                email = it
            },
            label = "Email",
            placeholder = "you@example.com",
            enabled = !state.isLoading,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            modifier = Modifier.testTag("register_email_field"),
            inputModifier = Modifier.testTag("register_email_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        SpeakingField(
            value = password,
            onValueChange = {
                if (state.error != null) onClearError()
                password = it
            },
            label = "Пароль",
            placeholder = "Минимум 6 символов",
            enabled = !state.isLoading,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                if (email.isNotBlank() && password.isNotBlank() && displayName.isNotBlank()) {
                    onRegister(email, password, displayName)
                }
            },
            modifier = Modifier.testTag("register_password_field"),
            inputModifier = Modifier.testTag("register_password_input")
        )

        // Ошибка сервера (state.error) — текст ошибки #B3261E (WCAG AA)
        if (state.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = speaking.errorText,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_error_message")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onRegister(email, password, displayName) },
            enabled = !state.isLoading &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                displayName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .testTag("register_button"),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(if (state.isLoading) "Создаём…" else "Создать аккаунт")
        }

        // weight в scrollable Column запрещён — фиксированный отступ вместо margin-top:auto
        Spacer(modifier = Modifier.height(48.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("login_link"),
                shape = MaterialTheme.shapes.medium
            ) {
                Row {
                    Text(
                        text = "Уже есть аккаунт? ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = speaking.textMuted
                    )
                    Text(
                        text = "Войти",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Состояние «Проверьте почту» после регистрации (email-верификация flag=on).
 * Стиль Playful Coach: SpeakingGate 📬 + resend + ссылка на логин.
 */
@Composable
private fun CheckEmailContent(
    email: String,
    isLoading: Boolean,
    resent: Boolean,
    onResend: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(speaking.background)
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeContent)
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 24.dp)
            .testTag("check_email_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        com.sotospeak.app.components.SpeakingGate(
            emoji = "📬",
            title = "Проверьте почту",
            text = "Мы отправили письмо со ссылкой на $email. Перейдите по ней, чтобы подтвердить аккаунт и войти.",
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onResend,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .testTag("resend_verification_button"),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(if (isLoading) "Отправляем…" else "Отправить письмо повторно")
            }
        }
        if (resent) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Письмо отправлено повторно",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = speaking.primary,
                modifier = Modifier.testTag("verification_resent_message")
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("check_email_login_link"),
            shape = MaterialTheme.shapes.medium
        ) {
            Row {
                Text(
                    text = "Уже подтвердили? ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.textMuted
                )
                Text(
                    text = "Войти",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
