package com.sotospeak.service.speaking

import com.sotospeak.dto.GradeSubmissionRequest
import com.sotospeak.entity.User
import com.sotospeak.entity.speaking.Grade
import com.sotospeak.entity.speaking.PracticeSubmission
import com.sotospeak.entity.speaking.SubmissionStatus
import com.sotospeak.entity.speaking.Topic
import com.sotospeak.repository.UserRepository
import com.sotospeak.repository.speaking.GradeRepository
import com.sotospeak.repository.speaking.PracticeSubmissionRepository
import com.sotospeak.repository.speaking.TopicRepository
import com.sotospeak.service.MediaUrlService
import com.sotospeak.service.StorageService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.multipart.MultipartFile
import java.util.Optional
import java.util.UUID

class PracticeSubmissionServiceTest {

    private val submissionRepository = mockk<PracticeSubmissionRepository>()
    private val gradeRepository = mockk<GradeRepository>()
    private val topicRepository = mockk<TopicRepository>()
    private val userRepository = mockk<UserRepository>()
    private val storageService = mockk<StorageService>()
    private val mediaUrlService = mockk<MediaUrlService>()
    private val entityManager = mockk<EntityManager>(relaxed = true)

    private lateinit var service: PracticeSubmissionService

    private val userId = UUID.randomUUID()
    private val topicId = UUID.randomUUID()
    private val topic = Topic(title = "Morning Routine").apply {
        ReflectionTestUtils.setField(this, "id", topicId)
    }

    @BeforeEach
    fun setup() {
        service = PracticeSubmissionService(
            submissionRepository, gradeRepository, topicRepository,
            userRepository, storageService, mediaUrlService
        )
        ReflectionTestUtils.setField(service, "entityManager", entityManager)
        every { mediaUrlService.normalize(any()) } answers { firstArg() }
        // Дефолт: дублей Practice-отправки нет (backend gate DUPLICATE_SUBMISSION, memory.md №79)
        every { submissionRepository.findFirstByUserIdAndTopicId(any(), any()) } returns null
    }

    private fun audioFile(size: Long = 1024): MultipartFile = mockk {
        every { this@mockk.size } returns size
        every { originalFilename } returns "rec.m4a"
        every { contentType } returns "audio/m4a"
    }

    // 1. createSubmission — успех
    @Test
    fun `createSubmission success - saves with status NEW and storage url`() {
        every { topicRepository.findByIdAndIsPublishedTrueAndDeletedAtIsNull(topicId) } returns Optional.of(topic)
        every { storageService.uploadFile(any(), "speaking/submissions/u_$userId") } returns
            "https://media.example.com/sotospeak/speaking/submissions/u_$userId/abc.m4a"
        every { userRepository.getReferenceById(userId) } returns
            User(email = "student@test.com", displayName = "Student")
        every { submissionRepository.saveAndFlush(any()) } answers { firstArg() }

        val result = service.createSubmission(userId, topicId, 30, audioFile())

        assertEquals("NEW", result.status)
        assertEquals("https://media.example.com/sotospeak/speaking/submissions/u_$userId/abc.m4a", result.audioUrl)
        assertEquals(30, result.durationSec)
        verify(exactly = 1) { submissionRepository.saveAndFlush(match { it.status == SubmissionStatus.NEW }) }
    }

    // 1b. createSubmission — race двух параллельных POST: UNIQUE (user_id, topic_id) → 409 fallback
    @Test
    fun `createSubmission - unique constraint violation on insert throws DuplicateSubmissionException`() {
        every { topicRepository.findByIdAndIsPublishedTrueAndDeletedAtIsNull(topicId) } returns Optional.of(topic)
        every { storageService.uploadFile(any(), "speaking/submissions/u_$userId") } returns
            "https://media.example.com/sotospeak/speaking/submissions/u_$userId/abc.m4a"
        every { userRepository.getReferenceById(userId) } returns
            User(email = "student@test.com", displayName = "Student")
        every { submissionRepository.saveAndFlush(any()) } throws
            DataIntegrityViolationException("duplicate key value violates unique constraint uq_practice_submissions_user_topic")

        assertThrows<com.sotospeak.exception.DuplicateSubmissionException> {
            service.createSubmission(userId, topicId, 30, audioFile())
        }
    }

    // 2. createSubmission — топик не опубликован/удалён → 404
    @Test
    fun `createSubmission - topic not published or deleted throws NoSuchElementException`() {
        every { topicRepository.findByIdAndIsPublishedTrueAndDeletedAtIsNull(topicId) } returns Optional.empty()

        assertThrows<NoSuchElementException> {
            service.createSubmission(userId, topicId, 30, audioFile())
        }
        verify(exactly = 0) { storageService.uploadFile(any(), any()) }
    }

