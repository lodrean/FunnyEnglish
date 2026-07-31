package com.funnyenglish.dto

import com.funnyenglish.entity.audio.QuestionType
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.Instant
import java.util.UUID

// ============== Response DTOs ==============

data class AudioTestResponse(
    val id: String,
    val title: String,
    val description: String?,
    val audioFileUrl: String,
    val durationSeconds: Int,
    val difficulty: Int,
    val category: CategoryResponse?,
    val isPublished: Boolean,
    val playsLimit: Int?,
    val questionCount: Int,
    val createdAt: Instant?
)

data class AudioTestDetailResponse(
    val id: String? = null,
    val title: String = "",
    val description: String? = null,
    val audioFileUrl: String = "",
    val durationSeconds: Int = 0,
    val difficulty: Int = 1,
    val category: CategoryResponse? = null,
    val isPublished: Boolean = false,
    val playsLimit: Int? = null,
    val questions: List<AudioTestQuestionResponse> = emptyList(),
    val transcript: AudioTranscriptResponse? = null,
    val createdAt: Instant? = null
)

data class AudioTestQuestionResponse(
    val id: String,
    val questionType: QuestionType,
    val title: String?,
    val text: String?,
    val startTimeSeconds: Int,
    val endTimeSeconds: Int,
    val points: Int,
    val displayOrder: Int,
    val answers: List<AudioTestAnswerResponse>
)

data class AudioTestAnswerResponse(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
    val displayOrder: Int
)

data class AudioTranscriptResponse(
    val id: String,
    val content: String,
    val language: String,
    val isGenerated: Boolean
)

data class AudioTestProgressResponse(
    val audioTestId: String,
    val score: Int,
    val maxScore: Int,
    val percentage: Int,
    val stars: Int,
    val attemptsCount: Int,
    val bestScore: Int,
    val playsUsed: Int,
    val playsLimit: Int?,
    val canPlay: Boolean,
    val completedAt: Instant?,
    val lastAttemptAt: Instant?
)

// ============== Request DTOs ==============

data class CreateAudioTestRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must be less than 255 characters")
    val title: String,

    @field:Size(max = 2000, message = "Description must be less than 2000 characters")
    val description: String? = null,

    @field:NotBlank(message = "Audio file URL is required")
    @field:Size(max = 500, message = "Audio file URL must be less than 500 characters")
    val audioFileUrl: String,

    @field:NotNull(message = "Duration is required")
    @field:Min(value = 1, message = "Duration must be at least 1 second")
    val durationSeconds: Int,

    @field:NotNull(message = "Difficulty is required")
    @field:Min(value = 1, message = "Difficulty must be between 1 and 5")
    @field:Max(value = 5, message = "Difficulty must be between 1 and 5")
    val difficulty: Int,

    val categoryId: UUID? = null,

    val playsLimit: Int? = null,

    @field:Valid
    val questions: List<CreateAudioQuestionRequest> = emptyList(),

    val transcript: CreateTranscriptRequest? = null
)

data class UpdateAudioTestRequest(
    @field:Size(max = 255, message = "Title must be less than 255 characters")
    val title: String? = null,

    @field:Size(max = 2000, message = "Description must be less than 2000 characters")
    val description: String? = null,

    @field:Min(value = 1, message = "Duration must be at least 1 second")
    val durationSeconds: Int? = null,

    @field:Min(value = 1, message = "Difficulty must be between 1 and 5")
    @field:Max(value = 5, message = "Difficulty must be between 1 and 5")
    val difficulty: Int? = null,

    val categoryId: UUID? = null,

    val playsLimit: Int? = null,

    val isPublished: Boolean? = null
)

data class CreateAudioQuestionRequest(
    @field:NotNull(message = "Question type is required")
    val questionType: QuestionType,

    @field:Size(max = 500, message = "Title must be less than 500 characters")
    val title: String? = null,

    val text: String? = null,

    @field:NotNull(message = "Start time is required")
    @field:Min(value = 0, message = "Start time cannot be negative")
    val startTimeSeconds: Int,

    @field:NotNull(message = "End time is required")
    @field:Min(value = 1, message = "End time must be at least 1 second")
    val endTimeSeconds: Int,

    @field:NotNull(message = "Points is required")
    @field:Min(value = 1, message = "Points must be at least 1")
    val points: Int = 1,

    val displayOrder: Int = 0,

    @field:Valid
    @field:NotEmpty(message = "At least one answer is required")
    val answers: List<CreateAudioAnswerRequest>
)

data class CreateAudioAnswerRequest(
    @field:NotBlank(message = "Answer text is required")
    val text: String,

    val isCorrect: Boolean = false,

    val displayOrder: Int = 0
)

data class CreateTranscriptRequest(
    @field:NotBlank(message = "Transcript content is required")
    val content: String,

    @field:NotBlank(message = "Language is required")
    @field:Size(min = 2, max = 10, message = "Language code must be 2-10 characters")
    val language: String = "en",

    val isGenerated: Boolean = false
)

// ============== Submit DTOs ==============

data class SubmitAudioTestRequest(
    @field:NotBlank(message = "Audio test ID is required")
    val audioTestId: String,

    @field:Valid
    @field:NotEmpty(message = "Answers are required")
    val answers: List<SubmitAudioAnswerRequest>,

    @field:Min(value = 0, message = "Time spent cannot be negative")
    val timeSpentSeconds: Int? = null
)

data class SubmitAudioAnswerRequest(
    @field:NotBlank(message = "Question ID is required")
    val questionId: String,

    val selectedAnswerIds: List<String> = emptyList(),

    val textAnswer: String? = null
)

data class SubmitAudioTestResponse(
    val score: Int,
    val maxScore: Int,
    val percentage: Int,
    val stars: Int,
    val pointsEarned: Int,
    val isNewBestScore: Boolean,
    val levelUp: LevelUpInfo?,
    val newAchievements: List<AchievementResponse>
)

// ============== Upload DTOs ==============

data class AudioUploadResponse(
    val url: String,
    val originalFilename: String,
    val fileSize: Long,
    val durationSeconds: Int?,
    val contentType: String
)
