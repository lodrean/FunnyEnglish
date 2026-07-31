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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import androidx.compose.ui.unit.sp
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.components.inputs.FunnyTextField
import com.funnyenglish.designsystem.tokens.*
import com.funnyenglish.app.viewmodel.AuthState
import com.funnyenglish.designsystem.theme.funnyColors

@Composable
fun RegisterScreen(
    state: AuthState,
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onClearError: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
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
                .windowInsetsPadding(WindowInsets.systemBars)
                .imePadding()
                .padding(horizontal = SpaceLg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
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
                        fontSize = 36.sp
                    )
                }

                Spacer(modifier = Modifier.height(SpaceSm))

                Text(
                    text = "FunnyEnglish",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(SpaceLg))

            // Register Card
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
                        text = "Создать аккаунт",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(SpaceSm))

                    Text(
                        text = "Начни свое обучение сегодня",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(SpaceLg))

                    // Name field
                    FunnyTextField(
                        value = displayName,
                        onValueChange = {
                            if (state.error != null) onClearError()
                            displayName = it
                        },
                        label = "Имя",
                        placeholder = "Введите имя",
                        leadingIcon = Icons.Default.Person,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(SpaceMd))

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
                        modifier = Modifier.fillMaxWidth()
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
                            if (email.isNotBlank() && password.isNotBlank() && displayName.isNotBlank()) {
                                onRegister(email, password, displayName)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
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

                    // Register button
                    FunnyButton(
                        text = if (state.isLoading) "Создание..." else "Зарегистрироваться",
                        onClick = { onRegister(email, password, displayName) },
                        type = FunnyButtonType.PRIMARY,
                        size = FunnyButtonSize.LARGE,
                        enabled = !state.isLoading &&
                            email.isNotBlank() &&
                            password.isNotBlank() &&
                            displayName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpaceLg))

            // Login link
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Уже есть аккаунт? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    contentPadding = PaddingValues(horizontal = SpaceXs)
                ) {
                    Text(
                        text = "Войти",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpaceXl))
        }
    }
}
