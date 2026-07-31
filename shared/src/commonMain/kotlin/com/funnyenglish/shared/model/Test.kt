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
) {
    val percentage: Int
        get() = if (maxScore > 0) (bestScore * 100 / maxScore) else 0
}

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
    val title: String? = null,
    val text: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val points: Int,
    val answers: List<Answer>,
    val imageWordMatchContent: ImageWordMatchContent? = null  // For IMAGE_WORD_MATCH type
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
    FILL_BLANK,
    IMAGE_WORD_MATCH  // NEW: Перетаскивание слов к областям на изображении
}

// ============ IMAGE_WORD_MATCH Models ============

@Serializable
data class ImageWordMatchContent(
    val id: String? = null,
    val type: String? = null,
    val points: Int? = null,
    val imageUrl: String,
    val instruction: String,
    val hotspots: List<HotspotData>,
    val words: List<WordData>
)

@Serializable
data class HotspotData(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val shape: HotspotShape = HotspotShape.RECTANGLE,
    val wordId: String? = null  // Not sent to client for security (correct answer)
) {
    fun contains(rx: Float, ry: Float): Boolean = when (shape) {
        HotspotShape.RECTANGLE -> rx >= x && rx <= x + width && ry >= y && ry <= y + height
        HotspotShape.CIRCLE -> {
            val centerX = x + width / 2
            val centerY = y + height / 2
            val radius = kotlin.math.min(width, height) / 2
            val dx = rx - centerX
            val dy = ry - centerY
            (dx * dx + dy * dy) <= (radius * radius)
        }
    }
}

@Serializable
enum class HotspotShape {
    RECTANGLE,
    CIRCLE
}

@Serializable
data class WordData(
    val id: String,
    val text: String,
    val translation: String? = null,
    val audioUrl: String? = null
)
