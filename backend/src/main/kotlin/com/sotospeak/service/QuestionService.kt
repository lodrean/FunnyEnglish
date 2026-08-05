package com.sotospeak.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.sotospeak.dto.*
import com.sotospeak.entity.*
import com.sotospeak.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * ВРЕМЕННО УПРОЩЁННАЯ ВЕРСИЯ
 * 
 * NOTE: Content/JSONB функциональность временно отключена.
 * Используются legacy поля (text, imageUrl, answers).
 */
@Service
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val testRepository: TestRepository,
    private val answerRepository: AnswerRepository,
    private val answerValidationService: AnswerValidationService,
    private val iwQuestionRepository: ImageWordMatchQuestionRepository,
    private val iwWordRepository: ImageWordMatchWordRepository,
    private val iwHotspotRepository: ImageWordMatchHotspotRepository,
    private val objectMapper: ObjectMapper
) {

    /**
     * Маппинг JSON content в legacy-поля (text/audioUrl) и список Answer.
     * JSONB content в сущности отключён — скоринг (TestValidationService) и
     * публичная выдача работают по legacy Answer-строкам.
     */
    private fun mapContentToLegacy(question: Question, type: QuestionType, content: JsonNode?): Pair<Question, List<Answer>> {
        if (content == null || content.isNull) return question to emptyList()

        fun answer(text: String? = null, imageUrl: String? = null, isCorrect: Boolean = false,
                   displayOrder: Int = 0, matchTarget: String? = null) = Answer(
            question = question, text = text, imageUrl = imageUrl,
            isCorrect = isCorrect, displayOrder = displayOrder, matchTarget = matchTarget
        )

        return when (type) {
            QuestionType.TEXT_SELECT -> {
                val c = objectMapper.treeToValue(content, TextSelectContentRequest::class.java)
                question.copy(text = c.text) to c.answers.mapIndexed { i, a ->
                    answer(text = a.text, isCorrect = a.isCorrect, displayOrder = i)
                }
            }
            QuestionType.IMAGE_SELECT -> {
                val c = objectMapper.treeToValue(content, ImageSelectContentRequest::class.java)
                question.copy(text = c.text) to c.answers.mapIndexed { i, a ->
                    answer(text = a.text ?: a.emoji, imageUrl = a.imageUrl, isCorrect = a.isCorrect, displayOrder = i)
                }
            }
            QuestionType.AUDIO_SELECT -> {
                val c = objectMapper.treeToValue(content, AudioSelectContentRequest::class.java)
                question.copy(text = c.text, audioUrl = c.audioUrl) to c.answers.mapIndexed { i, a ->
                    answer(text = a.text, isCorrect = a.isCorrect, displayOrder = i)
                }
            }
            QuestionType.FILL_BLANK -> {
                val c = objectMapper.treeToValue(content, FillBlankContentRequest::class.java)
                question.copy(text = "${c.textBefore} ___ ${c.textAfter}") to c.answers.mapIndexed { i, a ->
                    answer(text = a.text, isCorrect = a.isCorrect, displayOrder = i)
                }
            }
            QuestionType.DRAG_DROP_MATCH, QuestionType.DRAG_DROP_IMAGE -> {
                val c = objectMapper.treeToValue(content, DragDropMatchContentRequest::class.java)
                question.copy(text = c.text) to c.items.mapIndexed { i, item ->
                    answer(text = item.text, isCorrect = true, displayOrder = i, matchTarget = item.targetId)
                }
            }
            QuestionType.DRAG_DROP_SORT -> {
                val c = objectMapper.treeToValue(content, DragDropSortContentRequest::class.java)
                question.copy(text = c.text) to c.items.map { item ->
                    answer(text = item.text, isCorrect = true, displayOrder = item.correctOrder)
                }
            }
            QuestionType.IMAGE_WORD_MATCH -> question to emptyList() // свой endpoint/репозитории
        }
    }

    @Transactional
    fun createQuestion(request: QuestionCreateRequest): QuestionResponse {
        request.testId?.let { testId ->
            testRepository.findByIdOrNull(testId)
                ?: throw IllegalArgumentException("Test not found")
        }

        val question = Question(
            test = request.testId?.let { testRepository.getReferenceById(it) },
            type = request.type,
            title = request.title,
            text = null,
            imageUrl = null,
            audioUrl = null,
            mediaUrl = request.mediaUrl,
            displayOrder = request.displayOrder,
            points = request.points,
            timeLimitSeconds = request.timeLimitSeconds,
            explanation = request.explanation,
            hint = request.hint
        )

        val (mapped, answers) = mapContentToLegacy(question, request.type, request.content)
        val saved = questionRepository.save(mapped)
        answers.forEach { answerRepository.save(it) }

        return saved.toDtoResponse()
    }

    @Transactional
    fun updateQuestion(id: UUID, request: QuestionUpdateRequest): QuestionResponse {
        val question = questionRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Question not found")

        var updated = question.copy(
            title = request.title ?: question.title,
            mediaUrl = request.mediaUrl ?: question.mediaUrl,
            displayOrder = request.displayOrder ?: question.displayOrder,
            points = request.points ?: question.points,
            timeLimitSeconds = request.timeLimitSeconds ?: question.timeLimitSeconds,
            explanation = request.explanation ?: question.explanation,
            hint = request.hint ?: question.hint,
            isPublished = request.isPublished ?: question.isPublished,
            updatedAt = Instant.now()
        )

        // content прислан — пересобираем legacy-поля и ответы
        if (request.content != null && !request.content.isNull) {
            val (mapped, answers) = mapContentToLegacy(updated, question.type, request.content)
            updated = mapped
            answerRepository.deleteByQuestionId(question.id)
            answers.forEach { answerRepository.save(it) }
        }

        return questionRepository.save(updated).toDtoResponse()
    }

    @Transactional
    fun deleteQuestion(id: UUID) {
        questionRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getQuestionById(id: UUID): QuestionResponse {
        return questionRepository.findByIdOrNull(id)?.toDtoResponse()
            ?: throw IllegalArgumentException("Question not found")
    }

    @Transactional(readOnly = true)
    fun getQuestionsByTest(testId: UUID): List<QuestionResponse> {
        return questionRepository.findByTestIdOrderByDisplayOrderAsc(testId)
            .map { it.toDtoResponse() }
    }
    
    @Transactional(readOnly = true)
    fun getQuestionsByTestWithDetails(testId: UUID): List<QuestionDetailResponse> {
        val questions = questionRepository.findByTestIdOrderByDisplayOrderAsc(testId)
        
        return questions.map { question ->
            val iwData = if (question.type == QuestionType.IMAGE_WORD_MATCH) {
                val iwQuestion = iwQuestionRepository.findByQuestionId(question.id)
                if (iwQuestion != null) {
                    val words = iwWordRepository.findByQuestionId(question.id)
                        .sortedBy { it.displayOrder }
                        .map { WordResponse(it.wordId, it.text, it.translation, it.audioUrl) }
                    
                    val hotspots = iwHotspotRepository.findByQuestionId(question.id)
                        .map { HotspotResponse(it.hotspotId, it.x, it.y, it.width, it.height, it.shape, it.wordId) }
                    
                    ImageWordMatchDetailResponse(
                        instruction = iwQuestion.instruction,
                        imageUrl = iwQuestion.imageUrl,
                        words = words,
                        hotspots = hotspots
                    )
                } else null
            } else null
            
            QuestionDetailResponse(
                id = question.id,
                testId = question.test?.id,
                type = question.type,
                title = question.title,
                mediaUrl = question.mediaUrl,
                displayOrder = question.displayOrder,
                points = question.points,
                timeLimitSeconds = question.timeLimitSeconds,
                explanation = question.explanation,
                hint = question.hint,
                isPublished = question.isPublished,
                createdAt = question.createdAt,
                updatedAt = question.updatedAt,
                imageWordMatchContent = iwData
            )
        }
    }

    @Transactional
    fun reorderQuestions(testId: UUID, questionIds: List<UUID>) {
        val questions = questionRepository.findByTestIdOrderByDisplayOrderAsc(testId)
        
        questionIds.forEachIndexed { index, id ->
            questions.find { it.id == id }?.let { question ->
                questionRepository.save(question.copy(displayOrder = index + 1))
            }
        }
    }

    @Transactional(readOnly = true)
    fun countQuestionsByTest(testId: UUID): Long {
        return questionRepository.countByTestId(testId)
    }
    
    // Временно отключённые методы
    fun duplicateQuestion(id: UUID): QuestionResponse {
        throw NotImplementedError("Duplicate temporarily disabled")
    }
    
    fun validateContent(type: QuestionType, content: JsonNode): Boolean {
        return true // Временно: всегда валидно
    }
    
    fun getQuestionForUser(id: UUID): QuestionPublicResponse {
        throw NotImplementedError("getQuestionForUser temporarily disabled")
    }
    
    fun getPublishedQuestionsByTest(testId: UUID): List<QuestionPublicResponse> {
        return questionRepository.findByTestIdAndIsPublishedTrueOrderByDisplayOrderAsc(testId)
            .map { it.toPublicResponse() }
    }
    
    // ============ IMAGE_WORD_MATCH Methods ============
    
    @Transactional
    fun createImageWordMatchQuestion(request: CreateImageWordMatchRequest): Question {
        validateCreateImageWordMatchRequest(request)
        
        val testId = UUID.fromString(request.testId)
        
        // Create base question
        val question = Question(
            test = testRepository.getReferenceById(testId),
            type = QuestionType.IMAGE_WORD_MATCH,
            title = request.instruction,
            text = null,
            imageUrl = request.imageUrl,
            audioUrl = null,
            mediaUrl = request.imageUrl,
            displayOrder = 0,
            points = request.points,
            timeLimitSeconds = null,
            explanation = null,
            hint = null
        )
        
        val savedQuestion = questionRepository.save(question)
        
        // Save IMAGE_WORD_MATCH specific data
        saveImageWordMatchData(savedQuestion.id, testId, request)
        
        return savedQuestion
    }
    
    private fun saveImageWordMatchData(
        questionId: UUID,
        testId: UUID,
        request: CreateImageWordMatchRequest
    ) {
        // Delete existing data if any
        iwWordRepository.deleteByQuestionId(questionId)
        iwHotspotRepository.deleteByQuestionId(questionId)
        iwQuestionRepository.deleteByQuestionId(questionId)
        
        // Save question data
        iwQuestionRepository.save(
            ImageWordMatchQuestionEntity(
                questionId = questionId,
                testId = testId,
                imageUrl = request.imageUrl,
                instruction = request.instruction,
                points = request.points
            )
        )
        
        // Save words
        request.words.forEachIndexed { index, word ->
            iwWordRepository.save(
                ImageWordMatchWordEntity(
                    questionId = questionId,
                    wordId = word.id,
                    text = word.text,
                    translation = word.translation,
                    audioUrl = word.audioUrl,
                    displayOrder = index
                )
            )
        }
        
        // Save hotspots
        request.hotspots.forEach { hotspot ->
            iwHotspotRepository.save(
                ImageWordMatchHotspotEntity(
                    questionId = questionId,
                    hotspotId = hotspot.id,
                    x = hotspot.x,
                    y = hotspot.y,
                    width = hotspot.width,
                    height = hotspot.height,
                    shape = hotspot.shape,
                    wordId = hotspot.wordId
                )
            )
        }
    }
    
    @Transactional
    fun updateImageWordMatchQuestion(id: UUID, request: CreateImageWordMatchRequest): Question {
        val existingQuestion = questionRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Question not found: $id")
        
        validateCreateImageWordMatchRequest(request)
        
        val updatedQuestion = existingQuestion.copy(
            title = request.instruction,
            imageUrl = request.imageUrl,
            mediaUrl = request.imageUrl,
            points = request.points,
            updatedAt = Instant.now()
        )
        
        val savedQuestion = questionRepository.save(updatedQuestion)
        
        // Update IMAGE_WORD_MATCH specific data
        saveImageWordMatchData(savedQuestion.id, existingQuestion.test?.id ?: 
            UUID.fromString(request.testId), request)
        
        return savedQuestion
    }
    
    private fun validateCreateImageWordMatchRequest(request: CreateImageWordMatchRequest) {
        require(request.words.size in 2..8) { 
            "Word count must be between 2 and 8, got ${request.words.size}" 
        }
        require(request.hotspots.size == request.words.size) { 
            "Each word must have exactly one hotspot. Words: ${request.words.size}, Hotspots: ${request.hotspots.size}" 
        }
        
        val wordIds = request.words.map { it.id }.toSet()
        require(request.hotspots.all { it.wordId in wordIds }) { 
            "All hotspots must reference valid words" 
        }
        
        val hotspotWordIds = request.hotspots.map { it.wordId }
        require(hotspotWordIds.size == hotspotWordIds.toSet().size) { 
            "Each word can only have one hotspot" 
        }
    }
    
    @Transactional(readOnly = true)
    fun getImageWordMatchQuestionForAdmin(id: UUID): ImageWordMatchQuestionResponse {
        val question = questionRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Question not found: $id")
        
        val iwData = iwQuestionRepository.findByQuestionId(id)
            ?: throw IllegalArgumentException("IMAGE_WORD_MATCH data not found for question: $id")
        
        val words = iwWordRepository.findByQuestionId(id)
            .sortedBy { it.displayOrder }
            .map { WordResponse(it.wordId, it.text, it.translation, it.audioUrl) }
        
        val hotspots = iwHotspotRepository.findByQuestionId(id)
            .map { HotspotResponse(it.hotspotId, it.x, it.y, it.width, it.height, it.shape, it.wordId) }
        
        return ImageWordMatchQuestionResponse(
            id = question.id.toString(),
            type = QuestionType.IMAGE_WORD_MATCH,
            instruction = iwData.instruction,
            points = iwData.points,
            imageUrl = iwData.imageUrl,
            words = words,
            hotspots = hotspots
        )
    }
    
    @Transactional(readOnly = true)
    fun getImageWordMatchQuestionForUser(id: UUID): ImageWordMatchPublicResponse {
        val question = questionRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Question not found: $id")
        
        val iwData = iwQuestionRepository.findByQuestionId(id)
            ?: throw IllegalArgumentException("IMAGE_WORD_MATCH data not found for question: $id")
        
        val words = iwWordRepository.findByQuestionId(id)
            .sortedBy { it.displayOrder }
            .map { WordResponse(it.wordId, it.text, it.translation, it.audioUrl) }
        
        // For users, don't include wordId in hotspots (correct answers hidden)
        val hotspots = iwHotspotRepository.findByQuestionId(id)
            .map { HotspotWithoutWordResponse(it.hotspotId, it.x, it.y, it.width, it.height, it.shape) }
        
        return ImageWordMatchPublicResponse(
            id = question.id.toString(),
            type = QuestionType.IMAGE_WORD_MATCH,
            instruction = iwData.instruction,
            points = iwData.points,
            imageUrl = iwData.imageUrl,
            words = words,
            hotspots = hotspots
        )
    }
    
    fun validateImageWordMatchAnswer(
        questionId: UUID, 
        matches: List<WordHotspotMatch>
    ): ImageWordMatchResultResponse {
        // Load IMAGE_WORD_MATCH data from separate tables
        val iwData = iwQuestionRepository.findByQuestionId(questionId)
            ?: throw IllegalArgumentException("IMAGE_WORD_MATCH question not found: $questionId")
        
        val words = iwWordRepository.findByQuestionId(questionId)
        val hotspots = iwHotspotRepository.findByQuestionId(questionId)
        
        // Create correct mapping: wordId -> hotspotId
        val correctMapping = hotspots.associate { it.wordId to it.hotspotId }
        
        // Validate each word
        val details = words.map { word ->
            val submittedMatch = matches.find { it.wordId == word.wordId }
            val correctHotspotId = correctMapping[word.wordId]
            
            if (submittedMatch != null) {
                // User made a choice for this word
                val isCorrect = submittedMatch.hotspotId == correctHotspotId
                MatchResultDetail(
                    wordId = word.wordId,
                    wordText = word.text,
                    selectedHotspotId = submittedMatch.hotspotId,
                    isCorrect = isCorrect,
                    correctHotspotId = if (isCorrect) null else correctHotspotId
                )
            } else {
                // User didn't make a choice for this word
                MatchResultDetail(
                    wordId = word.wordId,
                    wordText = word.text,
                    selectedHotspotId = "",
                    isCorrect = false,
                    correctHotspotId = correctHotspotId
                )
            }
        }
        
        // Calculate results
        val correctCount = details.count { it.isCorrect }
        val totalWords = words.size
        val percentage = if (totalWords > 0) {
            (correctCount.toFloat() / totalWords) * 100
        } else 0f
        
        // Points are proportional
        val totalPoints = iwData.points
        val earnedPoints = (percentage / 100 * totalPoints).toInt()
        
        return ImageWordMatchResultResponse(
            questionId = questionId.toString(),
            earnedPoints = earnedPoints,
            totalPoints = totalPoints,
            percentage = percentage,
            details = details
        )
    }
}

// ============ Extension Functions ============

fun Question.toDtoResponse(): QuestionResponse = QuestionResponse(
    id = id,
    testId = test?.id,
    type = type,
    title = title,
    content = null, // Временно отключено
    mediaUrl = mediaUrl,
    displayOrder = displayOrder,
    points = points,
    timeLimitSeconds = timeLimitSeconds,
    explanation = explanation,
    hint = hint,
    isPublished = isPublished,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Question.toPublicResponse(): QuestionPublicResponse = QuestionPublicResponse(
    id = id,
    type = type,
    title = title,
    content = TextSelectContentPublic("", emptyList()), // Временная заглушка
    mediaUrl = mediaUrl,
    displayOrder = displayOrder,
    points = points,
    timeLimitSeconds = timeLimitSeconds,
    hint = hint
)
