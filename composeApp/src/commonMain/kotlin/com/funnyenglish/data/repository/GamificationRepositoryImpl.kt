package com.funnyenglish.data.repository

import com.funnyenglish.data.remote.ClaimRewardResponse
import com.funnyenglish.data.remote.FunnyEnglishApi
import com.funnyenglish.data.remote.RecoveryResponse
import com.funnyenglish.shared.model.Achievement
import com.funnyenglish.shared.model.DailyQuest
import com.funnyenglish.viewmodel.QuestReward
import com.funnyenglish.shared.model.StreakData
import com.funnyenglish.shared.model.StreakUpdateResult
import com.funnyenglish.shared.model.WeeklyQuest
import com.funnyenglish.shared.model.XpData
import com.funnyenglish.shared.model.XpGain
import com.funnyenglish.viewmodel.GamificationRepository

/**
 * Gamification Repository Implementation
 * 
 * Bridge between ViewModel and API client
 */

class GamificationRepositoryImpl(
    private val api: FunnyEnglishApi
) : GamificationRepository {
    
    // ==================== Streak ====================
    
    override suspend fun getStreakData(): StreakData {
        return api.getStreakData()
    }
    
    override suspend fun recordActivity(): StreakUpdateResult {
        return api.recordActivity()
    }
    
    // ==================== XP ====================
    
    override suspend fun getXpData(): XpData {
        return api.getXpData()
    }
    
    override suspend fun getXpHistory(limit: Int): List<XpGain> {
        return api.getXpHistory(limit)
    }
    
    // ==================== Quests ====================
    
    override suspend fun getDailyQuests(): List<DailyQuest> {
        return api.getDailyQuests()
    }
    
    override suspend fun getWeeklyQuests(): List<WeeklyQuest> {
        return api.getWeeklyQuests()
    }
    
    override suspend fun claimQuestReward(questId: String): QuestReward {
        val response = api.claimQuestReward(questId)
        return QuestReward(
            xp = response.xpEarned,
            gems = response.gemsEarned
        )
    }
    
    // ==================== Achievements ====================
    
    override suspend fun getAchievements(): List<Achievement> {
        return api.getAchievements()
    }
}
