package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.components.inputs.FunnyTextField
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.app.viewmodel.AuthState
import com.funnyenglish.designsystem.theme.funnyColors

@Composable
fun LoginScreen(
    state: AuthState,
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onClearError: () -> Unit,
    onContinueAsGuest: (() -> Unit)? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var oauthMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // safeContent (а не systemBars): интерактивный контент не должен
                // попадать в зону gesture-навигации (кнопка «Продолжить без регистрации»
                // была перекрыта жестовой панелью и не нажималась — BUG, найден Maestro e2e)
                .windowInsetsPadding(WindowInsets.safeContent)
                .imePadding()
                .padding(horizontal = SpaceLg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo
            AppLogo()

            Spacer(modifier = Modifier.height(48.dp))

            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (MaterialTheme.funnyColors.isDark)
                        ElevationSmall else ElevationMedium
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpaceXl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "С возвращением!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(SpaceSm))

                    Text(
                        text = "Войди, чтобы продолжить обучение",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(SpaceXl))

                    // Email field
                    FunnyTextField(
                        value = email,
                        onValueChange = {
                            if (state.error != null) onClearError()
                            email = it
                        },
                        label = "Email",
                        placeholder = "Введите email",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                        modifier = Modifier.fillMaxWidth().testTag("login_email_field")
                    )

                    Spacer(modifier = Modifier.height(SpaceMd))

                    // Password field
                    FunnyTextField(
                        value = password,
                        onValueChange = {
                            if (state.error != null) onClearError()
                            password = it
                        },
                        label = "Пароль",
                        placeholder = "Введите пароль",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isPasswordVisible = passwordVisible,
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            focusManager.clearFocus()
                            if (email.isNotBlank() && password.isNotBlank()) {
                                onLogin(email, password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("login_password_field")
                    )

                    // Error message
                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(SpaceMd))
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(SpaceXl))

                    // Login button
                    FunnyButton(
                        text = if (state.isLoading) "Вход..." else "Войти",
                        onClick = { onLogin(email, password) },
                        type = FunnyButtonType.PRIMARY,
                        size = FunnyButtonSize.LARGE,
                        enabled = !state.isLoading && email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("login_button")
                    )

                    Spacer(modifier = Modifier.height(SpaceMd))

                    Text(
                        text = "или",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(SpaceMd))

                    // OAuth buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(SpaceSm)
                    ) {
                        FunnyButton(
                            text = "Войти через Google",
                            onClick = { oauthMessage = "OAuth в разработке" },
                            type = FunnyButtonType.SECONDARY,
                            size = FunnyButtonSize.MEDIUM,
                            modifier = Modifier.fillMaxWidth()
                        )

                        FunnyButton(
                            text = "Войти через VK",
                            onClick = { oauthMessage = "OAuth в разработке" },
                            type = FunnyButtonType.GHOST,
                            size = FunnyButtonSize.MEDIUM,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    oauthMessage?.let { message ->
                        Spacer(modifier = Modifier.height(SpaceMd))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    onContinueAsGuest?.let {
                        Spacer(modifier = Modifier.height(SpaceMd))
                        TextButton(
                            onClick = it,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Продолжить без регистрации",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SpaceLg))

            // Register link
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Нет аккаунта? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(horizontal = SpaceXs),
                    modifier = Modifier.testTag("register_link")
                ) {
                    Text(
                        text = "Зарегистрироваться",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Дополнительный нижний отступ, чтобы последние элементы
            // не упирались в зону системных жестов
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun AppLogo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo circle with emoji
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.funnyColors.achievement)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎓",
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(SpaceMd))

        Text(
            text = "FunnyEnglish",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Учи English весело!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
