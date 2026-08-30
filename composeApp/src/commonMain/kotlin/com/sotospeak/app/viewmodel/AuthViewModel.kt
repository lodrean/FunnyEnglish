package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.app.di.SessionEvents
import com.sotospeak.app.util.GuestAnalytics
import com.sotospeak.shared.api.ApiException
import com.sotospeak.shared.api.AuthApi
import com.sotospeak.shared.api.GuestApi
import com.sotospeak.shared.api.TokenProvider
import com.sotospeak.shared.model.*
import com.sotospeak.shared.repository.GuestProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ISO-8601 UTC Instant (с суффиксом Z) — совместимо с java.time.Instant на backend.
 * Раньше был LocalDateTime без таймзоны → backend 500 на /api/public/guest-events.
 */
fun formatIsoDateTime(): String {
    return Clock.System.now().toString()
}

data class AuthState(
    val isLoading: Boolean = false,
    val mode: AuthMode = AuthMode.UNKNOWN,
    val user: User? = null,
    val error: String? = null,
    val hasPendingGuestProgress: Boolean = false,
    /** Регистрация прошла, письмо отправлено — ждём подтверждения (auto-login нет, email-верификация flag=on). */
    val verificationEmailSentTo: String? = null,
    /** Login вернул 403 EMAIL_NOT_VERIFIED — показать плашку с resend. */
    val emailNotVerified: Boolean = false,
    /** Resend-письмо отправлено повторно (индикация в UI). */
    val verificationResent: Boolean = false
)

class AuthViewModel(
    private val authApi: AuthApi,
    private val guestApi: GuestApi,
    private val tokenProvider: TokenProvider,
    private val guestRepo: GuestProgressRepository,
    private val guestAnalytics: GuestAnalytics,
    private val sessionEvents: SessionEvents
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        sessionEvents.listener = { onSessionExpired() }
        checkAuthStatus()
    }

    override fun onCleared() {
        if (sessionEvents.listener != null) sessionEvents.listener = null
        super.onCleared()
    }

    /**
     * Refresh токена не удался (окно истекло) — SoToSpeakApi уже очистил токен.
     * Приводим UI к гостевому режиму: публичный контент работает, Practice-гейт предложит войти.
     * Гостевую сессию НЕ чистим (в отличие от logout).
     */
    fun onSessionExpired() {
        val mode = if (guestRepo.getSession() != null) AuthMode.GUEST else AuthMode.UNKNOWN
        _state.value = AuthState(mode = mode)
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

            authApi.login(LoginRequest(email.trim(), password.trim()))
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
                    val notVerified = (error as? ApiException)?.errorCode == "EMAIL_NOT_VERIFIED"
                    _state.value = _state.value.copy(
                        isLoading = false,
                        emailNotVerified = notVerified,
                        error = if (notVerified) null else (error.message ?: "Ошибка входа")
                    )
                }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            authApi.register(RegisterRequest(email.trim(), password.trim(), displayName.trim()))
                .onSuccess { response ->
                    if (response.token == null) {
                        // Email-верификация включена: auto-login нет, ждём подтверждения почты
                        _state.value = _state.value.copy(
                            isLoading = false,
                            verificationEmailSentTo = response.user.email
                        )
                        return@onSuccess
                    }
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

            authApi.oauthLogin(provider, OAuthRequest(token, email, displayName, avatarUrl))
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
        _state.value = _state.value.copy(error = null, emailNotVerified = false, verificationResent = false)
    }

    /** Повторная отправка письма верификации (resend — anti-enumeration, всегда «успех»). */
    fun resendVerificationEmail(email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            authApi.resendVerification(email.trim())
            // Ответ 200 независимо от существования email — показываем успех всегда
            _state.value = _state.value.copy(isLoading = false, verificationResent = true)
        }
    }

    fun clearVerificationState() {
        _state.value = _state.value.copy(verificationEmailSentTo = null, verificationResent = false)
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
            guestApi.mergeGuestProgress(request)
                .onSuccess {
                    guestRepo.clearSession()
                    _state.value = _state.value.copy(hasPendingGuestProgress = false)
                }
                .onFailure { error ->
                    // Keep dialog open so user can retry; log error
                    _state.value = _state.value.copy(
                        error = error.message ?: "Не удалось перенести прогресс"
                    )
                }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            authApi.getCurrentUser()
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
        // Backend хранит anonymous_id как UUID → генерируем валидный UUID v4.
        // Без внешних зависимостей: формат xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx.
        val hex = "0123456789abcdef"
        return buildString(36) {
            repeat(8) { append(hex.random()) }
            append('-')
            repeat(3) { append(hex.random()) }
            append('4')
            append('-')
            append((8..11).random().toString(16))
            repeat(3) { append(hex.random()) }
            append('-')
            repeat(12) { append(hex.random()) }
        }
    }
}
