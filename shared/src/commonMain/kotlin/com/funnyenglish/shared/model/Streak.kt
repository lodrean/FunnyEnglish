package com.funnyenglish.shared.model

import kotlinx.serialization.Serializable


/**
 * Streak система - ежедневные серии
 */

@Serializable
data class StreakData(
    val currentStreak: Int,
    val longestStreak: Int,
    val weeklyCalendar: List<DayStatus>,
    val streakFreezesAvailable: Int,
    val nextMilestone: Int,
    val isAtRisk: Boolean,
    val lastActivityDate: String?,
    val recoveryChallengeAvailable: Boolean = false
)

@Serializable
data class DayStatus(
    val date: String, // ISO date
    val status: StreakDayStatus,
    val xpEarned: Int
)

@Serializable
enum class StreakDayStatus {
    COMPLETED,      // 🔥 Streak maintained
    FREEZE_USED,    // ❄️ Streak freeze applied
    MISSED,         // ❌ Streak broken
    TODAY_PENDING,  // ⭕ Today, not yet done
    TODAY_COMPLETED,// ✅ Today completed
    AT_RISK         // ⚠️ Haven't practiced today
}

@Serializable
data class StreakUpdateResult(
    val newStreak: Int,
    val milestoneReached: Int?,
    val message: String,
    val celebrationType: CelebrationType?
)

@Serializable
enum class CelebrationType {
    STREAK_7,
    STREAK_14,
    STREAK_30,
    STREAK_60,
    STREAK_100,
    PERSONAL_BEST
}
