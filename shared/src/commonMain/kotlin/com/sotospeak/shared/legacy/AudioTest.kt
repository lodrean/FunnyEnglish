package com.sotospeak.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class AudioTest(
    val id: String,
    val title: String,
    val description: String? = null,
    val audioFileUrl: String,
    val durationSeconds: Int,
    val difficulty: Int,
    val category: Category? = null,
    val isPublished: Boolean = false,
    val playsLimit: Int? = null,
    val questionCount: Int = 0,
    val createdAt: String? = null
)

@Serializable
data class AudioTestListItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val durationSeconds: Int,
    val difficulty: Int,
    val categoryName: String? = null,
    val isPublished: Boolean = false,
    val questionsCount: Int = 0,
    val playsLimit: Int? = null
)

@Serializable
data class AudioTestDetail(
    val id: String,
    val title: String,
    val description: String? = null,
    val audioFileUrl: String,
    val durationSeconds: Int,
    val difficulty: Int,
    val category: Category? = null,
    val isPublished: Boolean = false,
    val playsLimit: Int? = null,
    val questions: List<AudioTestQuestion> = emptyList(),
    val transcript: AudioTranscript? = null,
    val createdAt: String? = null
)

@Serializable
data class AudioTestQuestion(
    val id: String,
    val questionType: AudioQuestionType,
    val title: String? = null,
    val text: String? = null,
    val startTimeSeconds: Int,
    val endTimeSeconds: Int,
    val points: Int = 1,
    val displayOrder: Int = 0,
    val answers: List<AudioTestAnswer> = emptyList()
) {
    fun isActiveAtTime(currentTimeSeconds: Int): Boolean {
        return currentTimeSeconds in startTimeSeconds..endTimeSeconds
    }
}

@Serializable
enum class AudioQuestionType {
    LISTENING_COMPREHENSION,
    FILL_BLANK,
    TRUE_FALSE,
    DICTATION
}

@Serializable
data class AudioTestAnswer(
    val id: String,
    val text: String,
    val isCorrect: Boolean = false,
    val displayOrder: Int = 0
)

@Serializable
data class AudioTranscript(
    val id: String,
    val content: String,
    val language: String = "en",
    val isGenerated: Boolean = false
)

@Serializable
data class AudioTestProgress(
    val audioTestId: String,
    val score: Int = 0,
    val maxScore: Int = 0,
    val percentage: Int = 0,
    val stars: Int = 0,
    val attemptsCount: Int = 0,
    val bestScore: Int = 0,
    val playsUsed: Int = 0,
    val playsLimit: Int? = null,
    val canPlay: Boolean = true,
    val completedAt: String? = null,
    val lastAttemptAt: String? = null
)

@Serializable
data class SubmitAudioTestRequest(
    val audioTestId: String,
    val answers: List<SubmitAudioAnswerRequest>,
    val timeSpentSeconds: Int? = null
)

@Serializable
data class SubmitAudioAnswerRequest(
    val questionId: String,
    val selectedAnswerIds: List<String> = emptyList(),
    val textAnswer: String? = null
)

@Serializable
data class SubmitAudioTestResult(
    val score: Int,
    val maxScore: Int,
    val percentage: Int,
    val stars: Int,
    val pointsEarned: Int,
    val isNewBestScore: Boolean,
    val levelUp: LevelUpInfo? = null,
    val newAchievements: List<Achievement> = emptyList()
)

@Serializable
data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int
)

@Serializable
data class WaveformData(
    val audioTestId: String,
    val samples: List<Float>,
    val sampleRate: Int
)
