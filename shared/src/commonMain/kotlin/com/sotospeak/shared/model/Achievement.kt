package com.sotospeak.shared.model

import kotlinx.serialization.Serializable
// Timestamp as ISO string

/**
 * Achievement система - достижения и награды
 */

@Serializable
data class Achievement(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val iconUrl: String? = null,
    val category: AchievementCategory? = null,
    val rarity: Rarity? = null,
    val isHidden: Boolean? = null,
    val condition: AchievementCondition? = null,
    val pointsReward: Int,
    val earned: Boolean = false // From backend AchievementResponse
)

@Serializable
data class UserAchievement(
    val achievement: Achievement,
    val earnedAt: String?,
    val progress: Float, // 0.0 - 1.0
    val isEarned: Boolean
)

@Serializable
enum class AchievementCategory {
    LEARNING,      // Обучение (уроки, слова)
    CONSISTENCY,   // Последовательность (streaks)
    SOCIAL,        // Социальное (приглашения)
    EXPLORER,      // Исследование (все типы заданий)
    SECRET         // Скрытые
}

@Serializable
enum class Rarity {
    COMMON,      // Белый/серый
    UNCOMMON,    // Зеленый
    RARE,        // Синий
    EPIC,        // Фиолетовый
    LEGENDARY    // Золотой
}

@Serializable
data class AchievementCondition(
    val type: ConditionType,
    val targetValue: Int,
    val parameters: Map<String, String> = emptyMap()
)

@Serializable
data class AchievementStats(
    val totalAchievements: Int = 0,
    val unlockedCount: Int = 0,
    val completionPercentage: Float = 0f,
    val rarestUnlocked: Rarity? = null
)

@Serializable
enum class ConditionType {
    TESTS_COMPLETED,      // Пройдено N тестов
    LESSONS_COMPLETED,    // Пройдено N уроков
    WORDS_MASTERED,       // Выучено N слов
    STREAK_DAYS,          // Streak N дней
    PERFECT_LESSONS,      // Идеально пройдено N уроков
    PERFECT_TESTS,        // Идеально пройдено N тестов
    ALL_EXERCISE_TYPES,   // Все типы упражнений
    ALL_STARS_CATEGORY,   // Все звезды в категории
    FAST_TEST,            // Быстрый тест
    EARLY_BIRD,           // Урок до 8 утра
    NIGHT_OWL,            // Урок после 10 вечера
    WEEKEND_WARRIOR,      // Уроки на выходных
    COMEBACK_KID,         // Возвращение после перерыва
    FRIEND_INVITED,       // Приглашен друг
    STREAK_RECOVERED      // Восстановлен streak
}
