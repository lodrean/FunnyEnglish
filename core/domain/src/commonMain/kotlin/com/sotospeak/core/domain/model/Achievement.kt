package com.sotospeak.core.domain.model

import kotlinx.serialization.Serializable

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
    val earned: Boolean = false
)

@Serializable
enum class AchievementCategory {
    LEARNING,
    CONSISTENCY,
    SOCIAL,
    EXPLORER,
    SECRET
}

@Serializable
enum class Rarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

@Serializable
data class AchievementCondition(
    val type: ConditionType,
    val targetValue: Int,
    val parameters: Map<String, String> = emptyMap()
)

@Serializable
enum class ConditionType {
    TESTS_COMPLETED,
    LESSONS_COMPLETED,
    WORDS_MASTERED,
    STREAK_DAYS,
    PERFECT_LESSONS,
    PERFECT_TESTS,
    ALL_EXERCISE_TYPES,
    ALL_STARS_CATEGORY,
    FAST_TEST,
    EARLY_BIRD,
    NIGHT_OWL,
    WEEKEND_WARRIOR,
    COMEBACK_KID,
    FRIEND_INVITED,
    STREAK_RECOVERED
}

@Serializable
data class LevelUpInfo(
    val previousLevel: Int,
    val newLevel: Int,
    val newTitle: String
)
