package com.funnyenglish.service

import com.funnyenglish.shared.model.LeaderboardEntry
import com.funnyenglish.shared.model.LevelUpInfo
import com.funnyenglish.entity.XpHistory
import com.funnyenglish.repository.XpHistoryRepository
import com.funnyenglish.shared.model.SkillType
import com.funnyenglish.shared.model.XpData
import com.funnyenglish.shared.model.XpGain
import com.funnyenglish.shared.model.XpSource

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * Сервис управления XP и уровнями
 */
@Service
class XpService(
    private val xpHistoryRepository: XpHistoryRepository,
    private val userRepository: com.funnyenglish.repository.UserRepository
) {
    
    companion object {
        // XP needed for each level (exponential growth)
        fun xpForLevel(level: Int): Int {
            return when (level) {
                1 -> 0
                2 -> 100
                3 -> 250
                4 -> 450
                5 -> 700
                6 -> 1000
                7 -> 1400
                8 -> 1900
                9 -> 2500
                10 -> 3200
                else -> 3200 + (level - 10) * 1000
            }
        }
        
        const val MAX_LEVEL = 50
    }
    
    /**
     * Получить XP данные пользователя
     */
    @Transactional(readOnly = true)
    fun getXpData(userId: UUID): XpData {
        val user = userRepository.findById(userId).orElseThrow()
        val currentXp = user.totalPoints
        val currentLevel = calculateLevel(currentXp)
        val xpForNext = xpForLevel(currentLevel + 1)
        val xpInCurrent = currentXp - xpForLevel(currentLevel)
        
        // Get skill-specific XP (mock for now)
        val skillXp = mapOf(
            SkillType.GRAMMAR_TENSES to (currentXp * 0.3).toInt(),
            SkillType.VOCABULARY_NOUNS to (currentXp * 0.4).toInt(),
            SkillType.LISTENING to (currentXp * 0.2).toInt(),
            SkillType.PRONUNCIATION to (currentXp * 0.1).toInt()
        )
        
        // Get recent gains
        val recentGains = xpHistoryRepository.findRecentByUserId(userId, 10)
            .map { it.toModel() }
        
        return XpData(
            currentXp = currentXp,
            currentLevel = currentLevel,
            xpForNextLevel = xpForNext,
            xpInCurrentLevel = xpInCurrent,
            skillXp = skillXp,
            recentXpGains = recentGains
        )
    }
    
    /**
     * Добавить XP пользователю
     */
    @Transactional
    fun addXp(
        userId: UUID,
        amount: Int,
        source: XpSource,
        description: String
    ): XpGainResultInternal {
        val user = userRepository.findById(userId).orElseThrow()
        val previousLevel = calculateLevel(user.totalPoints)
        
        // Add XP
        user.totalPoints += amount
        userRepository.save(user)
        
        // Record in history
        val history = XpHistory(
            id = UUID.randomUUID(),
            userId = userId,
            amount = amount,
            source = source.name,
            description = description,
            createdAt = Instant.now()
        )
        xpHistoryRepository.save(history)
        
        // Check for level up
        val newLevel = calculateLevel(user.totalPoints)
        val levelUp = if (newLevel > previousLevel) {
            LevelUpInfo(
                previousLevel = previousLevel,
                newLevel = newLevel,
                newTitle = getLevelTitle(newLevel)
            )
        } else null
        
        return XpGainResultInternal(
            amount = amount,
            newTotal = user.totalPoints,
            levelUp = levelUp
        )
    }
    
    /**
     * Получить историю XP
     */
    @Transactional(readOnly = true)
    fun getRecentXpGains(userId: UUID, limit: Int): List<XpGain> {
        return xpHistoryRepository.findRecentByUserId(userId, limit)
            .map { it.toModel() }
    }
    
    /**
     * Получить таблицу лидеров
     */
    @Transactional(readOnly = true)
    fun getLeaderboard(userId: UUID, limit: Int, scope: String): List<LeaderboardEntry> {
        return when (scope) {
            "global" -> getGlobalLeaderboard(limit)
            "friends" -> getFriendsLeaderboard(userId, limit)
            else -> getGlobalLeaderboard(limit)
        }
    }
    
    /**
     * Получить ранг пользователя
     */
    @Transactional(readOnly = true)
    fun getUserRank(userId: UUID, scope: String): Int? {
        return when (scope) {
            "global" -> xpHistoryRepository.getGlobalRank(userId)
            else -> null
        }
    }
    
    // ==================== Private Methods ====================
    
    private fun calculateLevel(totalXp: Int): Int {
        var level = 1
        while (level < MAX_LEVEL && totalXp >= xpForLevel(level + 1)) {
            level++
        }
        return level
    }
    
    private fun getLevelTitle(level: Int): String {
        return when (level) {
            1 -> "Новичок"
            2 -> "Ученик"
            3 -> "Стажёр"
            4 -> "Практикант"
            5 -> "Знаток"
            6 -> "Эксперт"
            7 -> "Мастер"
            8 -> "Гуру"
            9 -> "Профессор"
            10 -> "Легенда"
            else -> "Магистр $level"
        }
    }
    
    private fun getGlobalLeaderboard(limit: Int): List<LeaderboardEntry> {
        // TODO: Implement proper leaderboard query
        return emptyList()
    }
    
    private fun getFriendsLeaderboard(userId: UUID, limit: Int): List<LeaderboardEntry> {
        // TODO: Implement friends system
        return emptyList()
    }
}

// ==================== Result Classes ====================

data class XpGainResultInternal(
    val amount: Int,
    val newTotal: Int,
    val levelUp: LevelUpInfo?
)

// ==================== Extension Functions ====================

private fun XpHistory.toModel(): com.funnyenglish.shared.model.XpGain {
    return com.funnyenglish.shared.model.XpGain(
        amount = this.amount,
        source = com.funnyenglish.shared.model.XpSource.valueOf(this.source),
        timestamp = this.createdAt.toString(),
        description = this.description
    )
}
