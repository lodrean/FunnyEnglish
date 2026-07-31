package com.funnyenglish.dto

import com.funnyenglish.entity.speaking.Grade
import com.funnyenglish.entity.speaking.Library
import com.funnyenglish.entity.speaking.PracticeSubmission
import com.funnyenglish.entity.speaking.SpeakingQuestion
import com.funnyenglish.entity.speaking.Topic
import com.funnyenglish.entity.speaking.Video
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant

// ============ Public responses ============

data class LibraryResponse(
    val id: String,
    val title: String,
    val description: String?,
    val coverUrl: String?,
    val topicCount: Int          // только опубликованные и не удалённые топики
)

data class TopicListItemResponse(
    val id: String,
    val title: String,
    val description: String?,
    val durationSeconds: Int?,   // из Video; null если видео ещё не загружено
    val questionCount: Int,
    val hasSubtitles: Boolean
)

data class TopicDetailResponse(
    val id: String,
    val libraryId: String,
    val title: String,
    val description: String?,
    val video: VideoResponse?,
    val questions: List<SpeakingQuestionResponse>
)

data class VideoResponse(
    val videoUrl: String,
    val subtitleUrl: String?,    // WebVTT, публичный URL (S3_PUBLIC_URL)
    val durationSeconds: Int
)

data class SpeakingQuestionResponse(
    val id: String,
    val text: String,
    val displayOrder: Int
)

// ============ Submissions (user) ============

data class SubmissionResponse(
    val id: String,
    val topicId: String,
    val topicTitle: String,
    val audioUrl: String,
    val durationSec: Int,
    val status: String,          // "NEW" | "REVIEWED"
    val grade: GradeResponse?,
    val createdAt: Instant?
)

data class GradeResponse(
    val grammar: Int,
    val vocabulary: Int,
    val pronunciation: Int,
    val fluency: Int,
    val total: BigDecimal,       // авто-усреднённый балл
    val comment: String?,
    val reviewerName: String,
    val createdAt: Instant?,
    val updatedAt: Instant?
)

// ============ Admin requests ============

data class CreateLibraryRequest(
    @field:NotBlank @field:Size(max = 255) val title: String,
    @field:Size(max = 2000) val description: String? = null,
    @field:Size(max = 500) val coverUrl: String? = null,
    val displayOrder: Int = 0,
    val isPublished: Boolean = false
)

data class UpdateLibraryRequest(
    @field:Size(max = 255) val title: String? = null,
    @field:Size(max = 2000) val description: String? = null,
    @field:Size(max = 500) val coverUrl: String? = null,
    val displayOrder: Int? = null,
    val isPublished: Boolean? = null
)

data class CreateTopicRequest(
    @field:NotNull val libraryId: String,      // UUID строкой — паттерн AudioTestDtos
    @field:NotBlank @field:Size(max = 255) val title: String,
    @field:Size(max = 2000) val description: String? = null,
    val displayOrder: Int = 0,
    val isPublished: Boolean = false
)

data class UpdateTopicRequest(
    @field:Size(max = 255) val title: String? = null,
    @field:Size(max = 2000) val description: String? = null,
    val displayOrder: Int? = null,
    val isPublished: Boolean? = null
)

data class UpsertVideoRequest(
    @field:NotBlank @field:Size(max = 500) val videoUrl: String,
    @field:Size(max = 500) val subtitleUrl: String? = null,
    @field:NotNull @field:Min(1) val durationSeconds: Int
)

data class CreateSpeakingQuestionRequest(
    @field:NotBlank val text: String,
    val displayOrder: Int = 0
)

data class GradeSubmissionRequest(
    @field:NotNull @field:Min(1) @field:Max(10) val grammar: Int,
    @field:NotNull @field:Min(1) @field:Max(10) val vocabulary: Int,
    @field:NotNull @field:Min(1) @field:Max(10) val pronunciation: Int,
    @field:NotNull @field:Min(1) @field:Max(10) val fluency: Int,
    @field:Size(max = 5000) val comment: String? = null
)

