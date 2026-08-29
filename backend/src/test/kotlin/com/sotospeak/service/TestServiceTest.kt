package com.sotospeak.service

import com.sotospeak.entity.Category
import com.sotospeak.entity.Test as TestEntity
import com.sotospeak.repository.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class TestServiceTest {

    private lateinit var testRepository: TestRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var questionRepository: QuestionRepository
    private lateinit var answerRepository: AnswerRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var mediaUrlService: MediaUrlService
    private lateinit var iwQuestionRepository: ImageWordMatchQuestionRepository
    private lateinit var iwWordRepository: ImageWordMatchWordRepository
    private lateinit var iwHotspotRepository: ImageWordMatchHotspotRepository

    private lateinit var testService: TestService

    private val categoryId = UUID.randomUUID()
    private val testId = UUID.randomUUID()

    private val category = Category(
        id = categoryId,
        name = "Test Category",
        displayOrder = 1
    )

    @BeforeEach
    fun setup() {
        testRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        questionRepository = mockk(relaxed = true)
        answerRepository = mockk(relaxed = true)
        progressRepository = mockk(relaxed = true)
        mediaUrlService = mockk(relaxed = true)
        iwQuestionRepository = mockk(relaxed = true)
        iwWordRepository = mockk(relaxed = true)
        iwHotspotRepository = mockk(relaxed = true)

        testService = TestService(
            testRepository = testRepository,
            categoryRepository = categoryRepository,
            questionRepository = questionRepository,
            answerRepository = answerRepository,
            progressRepository = progressRepository,
            mediaUrlService = mediaUrlService,
            iwQuestionRepository = iwQuestionRepository,
            iwWordRepository = iwWordRepository,
            iwHotspotRepository = iwHotspotRepository
        )
    }

    @Test
    fun `getTestById throws NoSuchElementException for unpublished draft`() {
        val draft = TestEntity(
            id = testId,
            category = category,
            title = "Draft Quiz",
            isPublished = false
        )
        every { testRepository.findByIdWithQuestions(testId) } returns draft

        assertThrows<NoSuchElementException> {
            testService.getTestById(testId.toString())
        }
    }

    @Test
    fun `getTestById returns details for published test`() {
        val published = TestEntity(
            id = testId,
            category = category,
            title = "Published Quiz",
            isPublished = true
        )
        every { testRepository.findByIdWithQuestions(testId) } returns published

        val result = testService.getTestById(testId.toString())

        assertEquals(testId.toString(), result.id)
        assertEquals("Published Quiz", result.title)
    }

    @Test
    fun `getTestById throws NoSuchElementException for missing test`() {
        every { testRepository.findByIdWithQuestions(testId) } returns null

        assertThrows<NoSuchElementException> {
            testService.getTestById(testId.toString())
        }
    }
}
