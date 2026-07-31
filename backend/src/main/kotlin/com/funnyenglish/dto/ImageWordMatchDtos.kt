package com.funnyenglish.dto

import com.funnyenglish.entity.*
import java.util.UUID

// ============ Request DTOs ============

data class CreateImageWordMatchRequest(
    val testId: String,
    val instruction: String,
    val imageUrl: String,
    val words: List<WordRequest>,
    val hotspots: List<HotspotRequest>,
    val points: Int = 10
)

data class WordRequest(
    val id: String,
    val text: String,
    val translation: String? = null,
    val audioUrl: String? = null
)

data class HotspotRequest(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val shape: HotspotShape = HotspotShape.RECTANGLE,
    val wordId: String
)

data class SubmitImageWordMatchAnswerRequest(
    val questionId: String,
    val matches: List<WordHotspotMatch>
)

data class WordHotspotMatch(
    val wordId: String,
    val hotspotId: String
)

// ============ Response DTOs ============

data class ImageWordMatchQuestionResponse(
    val id: String,
    val type: QuestionType,
    val instruction: String,
    val points: Int,
    val imageUrl: String,
    val words: List<WordResponse>,
    val hotspots: List<HotspotResponse>
)

data class WordResponse(
    val id: String,
    val text: String,
    val translation: String?,
    val audioUrl: String?
)

data class HotspotResponse(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val shape: HotspotShape,
    val wordId: String
)

/**
 * Публичный response без правильных ответов (для клиентов)
 */
data class ImageWordMatchPublicResponse(
    val id: String,
    val type: QuestionType,
    val instruction: String,
    val points: Int,
    val imageUrl: String,
    val words: List<WordResponse>,
    val hotspots: List<HotspotWithoutWordResponse>
)

/**
 * Hotspot без wordId (чтобы клиент не видел правильные ответы)
 */
data class HotspotWithoutWordResponse(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val shape: HotspotShape
)

/**
 * Результат проверки ответов
 */
data class ImageWordMatchResultResponse(
    val questionId: String,
    val earnedPoints: Int,
    val totalPoints: Int,
    val percentage: Float,
    val details: List<MatchResultDetail>
)

data class MatchResultDetail(
    val wordId: String,
    val wordText: String,
    val selectedHotspotId: String,
    val isCorrect: Boolean,
    val correctHotspotId: String? = null
)

// ============ Mappers ============

fun ImageWordMatchContent.toAdminResponse(
    questionId: UUID,
    type: QuestionType,
    points: Int,
    instruction: String
) = ImageWordMatchQuestionResponse(
    id = questionId.toString(),
    type = type,
    instruction = instruction,
    points = points,
    imageUrl = imageUrl,
    words = words.map { WordResponse(it.id, it.text, it.translation, it.audioUrl) },
    hotspots = hotspots.map { 
        HotspotResponse(it.id, it.x, it.y, it.width, it.height, it.shape, it.wordId) 
    }
)

fun ImageWordMatchContent.toPublicResponse(
    questionId: UUID,
    type: QuestionType,
    points: Int,
    instruction: String
) = ImageWordMatchPublicResponse(
    id = questionId.toString(),
    type = type,
    instruction = instruction,
    points = points,
    imageUrl = imageUrl,
    words = words.map { WordResponse(it.id, it.text, it.translation, it.audioUrl) },
    hotspots = hotspots.map { 
        HotspotWithoutWordResponse(it.id, it.x, it.y, it.width, it.height, it.shape) 
    }
)
