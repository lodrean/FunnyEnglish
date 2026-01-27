package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.Achievement
import com.funnyenglish.shared.model.ProgressSummary
import com.funnyenglish.shared.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val progressSummary: ProgressSummary? = null,
    val error: String? = null
)

data class AchievementsState(
    val isLoading: Boolean = false,
    val achievements: List<Achievement> = emptyList(),
    val error: String? = null
)

class ProfileViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _achievementsState = MutableStateFlow(AchievementsState())
    val achievementsState: StateFlow<AchievementsState> = _achievementsState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, error = null)

            api.getUserProfile()
                .onSuccess { profile ->
                    _profileState.value = _profileState.value.copy(userProfile = profile)
                }

            api.getUserProgressSummary()
                .onSuccess { summary ->
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        progressSummary = summary
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

    fun loadAchievements() {
        viewModelScope.launch {
            _achievementsState.value = _achievementsState.value.copy(isLoading = true, error = null)

            api.getAllAchievements()
                .onSuccess { achievements ->
                    _achievementsState.value = _achievementsState.value.copy(
                        isLoading = false,
                        achievements = achievements
                    )
                }
                .onFailure { error ->
                    _achievementsState.value = _achievementsState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun clearError() {
        _profileState.value = _profileState.value.copy(error = null)
        _achievementsState.value = _achievementsState.value.copy(error = null)
    }
}
