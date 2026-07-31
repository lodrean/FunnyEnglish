package com.funnyenglish.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Типы ежедневных заданий
 */
enum class TaskType {
    COMPLETE_LESSON,      // Пройти урок
    LEARN_WORDS,          // Выучить N слов
    PRACTICE_MINUTES,     // Практиковаться N минут
    COMPLETE_TESTS,       // Пройти N тестов
    STREAK_MAINTAIN       // Поддержать streak
}

/**
 * Daily Task - ежедневное задание пользователя
 */
@Entity
@Table(name = "daily_tasks")
data class DailyTask(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    val taskType: TaskType,

    @Column(name = "target_value", nullable = false)
    val targetValue: Int = 1,

    @Column(name = "current_value", nullable = false)
    val currentValue: Int = 0,

    @Column(name = "reward_xp", nullable = false)
    val rewardXp: Int = 10,

    @Column(name = "task_date", nullable = false)
    val taskDate: LocalDate,

    @Column(name = "is_completed", nullable = false)
    val isCompleted: Boolean = false,

    @Column(name = "completed_at")
    val completedAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    /**
     * Обновить прогресс и проверить завершение
     */
    fun updateProgress(value: Int): DailyTask {
        val newValue = minOf(currentValue + value, targetValue)
        val isNowCompleted = newValue >= targetValue
        return copy(
            currentValue = newValue,
            isCompleted = isNowCompleted || isCompleted,
            completedAt = if (isNowCompleted && !isCompleted) Instant.now() else completedAt
        )
    }
}

/**
 * Шаблон для генерации daily tasks
 */
@Entity
@Table(name = "daily_task_templates")
data class DailyTaskTemplate(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    val taskType: TaskType,

    @Column(name = "min_target")
    val minTarget: Int = 1,

    @Column(name = "max_target")
    val maxTarget: Int = 5,

    @Column(name = "base_reward_xp", nullable = false)
    val baseRewardXp: Int = 10,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "priority", nullable = false)
    val priority: Int = 0,

    @Column(name = "description")
    val description: String? = null
)