    // 3. createSubmission — durationSec вне 1..60 → 400
    @Test
    fun `createSubmission - invalid duration throws IllegalArgumentException`() {
        every { topicRepository.findByIdAndIsPublishedTrueAndDeletedAtIsNull(topicId) } returns Optional.of(topic)

        assertThrows<IllegalArgumentException> { service.createSubmission(userId, topicId, 0, audioFile()) }
        assertThrows<IllegalArgumentException> { service.createSubmission(userId, topicId, 61, audioFile()) }
        verify(exactly = 0) { storageService.uploadFile(any(), any()) }
    }

    // 4. createSubmission — файл > 5 МБ → 400
    @Test
    fun `createSubmission - file larger than 5MB throws IllegalArgumentException`() {
        every { topicRepository.findByIdAndIsPublishedTrueAndDeletedAtIsNull(topicId) } returns Optional.of(topic)

        assertThrows<IllegalArgumentException> {
            service.createSubmission(userId, topicId, 30, audioFile(size = 5 * 1024 * 1024 + 1))
        }
        verify(exactly = 0) { storageService.uploadFile(any(), any()) }
    }

    private fun submissionWithGrade(grade: Grade?): PracticeSubmission {
        val submission = PracticeSubmission(
            audioUrl = "https://media.example.com/a.m4a",
            durationSec = 30,
            status = if (grade == null) SubmissionStatus.NEW else SubmissionStatus.REVIEWED
        )
        ReflectionTestUtils.setField(submission, "id", UUID.randomUUID())
        submission.grade = grade
        grade?.let { ReflectionTestUtils.setField(it, "submission", submission) }
        return submission
    }

    private fun gradeRequest() = GradeSubmissionRequest(
        grammar = 7, vocabulary = 8, pronunciation = 6, fluency = 7, comment = "Good"
    )

    // 5. gradeSubmission — успех: grade создан, статус NEW→REVIEWED, reviewer проставлен
    @Test
    fun `gradeSubmission success - grade created, status NEW to REVIEWED, reviewer set`() {
        val submission = submissionWithGrade(null)
        val reviewer = User(email = "teacher@test.com", displayName = "Teacher Anna")
        every { submissionRepository.findByIdWithDetails(submission.id!!) } returns Optional.of(submission)
        every { userRepository.getReferenceById(any()) } returns reviewer
        every { gradeRepository.saveAndFlush(any()) } answers { firstArg() }
        every { submissionRepository.save(any()) } answers { firstArg() }

        service.gradeSubmission(submission.id!!, gradeRequest(), reviewerId = UUID.randomUUID())

        assertEquals(SubmissionStatus.REVIEWED, submission.status)
        assertEquals(7, submission.grade?.grammar)
        assertEquals(reviewer, submission.grade?.reviewer)
        verify(exactly = 1) { gradeRepository.saveAndFlush(any()) }
        verify(exactly = 1) { submissionRepository.save(submission) }
    }

    // 6. gradeSubmission — повторный POST на оценённый → 400
    @Test
    fun `gradeSubmission - already graded throws IllegalArgumentException`() {
        val submission = submissionWithGrade(mockk<Grade>())
        every { submissionRepository.findByIdWithDetails(submission.id!!) } returns Optional.of(submission)

        assertThrows<IllegalArgumentException> {
            service.gradeSubmission(submission.id!!, gradeRequest(), reviewerId = UUID.randomUUID())
        }
    }

    // 7. editGrade — успех: поля обновлены, статус остался REVIEWED
    @Test
    fun `editGrade success - fields updated, status untouched`() {
        val existingGrade = Grade(
            grammar = 5, vocabulary = 5, pronunciation = 5, fluency = 5, comment = null
        )
        val submission = submissionWithGrade(existingGrade)
        every { gradeRepository.findBySubmissionId(submission.id!!) } returns Optional.of(existingGrade)
        every { userRepository.getReferenceById(any()) } returns
            User(email = "teacher@test.com", displayName = "Teacher Anna")
        every { gradeRepository.saveAndFlush(any()) } answers { firstArg() }

        service.editGrade(submission.id!!, gradeRequest(), reviewerId = UUID.randomUUID())

        assertEquals(7, existingGrade.grammar)
        assertEquals(8, existingGrade.vocabulary)
        assertEquals("Good", existingGrade.comment)
        assertEquals(SubmissionStatus.REVIEWED, submission.status) // статус не трогаем
    }

    // 8. editGrade — оценки нет → 404
    @Test
    fun `editGrade - grade not found throws NoSuchElementException`() {
        val submissionId = UUID.randomUUID()
        every { gradeRepository.findBySubmissionId(submissionId) } returns Optional.empty()

        assertThrows<NoSuchElementException> {
            service.editGrade(submissionId, gradeRequest(), reviewerId = UUID.randomUUID())
        }
    }
}
