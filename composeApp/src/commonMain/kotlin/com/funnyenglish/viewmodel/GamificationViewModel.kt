package com.funnyenglish.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.model.Achievement
import com.funnyenglish.shared.model.DailyQuest
import com.funnyenglish.shared.model.StreakData
import com.funnyenglish.shared.model.StreakUpdateResult
import com.funnyenglish.shared.model.WeeklyQuest
import com.funnyenglish.shared.model.XpData
import com.funnyenglish.shared.model.XpGain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * FunnyEnglish Gamification ViewModel
 * 
 * Manages:
 * - Streak data and updates
 * - XP data and history
 * - Daily and Weekly quests
 * - Achievements
 * 
 * Integrates with Backend API:
 * - GET /api/v1/gamification/streak
 * - POST /api/v1/gamification/streak/record
 * - GET /api/v1/gamification/xp
 * - GET /api/v1/gamification/xp/history
 * - GET /api/v1/gamification/quests/daily
 * - GET /api/v1/gamification/quests/weekly
 * - POST /api/v1/gamification/quests/{id}/claim
 * - GET /api/v1/gamification/achievements
 */

// ==================== UI States ====================

data class StreakUiState(
    val streakData: StreakData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val celebration: StreakUpdateResult? = null
)

data class XpUiState(
    val xpData: XpData? = null,
    val xpHistory: List<XpGain> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCelebration: Boolean = false,
    val xpGained: Int = 0
)

data class QuestsUiState(
    val dailyQuests: List<DailyQuest> = emptyList(),
    val weeklyQuests: List<WeeklyQuest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val claimedQuestId: String? = null
)

data class AchievementsUiState(
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val newlyUnlocked: List<Achievement> = emptyList()
)

// ==================== ViewModel ====================

class GamificationViewModel(
    private val repository: GamificationRepository
) : ViewModel() {

    // Streak
    private val _streakState = MutableStateFlow(StreakUiState())
    val streakState: StateFlow<StreakUiState> = _streakState.asStateFlow()

    // XP
    private val _xpState = MutableStateFlow(XpUiState())
    val xpState: StateFlow<XpUiState> = _xpState.asStateFlow()

    // Quests
    private val _questsState = MutableStateFlow(QuestsUiState())
    val questsState: StateFlow<QuestsUiState> = _questsState.asStateFlow()

    // Achievements
    private val _achievementsState = MutableStateFlow(AchievementsUiState())
    val achievementsState: StateFlow<AchievementsUiState> = _achievementsState.asStateFlow()

    init {
        loadAllData()
    }

    // ==================== Streak ====================

    fun loadStreakData() {
        viewModelScope.launch {
            _streakState.value = _streakState.value.copy(isLoading = true, error = null)
            try {
                val streakData = repository.getStreakData()
                _streakState.value = StreakUiState(streakData = streakData)
            } catch (e: Exception) {
                _streakState.value = _streakState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load streak data"
                )
            }
        }
    }

    fun recordActivity() {
        viewModelScope.launch {
            try {
                val result = repository.recordActivity()
                _streakState.value = _streakState.value.copy(
                    streakData = _streakState.value.streakData?.copy(
                        currentStreak = result.newStreak
                    ),
                    celebration = result
                )
                // Clear celebration after delay
                kotlinx.coroutines.delay(3000)
                _streakState.value = _streakState.value.copy(celebration = null)
            } catch (e: Exception) {
                _streakState.value = _streakState.value.copy(
                    error = e.message ?: "Failed to record activity"
                )
            }
        }
    }

    // ==================== XP ====================

    fun loadXpData() {
        viewModelScope.launch {
            _xpState.value = _xpState.value.copy(isLoading = true, error = null)
            try {
                val xpData = repository.getXpData()
                _xpState.value = _xpState.value.copy(
                    xpData = xpData,
                    isLoading = false
                )
            } catch (e: Exception) {
                _xpState.value = _xpState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load XP data"
                )
            }
        }
    }

    fun loadXpHistory(limit: Int = 10) {
        viewModelScope.launch {
            try {
                val history = repository.getXpHistory(limit)
                _xpState.value = _xpState.value.copy(xpHistory = history)
            } catch (e: Exception) {
                // Non-critical error, don't update state
            }
        }
    }

    fun showXpCelebration(amount: Int) {
        _xpState.value = _xpState.value.copy(
            showCelebration = true,
            xpGained = amount
        )
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _xpState.value = _xpState.value.copy(showCelebration = false)
        }
    }

    // ==================== Quests ====================

    fun loadQuests() {
        viewModelScope.launch {
            _questsState.value = _questsState.value.copy(isLoading = true, error = null)
            try {
                val dailyQuests = repository.getDailyQuests()
                val weeklyQuests = repository.getWeeklyQuests()
                _questsState.value = QuestsUiState(
                    dailyQuests = dailyQuests,
                    weeklyQuests = weeklyQuests
                )
            } catch (e: Exception) {
                _questsState.value = _questsState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load quests"
                )
            }
        }
    }

    fun claimQuestReward(questId: String) {
        viewModelScope.launch {
            try {
                val reward = repository.claimQuestReward(questId)
                _questsState.value = _questsState.value.copy(
                    claimedQuestId = questId
                )
                // Refresh XP data after claiming
                loadXpData()
                // Show XP celebration
                showXpCelebration(reward.xp)
                // Clear claimed state
                kotlinx.coroutines.delay(1000)
                _questsState.value = _questsState.value.copy(claimedQuestId = null)
                // Refresh quests
                loadQuests()
            } catch (e: Exception) {
                _questsState.value = _questsState.value.copy(
                    error = e.message ?: "Failed to claim reward"
                )
            }
        }
    }

    // ==================== Achievements ====================

    fun loadAchievements() {
        viewModelScope.launch {
            _achievementsState.value = _achievementsState.value.copy(isLoading = true, error = null)
            try {
                val achievements = repository.getAchievements()
                _achievementsState.value = AchievementsUiState(achievements = achievements)
            } catch (e: Exception) {
                _achievementsState.value = _achievementsState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load achievements"
                )
            }
        }
    }

    // ==================== Combined ====================

    fun loadAllData() {
        loadStreakData()
        loadXpData()
        loadXpHistory()
        loadQuests()
        loadAchievements()
    }

    fun clearErrors() {
        _streakState.value = _streakState.value.copy(error = null)
        _xpState.value = _xpState.value.copy(error = null)
        _questsState.value = _questsState.value.copy(error = null)
        _achievementsState.value = _achievementsState.value.copy(error = null)
    }
}

// ==================== Repository Interface ====================

interface GamificationRepository {
    // Streak
    suspend fun getStreakData(): StreakData
    suspend fun recordActivity(): StreakUpdateResult
    
    // XP
    suspend fun getXpData(): XpData
    suspend fun getXpHistory(limit: Int): List<XpGain>
    
    // Quests
    suspend fun getDailyQuests(): List<DailyQuest>
    suspend fun getWeeklyQuests(): List<WeeklyQuest>
    suspend fun claimQuestReward(questId: String): QuestReward
    
    // Achievements
    suspend fun getAchievements(): List<Achievement>
}

data class QuestReward(
    val xp: Int,
    val gems: Int
)
