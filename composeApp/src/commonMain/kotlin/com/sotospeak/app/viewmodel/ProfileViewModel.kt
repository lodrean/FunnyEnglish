package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.app.error.UiText
import com.sotospeak.app.storage.RecordingStore
import com.sotospeak.app.error.toUiText
import com.sotospeak.shared.api.AuthApi
import com.sotospeak.shared.contracts.GuestSession
import com.sotospeak.shared.contracts.UserProfile
import com.sotospeak.shared.repository.GuestProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val guestSession: GuestSession? = null,
    val error: UiText? = null,
    /** Локальный streak «дней с записью» (bd h3l.6) — считается из RecordingStore. */
    val recordingStreak: Int = 0,
    /** Цель на сегодня (bd h3l.14): запись сегодня уже есть. */
    val dailyGoalDone: Boolean = false
)

class ProfileViewModel(
    private val authApi: AuthApi,
    private val guestRepo: GuestProgressRepository,
    private val recordingStore: RecordingStore
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(
                isLoading = true,
                error = null,
                guestSession = guestRepo.getSession(),
                recordingStreak = recordingStore.streakDays(),
                dailyGoalDone = recordingStore.hasRecordingToday()
            )

            authApi.getUserProfile()
                .onSuccess { profile ->
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        userProfile = profile
                    )
                }
                .onFailure { error ->
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        error = error.toUiText()
                    )
                }
        }
    }

    fun clearError() {
        _profileState.value = _profileState.value.copy(error = null)
    }
}
