package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel для управления достижениями
 */

data class AchievementsUiState(
    val isLoading: Boolean = false,
    val achievements: List<UserAchievement> = emptyList(),
    val filteredAchievements: List<UserAchievement> = emptyList(),
    val selectedCategory: AchievementCategory? = null,
    val error: String? = null,
    val newlyUnlocked: Achievement? = null,
    val stats: AchievementStats = AchievementStats()
)

data class AchievementStats(
    val totalAchievements: Int = 0,
    val unlockedCount: Int = 0,
    val completionPercentage: Float = 0f,
    val rarestUnlocked: Rarity? = null
)

class AchievementsViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    fun loadAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            api.getUserAchievements()
                .onSuccess { achievements ->
                    val stats = calculateStats(achievements)
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            achievements = achievements,
                            filteredAchievements = achievements,
                            stats = stats
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load achievements"
                        )
                    }
                }
        }
    }

    fun filterByCategory(category: AchievementCategory?) {
        val filtered = if (category == null) {
            _uiState.value.achievements
        } else {
            _uiState.value.achievements.filter { 
                it.achievement.category == category 
            }
        }
        
        _uiState.update {
            it.copy(
                selectedCategory = category,
                filteredAchievements = filtered
            )
        }
    }

    fun checkForNewAchievements() {
        viewModelScope.launch {
            api.checkNewAchievements()
                .onSuccess { newAchievements ->
                    if (newAchievements.isNotEmpty()) {
                        _uiState.update {
                            it.copy(newlyUnlocked = newAchievements.first())
                        }
                        loadAchievements() // Refresh the list
                    }
                }
        }
    }

    fun dismissNewAchievementNotification() {
        _uiState.update { it.copy(newlyUnlocked = null) }
    }

    fun shareAchievement(achievementId: String) {
        // TODO: Implement sharing functionality
    }

    private fun calculateStats(achievements: List<UserAchievement>): AchievementStats {
        val total = achievements.size
        val unlocked = achievements.count { it.isEarned }
        val percentage = if (total > 0) unlocked.toFloat() / total else 0f
        
        val rarest = achievements
            .filter { it.isEarned }
            .maxByOrNull { it.achievement.rarity?.ordinal ?: 0 }
            ?.achievement?.rarity
        
        return AchievementStats(
            totalAchievements = total,
            unlockedCount = unlocked,
            completionPercentage = percentage,
            rarestUnlocked = rarest
        )
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

// Extension functions for API
suspend fun FunnyEnglishApi.getUserAchievements(): Result<List<UserAchievement>> {
    // TODO: Implement actual API call
    return Result.success(generateSampleAchievements())
}

suspend fun FunnyEnglishApi.checkNewAchievements(): Result<List<Achievement>> {
    // TODO: Implement actual API call
    return Result.success(emptyList())
}

fun generateSampleAchievements(): List<UserAchievement> = listOf(
    UserAchievement(
        achievement = Achievement(
            id = "1",
            code = "FIRST_TEST",
            name = "Первые шаги",
            description = "Пройди свой первый тест",
            iconUrl = "🎯",
            category = AchievementCategory.LEARNING,
            rarity = Rarity.COMMON,
            isHidden = false,
            condition = AchievementCondition(ConditionType.LESSONS_COMPLETED, 1),
            pointsReward = 50
        ),
        isEarned = true,
        earnedAt = "2026-01-15T10:30:00Z",
        progress = 1f
    ),
    UserAchievement(
        achievement = Achievement(
            id = "2",
            code = "STREAK_7",
            name = "Неделя успеха",
            description = "Занимайся 7 дней подряд",
            iconUrl = "🔥",
            category = AchievementCategory.CONSISTENCY,
            rarity = Rarity.COMMON,
            isHidden = false,
            condition = AchievementCondition(ConditionType.STREAK_DAYS, 7),
            pointsReward = 100
        ),
        isEarned = true,
        earnedAt = "2026-01-22T10:30:00Z",
        progress = 1f
    ),
    UserAchievement(
        achievement = Achievement(
            id = "3",
            code = "PERFECT_SCORE",
            name = "Перфекционист",
            description = "Получи 100% на любом тесте",
            iconUrl = "⭐",
            category = AchievementCategory.LEARNING,
            rarity = Rarity.RARE,
            isHidden = false,
            condition = AchievementCondition(ConditionType.PERFECT_LESSONS, 1),
            pointsReward = 150
        ),
        isEarned = false,
        earnedAt = null,
        progress = 0f
    ),
    UserAchievement(
        achievement = Achievement(
            id = "4",
            code = "NIGHT_OWL",
            name = "Сова",
            description = "???",
            iconUrl = "🦉",
            category = AchievementCategory.SECRET,
            rarity = Rarity.EPIC,
            isHidden = true,
            condition = AchievementCondition(ConditionType.NIGHT_OWL, 1),
            pointsReward = 200
        ),
        isEarned = false,
        earnedAt = null,
        progress = 0f
    )
)
