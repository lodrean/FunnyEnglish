package com.funnyenglish.shared.model

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
)

@Serializable
data class TestDetail(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val difficulty: Difficulty,
    val pointsReward: Int,
    val timeLimitSeconds: Int? = null,
    val questions: List<Question>
)

@Serializable
data class Question(
    val id: String,
    val type: QuestionType,
    val text: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val points: Int,
    val answers: List<Answer>
)

@Serializable
data class Answer(
    val id: String,
    val text: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val matchTarget: String? = null
)

@Serializable
enum class Difficulty {
    EASY, MEDIUM, HARD
}

@Serializable
enum class QuestionType {
    DRAG_DROP_IMAGE,
    AUDIO_SELECT,
    IMAGE_SELECT,
    TEXT_SELECT,
    FILL_BLANK
}
