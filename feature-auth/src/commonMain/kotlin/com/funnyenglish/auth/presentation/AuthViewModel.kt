package com.funnyenglish.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.auth.domain.AuthRepository
import com.funnyenglish.core.domain.model.AuthMode
import com.funnyenglish.core.domain.model.GuestSession
import com.funnyenglish.core.domain.model.LoginRequest
import com.funnyenglish.core.domain.model.MergeGuestProgressRequest
import com.funnyenglish.core.domain.model.OAuthRequest
import com.funnyenglish.core.domain.model.RegisterRequest
import com.funnyenglish.core.domain.repository.GuestProgressRepository
import com.funnyenglish.core.domain.repository.TokenProvider
import com.funnyenglish.core.domain.util.Result
import com.funnyenglish.core.presentation.ui.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AuthViewModel(
    private val repository: AuthRepository,
    private val tokenProvider: TokenProvider,
    private val guestRepo: GuestProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    init {
        checkAuthStatus()
    }

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.OnLoginClick -> login(action.email, action.password)
            is AuthAction.OnRegisterClick -> register(action.email, action.password, action.displayName)
            is AuthAction.OnOAuthClick -> oauthLogin(
                action.provider, action.token, action.email, action.displayName, action.avatarUrl
            )
            AuthAction.OnContinueAsGuestClick -> startGuestSession()
            AuthAction.OnLogoutClick -> logout()
            AuthAction.OnClearError -> _state.update { it.copy(error = null) }
            AuthAction.OnMergeGuestProgress -> mergeGuestProgress()
            AuthAction.OnMergeCompletedHandled -> _state.update { it.copy(mergeCompleted = false) }
        }
    }

    private fun checkAuthStatus() {
        val token = tokenProvider.getToken()
        if (token != null) {
            loadCurrentUser()
        } else {
            val session = guestRepo.getSession()
            _state.value = AuthState(
                mode = if (session != null) AuthMode.GUEST else AuthMode.UNKNOWN
            )
        }
    }

    private fun startGuestSession() {
        val session = GuestSession(
            guestId = generateGuestId(),
            createdAt = formatIsoDateTime()
        )
        guestRepo.saveSession(session)
        _state.value = AuthState(mode = AuthMode.GUEST)
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.login(LoginRequest(email.trim(), password.trim()))) {
                is Result.Success -> handleAuthSuccess(result.data)
                is Result.Failure -> handleAuthError(result.error, "Ошибка входа")
            }
        }
    }

    private fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.register(
                RegisterRequest(email.trim(), password.trim(), displayName.trim())
            )) {
                is Result.Success -> handleAuthSuccess(result.data)
                is Result.Failure -> handleAuthError(result.error, "Ошибка регистрации")
            }
        }
    }

    private fun oauthLogin(
        provider: String, token: String, email: String?, displayName: String?, avatarUrl: String?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.oauthLogin(
                provider, OAuthRequest(token, email, displayName, avatarUrl)
            )) {
                is Result.Success -> handleAuthSuccess(result.data)
                is Result.Failure -> handleAuthError(result.error, "Ошибка авторизации")
            }
        }
    }

    private fun handleAuthSuccess(response: com.funnyenglish.core.domain.model.AuthResponse) {
        tokenProvider.setToken(response.token)
        val hasGuestProgress = guestRepo.hasProgress()
        _state.update {
            it.copy(
                isLoading = false,
                mode = AuthMode.AUTHENTICATED,
                user = response.user,
                hasPendingGuestProgress = hasGuestProgress
            )
        }
        viewModelScope.launch { _events.send(AuthEvent.NavigateToHome) }
    }

    private fun handleAuthError(error: com.funnyenglish.core.domain.util.DomainError, fallback: String) {
        _state.update {
            it.copy(
                isLoading = false,
                error = UiText.plain(fallback)
            )
        }
    }

    private fun logout() {
        tokenProvider.setToken(null)
        guestRepo.clearSession()
        _state.value = AuthState(mode = AuthMode.UNKNOWN)
    }

    private fun mergeGuestProgress() {
        viewModelScope.launch {
            val session = guestRepo.getSession() ?: return@launch
            val progress = session.testProgress
            if (progress.isEmpty()) {
                _state.update { it.copy(hasPendingGuestProgress = false) }
                return@launch
            }

            when (val result = repository.mergeGuestProgress(
                MergeGuestProgressRequest(testProgress = progress)
            )) {
                is Result.Success -> {
                    guestRepo.clearSession()
                    _state.update { it.copy(hasPendingGuestProgress = false, mergeCompleted = true) }
                }
                is Result.Failure -> {
                    _state.update {
                        it.copy(error = UiText.plain("Не удалось перенести прогресс"))
                    }
                }
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = repository.getCurrentUser()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            mode = AuthMode.AUTHENTICATED,
                            user = result.data,
                            hasPendingGuestProgress = guestRepo.hasProgress()
                        )
                    }
                }
                is Result.Failure -> {
                    tokenProvider.setToken(null)
                    _state.value = AuthState(mode = AuthMode.UNKNOWN)
                }
            }
        }
    }

    private fun generateGuestId(): String {
        return "guest_" + Clock.System.now().toEpochMilliseconds() + "_" + (0..9999).random()
    }

    private fun formatIsoDateTime(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
    }
}
