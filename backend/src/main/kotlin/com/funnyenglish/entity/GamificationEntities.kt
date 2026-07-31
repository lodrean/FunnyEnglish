package com.funnyenglish.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * Достижение (справочник)
 */
@Entity
@Table(name = "achievements")
data class AchievementEntity(
    @Id
    val id: String,
    
    @Column(name = "code", unique = true, nullable = false)
    val code: String,
    
    @Column(name = "name", nullable = false)
    val name: String,
    
    @Column(name = "description", nullable = false)
    val description: String,
    
    @Column(name = "icon_url")
    val iconUrl: String? = null,
    
    @Column(name = "category", nullable = false)
    val category: String,
    
    @Column(name = "rarity", nullable = false)
    val rarity: String,
    
    @Column(name = "is_hidden", nullable = false)
    val isHidden: Boolean = false,
    
    @Column(name = "condition_type", nullable = false)
    val conditionType: String,
    
    @Column(name = "condition_target", nullable = false)
    val conditionTarget: Int,
    
    @Column(name = "points_reward", nullable = false)
    val pointsReward: Int = 0
)

/**
 * Достижения пользователя
 */
@Entity
@Table(name = "user_achievements")
data class UserAchievementEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    
    @Column(name = "achievement_id", nullable = false)
    val achievementId: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", insertable = false, updatable = false)
    val achievement: AchievementEntity? = null,
    
    @Column(name = "earned_at")
    var earnedAt: Instant? = null,
    
    @Column(name = "progress", nullable = false)
    var progress: Float = 0f,
    
    @Column(name = "is_earned", nullable = false)
    var isEarned: Boolean = false
)

/**
 * Квест/задание
 */
@Entity
@Table(name = "quests")
data class Quest(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    
    @Column(name = "title", nullable = false)
    val title: String,
    
    @Column(name = "description", nullable = false)
    val description: String,
    
    @Column(name = "quest_type", nullable = false)
    val questType: String,
    
    @Column(name = "target_value", nullable = false)
    val targetValue: Int,
    
    @Column(name = "current_value", nullable = false)
    var currentValue: Int = 0,
    
    @Column(name = "reward_xp", nullable = false)
    val rewardXp: Int,
    
    @Column(name = "reward_gems", nullable = false)
    val rewardGems: Int,
    
    @Column(name = "is_completed", nullable = false)
    var isCompleted: Boolean = false,
    
    @Column(name = "is_reward_claimed", nullable = false)
    var isRewardClaimed: Boolean = false,
    
    @Column(name = "completed_at")
    var completedAt: Instant? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant
)

/**
 * История начисления XP
 */
@Entity
@Table(name = "xp_history")
data class XpHistory(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    
    @Column(name = "amount", nullable = false)
    val amount: Int,
    
    @Column(name = "source", nullable = false)
    val source: String,
    
    @Column(name = "description")
    val description: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)

/**
 * Streak пользователя
 */
@Entity
@Table(name = "user_streaks")
data class UserStreak(
    @Id
    val userId: UUID,
    
    @Column(name = "current_streak", nullable = false)
    var currentStreak: Int = 0,
    
    @Column(name = "longest_streak", nullable = false)
    var longestStreak: Int = 0,
    
    @Column(name = "last_activity_date")
    var lastActivityDate: java.time.LocalDate? = null,
    
    @Column(name = "freezes_used_this_week", nullable = false)
    var freezesUsedThisWeek: Int = 0,
    
    @Column(name = "week_reset_at", nullable = false)
    var weekResetAt: Instant = Instant.now(),
    
    @Column(name = "previous_streak_before_break")
    var previousStreakBeforeBreak: Int? = null,
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
