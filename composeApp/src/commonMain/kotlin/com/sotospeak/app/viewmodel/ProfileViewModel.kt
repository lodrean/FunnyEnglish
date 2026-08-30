package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.shared.api.AuthApi
import com.sotospeak.shared.model.GuestSession
import com.sotospeak.shared.model.UserProfile
import com.sotospeak.shared.repository.GuestProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val guestSession: GuestSession? = null,
    val error: String? = null
)

class ProfileViewModel(
    private val authApi: AuthApi,
    private val guestRepo: GuestProgressRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(
                isLoading = true,
                error = null,
                guestSession = guestRepo.getSession()
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
                        error = error.message
                    )
                }
        }
    }

    fun clearError() {
        _profileState.value = _profileState.value.copy(error = null)
    }
}
