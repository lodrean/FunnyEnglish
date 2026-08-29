package com.sotospeak.controller

import com.sotospeak.dto.AchievementResponse
import com.sotospeak.service.*
import com.sotospeak.shared.model.*
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST API для геймификации: streaks, achievements, quests
 */
@Deprecated(
    message = "Legacy pre-pivot API (AR-5): сломан на runtime (UserPrincipal не реализует UserDetails -> 500). " +
        "Ожидает решения владельца об удалении в bd FunnyEnglish-8zm. Не использовать.",
    level = DeprecationLevel.WARNING
)
@RestController
@RequestMapping("/api/v1/gamification")
class GamificationController(
    private val streakService: StreakService,
    private val achievementService: AchievementService,
    private val questService: QuestService,
    private val xpService: XpService
) {

    // ==================== STREAK ====================

    @GetMapping("/streak")
    fun getStreakData(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<StreakData> {
        val userId = UUID.fromString(userDetails.username)
        val streakData = streakService.getStreakData(userId)
        return ResponseEntity.ok(streakData)
    }

    @PostMapping("/streak/record")
    fun recordActivity(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<StreakUpdateResult> {
        val userId = UUID.fromString(userDetails.username)
        val result = streakService.recordActivity(userId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/streak/recovery/{challengeId}")
    fun recoverStreak(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable challengeId: String
    ): ResponseEntity<RecoveryResponse> {
        val userId = UUID.fromString(userDetails.username)
        val success = streakService.recoverStreak(userId, challengeId)
        return ResponseEntity.ok(RecoveryResponse(success))
    }

    // ==================== ACHIEVEMENTS ====================

    @GetMapping("/achievements")
    fun getAchievements(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<AchievementsResponse> {
        val userId = userDetails.username
        val achievements = achievementService.getUserAchievements(userId)
        val stats = achievementService.getAchievementStats(UUID.fromString(userId))
        return ResponseEntity.ok(AchievementsResponse(achievements, stats))
    }

    @GetMapping("/achievements/{achievementId}")
    fun getAchievementDetail(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable achievementId: String
    ): ResponseEntity<UserAchievement> {
        val userId = UUID.fromString(userDetails.username)
        val achievement = achievementService.getAchievementDetail(userId, achievementId)
        return ResponseEntity.ok(achievement)
    }

    // ==================== QUESTS ====================

    @GetMapping("/quests/daily")
    fun getDailyQuests(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<DailyQuestsResponse> {
        val userId = UUID.fromString(userDetails.username)
        val quests = questService.getDailyQuests(userId)
        val resetsAt = questService.getDailyResetTime()
        val allCompleted = quests.all { it.isCompleted }
        val totalReward = QuestReward(
            xp = quests.filter { it.isCompleted }.sumOf { it.reward.xp },
            gems = quests.filter { it.isCompleted }.sumOf { it.reward.gems }
        )
        return ResponseEntity.ok(
            DailyQuestsResponse(
                quests = quests,
                resetsAt = resetsAt.toString(),
                allCompleted = allCompleted,
                totalReward = totalReward
            )
        )
    }

    @GetMapping("/quests/weekly")
    fun getWeeklyQuests(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<WeeklyQuestsResponse> {
        val userId = UUID.fromString(userDetails.username)
        val quests = questService.getWeeklyQuests(userId)
        val resetsAt = questService.getWeeklyResetTime()
        return ResponseEntity.ok(
            WeeklyQuestsResponse(
                quests = quests,
                resetsAt = resetsAt.toString()
            )
        )
    }

    @PostMapping("/quests/{questId}/claim")
    fun claimQuestReward(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable questId: String
    ): ResponseEntity<ClaimRewardResponse> {
        val userId = UUID.fromString(userDetails.username)
        val reward = questService.claimReward(userId, questId)
        
        // Check for achievements unlocked
        val event = GameEvent.QuestCompleted(
            questId = questId,
            timestamp = java.time.Instant.now().toString()
        )
        val newAchievements = achievementService.checkAchievements(userId, event)
        
        return ResponseEntity.ok(
            ClaimRewardResponse(
                xpEarned = reward.xp,
                gemsEarned = reward.gems,
                newAchievements = newAchievements
            )
        )
    }

    // ==================== XP & LEVELS ====================

    @GetMapping("/xp")
    fun getXpData(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<XpData> {
        val userId = UUID.fromString(userDetails.username)
        val xpData = xpService.getXpData(userId)
        return ResponseEntity.ok(xpData)
    }

    @GetMapping("/xp/history")
    fun getXpHistory(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<XpHistoryResponse> {
        val userId = UUID.fromString(userDetails.username)
        val history = xpService.getRecentXpGains(userId, limit)
        return ResponseEntity.ok(XpHistoryResponse(history))
    }

    // ==================== LEADERBOARD ====================

    @GetMapping("/leaderboard")
    fun getLeaderboard(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "global") scope: String // global, friends, class
    ): ResponseEntity<LeaderboardResponse> {
        val userId = UUID.fromString(userDetails.username)
        val entries = xpService.getLeaderboard(userId, limit, scope)
        val userRank = xpService.getUserRank(userId, scope)
        return ResponseEntity.ok(
            LeaderboardResponse(
                entries = entries,
                userRank = userRank,
                scope = scope
            )
        )
    }
}

// ==================== Response DTOs ====================

data class RecoveryResponse(
    val success: Boolean,
    val message: String = if (success) "Streak recovered successfully!" else "Recovery failed"
)

data class AchievementsResponse(
    val achievements: List<AchievementResponse>,
    val stats: AchievementStats
)

data class AchievementStats(
    val totalEarned: Int,
    val totalAvailable: Int,
    val categoryProgress: Map<AchievementCategory, CategoryStat>
)

data class CategoryStat(
    val earned: Int,
    val total: Int,
    val percentage: Int
)

data class DailyQuestsResponse(
    val quests: List<DailyQuest>,
    val resetsAt: String,
    val allCompleted: Boolean,
    val totalReward: QuestReward
)

data class WeeklyQuestsResponse(
    val quests: List<WeeklyQuest>,
    val resetsAt: String
)

data class ClaimRewardResponse(
    val xpEarned: Int,
    val gemsEarned: Int,
    val newAchievements: List<Achievement>
)

data class XpHistoryResponse(
    val history: List<XpGain>
)

data class LeaderboardResponse(
    val entries: List<LeaderboardEntry>,
    val userRank: Int?,
    val scope: String
)
