package com.funnyenglish.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val description: String? = null,
    val iconUrl: String? = null,
    val testsCount: Int,
    val completedCount: Int = 0,
    val totalStars: Int = 0
)

@Serializable
data class TestListItem(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val difficulty: Difficulty,
    val pointsReward: Int,
    val questionsCount: Int,
    val userProgress: TestProgressSummary? = null
)

@Serializable
data class TestProgressSummary(
    val completed: Boolean,
    val bestScore: Int,
    val maxScore: Int,
    val stars: Int
) {
    val percentage: Int
        get() = if (maxScore > 0) (bestScore * 100 / maxScore) else 0
}

@Serializable
enum class Difficulty {
    EASY, MEDIUM, HARD
}
