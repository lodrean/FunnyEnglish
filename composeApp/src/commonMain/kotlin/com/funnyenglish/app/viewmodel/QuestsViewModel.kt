package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
// kotlinx.datetime not available in composeApp, using simplified approach

/**
 * ViewModel для управления квестами
 */

data class QuestsUiState(
    val isLoading: Boolean = false,
    val dailyQuests: List<DailyQuest> = emptyList(),
    val weeklyQuests: List<WeeklyQuest> = emptyList(),
    val completedQuests: List<DailyQuest> = emptyList(),
    val expiredQuests: List<DailyQuest> = emptyList(),
    val error: String? = null,
    val claimedQuestId: String? = null,
    val timeUntilReset: Long = 0L,
    val questProgress: QuestProgress = QuestProgress()
)

data class QuestProgress(
    val totalQuests: Int = 0,
    val completedQuests: Int = 0,
    val completionPercentage: Float = 0f,
    val availableRewardsXp: Int = 0,
    val availableRewardsGems: Int = 0
)

class QuestsViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestsUiState())
    val uiState: StateFlow<QuestsUiState> = _uiState.asStateFlow()

    init {
        loadQuests()
        startCountdownTimer()
    }

    fun loadQuests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val dailyResult = api.getDailyQuests()
                val weeklyResult = api.getWeeklyQuests()
                
                dailyResult.onSuccess { dailyQuests ->
                    weeklyResult.onSuccess { weeklyQuests ->
                        val progress = calculateProgress(dailyQuests, weeklyQuests)
                        
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                dailyQuests = dailyQuests.filter { !it.isCompleted },
                                completedQuests = dailyQuests.filter { it.isCompleted },
                                weeklyQuests = weeklyQuests,
                                questProgress = progress
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = e.message ?: "Unknown error") 
                }
            }
        }
    }

    fun refreshQuests() {
        loadQuests()
    }

    fun claimQuestReward(questId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            api.claimQuestReward(questId)
                .onSuccess { reward ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            claimedQuestId = questId
                        )
                    }
                    loadQuests() // Refresh to update state
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun dismissClaimNotification() {
        _uiState.update { it.copy(claimedQuestId = null) }
    }

    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (true) {
                updateTimeUntilReset()
                kotlinx.coroutines.delay(60000) // Update every minute
            }
        }
    }

    private fun updateTimeUntilReset() {
        // TODO: Implement platform-specific time calculation
        // For now, just set a placeholder value (8 hours in milliseconds)
        _uiState.update { it.copy(timeUntilReset = 8 * 60 * 60 * 1000L) }
    }

    private fun calculateProgress(
        dailyQuests: List<DailyQuest>,
        weeklyQuests: List<WeeklyQuest>
    ): QuestProgress {
        val totalDaily = dailyQuests.size
        val completedDaily = dailyQuests.count { it.isCompleted }
        
        val completedWeekly = weeklyQuests.count { it.isCompleted }
        
        val total = totalDaily + weeklyQuests.size
        val completed = completedDaily + completedWeekly
        
        val percentage = if (total > 0) completed.toFloat() / total else 0f
        
        val availableXp = dailyQuests
            .filter { it.isCompleted }
            .sumOf { it.reward.xp }
        
        val availableGems = dailyQuests
            .filter { it.isCompleted }
            .sumOf { it.reward.gems }
        
        return QuestProgress(
            totalQuests = total,
            completedQuests = completed,
            completionPercentage = percentage,
            availableRewardsXp = availableXp,
            availableRewardsGems = availableGems
        )
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

// Extension functions for API
suspend fun FunnyEnglishApi.getDailyQuests(): Result<List<DailyQuest>> {
    // TODO: Implement actual API call
    return Result.success(generateSampleDailyQuests())
}

suspend fun FunnyEnglishApi.getWeeklyQuests(): Result<List<WeeklyQuest>> {
    // TODO: Implement actual API call
    return Result.success(emptyList())
}

suspend fun FunnyEnglishApi.claimQuestReward(questId: String): Result<QuestReward> {
    // TODO: Implement actual API call
    return Result.success(QuestReward(xp = 100, gems = 10))
}

fun generateSampleDailyQuests(): List<DailyQuest> = listOf(
    DailyQuest(
        id = "daily_1",
        title = "Учёба начинается",
        description = "Пройди 3 теста",
        type = QuestType.COMPLETE_LESSONS,
        targetValue = 3,
        currentValue = 2,
        reward = QuestReward(xp = 50, gems = 5),
        expiresAt = "2026-02-06T00:00:00Z",
        isCompleted = false,
        difficulty = QuestDifficulty.EASY
    ),
    DailyQuest(
        id = "daily_2",
        title = "Быстрый старт",
        description = "Набери 100 XP",
        type = QuestType.EARN_XP,
        targetValue = 100,
        currentValue = 75,
        reward = QuestReward(xp = 30, gems = 3),
        expiresAt = "2026-02-06T00:00:00Z",
        isCompleted = false,
        difficulty = QuestDifficulty.EASY
    ),
    DailyQuest(
        id = "daily_3",
        title = "Серия продолжается",
        description = "Сохрани стрик 3 дня",
        type = QuestType.PRACTICE_STREAK,
        targetValue = 3,
        currentValue = 3,
        reward = QuestReward(xp = 100, gems = 10),
        expiresAt = "2026-02-06T00:00:00Z",
        isCompleted = true,
        difficulty = QuestDifficulty.MEDIUM
    )
)
