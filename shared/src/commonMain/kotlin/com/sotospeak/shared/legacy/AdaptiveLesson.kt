package com.sotospeak.shared.model

import kotlinx.serialization.Serializable

/**
 * Адаптивная система уроков с микро-форматом и динамической сложностью
 * DifficultyLevel, SegmentType, SkillType определены в LessonModels.kt
 */

@Serializable
data class MicroLessonSegment(
    val id: String,
    val type: SegmentType,
    val questions: List<Question>,
    val estimatedDurationSeconds: Int,
    val learningObjective: String,
    val grammarHint: String? = null
)

@Serializable
data class AdaptiveLessonState(
    val lessonId: String,
    val currentSegmentIndex: Int,
    val totalSegments: Int,
    val currentDifficulty: DifficultyLevel,
    val segments: List<MicroLessonSegment>,
    val timeSpentSeconds: Int,
    val requiresBreak: Boolean,
    val weakAreas: List<SkillGap>,
    val performanceHistory: List<SegmentPerformance>
)

@Serializable
data class SkillGap(
    val skillType: SkillType,
    val masteryLevel: Float, // 0.0 - 1.0
    val relatedQuestions: List<String>
)

@Serializable
data class SegmentPerformance(
    val segmentId: String,
    val correctAnswers: Int,
    val totalAnswers: Int,
    val timeSpentSeconds: Int,
    val difficulty: DifficultyLevel
)

@Serializable
data class AdaptiveQuestionResponse(
    val segment: MicroLessonSegment,
    val segmentProgress: Float, // 0.0 - 1.0 within segment
    val overallProgress: Float, // 0.0 - 1.0 overall lesson
    val timeRemainingSeconds: Int,
    val requiresBreak: Boolean
)

@Serializable
data class FeedbackResponse(
    val isCorrect: Boolean,
    val explanation: String?,
    val grammarNote: String?,
    val nextQuestion: Question?,
    val segmentComplete: Boolean,
    val xpEarned: Int,
    val weakAreaIdentified: SkillGap?,
    val requiresBreak: Boolean = false
)

@Serializable
data class DifficultyAdjustment(
    val newDifficulty: DifficultyLevel,
    val reason: String,
    val confidenceScore: Float
)

@Serializable
data class LessonCompleteResult(
    val totalXp: Int,
    val improvedSkills: List<SkillGap>,
    val newAchievements: List<String>,
    val recommendedNextLesson: String?
)
