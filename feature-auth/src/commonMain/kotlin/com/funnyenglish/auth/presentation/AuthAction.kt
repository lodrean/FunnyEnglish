package com.funnyenglish.auth.presentation

sealed interface AuthAction {
    data class OnLoginClick(val email: String, val password: String) : AuthAction
    data class OnRegisterClick(val email: String, val password: String, val displayName: String) : AuthAction
    data class OnOAuthClick(
        val provider: String,
        val token: String,
        val email: String?,
        val displayName: String?,
        val avatarUrl: String?
    ) : AuthAction
    data object OnContinueAsGuestClick : AuthAction
    data object OnLogoutClick : AuthAction
    data object OnClearError : AuthAction
    data object OnMergeGuestProgress : AuthAction
    data object OnMergeCompletedHandled : AuthAction
}