// ============ Admin responses ============

data class AdminLibraryResponse(
    val id: String, val title: String, val description: String?,
    val coverUrl: String?, val displayOrder: Int, val isPublished: Boolean,
    val topicCount: Int, val createdAt: Instant?, val updatedAt: Instant?
)

data class AdminTopicResponse(
    val id: String, val libraryId: String, val title: String, val description: String?,
    val displayOrder: Int, val isPublished: Boolean, val isDeleted: Boolean,
    val video: VideoResponse?, val questions: List<SpeakingQuestionResponse>,
    val createdAt: Instant?, val updatedAt: Instant?
)

data class AdminSubmissionResponse(
    val id: String,
    val userId: String,
    val userEmail: String,
    val userDisplayName: String,
    val topicId: String,
    val topicTitle: String,
    val audioUrl: String,
    val durationSec: Int,
    val status: String,
    val grade: GradeResponse?,
    val createdAt: Instant?
)

// ============ Мапперы ============
// Медиа-URL нормализуются в сервисе через MediaUrlService (BUG-004) — сюда приходят уже публичные.

fun Library.toPublicResponse(topicCount: Int) = LibraryResponse(
    id = id.toString(),
    title = title,
    description = description,
    coverUrl = coverUrl,
    topicCount = topicCount
)

fun Library.toAdminResponse(topicCount: Int) = AdminLibraryResponse(
    id = id.toString(),
    title = title,
    description = description,
    coverUrl = coverUrl,
    displayOrder = displayOrder,
    isPublished = isPublished,
    topicCount = topicCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Topic.toListItemResponse(questionCount: Int) = TopicListItemResponse(
    id = id.toString(),
    title = title,
    description = description,
    durationSeconds = video?.durationSeconds,
    questionCount = questionCount,
    hasSubtitles = video?.subtitleUrl != null
)

fun Topic.toDetailResponse() = TopicDetailResponse(
    id = id.toString(),
    libraryId = library?.id.toString(),
    title = title,
    description = description,
    video = video?.toResponse(),
    questions = questions.sortedBy { it.displayOrder }.map { it.toResponse() }
)

fun Topic.toAdminResponse() = AdminTopicResponse(
    id = id.toString(),
    libraryId = library?.id.toString(),
    title = title,
    description = description,
    displayOrder = displayOrder,
    isPublished = isPublished,
    isDeleted = deletedAt != null,
    video = video?.toResponse(),
    questions = questions.sortedBy { it.displayOrder }.map { it.toResponse() },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Video.toResponse() = VideoResponse(
    videoUrl = videoUrl,
    subtitleUrl = subtitleUrl,
    durationSeconds = durationSeconds
)

fun SpeakingQuestion.toResponse() = SpeakingQuestionResponse(
    id = id.toString(),
    text = text,
    displayOrder = displayOrder
)

fun Grade.toResponse() = GradeResponse(
    grammar = grammar,
    vocabulary = vocabulary,
    pronunciation = pronunciation,
    fluency = fluency,
    total = total ?: BigDecimal.ZERO,
    comment = comment,
    reviewerName = reviewer?.displayName ?: "",
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PracticeSubmission.toResponse() = SubmissionResponse(
    id = id.toString(),
    topicId = topic?.id.toString(),
    topicTitle = topic?.title ?: "",
    audioUrl = audioUrl,
    durationSec = durationSec,
    status = status.name,
    grade = grade?.toResponse(),
    createdAt = createdAt
)

fun PracticeSubmission.toAdminResponse() = AdminSubmissionResponse(
    id = id.toString(),
    userId = user?.id.toString(),
    userEmail = user?.email ?: "",
    userDisplayName = user?.displayName ?: "",
    topicId = topic?.id.toString(),
    topicTitle = topic?.title ?: "",
    audioUrl = audioUrl,
    durationSec = durationSec,
    status = status.name,
    grade = grade?.toResponse(),
    createdAt = createdAt
)
