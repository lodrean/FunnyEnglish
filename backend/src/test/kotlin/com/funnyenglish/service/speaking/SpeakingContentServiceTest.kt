package com.funnyenglish.service.speaking

import com.funnyenglish.dto.UpsertVideoRequest
import com.funnyenglish.entity.speaking.Library
import com.funnyenglish.entity.speaking.Topic
import com.funnyenglish.entity.speaking.Video
import com.funnyenglish.repository.speaking.LibraryRepository
import com.funnyenglish.repository.speaking.SpeakingQuestionRepository
import com.funnyenglish.repository.speaking.TopicRepository
import com.funnyenglish.repository.speaking.VideoRepository
import com.funnyenglish.service.MediaUrlService
import com.funnyenglish.service.StorageService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional
import java.util.UUID

class SpeakingContentServiceTest {

    private val libraryRepository = mockk<LibraryRepository>()
    private val topicRepository = mockk<TopicRepository>()
    private val videoRepository = mockk<VideoRepository>()
    private val questionRepository = mockk<SpeakingQuestionRepository>()
    private val storageService = mockk<StorageService>(relaxed = true)
    private val mediaUrlService = mockk<MediaUrlService>()

    private lateinit var service: SpeakingContentService

    @BeforeEach
    fun setup() {
        service = SpeakingContentService(
            libraryRepository, topicRepository, videoRepository,
            questionRepository, storageService, mediaUrlService
        )
        every { mediaUrlService.normalize(any()) } answers { firstArg() }
    }

    private fun libraryWithId(name: String): Library {
        val library = Library(title = name, isPublished = true)
        ReflectionTestUtils.setField(library, "id", UUID.randomUUID())
        return library
    }

    // 1. Пустые темы (topicCount=0) отфильтрованы из публичной выдачи
    @Test
    fun `getPublishedLibraries - libraries without published topics are hidden`() {
        val withTopics = libraryWithId("Everyday Life")
        val empty = libraryWithId("Empty Theme")
        every { libraryRepository.findAllByIsPublishedTrueOrderByDisplayOrderAsc() } returns listOf(withTopics, empty)
        every { libraryRepository.countPublishedActiveTopicsByLibrary() } returns listOf(
            arrayOf(withTopics.id as Any, 3L as Any)
        )

        val result = service.getPublishedLibraries()

        assertEquals(1, result.size)
        assertEquals("Everyday Life", result[0].title)
        assertEquals(3, result[0].topicCount)
    }

    // 2. Soft-deleted топик не возвращается публичным API (репозиторий отдаёт empty → 404)
    @Test
    fun `getTopicDetail - soft-deleted topic is not found`() {
        val topicId = UUID.randomUUID()
        every { topicRepository.findPublishedActiveByIdWithDetails(topicId) } returns Optional.empty()

        assertThrows<NoSuchElementException> { service.getTopicDetail(topicId) }
    }

    // 3. upsertVideo — создание и замена (старый URL удаляется через StorageService.deleteFile)
    @Test
    fun `upsertVideo - create does not delete files, replace deletes old files best-effort`() {
        val topicId = UUID.randomUUID()
        val topic = Topic(title = "T")
        ReflectionTestUtils.setField(topic, "id", topicId)

        // --- создание ---
        every { topicRepository.findByIdWithDetails(topicId) } returns Optional.of(topic)
        every { topicRepository.save(any()) } answers { firstArg() }

        service.upsertVideo(topicId, UpsertVideoRequest(videoUrl = "https://m/v1.mp4", subtitleUrl = null, durationSeconds = 95))

        assertEquals("https://m/v1.mp4", topic.video?.videoUrl)
        verify(exactly = 0) { storageService.deleteFile(any()) }

        // --- замена ---
        every { videoRepository.save(any()) } answers { firstArg() }

        service.upsertVideo(
            topicId,
            UpsertVideoRequest(videoUrl = "https://m/v2.mp4", subtitleUrl = "https://m/s2.vtt", durationSeconds = 100)
        )

        assertEquals("https://m/v2.mp4", topic.video?.videoUrl)
        verify(exactly = 1) { storageService.deleteFile("https://m/v1.mp4") }
    }
}
