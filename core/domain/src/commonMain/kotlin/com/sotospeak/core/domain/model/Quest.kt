package com.sotospeak.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyQuest(
    val id: String,
    val title: String,
    val description: String,
    val type: QuestType,
    val targetValue: Int,
    val currentValue: Int,
    val reward: QuestReward,
    val expiresAt: String,
    val isCompleted: Boolean,
    val difficulty: QuestDifficulty
)

@Serializable
data class WeeklyQuest(
    val id: String,
    val title: String,
    val description: String,
    val objectives: List<QuestObjective>,
    val reward: QuestReward,
    val expiresAt: String,
    val isCompleted: Boolean
)

@Serializable
data class QuestObjective(
    val type: QuestType,
    val target: Int,
    val current: Int,
    val description: String
)

@Serializable
enum class QuestType {
    COMPLETE_LESSONS,
    EARN_XP,
    PRACTICE_STREAK,
    REVIEW_WORDS,
    PERFECT_SCORE,
    TRY_NEW_CATEGORY,
    SHARE_PROGRESS,
    PRACTICE_PRONUNCIATION
}

@Serializable
enum class QuestDifficulty {
    EASY,
    MEDIUM,
    HARD
}

@Serializable
data class QuestReward(
    val xp: Int,
    val gems: Int,
    val badge: String? = null
)
