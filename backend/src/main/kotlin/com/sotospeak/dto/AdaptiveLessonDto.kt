package com.sotospeak.dto

import com.sotospeak.shared.model.DifficultyLevel
import com.sotospeak.shared.model.SkillType

// ==================== Request DTOs ====================

data class StartAdaptiveLessonRequest(
    val categoryId: String? = null,
    val skillType: SkillType? = null,
    val duration: Int = 7 // 5, 7, or 10 minutes
)

data class SubmitAdaptiveAnswerRequest(
    val questionId: String,
    val answerId: String,
    val timeSpent: Int // seconds
)

// ==================== Response DTOs ====================

data class StartAdaptiveLessonResponse(
    val lessonId: String,
    val segments: List<SegmentInfo>,
    val estimatedDurationMinutes: Int,
    val targetDifficulty: DifficultyLevel
)

data class SegmentInfo(
    val id: String,
    val type: String,
    val estimatedDurationSeconds: Int,
    val learningObjective: String
)

data class NextQuestionResponse(
    val question: QuestionDto?,
    val segmentProgress: Float,
    val overallProgress: Float,
    val timeRemainingSeconds: Int,
    val requiresBreak: Boolean,
    val isLastQuestion: Boolean
)

data class QuestionDto(
    val id: String,
    val type: String,
    val text: String?,
    val imageUrl: String?,
    val audioUrl: String?,
    val answers: List<AnswerDto>,
    val difficulty: DifficultyLevel,
    val skillType: SkillType
)

data class AnswerDto(
    val id: String,
    val text: String?,
    val imageUrl: String?
)

data class SubmitAnswerResponse(
    val isCorrect: Boolean,
    val explanation: String?,
    val grammarNote: String?,
    val xpEarned: Int,
    val difficultyAdjusted: Boolean,
    val newDifficulty: DifficultyLevel?,
    val segmentComplete: Boolean,
    val correctAnswer: AnswerDto?
)

data class BreakResponse(
    val breakDuration: Int, // seconds (30 seconds recommended)
    val canResume: Boolean
)

data class ResumeLessonResponse(
    val success: Boolean,
    val nextQuestion: NextQuestionResponse?
)

data class CompleteLessonResponse(
    val totalXp: Int,
    val skillImprovements: Map<SkillType, Float>,
    val weakAreasIdentified: List<WeakAreaDto>,
    val nextRecommendedLesson: LessonRecommendation?,
    val timeSpentSeconds: Int,
    val questionsAnswered: Int,
    val accuracy: Float
)

data class WeakAreaDto(
    val skillType: SkillType,
    val masteryLevel: Float,
    val recommendedExercises: Int
)

data class LessonRecommendation(
    val categoryId: String?,
    val skillType: SkillType?,
    val difficulty: DifficultyLevel,
    val reason: String
)

data class LessonStateResponse(
    val lessonId: String,
    val status: String, // IN_PROGRESS, ON_BREAK, COMPLETED
    val currentSegment: Int,
    val totalSegments: Int,
    val currentDifficulty: DifficultyLevel,
    val timeSpentSeconds: Int,
    val questionsAnswered: Int,
    val correctAnswers: Int
)

data class WeakAreasResponse(
    val weakAreas: List<WeakAreaDto>
)

data class LessonRecommendationResponse(
    val recommendation: LessonRecommendation?
)
