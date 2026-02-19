package com.funnyenglish.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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
    val dragDropMatches: Map<String, String>? = null,
    val imageWordMatches: Map<String, String>? = null  // wordId -> hotspotId for IMAGE_WORD_MATCH
)

@Serializable
data class SubmitTestResult(
    val score: Int,
    val maxScore: Int,
    val percentage: Int,
    val stars: Int,
    val pointsEarned: Int,
    @SerialName("newBestScore")
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

// XP and Leveling System

@Serializable
data class XpData(
    val currentXp: Int,
    val currentLevel: Int,
    val xpForNextLevel: Int,
    val xpInCurrentLevel: Int,
    val skillXp: Map<SkillType, Int>,
    val recentXpGains: List<XpGain>
)

@Serializable
data class XpGain(
    val amount: Int,
    val source: XpSource,
    val timestamp: String,
    val description: String?
)

@Serializable
enum class XpSource {
    LESSON_COMPLETION,
    PERFECT_ANSWER,
    STREAK_MAINTAINED,
    QUEST_COMPLETED,
    ACHIEVEMENT_UNLOCKED,
    REVIEW_COMPLETED,
    CHALLENGE_COMPLETED
}
