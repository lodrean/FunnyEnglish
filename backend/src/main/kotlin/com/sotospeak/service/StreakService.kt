package com.sotospeak.service

import com.sotospeak.entity.UserStreak
import com.sotospeak.repository.UserStreakRepository
import com.sotospeak.shared.model.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*


/**
 * Сервис управления streaks пользователей
 */
@Service
class StreakService(
    private val userStreakRepository: UserStreakRepository
) {
    companion object {
        val MILESTONES = listOf(7, 14, 30, 60, 100, 200, 365)
        const val FREEZES_PER_WEEK = 1
        const val RECOVERY_WINDOW_HOURS = 48
    }

    /**
     * Получить данные о streak пользователя
     */
    @Transactional // read + save в getOrCreateUserStreak при первом обращении
    fun getStreakData(userId: UUID): StreakData {
        val userStreak = getOrCreateUserStreak(userId)
        val activities = getRecentActivities(userId, 30)
        
        val calendar = buildWeeklyCalendar(userId, activities)
        val currentStreak = userStreak.currentStreak
        val nextMilestone = MILESTONES.firstOrNull { it > currentStreak } ?: MILESTONES.last()
        
        return StreakData(
            currentStreak = currentStreak,
            longestStreak = userStreak.longestStreak,
            weeklyCalendar = calendar,
            streakFreezesAvailable = calculateAvailableFreezes(userId),
            nextMilestone = nextMilestone,
            isAtRisk = isStreakAtRisk(userId, activities),
            lastActivityDate = userStreak.lastActivityDate?.toString(),
            recoveryChallengeAvailable = isRecoveryAvailable(userId, activities)
        )
    }

    /**
     * Записать активность и обновить streak
     */
    @Transactional
    fun recordActivity(userId: UUID): StreakUpdateResult {
        val userStreak = getOrCreateUserStreak(userId)
        val today = LocalDate.now()
        val lastActivity = userStreak.lastActivityDate
        
        // Reset weekly freezes if needed
        resetWeeklyFreezesIfNeeded(userStreak)
        
        return when {
            // Уже активность сегодня - streak не меняется
            lastActivity == today -> {
                StreakUpdateResult(
                    newStreak = userStreak.currentStreak,
                    milestoneReached = null,
                    message = "Отличная работа сегодня! Продолжай в том же духе!",
                    celebrationType = null
                )
            }
            
            // Активность вчера - streak продолжается
            lastActivity == today.minusDays(1) -> {
                val newStreak = userStreak.currentStreak + 1
                userStreak.currentStreak = newStreak
                userStreak.lastActivityDate = today
                
                if (newStreak > userStreak.longestStreak) {
                    userStreak.longestStreak = newStreak
                }
                
                userStreakRepository.save(userStreak)
                
                val milestone = MILESTONES.find { it == newStreak }
                val celebration = milestone?.let { getCelebrationType(it) }
                
                StreakUpdateResult(
                    newStreak = newStreak,
                    milestoneReached = milestone,
                    message = buildStreakMessage(newStreak, milestone),
                    celebrationType = celebration
                )
            }
            
            // Пропущены дни - streak сломан (если не использован freeze)
            else -> {
                val daysMissed = ChronoUnit.DAYS.between(lastActivity ?: today.minusDays(1), today) - 1
                
                // Проверяем, можно ли использовать freeze
                val freezesAvailable = calculateAvailableFreezes(userId)
                if (daysMissed <= freezesAvailable) {
                    // Используем freeze автоматически
                    useStreakFreeze(userId, daysMissed.toInt())
                    
                    val newStreak = userStreak.currentStreak + 1
                    userStreak.currentStreak = newStreak
                    userStreak.lastActivityDate = today
                    userStreakRepository.save(userStreak)
                    
                    StreakUpdateResult(
                        newStreak = newStreak,
                        milestoneReached = null,
                        message = "Твоя серия продолжается благодаря заморозке! 🔥❄️",
                        celebrationType = null
                    )
                } else {
                    // Streak сломан, начинаем заново
                    userStreak.previousStreakBeforeBreak = userStreak.currentStreak
                    userStreak.currentStreak = 1
                    userStreak.lastActivityDate = today
                    userStreakRepository.save(userStreak)
                    
                    StreakUpdateResult(
                        newStreak = 1,
                        milestoneReached = null,
                        message = "Новое начало! Твоя серия начинается заново. 🌱",
                        celebrationType = null
                    )
                }
            }
        }
    }

    /**
     * Использовать streak freeze
     */
    @Transactional
    fun useStreakFreeze(userId: UUID, days: Int = 1): Boolean {
        val userStreak = getOrCreateUserStreak(userId)
        resetWeeklyFreezesIfNeeded(userStreak)
        
        val available = FREEZES_PER_WEEK - userStreak.freezesUsedThisWeek
        if (available < days) return false
        
        // Записываем использование freeze
        userStreak.freezesUsedThisWeek += days.toInt()
        userStreakRepository.save(userStreak)
        
        return true
    }

    /**
     * Восстановить streak через челлендж
     */
    @Transactional
    fun recoverStreak(userId: UUID, challengeId: String): Boolean {
        if (!isRecoveryAvailable(userId, getRecentActivities(userId, 7))) {
            return false
        }
        
        val userStreak = getOrCreateUserStreak(userId)
        val previousStreak = userStreak.previousStreakBeforeBreak ?: 0
        
        // Восстанавливаем 50% от предыдущего streak
        userStreak.currentStreak = (previousStreak * 0.5).toInt().coerceAtLeast(5)
        userStreak.lastActivityDate = LocalDate.now()
        userStreakRepository.save(userStreak)
        
        return true
    }

    // ==================== Private Methods ====================

    private fun getOrCreateUserStreak(userId: UUID): UserStreak {
        return userStreakRepository.findByUserId(userId) ?: run {
            val newStreak = UserStreak(
                userId = userId,
                currentStreak = 0,
                longestStreak = 0,
                lastActivityDate = null,
                freezesUsedThisWeek = 0,
                weekResetAt = Instant.now(),
                previousStreakBeforeBreak = null
            )
            userStreakRepository.save(newStreak)
            newStreak
        }
    }

    private fun resetWeeklyFreezesIfNeeded(userStreak: UserStreak) {
        val now = Instant.now()
        val weekResetAt = userStreak.weekResetAt
        val daysSinceReset = ChronoUnit.DAYS.between(
            weekResetAt.atZone(ZoneId.systemDefault()).toLocalDate(),
            now.atZone(ZoneId.systemDefault()).toLocalDate()
        )
        
        if (daysSinceReset >= 7) {
            userStreak.freezesUsedThisWeek = 0
            userStreak.weekResetAt = now
        }
    }

    private fun getRecentActivities(userId: UUID, days: Int): List<UserActivity> {
        // This would typically query an activity repository
        // For now, return empty list - in production this should query actual activity data
        return emptyList()
    }

    private fun buildWeeklyCalendar(userId: UUID, activities: List<UserActivity>): List<DayStatus> {
        val today = LocalDate.now()
        val userStreak = getOrCreateUserStreak(userId)
        val activityDates = getActivityDates(userId, 7)
        val freezeDates = getFreezeDates(userId, 7)
        
        return (0..6).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val dateStr = date.toString()
            
            val status = when {
                date == today && activityDates.contains(date) -> StreakDayStatus.TODAY_COMPLETED
                date == today && !activityDates.contains(date) -> StreakDayStatus.TODAY_PENDING
                freezeDates.contains(date) -> StreakDayStatus.FREEZE_USED
                activityDates.contains(date) -> StreakDayStatus.COMPLETED
                else -> StreakDayStatus.MISSED
            }
            
            DayStatus(
                date = dateStr,
                status = status,
                xpEarned = 0 // Would be populated from actual activity data
            )
        }.reversed()
    }

    private fun getActivityDates(userId: UUID, days: Int): Set<LocalDate> {
        val userStreak = getOrCreateUserStreak(userId)
        val today = LocalDate.now()
        val result = mutableSetOf<LocalDate>()
        
        // Add last activity date if within range
        userStreak.lastActivityDate?.let { lastActivity ->
            if (ChronoUnit.DAYS.between(lastActivity, today) < days) {
                result.add(lastActivity)
            }
        }
        
        return result
    }

    private fun getFreezeDates(userId: UUID, days: Int): Set<LocalDate> {
        // This would query freeze usage history
        // For now, return empty set
        return emptySet()
    }

    private fun calculateAvailableFreezes(userId: UUID): Int {
        val userStreak = getOrCreateUserStreak(userId)
        resetWeeklyFreezesIfNeeded(userStreak)
        return (FREEZES_PER_WEEK - userStreak.freezesUsedThisWeek).coerceAtLeast(0)
    }

    private fun isStreakAtRisk(userId: UUID, activities: List<UserActivity>): Boolean {
        val userStreak = getOrCreateUserStreak(userId)
        val lastActivity = userStreak.lastActivityDate ?: return false
        val today = LocalDate.now()
        
        return lastActivity < today.minusDays(1)
    }

    private fun isRecoveryAvailable(userId: UUID, activities: List<UserActivity>): Boolean {
        val userStreak = getOrCreateUserStreak(userId)
        val lastActivity = userStreak.lastActivityDate ?: return false
        val today = LocalDate.now()
        val daysSinceBreak = ChronoUnit.DAYS.between(lastActivity, today)
        
        // Recovery available if streak was broken within last 48 hours (2 days)
        return daysSinceBreak in 1..2 && (userStreak.previousStreakBeforeBreak ?: 0) > 0
    }

    private fun getCelebrationType(streak: Int): CelebrationType? {
        return when (streak) {
            7 -> CelebrationType.STREAK_7
            14 -> CelebrationType.STREAK_14
            30 -> CelebrationType.STREAK_30
            60 -> CelebrationType.STREAK_60
            100 -> CelebrationType.STREAK_100
            else -> null
        }
    }

    private fun buildStreakMessage(streak: Int, milestone: Int?): String {
        return when {
            milestone != null -> "🎉 Ура! Ты достиг серии в $streak дней! Продолжай в том же духе!"
            streak == 1 -> "Отличное начало! Твоя серия началась! 🔥"
            streak < 7 -> "Ты на правильном пути! Серия: $streak дней 🔥"
            else -> "Невероятно! Твоя серия: $streak дней! 🔥🔥🔥"
        }
    }
}

// Temporary data class for user activities
data class UserActivity(
    val timestamp: Instant,
    val xpEarned: Int,
    val activityType: String
)
