package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.Leaderboard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LeaderboardState(
    val isLoading: Boolean = false,
    val leaderboard: Leaderboard? = null,
    val error: String? = null
)

class LeaderboardViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardState())
    val state: StateFlow<LeaderboardState> = _state.asStateFlow()

    fun loadLeaderboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            api.getLeaderboard(20)
                .onSuccess { leaderboard ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        leaderboard = leaderboard
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
