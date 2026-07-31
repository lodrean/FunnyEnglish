package com.funnyenglish.auth.presentation

import com.funnyenglish.core.domain.model.AuthMode
import com.funnyenglish.core.domain.model.User
import com.funnyenglish.core.presentation.ui.UiText

data class AuthState(
    val isLoading: Boolean = false,
    val mode: AuthMode = AuthMode.UNKNOWN,
    val user: User? = null,
    val error: UiText? = null,
    val hasPendingGuestProgress: Boolean = false,
    val mergeCompleted: Boolean = false
)
