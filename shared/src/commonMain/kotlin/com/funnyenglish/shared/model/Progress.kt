package com.funnyenglish.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Progress(
    val testId: String,
    val testTitle: String,
    val score: Int,
    val maxScore: Int,
    val stars: Int,
    val attemptsCount: Int,
    val bestScore: Int,
    val completedAt: String,
    val lastAttemptAt: String
)

@Serializable
data class ProgressSummary(
    val totalTests: Int,
    val completedTests: Int,
    val totalStars: Int,
    val maxPossibleStars: Int,
    val categoriesProgress: List<CategoryProgress>
)

@Serializable
data class CategoryProgress(
    val categoryId: String,
    val categoryName: String,
    val testsCount: Int,
    val completedCount: Int,
    val totalStars: Int,
    val maxStars: Int
)

@Serializable
data class SubmitTestRequest(
    val testId: String,
    val answers: List<SubmitAnswer>,
    val timeSpentSeconds: Int? = null
)

@Serializable
data class SubmitAnswer(
    val questionId: String,
    val selectedAnswerIds: List<String> = emptyList(),
    val dragDropMatches: Map<String, String>? = null
)

@Serializable
data class SubmitTestResult(
    val score: Int,
    val maxScore: Int,
    val percentage: Int,
    val stars: Int,
    val pointsEarned: Int,
    val isNewBestScore: Boolean,
    val newAchievements: List<Achievement>,
    val levelUp: LevelUpInfo? = null
)

@Serializable
data class LevelUpInfo(
    val previousLevel: Int,
    val newLevel: Int,
    val newTitle: String
)
