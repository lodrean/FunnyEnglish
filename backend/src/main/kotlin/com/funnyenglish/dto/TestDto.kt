package com.funnyenglish.dto

import com.funnyenglish.entity.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

// Response DTOs
data class CategoryResponse(
    val id: String,
    val name: String,
    val description: String?,
    val iconUrl: String?,
    val testsCount: Int,
    val completedCount: Int = 0,
    val totalStars: Int = 0
)

data class TestListResponse(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val difficulty: String,
    val pointsReward: Int,
    val questionsCount: Int,
    val userProgress: TestProgressSummary? = null
)

data class TestProgressSummary(
    val completed: Boolean,
    val bestScore: Int,
    val maxScore: Int,
    val stars: Int
)

data class TestDetailResponse(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val difficulty: String,
    val pointsReward: Int,
    val timeLimitSeconds: Int?,
    val questions: List<QuestionResponse>
)

data class QuestionResponse(
    val id: String,
    val type: String,
    val text: String?,
    val audioUrl: String?,
    val imageUrl: String?,
    val points: Int,
    val answers: List<AnswerResponse>
)

data class AnswerResponse(
    val id: String,
    val text: String?,
    val imageUrl: String?,
    val audioUrl: String?,
    val matchTarget: String?
    // Note: isCorrect is NOT included to prevent cheating
)

// Admin Response DTOs (include isCorrect)
data class AdminTestDetailResponse(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val difficulty: String,
    val pointsReward: Int,
    val timeLimitSeconds: Int?,
    val isPublished: Boolean,
    val displayOrder: Int,
    val questions: List<AdminQuestionResponse>
)

data class AdminQuestionResponse(
    val id: String,
    val type: String,
    val text: String?,
    val audioUrl: String?,
    val imageUrl: String?,
    val displayOrder: Int,
    val points: Int,
    val answers: List<AdminAnswerResponse>
)

data class AdminAnswerResponse(
    val id: String,
    val text: String?,
    val imageUrl: String?,
    val audioUrl: String?,
    val isCorrect: Boolean,
    val displayOrder: Int,
    val matchTarget: String?
)

// Request DTOs
data class CreateTestRequest(
    @field:NotBlank(message = "Category ID is required")
    val categoryId: String,

    @field:NotBlank(message = "Title is required")
    @field:Size(max = 200, message = "Title must not exceed 200 characters")
    val title: String,

    val description: String? = null,
    val thumbnailUrl: String? = null,
    val difficulty: String = "EASY",
    val pointsReward: Int = 10,
    val timeLimitSeconds: Int? = null,
    val isPublished: Boolean = false,
    val displayOrder: Int = 0,

    @field:NotEmpty(message = "At least one question is required")
    val questions: List<CreateQuestionRequest>
)

data class CreateQuestionRequest(
    @field:NotBlank(message = "Question type is required")
    val type: String,

    val text: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val displayOrder: Int = 0,
    val points: Int = 1,

    @field:NotEmpty(message = "At least one answer is required")
    val answers: List<CreateAnswerRequest>
)

data class CreateAnswerRequest(
    val text: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val isCorrect: Boolean = false,
    val displayOrder: Int = 0,
    val matchTarget: String? = null
)

data class UpdateTestRequest(
    val categoryId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val difficulty: String? = null,
    val pointsReward: Int? = null,
    val timeLimitSeconds: Int? = null,
    val isPublished: Boolean? = null,
    val displayOrder: Int? = null,
    val questions: List<CreateQuestionRequest>? = null
)

// Mapping functions
fun Category.toResponse(completedCount: Int = 0, totalStars: Int = 0) = CategoryResponse(
    id = id.toString(),
    name = name,
    description = description,
    iconUrl = iconUrl,
    testsCount = tests.count { it.isPublished },
    completedCount = completedCount,
    totalStars = totalStars
)

fun Test.toListResponse(progress: Progress? = null) = TestListResponse(
    id = id.toString(),
    categoryId = category.id.toString(),
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    difficulty = difficulty.name,
    pointsReward = pointsReward,
    questionsCount = questions.size,
    userProgress = progress?.let {
        TestProgressSummary(
            completed = true,
            bestScore = it.bestScore,
            maxScore = it.maxScore,
            stars = it.stars
        )
    }
)

fun Test.toDetailResponse() = TestDetailResponse(
    id = id.toString(),
    categoryId = category.id.toString(),
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    difficulty = difficulty.name,
    pointsReward = pointsReward,
    timeLimitSeconds = timeLimitSeconds,
    questions = questions.map { it.toResponse() }
)

fun Question.toResponse() = QuestionResponse(
    id = id.toString(),
    type = type.name,
    text = text,
    audioUrl = audioUrl,
    imageUrl = imageUrl,
    points = points,
    answers = answers.shuffled().map { it.toResponse() }
)

fun Answer.toResponse() = AnswerResponse(
    id = id.toString(),
    text = text,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    matchTarget = matchTarget
)

fun Test.toAdminResponse() = AdminTestDetailResponse(
    id = id.toString(),
    categoryId = category.id.toString(),
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    difficulty = difficulty.name,
    pointsReward = pointsReward,
    timeLimitSeconds = timeLimitSeconds,
    isPublished = isPublished,
    displayOrder = displayOrder,
    questions = questions.map { it.toAdminResponse() }
)

fun Question.toAdminResponse() = AdminQuestionResponse(
    id = id.toString(),
    type = type.name,
    text = text,
    audioUrl = audioUrl,
    imageUrl = imageUrl,
    displayOrder = displayOrder,
    points = points,
    answers = answers.map { it.toAdminResponse() }
)

fun Answer.toAdminResponse() = AdminAnswerResponse(
    id = id.toString(),
    text = text,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    isCorrect = isCorrect,
    displayOrder = displayOrder,
    matchTarget = matchTarget
)
