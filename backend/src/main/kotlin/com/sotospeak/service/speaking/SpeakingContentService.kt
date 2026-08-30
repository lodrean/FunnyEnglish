package com.sotospeak.service.speaking

import com.sotospeak.config.EvictSpeakingPublicCache
import com.sotospeak.config.SPEAKING_PUBLIC_LIBRARIES
import com.sotospeak.config.SPEAKING_PUBLIC_TOPICS
import com.sotospeak.config.SPEAKING_PUBLIC_TOPIC_DETAILS
import com.sotospeak.dto.*
import com.sotospeak.entity.speaking.Library
import com.sotospeak.entity.speaking.SpeakingQuestion
import com.sotospeak.entity.speaking.Topic
import com.sotospeak.entity.speaking.Video
import com.sotospeak.repository.speaking.LibraryRepository
import com.sotospeak.repository.speaking.SpeakingQuestionRepository
import com.sotospeak.repository.speaking.TopicRepository
import com.sotospeak.repository.speaking.VideoRepository
import com.sotospeak.service.MediaUrlService
import com.sotospeak.service.StorageService
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Speaking Trainer: публичная выдача контента + admin CRUD (Part 1 §6.4, §5.4).
 * Контроллеры маппятся БЕЗ /api (context-path), см. спеку §1.2.
 */
