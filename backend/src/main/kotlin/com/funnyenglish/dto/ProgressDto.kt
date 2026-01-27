package com.funnyenglish.dto

import com.funnyenglish.entity.Progress
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.time.Instant

data class SubmitTestRequest(
    @field:NotBlank(message = "Test ID is required")
    val testId: String,

    @field:NotEmpty(message = "Answers are required")
    val answers: List<SubmitAnswerRequest>,

    val timeSpentSeconds: Int? = null
)

data class SubmitAnswerRequest(
    val questionId: String,
    val selectedAnswerIds: List<String> = emptyList(),
    val dragDropMatches: Map<String, String>? = null  // answerId -> matchTarget
)

data class SubmitTestResponse(
    val score: Int,
    val maxScore: Int,
    val percentage: Int,
    val stars: Int,
    val pointsEarned: Int,
    val isNewBestScore: Boolean,
    val newAchievements: List<AchievementResponse>,
    val levelUp: LevelUpInfo?
)

data class LevelUpInfo(
    val previousLevel: Int,
    val newLevel: Int,
    val newTitle: String
)

data class ProgressResponse(
    val testId: String,
    val testTitle: String,
    val score: Int,
    val maxScore: Int,
    val stars: Int,
    val attemptsCount: Int,
    val bestScore: Int,
    val completedAt: Instant,
    val lastAttemptAt: Instant
)

data class UserProgressSummary(
    val totalTests: Int,
    val completedTests: Int,
    val totalStars: Int,
    val maxPossibleStars: Int,
    val categoriesProgress: List<CategoryProgressResponse>
)

data class CategoryProgressResponse(
    val categoryId: String,
    val categoryName: String,
    val testsCount: Int,
    val completedCount: Int,
    val totalStars: Int,
    val maxStars: Int
)

fun Progress.toResponse(testTitle: String) = ProgressResponse(
    testId = test.id.toString(),
    testTitle = testTitle,
    score = score,
    maxScore = maxScore,
    stars = stars,
    attemptsCount = attemptsCount,
    bestScore = bestScore,
    completedAt = completedAt,
    lastAttemptAt = lastAttemptAt
)
