package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.api.TokenProvider
import com.funnyenglish.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

class AuthViewModel(
    private val api: FunnyEnglishApi,
    private val tokenProvider: TokenProvider
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val token = tokenProvider.getToken()
        if (token != null) {
            loadCurrentUser()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            api.login(LoginRequest(email, password))
                .onSuccess { response ->
                    tokenProvider.setToken(response.token)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = response.user
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка входа"
                    )
                }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            api.register(RegisterRequest(email, password, displayName))
                .onSuccess { response ->
                    tokenProvider.setToken(response.token)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = response.user
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка регистрации"
                    )
                }
        }
    }

    fun oauthLogin(provider: String, token: String, email: String?, displayName: String?, avatarUrl: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            api.oauthLogin(provider, OAuthRequest(token, email, displayName, avatarUrl))
                .onSuccess { response ->
                    tokenProvider.setToken(response.token)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = response.user
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка авторизации"
                    )
                }
        }
    }

    fun logout() {
        tokenProvider.setToken(null)
        _state.value = AuthState()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            api.getCurrentUser()
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = user
                    )
                }
                .onFailure {
                    tokenProvider.setToken(null)
                    _state.value = _state.value.copy(isLoading = false)
                }
        }
    }
}
