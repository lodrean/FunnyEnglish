package com.sotospeak.service

import com.sotospeak.dto.*
import com.sotospeak.entity.*
import com.sotospeak.repository.*
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
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
    private val progressRepository: ProgressRepository,
    private val mediaUrlService: MediaUrlService,
    private val iwQuestionRepository: ImageWordMatchQuestionRepository,
    private val iwWordRepository: ImageWordMatchWordRepository,
    private val iwHotspotRepository: ImageWordMatchHotspotRepository
) {
    @Cacheable(value = ["categories"], key = "#userId ?: 'anonymous'")
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

            category.toResponse(completedCount, totalStars, mediaUrlService::normalize)
        }
    }

    @Cacheable(value = ["tests"], key = "#categoryId + '-' + (#userId ?: 'anonymous')")
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
            test.toListResponse(progressMap[test.id], mediaUrlService::normalize)
        }
    }

    @Cacheable(value = ["tests"], key = "'all-' + (#userId ?: 'anonymous')")
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
            test.toListResponse(progressMap[test.id], mediaUrlService::normalize)
        }
    }

    @Cacheable(value = ["testDetails"], key = "#testId")
    fun getTestById(testId: String): TestDetailResponse {
        val test = testRepository.findByIdWithQuestions(UUID.fromString(testId))
            ?: throw NoSuchElementException("Test not found")

        // Eagerly load answers
        test.questions.forEach { question ->
            question.answers.size // trigger lazy load
        }

        // Load IMAGE_WORD_MATCH content for relevant questions
        val imageWordMatchContents = test.questions
            .filter { it.type == QuestionType.IMAGE_WORD_MATCH }
            .mapNotNull { question ->
                val iwData = iwQuestionRepository.findByQuestionId(question.id!!)
                if (iwData != null) {
                    val words = iwWordRepository.findByQuestionId(question.id)
                        .sortedBy { it.displayOrder }
                        .map { WordResponse(it.wordId, it.text, it.translation, it.audioUrl) }
                    val hotspots = iwHotspotRepository.findByQuestionId(question.id)
                        .map { HotspotWithoutWordResponse(it.hotspotId, it.x, it.y, it.width, it.height, it.shape) }
                    question.id to ImageWordMatchPublicResponse(
                        id = question.id.toString(),
                        type = QuestionType.IMAGE_WORD_MATCH,
                        instruction = iwData.instruction,
                        points = iwData.points,
                        imageUrl = mediaUrlService.normalize(iwData.imageUrl) ?: iwData.imageUrl,
                        words = words,
                        hotspots = hotspots
                    )
                } else null
            }
            .toMap()

        return TestDetailResponse(
            id = test.id.toString(),
            categoryId = test.category.id.toString(),
            title = test.title,
            description = test.description,
            thumbnailUrl = mediaUrlService.normalize(test.thumbnailUrl),
            difficulty = test.difficulty.name,
            pointsReward = test.pointsReward,
            timeLimitSeconds = test.timeLimitSeconds,
            questions = test.questions.map { question ->
                val iwContent = imageWordMatchContents[question.id]
                question.toResponse(mediaUrlService::normalize, iwContent)
            }
        )
    }

    // Admin methods
    fun getTestByIdForAdmin(testId: String): AdminTestDetailResponse {
        val test = testRepository.findByIdWithQuestions(UUID.fromString(testId))
            ?: throw NoSuchElementException("Test not found")

        test.questions.forEach { question ->
            question.answers.size
        }

        return test.toAdminResponse(mediaUrlService::normalize)
    }

    @Transactional(readOnly = true)
    fun getAllTestsForAdmin(): List<AdminTestDetailResponse> {
        // TODO: Fix JSONB deserialization before loading questions
        val tests = testRepository.findAll()
        return tests.map { it.toAdminResponse(mediaUrlService::normalize) }
    }

    @Transactional
    @CacheEvict(value = ["tests", "testDetails"], allEntries = true)
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
                title = qRequest.text ?: "Вопрос ${qIndex + 1}",
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
    @CacheEvict(value = ["tests", "testDetails"], key = "#testId")
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
                    title = qRequest.text ?: "Вопрос ${qIndex + 1}",
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
        val uuid = UUID.fromString(testId)
        // Delete questions first (workaround for JSONB deserialization issue)
        questionRepository.deleteByTestId(uuid)
        // Delete test without loading it (to avoid JSONB deserialization)
        testRepository.deleteById(uuid)
    }
}
