package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.app.util.GuestAnalytics
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.api.TokenProvider
import com.funnyenglish.shared.model.*
import com.funnyenglish.shared.repository.GuestProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatIsoDateTime(): String {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
}

data class AuthState(
    val isLoading: Boolean = false,
    val mode: AuthMode = AuthMode.UNKNOWN,
    val user: User? = null,
    val error: String? = null,
    val hasPendingGuestProgress: Boolean = false,
    val mergeCompleted: Boolean = false
)

class AuthViewModel(
    private val api: FunnyEnglishApi,
    private val tokenProvider: TokenProvider,
    private val guestRepo: GuestProgressRepository,
    private val guestAnalytics: GuestAnalytics
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
        } else {
            val session = guestRepo.getSession()
            if (session != null) {
                _state.value = AuthState(mode = AuthMode.GUEST)
            } else {
                _state.value = AuthState(mode = AuthMode.UNKNOWN)
            }
        }
    }

    fun startGuestSession() {
        val session = GuestSession(
            guestId = generateGuestId(),
            createdAt = formatIsoDateTime()
        )
        guestRepo.saveSession(session)
        _state.value = AuthState(mode = AuthMode.GUEST)

        // Обезличенная аналитика: старт гостевой сессии
        viewModelScope.launch {
            guestAnalytics.track(
                GuestEventDto(
                    anonymousId = session.guestId,
                    type = "SESSION_STARTED",
                    clientTimestamp = formatIsoDateTime()
                )
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            api.login(LoginRequest(email.trim(), password.trim()))
                .onSuccess { response ->
                    tokenProvider.setToken(response.token)
                    val hasGuestProgress = guestRepo.hasProgress()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        mode = AuthMode.AUTHENTICATED,
                        user = response.user,
                        hasPendingGuestProgress = hasGuestProgress
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

            api.register(RegisterRequest(email.trim(), password.trim(), displayName.trim()))
                .onSuccess { response ->
                    tokenProvider.setToken(response.token)
                    val hasGuestProgress = guestRepo.hasProgress()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        mode = AuthMode.AUTHENTICATED,
                        user = response.user,
                        hasPendingGuestProgress = hasGuestProgress
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
                    val hasGuestProgress = guestRepo.hasProgress()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        mode = AuthMode.AUTHENTICATED,
                        user = response.user,
                        hasPendingGuestProgress = hasGuestProgress
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
        guestRepo.clearSession()
        _state.value = AuthState(mode = AuthMode.UNKNOWN)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun markGuestProgressMerged() {
        _state.value = _state.value.copy(hasPendingGuestProgress = false)
    }

    fun mergeGuestProgress() {
        viewModelScope.launch {
            val session = guestRepo.getSession() ?: return@launch
            val progress = session.testProgress
            if (progress.isEmpty()) {
                markGuestProgressMerged()
                return@launch
            }

            val request = MergeGuestProgressRequest(
                testProgress = progress,
                anonymousId = guestRepo.getAnonymousId() // метрика конверсии
            )
            api.mergeGuestProgress(request)
                .onSuccess {
                    guestRepo.clearSession()
                    _state.value = _state.value.copy(
                        hasPendingGuestProgress = false,
                        mergeCompleted = true
                    )
                }
                .onFailure { error ->
                    // Keep dialog open so user can retry; log error
                    _state.value = _state.value.copy(
                        error = error.message ?: "Не удалось перенести прогресс"
                    )
                }
        }
    }

    fun onMergeCompletedHandled() {
        _state.value = _state.value.copy(mergeCompleted = false)
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            api.getCurrentUser()
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        mode = AuthMode.AUTHENTICATED,
                        user = user,
                        hasPendingGuestProgress = guestRepo.hasProgress()
                    )
                }
                .onFailure {
                    tokenProvider.setToken(null)
                    _state.value = AuthState(mode = AuthMode.UNKNOWN)
                }
        }
    }

    private fun generateGuestId(): String {
        return "guest_" + Clock.System.now().toEpochMilliseconds() + "_" + (0..9999).random()
    }
}
