package com.sotospeak.auth.presentation

import com.sotospeak.core.domain.model.AuthMode
import com.sotospeak.core.domain.model.User
import com.sotospeak.core.presentation.ui.UiText

data class AuthState(
    val isLoading: Boolean = false,
    val mode: AuthMode = AuthMode.UNKNOWN,
    val user: User? = null,
    val error: UiText? = null,
    val hasPendingGuestProgress: Boolean = false,
    val mergeCompleted: Boolean = false
)
