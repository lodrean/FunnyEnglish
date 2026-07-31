package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel для управления streak (серией дней)
 */

data class StreakUiState(
    val isLoading: Boolean = false,
    val streakData: StreakData? = null,
    val error: String? = null,
    val showMilestoneCelebration: Boolean = false,
    val milestoneDays: Int = 0,
    val isStreakAtRisk: Boolean = false
)

class StreakViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreakUiState())
    val uiState: StateFlow<StreakUiState> = _uiState.asStateFlow()

    init {
        loadStreakData()
    }

    fun loadStreakData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            api.getStreakData()
                .onSuccess { data ->
                    // Check for milestone (7, 14, 30, 60, 100, 200, 365 days)
                    val milestone = checkMilestone(data.currentStreak)
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            streakData = data,
                            isStreakAtRisk = data.isAtRisk,
                            showMilestoneCelebration = milestone != null,
                            milestoneDays = milestone ?: 0
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load streak data"
                        )
                    }
                }
        }
    }

    fun useStreakFreeze() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            api.useStreakFreeze()
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            streakData = result.streakData
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to use streak freeze"
                        )
                    }
                }
        }
    }

    fun startRecoveryChallenge() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            api.startStreakRecovery()
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            streakData = result.streakData
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to start recovery"
                        )
                    }
                }
        }
    }

    fun dismissMilestoneCelebration() {
        _uiState.update { it.copy(showMilestoneCelebration = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun checkMilestone(streak: Int): Int? {
        return when (streak) {
            7, 14, 30, 60, 100, 200, 365 -> streak
            else -> null
        }
    }

    companion object {
        val MILESTONE_THRESHOLDS = listOf(7, 14, 30, 60, 100, 200, 365)
    }
}

// Extension functions for API
suspend fun FunnyEnglishApi.getStreakData(): Result<StreakData> {
    return try {
        Result.success(
            StreakData(
                currentStreak = 12,
                longestStreak = 45,
                weeklyCalendar = listOf(
                    DayStatus("2026-02-03", StreakDayStatus.COMPLETED, 150),
                    DayStatus("2026-02-04", StreakDayStatus.TODAY_COMPLETED, 200),
                    DayStatus("2026-02-05", StreakDayStatus.TODAY_PENDING, 0),
                    DayStatus("2026-02-06", StreakDayStatus.TODAY_PENDING, 0),
                    DayStatus("2026-02-07", StreakDayStatus.TODAY_PENDING, 0),
                    DayStatus("2026-02-08", StreakDayStatus.TODAY_PENDING, 0),
                    DayStatus("2026-02-09", StreakDayStatus.TODAY_PENDING, 0)
                ),
                streakFreezesAvailable = 1,
                nextMilestone = 14,
                isAtRisk = false,
                lastActivityDate = "2026-02-04",
                recoveryChallengeAvailable = false
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun FunnyEnglishApi.useStreakFreeze(): Result<StreakFreezeResult> {
    // TODO: Implement actual API call
    return Result.success(StreakFreezeResult(success = true))
}

suspend fun FunnyEnglishApi.startStreakRecovery(): Result<StreakRecoveryResult> {
    // TODO: Implement actual API call
    return Result.success(StreakRecoveryResult(success = true))
}

data class StreakFreezeResult(val success: Boolean, val streakData: StreakData? = null)
data class StreakRecoveryResult(val success: Boolean, val streakData: StreakData? = null)
