package com.funnyenglish.service.speaking

import com.funnyenglish.dto.*
import com.funnyenglish.entity.speaking.Library
import com.funnyenglish.entity.speaking.SpeakingQuestion
import com.funnyenglish.entity.speaking.Topic
import com.funnyenglish.entity.speaking.Video
import com.funnyenglish.repository.speaking.LibraryRepository
import com.funnyenglish.repository.speaking.SpeakingQuestionRepository
import com.funnyenglish.repository.speaking.TopicRepository
import com.funnyenglish.repository.speaking.VideoRepository
import com.funnyenglish.service.MediaUrlService
import com.funnyenglish.service.StorageService
import org.slf4j.LoggerFactory
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

    /** Soft delete, идемпотентно (Part 1 §5.4) */
    fun deleteTopic(id: UUID) {
        val topic = topicRepository.findById(id)
            .orElseThrow { NoSuchElementException("Topic not found") }
        if (topic.deletedAt == null) {
            topic.deletedAt = Instant.now()
            topicRepository.save(topic)
        }
    }

    // ============== Admin: Video (upsert) ==============

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

    fun addQuestion(topicId: UUID, request: CreateSpeakingQuestionRequest): SpeakingQuestionResponse {
        val topic = topicRepository.findById(topicId)
            .orElseThrow { NoSuchElementException("Topic not found") }
        val question = SpeakingQuestion(text = request.text, displayOrder = request.displayOrder)
        topic.addQuestion(question)
        return questionRepository.save(question).toResponse()
    }

    fun updateQuestion(id: UUID, request: CreateSpeakingQuestionRequest): SpeakingQuestionResponse {
        val question = questionRepository.findById(id)
            .orElseThrow { NoSuchElementException("Question not found") }
        question.text = request.text
        question.displayOrder = request.displayOrder
        return questionRepository.save(question).toResponse()
    }

    fun deleteQuestion(id: UUID) {
        val question = questionRepository.findById(id)
            .orElseThrow { NoSuchElementException("Question not found") }
        questionRepository.delete(question)
    }

    // ============== Helpers ==============

    private fun VideoResponse.normalized() = copy(
        videoUrl = mediaUrlService.normalize(videoUrl) ?: videoUrl,
        subtitleUrl = mediaUrlService.normalize(subtitleUrl)
    )

    private fun parseUuid(value: String): UUID = UUID.fromString(value)
}
