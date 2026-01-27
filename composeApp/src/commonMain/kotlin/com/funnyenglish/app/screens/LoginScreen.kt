package com.funnyenglish.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.app.components.FunnyButton
import com.funnyenglish.app.theme.FunnyColors
import com.funnyenglish.app.viewmodel.AuthState

@Composable
fun LoginScreen(
    state: AuthState,
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onClearError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            // Show snackbar or handle error
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo and title
        Text(
            text = "FunnyEnglish",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = FunnyColors.Primary
        )

        Text(
            text = "Учим английский весело!",
            fontSize = 16.sp,
            color = FunnyColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Check else Icons.Default.Refresh,
                        contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Error message
        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.error,
                color = FunnyColors.Error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login button
        FunnyButton(
            text = if (state.isLoading) "Вход..." else "Войти",
            onClick = { onLogin(email, password) },
            enabled = !state.isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Register link
        TextButton(onClick = onNavigateToRegister) {
            Text(
                text = "Нет аккаунта? Зарегистрироваться",
                color = FunnyColors.Primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Social login buttons
        Text(
            text = "Или войти через",
            color = FunnyColors.TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { /* Google OAuth */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Google")
            }

            OutlinedButton(
                onClick = { /* VK OAuth */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("VK")
            }
        }
    }
}
