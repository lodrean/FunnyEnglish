package com.funnyenglish.core.domain.model

import kotlinx.serialization.Serializable

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
    COMPLETED,
    FREEZE_USED,
    MISSED,
    TODAY_PENDING,
    TODAY_COMPLETED,
    AT_RISK
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
