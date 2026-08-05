package com.sotospeak.auth.presentation

sealed interface AuthEvent {
    data object NavigateToHome : AuthEvent
    data object NavigateToRegister : AuthEvent
    data object NavigateToLogin : AuthEvent
}
