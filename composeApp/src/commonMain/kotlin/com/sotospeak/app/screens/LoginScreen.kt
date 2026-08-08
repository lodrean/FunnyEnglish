package com.sotospeak.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.app.components.SpeakingField
import com.sotospeak.app.viewmodel.AuthState
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.animations.speakingPressable

/**
 * Экран логина по мокапу frame-login (Playful Coach v1.1).
 * Loading: кнопка disabled + «Входим…», поля формы disabled.
 */
@Composable
fun LoginScreen(
    state: AuthState,
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onClearError: () -> Unit,
    onContinueAsGuest: (() -> Unit)? = null,
    onResendVerification: (String) -> Unit = {}
) {
    val speaking = LocalSpeakingColors.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(speaking.background)
            .verticalScroll(rememberScrollState())
            // safeContent (а не systemBars): интерактивный контент не должен
            // попадать в зону gesture-навигации (грабля из Maestro e2e)
            .windowInsetsPadding(WindowInsets.safeContent)
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 24.dp)
            .testTag("login_screen")
    ) {
        Text(
            text = "С возвращением!",
            fontSize = 31.sp,
            fontWeight = FontWeight.ExtraBold,
            color = speaking.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Войди, чтобы отправлять записи учителю и видеть оценки",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = speaking.textMuted,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

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
            modifier = Modifier.testTag("login_email_field"),
            inputModifier = Modifier.testTag("login_email_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        SpeakingField(
            value = password,
            onValueChange = {
                if (state.error != null) onClearError()
                password = it
            },
            label = "Пароль",
            enabled = !state.isLoading,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                if (email.isNotBlank() && password.isNotBlank()) {
                    onLogin(email, password)
                }
            },
            modifier = Modifier.testTag("login_password_field"),
            inputModifier = Modifier.testTag("login_password_input")
        )

        // Плашка «Подтвердите почту» при 403 EMAIL_NOT_VERIFIED (email-верификация flag=on)
        if (state.emailNotVerified) {
            Spacer(modifier = Modifier.height(16.dp))
            com.sotospeak.app.components.SpeakingGate(
                emoji = "📬",
                title = "Подтвердите почту",
                text = "На $email отправлено письмо со ссылкой. Подтвердите email и войдите снова.",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_not_verified_panel")
            ) {
                Button(
                    onClick = { onResendVerification(email) },
                    enabled = !state.isLoading && email.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("login_resend_verification_button"),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(if (state.isLoading) "Отправляем…" else "Отправить письмо повторно")
                }
            }
            if (state.verificationResent) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Письмо отправлено повторно",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = speaking.primary,
                    modifier = Modifier.testTag("login_verification_resent_message")
                )
            }
        }

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
                    .testTag("login_error_message")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val loginIsrc = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        Button(
            onClick = { onLogin(email, password) },
            enabled = !state.isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .speakingPressable(loginIsrc)
                .testTag("login_button"),
            shape = MaterialTheme.shapes.medium,
            interactionSource = loginIsrc
        ) {
            Text(if (state.isLoading) "Входим…" else "Войти")
        }

        // weight в scrollable Column запрещён — фиксированный отступ вместо margin-top:auto
        Spacer(modifier = Modifier.height(48.dp))

        // .auth-links
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("register_link"),
                shape = MaterialTheme.shapes.medium
            ) {
                Row {
                    Text(
                        text = "Нет аккаунта? ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = speaking.textMuted
                    )
                    Text(
                        text = "Регистрация",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            onContinueAsGuest?.let {
                OutlinedButton(
                    onClick = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("login_guest_button"),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Продолжить как гость")
                }
            }
        }
    }
}
