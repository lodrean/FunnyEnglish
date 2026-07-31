package com.funnyenglish.shared.model

import kotlinx.serialization.Serializable

/**
 * Модели Speaking Trainer (спека Part 1, JSON-контракт backend).
 * Имена полей = JSON-контракту (isPublished-style; Instant → ISO-строка).
 */

@Serializable
data class SpeakingLibrary(
    val id: String,
    val title: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val topicCount: Int
)

@Serializable
data class SpeakingTopicListItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val durationSeconds: Int? = null,
    val questionCount: Int,
    val hasSubtitles: Boolean
)

@Serializable
data class SpeakingTopicDetail(
    val id: String,
    val libraryId: String,
    val title: String,
    val description: String? = null,
    val video: SpeakingVideo? = null,
    val questions: List<SpeakingQuestion>
)

@Serializable
data class SpeakingVideo(
    val videoUrl: String,
    val subtitleUrl: String? = null,
    val durationSeconds: Int
)

@Serializable
data class SpeakingQuestion(
    val id: String,
    val text: String,
    val displayOrder: Int
)

@Serializable
data class SpeakingSubmission(
    val id: String,
    val topicId: String,
    val topicTitle: String,
    val audioUrl: String,
    val durationSec: Int,
    val status: String,          // "NEW" | "REVIEWED"
    val grade: SpeakingGrade? = null,
    val createdAt: String? = null
)

@Serializable
data class SpeakingGrade(
    val grammar: Int,
    val vocabulary: Int,
    val pronunciation: Int,
    val fluency: Int,
    val total: Double,           // авто-усреднённый балл (generated column в БД)
    val comment: String? = null,
    val reviewerName: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
