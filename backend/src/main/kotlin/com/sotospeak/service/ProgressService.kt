package com.sotospeak.service

import com.sotospeak.dto.*
import com.sotospeak.entity.Progress
import com.sotospeak.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ProgressService(
    private val progressRepository: ProgressRepository,
    private val testRepository: TestRepository,
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository,
    private val userService: UserService,
    private val achievementService: AchievementService,
    private val testValidationService: TestValidationService
) {
    private val logger = LoggerFactory.getLogger(ProgressService::class.java)
    
    @Transactional
    fun submitTest(userId: String, request: SubmitTestRequest): SubmitTestResponse {
        val userUUID = UUID.fromString(userId)
        val testUUID = UUID.fromString(request.testId)

        logger.info("Submit test: testId=$testUUID, userId=$userUUID, answersCount=${request.answers.size}")
        request.answers.forEach { answer ->
            logger.info("Answer: questionId=${answer.questionId}, selectedIds=${answer.selectedAnswerIds}")
        }

        val test = testRepository.findByIdWithQuestions(testUUID)
            ?: throw NoSuchElementException("Test not found")

        val validation = testValidationService.validateTest(testUUID, request.answers)
        val score = validation.score
        val maxScore = validation.maxScore
        val percentage = validation.percentage
        val stars = validation.stars

        // Get or create progress
        val existingProgress = progressRepository.findByUserIdAndTestId(userUUID, testUUID)
        val isNewBestScore = existingProgress == null || score > existingProgress.bestScore

        val progress = if (existingProgress != null) {
            existingProgress.copy(
                score = score,
                maxScore = maxScore,
                stars = maxOf(existingProgress.stars, stars),
                attemptsCount = existingProgress.attemptsCount + 1,
                bestScore = maxOf(existingProgress.bestScore, score),
                timeSpentSeconds = request.timeSpentSeconds,
                lastAttemptAt = Instant.now()
            )
        } else {
            Progress(
                user = userService.getUserById(userId),
                test = test,
                score = score,
                maxScore = maxScore,
                stars = stars,
                bestScore = score,
                timeSpentSeconds = request.timeSpentSeconds
            )
        }

        progressRepository.save(progress)

        // Update user streak
        userService.updateStreak(userId)

        // Calculate points earned
        val pointsEarned = if (isNewBestScore) {
            test.pointsReward + (stars * 5)
        } else {
            stars * 2 // Smaller reward for retakes
        }

        // Add points to user
        val (_, levelUp) = userService.addPoints(userId, pointsEarned)

        // Check achievements
        val newAchievements = achievementService.checkAndAwardAchievements(userId, percentage, stars)

        return SubmitTestResponse(
            score = score,
            maxScore = maxScore,
            percentage = percentage,
            stars = stars,
            pointsEarned = pointsEarned,
            isNewBestScore = isNewBestScore,
            newAchievements = newAchievements,
            levelUp = levelUp
        )
    }

    @Transactional(readOnly = true)
    fun getUserProgress(userId: String): List<ProgressResponse> {
        val userUUID = UUID.fromString(userId)
        val progressList = progressRepository.findByUserId(userUUID)

        return progressList.map { progress ->
            progress.toResponse(progress.test.title)
        }
    }

    @Transactional(readOnly = true)
    fun getUserProgressSummary(userId: String): UserProgressSummary {
        val userUUID = UUID.fromString(userId)
        val progressList = progressRepository.findByUserId(userUUID)

        val allTests = testRepository.findByIsPublishedTrueOrderByDisplayOrder()
        val totalTests = allTests.size
        val completedTests = progressList.size
        val totalStars = progressList.sumOf { it.stars }
        val maxPossibleStars = totalTests * 3

        // Group by category
        val progressByCategory = progressList.groupBy { it.test.category.id }
        val testsByCategory = allTests.groupBy { it.category.id }

        val categoriesProgress = testsByCategory.map { (categoryId, tests) ->
            val categoryProgress = progressByCategory[categoryId] ?: emptyList()
            CategoryProgressResponse(
                categoryId = categoryId.toString(),
                categoryName = tests.first().category.name,
                testsCount = tests.size,
                completedCount = categoryProgress.size,
                totalStars = categoryProgress.sumOf { it.stars },
                maxStars = tests.size * 3
            )
        }

        return UserProgressSummary(
            totalTests = totalTests,
            completedTests = completedTests,
            totalStars = totalStars,
            maxPossibleStars = maxPossibleStars,
            categoriesProgress = categoriesProgress
        )
    }
}
