package com.funnyenglish.dto

import com.funnyenglish.entity.*
import java.time.Instant
import java.util.UUID

// ============ Request DTOs ============

data class QuestionCreateRequest(
    val testId: UUID? = null,
    val type: QuestionType,
    val title: String,
    // JsonNode, а не sealed QuestionContentRequest: у клиентов нет дискриминатора типа,
    // а DEDUCTION не работает (TEXT_SELECT и IMAGE_SELECT имеют одинаковые поля).
    // Типизированный маппинг — в QuestionService по полю type.
    val content: com.fasterxml.jackson.databind.JsonNode? = null,
    val mediaUrl: String? = null,
    val displayOrder: Int = 0,
    val points: Int = 1,
    val timeLimitSeconds: Int? = null,
    val explanation: String? = null,
    val hint: String? = null
)

data class QuestionUpdateRequest(
    val title: String? = null,
    val content: com.fasterxml.jackson.databind.JsonNode? = null,
    val mediaUrl: String? = null,
    val displayOrder: Int? = null,
    val points: Int? = null,
    val timeLimitSeconds: Int? = null,
    val explanation: String? = null,
    val hint: String? = null,
    val isPublished: Boolean? = null
)

/**
 * Union type для всех вариантов content
 * Клиент отправляет нужную структуру в зависимости от type
 */
sealed interface QuestionContentRequest

data class TextSelectContentRequest(
    val text: String,
    val answers: List<AnswerOptionRequest>
) : QuestionContentRequest

data class ImageSelectContentRequest(
    val text: String? = null,
    val answers: List<ImageAnswerOptionRequest>
) : QuestionContentRequest

data class AudioSelectContentRequest(
    val audioUrl: String,
    val transcript: String? = null,
    val text: String? = null,
    val answers: List<AnswerOptionRequest>
) : QuestionContentRequest

data class DragDropMatchContentRequest(
    val text: String,
    val items: List<DragItemRequest>,
    val targets: List<DropTargetRequest>
) : QuestionContentRequest

data class DragDropSortContentRequest(
    val text: String,
    val items: List<SortItemRequest>
) : QuestionContentRequest

data class FillBlankContentRequest(
    val textBefore: String,
    val textAfter: String,
    val answers: List<AnswerOptionRequest>
) : QuestionContentRequest

// Sub-Request DTOs
data class AnswerOptionRequest(
    val id: String,
    val text: String,
    val isCorrect: Boolean = false
)

data class ImageAnswerOptionRequest(
    val id: String,
    val imageUrl: String? = null,
    val emoji: String? = null,
    val text: String? = null,
    val isCorrect: Boolean = false
)

data class DragItemRequest(
    val id: String,
    val text: String,
    val targetId: String
)

data class DropTargetRequest(
    val id: String,
    val imageUrl: String? = null,
    val emoji: String? = null,
    val text: String? = null
)

data class SortItemRequest(
    val id: String,
    val text: String,
    val correctOrder: Int
)

// ============ Response DTOs ============

data class QuestionResponse(
    val id: UUID,
    val testId: UUID?,
    val type: QuestionType,
    val title: String,
    val content: QuestionContent?,
    val mediaUrl: String?,
    val displayOrder: Int,
    val points: Int,
    val timeLimitSeconds: Int?,
    val explanation: String?,
    val hint: String?,
    val isPublished: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Детальный ответ для админки с полными данными по типу вопроса
 */
data class QuestionDetailResponse(
    val id: UUID,
    val testId: UUID?,
    val type: QuestionType,
    val title: String,
    val mediaUrl: String?,
    val displayOrder: Int,
    val points: Int,
    val timeLimitSeconds: Int?,
    val explanation: String?,
    val hint: String?,
    val isPublished: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    // Для IMAGE_WORD_MATCH
    val imageWordMatchContent: ImageWordMatchDetailResponse? = null
)

data class ImageWordMatchDetailResponse(
    val instruction: String,
    val imageUrl: String,
    val words: List<WordResponse>,
    val hotspots: List<HotspotResponse>
)

/**
 * DTO для отображения вопроса пользователю (без правильных ответов)
 */
data class QuestionPublicResponse(
    val id: UUID,
    val type: QuestionType,
    val title: String,
    val content: QuestionContentPublic,
    val mediaUrl: String?,
    val displayOrder: Int,
    val points: Int,
    val timeLimitSeconds: Int?,
    val hint: String?
)

/**
 * Content без isCorrect флагов (для отправки клиенту)
 */
sealed interface QuestionContentPublic

data class TextSelectContentPublic(
    val text: String,
    val answers: List<AnswerOptionPublic>
) : QuestionContentPublic

data class ImageSelectContentPublic(
    val text: String? = null,
    val answers: List<ImageAnswerOptionPublic>
) : QuestionContentPublic

data class AudioSelectContentPublic(
    val audioUrl: String,
    val text: String? = null,
    val answers: List<AnswerOptionPublic>
) : QuestionContentPublic

data class DragDropMatchContentPublic(
    val text: String,
    val items: List<DragItemPublic>,
    val targets: List<DropTargetPublic>
) : QuestionContentPublic

data class DragDropSortContentPublic(
    val text: String,
    val items: List<SortItemPublic>
) : QuestionContentPublic

data class FillBlankContentPublic(
    val textBefore: String,
    val textAfter: String,
    val answers: List<AnswerOptionPublic>
) : QuestionContentPublic

// Public sub-DTOs (без isCorrect)
data class AnswerOptionPublic(
    val id: String,
    val text: String
)

data class ImageAnswerOptionPublic(
    val id: String,
    val imageUrl: String? = null,
    val emoji: String? = null,
    val text: String? = null
)

data class DragItemPublic(
    val id: String,
    val text: String
)

data class DropTargetPublic(
    val id: String,
    val imageUrl: String? = null,
    val emoji: String? = null,
    val text: String? = null
)

data class SortItemPublic(
    val id: String,
    val text: String
)

// ============ Admin List DTO ============

data class QuestionListItemResponse(
    val id: UUID,
    val type: QuestionType,
    val title: String,
    val displayOrder: Int,
    val points: Int,
    val isPublished: Boolean,
    val preview: String?,
    val updatedAt: Instant
)

// ============ Reorder DTO ============

data class ReorderQuestionsRequest(
    val questionIds: List<UUID>
)
