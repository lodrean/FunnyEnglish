package com.funnyenglish.service

import com.funnyenglish.dto.*
import com.funnyenglish.entity.*
import com.funnyenglish.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class TestService(
    private val testRepository: TestRepository,
    private val categoryRepository: CategoryRepository,
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository,
    private val progressRepository: ProgressRepository
) {
    fun getCategories(userId: String?): List<CategoryResponse> {
        val categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrder()

        return categories.map { category ->
            val completedCount: Int
            val totalStars: Int

            if (userId != null) {
                val userUUID = UUID.fromString(userId)
                val progress = progressRepository.findByUserIdAndCategoryId(userUUID, category.id)
                completedCount = progress.size
                totalStars = progress.sumOf { it.stars }
            } else {
                completedCount = 0
                totalStars = 0
            }

            category.toResponse(completedCount, totalStars)
        }
    }

    fun getTestsByCategory(categoryId: String, userId: String?): List<TestListResponse> {
        val tests = testRepository.findByCategoryIdAndIsPublishedTrueOrderByDisplayOrder(UUID.fromString(categoryId))

        val progressMap = if (userId != null) {
            val userUUID = UUID.fromString(userId)
            progressRepository.findByUserId(userUUID)
                .associateBy { it.test.id }
        } else {
            emptyMap()
        }

        return tests.map { test ->
            test.toListResponse(progressMap[test.id])
        }
    }

    fun getAllTests(userId: String?): List<TestListResponse> {
        val tests = testRepository.findByIsPublishedTrueOrderByDisplayOrder()

        val progressMap = if (userId != null) {
            val userUUID = UUID.fromString(userId)
            progressRepository.findByUserId(userUUID)
                .associateBy { it.test.id }
        } else {
            emptyMap()
        }

        return tests.map { test ->
            test.toListResponse(progressMap[test.id])
        }
    }

    fun getTestById(testId: String): TestDetailResponse {
        val test = testRepository.findByIdWithQuestions(UUID.fromString(testId))
            ?: throw NoSuchElementException("Test not found")

        // Eagerly load answers
        test.questions.forEach { question ->
            question.answers.size // trigger lazy load
        }

        return test.toDetailResponse()
    }

    // Admin methods
    fun getTestByIdForAdmin(testId: String): AdminTestDetailResponse {
        val test = testRepository.findByIdWithQuestions(UUID.fromString(testId))
            ?: throw NoSuchElementException("Test not found")

        test.questions.forEach { question ->
            question.answers.size
        }

        return test.toAdminResponse()
    }

    fun getAllTestsForAdmin(): List<AdminTestDetailResponse> {
        return testRepository.findAll().map { test ->
            test.questions.forEach { q -> q.answers.size }
            test.toAdminResponse()
        }
    }

    @Transactional
    fun createTest(request: CreateTestRequest): AdminTestDetailResponse {
        val category = categoryRepository.findById(UUID.fromString(request.categoryId))
            .orElseThrow { NoSuchElementException("Category not found") }

        val test = Test(
            category = category,
            title = request.title,
            description = request.description,
            thumbnailUrl = request.thumbnailUrl,
            difficulty = Difficulty.valueOf(request.difficulty.uppercase()),
            pointsReward = request.pointsReward,
            timeLimitSeconds = request.timeLimitSeconds,
            isPublished = request.isPublished,
            displayOrder = request.displayOrder
        )

        val savedTest = testRepository.save(test)

        request.questions.forEachIndexed { qIndex, qRequest ->
            val question = Question(
                test = savedTest,
                type = QuestionType.valueOf(qRequest.type.uppercase()),
                text = qRequest.text,
                audioUrl = qRequest.audioUrl,
                imageUrl = qRequest.imageUrl,
                displayOrder = qRequest.displayOrder.takeIf { it > 0 } ?: qIndex,
                points = qRequest.points
            )

            val savedQuestion = questionRepository.save(question)

            qRequest.answers.forEachIndexed { aIndex, aRequest ->
                val answer = Answer(
                    question = savedQuestion,
                    text = aRequest.text,
                    imageUrl = aRequest.imageUrl,
                    audioUrl = aRequest.audioUrl,
                    isCorrect = aRequest.isCorrect,
                    displayOrder = aRequest.displayOrder.takeIf { it > 0 } ?: aIndex,
                    matchTarget = aRequest.matchTarget
                )
                answerRepository.save(answer)
            }
        }

        return getTestByIdForAdmin(savedTest.id.toString())
    }

    @Transactional
    fun updateTest(testId: String, request: UpdateTestRequest): AdminTestDetailResponse {
        val test = testRepository.findById(UUID.fromString(testId))
            .orElseThrow { NoSuchElementException("Test not found") }

        val category = request.categoryId?.let {
            categoryRepository.findById(UUID.fromString(it))
                .orElseThrow { NoSuchElementException("Category not found") }
        } ?: test.category

        val updatedTest = test.copy(
            category = category,
            title = request.title ?: test.title,
            description = request.description ?: test.description,
            thumbnailUrl = request.thumbnailUrl ?: test.thumbnailUrl,
            difficulty = request.difficulty?.let { Difficulty.valueOf(it.uppercase()) } ?: test.difficulty,
            pointsReward = request.pointsReward ?: test.pointsReward,
            timeLimitSeconds = request.timeLimitSeconds ?: test.timeLimitSeconds,
            isPublished = request.isPublished ?: test.isPublished,
            displayOrder = request.displayOrder ?: test.displayOrder,
            updatedAt = Instant.now()
        )

        testRepository.save(updatedTest)

        // Update questions if provided
        if (request.questions != null) {
            // Delete existing questions (cascade will delete answers)
            questionRepository.deleteByTestId(test.id)

            request.questions.forEachIndexed { qIndex, qRequest ->
                val question = Question(
                    test = updatedTest,
                    type = QuestionType.valueOf(qRequest.type.uppercase()),
                    text = qRequest.text,
                    audioUrl = qRequest.audioUrl,
                    imageUrl = qRequest.imageUrl,
                    displayOrder = qRequest.displayOrder.takeIf { it > 0 } ?: qIndex,
                    points = qRequest.points
                )

                val savedQuestion = questionRepository.save(question)

                qRequest.answers.forEachIndexed { aIndex, aRequest ->
                    val answer = Answer(
                        question = savedQuestion,
                        text = aRequest.text,
                        imageUrl = aRequest.imageUrl,
                        audioUrl = aRequest.audioUrl,
                        isCorrect = aRequest.isCorrect,
                        displayOrder = aRequest.displayOrder.takeIf { it > 0 } ?: aIndex,
                        matchTarget = aRequest.matchTarget
                    )
                    answerRepository.save(answer)
                }
            }
        }

        return getTestByIdForAdmin(testId)
    }

    @Transactional
    fun deleteTest(testId: String) {
        val test = testRepository.findById(UUID.fromString(testId))
            .orElseThrow { NoSuchElementException("Test not found") }
        testRepository.delete(test)
    }
}