@Service
@Transactional
class SpeakingContentService(
    private val libraryRepository: LibraryRepository,
    private val topicRepository: TopicRepository,
    private val videoRepository: VideoRepository,
    private val questionRepository: SpeakingQuestionRepository,
    private val storageService: StorageService,
    private val mediaUrlService: MediaUrlService
) {
    private val logger = LoggerFactory.getLogger(SpeakingContentService::class.java)

    // ============== Public (guest-readable) ==============
    // Публичные read-методы кэшируются в Caffeine (bd FunnyEnglish-wy7.7, §4.3.3);
    // инвалидация — @EvictSpeakingPublicCache на всех admin-мутациях ниже.

    @Cacheable(SPEAKING_PUBLIC_LIBRARIES)
    @Transactional(readOnly = true)
    fun getPublishedLibraries(): List<LibraryResponse> {
        val counts = libraryRepository.countPublishedActiveTopicsByLibrary()
            .associate { (it[0] as UUID) to (it[1] as Long).toInt() }
        return libraryRepository.findAllByIsPublishedTrueOrderByDisplayOrderAsc()
            .mapNotNull { library ->
                val topicCount = counts[library.id] ?: 0
                if (topicCount == 0) return@mapNotNull null // пустые темы скрыты
                library.toPublicResponse(topicCount)
                    .copy(coverUrl = mediaUrlService.normalize(library.coverUrl))
            }
    }

    @Cacheable(SPEAKING_PUBLIC_TOPICS)
    @Transactional(readOnly = true)
    fun getPublishedTopics(libraryId: UUID): List<TopicListItemResponse> {
        val library = libraryRepository.findById(libraryId)
            .filter { it.isPublished }
            .orElseThrow { NoSuchElementException("Library not found") }
        val topics = topicRepository.findPublishedActiveByLibraryIdWithVideo(library.id!!)
        val questionCounts = topicRepository.countQuestionsByTopicIds(topics.mapNotNull { it.id })
            .associate { (it[0] as UUID) to (it[1] as Long).toInt() }
        return topics.map { it.toListItemResponse(questionCounts[it.id] ?: 0) }
    }

    @Cacheable(SPEAKING_PUBLIC_TOPIC_DETAILS)
    @Transactional(readOnly = true)
    fun getTopicDetail(id: UUID): TopicDetailResponse {
        val topic = topicRepository.findPublishedActiveByIdWithDetails(id)
            .orElseThrow { NoSuchElementException("Topic not found") }
        return topic.toDetailResponse().copy(video = topic.video?.toResponse()?.normalized())
    }

    // ============== Admin: Libraries ==============

    @Transactional(readOnly = true)
    fun getAllLibraries(): List<AdminLibraryResponse> =
        libraryRepository.findAllByOrderByDisplayOrderAsc().map { library ->
            library.toAdminResponse(library.topics.count { it.deletedAt == null })
                .copy(coverUrl = mediaUrlService.normalize(library.coverUrl))
        }

    @EvictSpeakingPublicCache
    fun createLibrary(request: CreateLibraryRequest): AdminLibraryResponse {
        val library = libraryRepository.save(
            Library(
                title = request.title,
                description = request.description,
                coverUrl = request.coverUrl,
                displayOrder = request.displayOrder,
                isPublished = request.isPublished
            )
        )
        return library.toAdminResponse(0)
    }

    @EvictSpeakingPublicCache
    fun updateLibrary(id: UUID, request: UpdateLibraryRequest): AdminLibraryResponse {
        val library = libraryRepository.findById(id)
            .orElseThrow { NoSuchElementException("Library not found") }
        request.title?.let { library.title = it }
        request.description?.let { library.description = it }
        request.coverUrl?.let { library.coverUrl = it }
        request.displayOrder?.let { library.displayOrder = it }
        request.isPublished?.let { library.isPublished = it }
        val saved = libraryRepository.save(library)
        return saved.toAdminResponse(saved.topics.count { it.deletedAt == null })
    }

    @EvictSpeakingPublicCache
    fun deleteLibrary(id: UUID) {
        val library = libraryRepository.findById(id)
            .orElseThrow { NoSuchElementException("Library not found") }
        try {
            libraryRepository.delete(library)
            libraryRepository.flush()
        } catch (e: DataIntegrityViolationException) {
            // practice_submissions.topic_id ON DELETE RESTRICT — есть записи учеников
            throw IllegalArgumentException("Library has submissions; archive topics instead")
        }
    }

    // ============== Admin: Topics ==============

    @Transactional(readOnly = true)
    fun getTopics(libraryId: UUID): List<AdminTopicResponse> =
        topicRepository.findByLibraryIdOrderByDisplayOrderAsc(libraryId)
            .map { it.toAdminResponse().copy(video = it.video?.toResponse()?.normalized()) }

    /** Детали топика для admin (включая черновики и soft-deleted — deep-link без N+1, Part 3 §3.3) */
    @Transactional(readOnly = true)
    fun getTopic(id: UUID): AdminTopicResponse {
        val topic = topicRepository.findByIdWithDetails(id)
            .orElseThrow { NoSuchElementException("Topic not found") }
        return topic.toAdminResponse().copy(video = topic.video?.toResponse()?.normalized())
    }

    @EvictSpeakingPublicCache
    fun createTopic(request: CreateTopicRequest): AdminTopicResponse {
        val library = libraryRepository.findById(parseUuid(request.libraryId))
            .orElseThrow { NoSuchElementException("Library not found") }
        val topic = Topic(
            title = request.title,
            description = request.description,
            displayOrder = request.displayOrder,
            isPublished = request.isPublished
        )
        library.addTopic(topic)
        val saved = topicRepository.save(topic)
        return saved.toAdminResponse()
    }

    @EvictSpeakingPublicCache
    fun updateTopic(id: UUID, request: UpdateTopicRequest): AdminTopicResponse {
        val topic = topicRepository.findByIdWithDetails(id)
            .orElseThrow { NoSuchElementException("Topic not found") }
        request.title?.let { topic.title = it }
        request.description?.let { topic.description = it }
        request.displayOrder?.let { topic.displayOrder = it }
        request.isPublished?.let { topic.isPublished = it }
        val saved = topicRepository.save(topic)
        return saved.toAdminResponse().copy(video = saved.video?.toResponse()?.normalized())
    }

    /** Точечный publish/unpublish без полного PUT (Part 3 §3.3) */
    @EvictSpeakingPublicCache
    fun publishLibrary(id: UUID, isPublished: Boolean): AdminLibraryResponse {
        val library = libraryRepository.findById(id)
            .orElseThrow { NoSuchElementException("Library not found") }
        library.isPublished = isPublished
        val saved = libraryRepository.save(library)
        return saved.toAdminResponse(saved.topics.count { it.deletedAt == null })
    }

    @EvictSpeakingPublicCache
    fun publishTopic(id: UUID, isPublished: Boolean): AdminTopicResponse {
        val topic = topicRepository.findByIdWithDetails(id)
            .orElseThrow { NoSuchElementException("Topic not found") }
        topic.isPublished = isPublished
        val saved = topicRepository.save(topic)
        return saved.toAdminResponse().copy(video = saved.video?.toResponse()?.normalized())
    }

    /** Soft delete, идемпотентно (Part 1 §5.4) */
    @EvictSpeakingPublicCache
    fun deleteTopic(id: UUID) {
        val topic = topicRepository.findById(id)
            .orElseThrow { NoSuchElementException("Topic not found") }
        if (topic.deletedAt == null) {
            topic.deletedAt = Instant.now()
            topicRepository.save(topic)
        }
    }

    // ============== Admin: Video (upsert) ==============

    @EvictSpeakingPublicCache
    fun upsertVideo(topicId: UUID, request: UpsertVideoRequest): AdminTopicResponse {
        val topic = topicRepository.findByIdWithDetails(topicId)
            .orElseThrow { NoSuchElementException("Topic not found") }

        val oldVideoUrl: String?
        val oldSubtitleUrl: String?
        val existing = topic.video
        if (existing != null) {
            oldVideoUrl = existing.videoUrl.takeIf { it != request.videoUrl }
            oldSubtitleUrl = existing.subtitleUrl.takeIf { it != request.subtitleUrl }
            existing.videoUrl = request.videoUrl
            existing.subtitleUrl = request.subtitleUrl
            existing.durationSeconds = request.durationSeconds
            videoRepository.save(existing)
        } else {
            oldVideoUrl = null
            oldSubtitleUrl = null
            topic.video = Video(
                topic = topic,
                videoUrl = request.videoUrl,
                subtitleUrl = request.subtitleUrl,
                durationSeconds = request.durationSeconds
            )
            topicRepository.save(topic)
        }

        // best-effort: старые файлы из MinIO, ошибка удаления не откатывает транзакцию (§5.4)
        listOfNotNull(oldVideoUrl, oldSubtitleUrl).forEach { url ->
            runCatching { storageService.deleteFile(url) }
                .onFailure { logger.warn("Failed to delete replaced media file: $url", it) }
        }

        val saved = topicRepository.findByIdWithDetails(topicId).get()
        return saved.toAdminResponse().copy(video = saved.video?.toResponse()?.normalized())
    }

    // ============== Admin: Questions ==============

    @EvictSpeakingPublicCache
    fun addQuestion(topicId: UUID, request: CreateSpeakingQuestionRequest): SpeakingQuestionResponse {
        val topic = topicRepository.findById(topicId)
            .orElseThrow { NoSuchElementException("Topic not found") }
        val question = SpeakingQuestion(text = request.text, displayOrder = request.displayOrder)
        topic.addQuestion(question)
        return questionRepository.save(question).toResponse()
    }

    @EvictSpeakingPublicCache
    fun updateQuestion(id: UUID, request: CreateSpeakingQuestionRequest): SpeakingQuestionResponse {
        val question = questionRepository.findById(id)
            .orElseThrow { NoSuchElementException("Question not found") }
        question.text = request.text
        question.displayOrder = request.displayOrder
        return questionRepository.save(question).toResponse()
    }

    @EvictSpeakingPublicCache
    fun deleteQuestion(id: UUID) {
        val question = questionRepository.findById(id)
            .orElseThrow { NoSuchElementException("Question not found") }
        questionRepository.delete(question)
    }

    /**
     * Batch-reorder вопросов топика (Part 3 §3.2): принимает ПОЛНЫЙ упорядоченный
     * список id вопросов топика; displayOrder = индекс в списке. Несовпадение
     * набора id с вопросами топика → 400 (IllegalArgumentException).
     */
    @EvictSpeakingPublicCache
    fun reorderQuestions(topicId: UUID, questionIds: List<String>) {
        val topic = topicRepository.findByIdWithDetails(topicId)
            .orElseThrow { NoSuchElementException("Topic not found") }
        val orderedIds = questionIds.map { parseUuid(it) } // невалидный UUID → IllegalArgumentException → 400
        val byId = topic.questions.mapNotNull { q -> q.id?.let { it to q } }.toMap()
        require(orderedIds.size == byId.size && orderedIds.all { it in byId }) {
            "questionIds must contain exactly all question ids of the topic"
        }
        orderedIds.forEachIndexed { index, questionId -> byId.getValue(questionId).displayOrder = index }
        questionRepository.saveAll(topic.questions)
    }

    // ============== Helpers ==============

    private fun VideoResponse.normalized() = copy(
        videoUrl = mediaUrlService.normalize(videoUrl) ?: videoUrl,
        subtitleUrl = mediaUrlService.normalize(subtitleUrl)
    )

    private fun parseUuid(value: String): UUID = UUID.fromString(value)
}
