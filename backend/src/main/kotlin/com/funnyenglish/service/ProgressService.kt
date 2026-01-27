package com.funnyenglish.service

import com.funnyenglish.dto.*
import com.funnyenglish.entity.Progress
import com.funnyenglish.entity.QuestionType
import com.funnyenglish.repository.*
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
    private val achievementService: AchievementService
) {
    @Transactional
    fun submitTest(userId: String, request: SubmitTestRequest): SubmitTestResponse {
        val userUUID = UUID.fromString(userId)
        val testUUID = UUID.fromString(request.testId)

        val test = testRepository.findByIdWithQuestions(testUUID)
            ?: throw NoSuchElementException("Test not found")

        // Load all questions with answers
        val questions = questionRepository.findByTestIdWithAnswers(testUUID)

        // Calculate score
        var score = 0
        var maxScore = 0

        for (question in questions) {
            maxScore += question.points
            val submittedAnswer = request.answers.find { it.questionId == question.id.toString() }

            if (submittedAnswer != null) {
                val isCorrect = when (question.type) {
                    QuestionType.DRAG_DROP_IMAGE -> {
                        // Check drag-drop matches
                        val matches = submittedAnswer.dragDropMatches ?: emptyMap()
                        val correctAnswers = question.answers.filter { it.isCorrect }
                        correctAnswers.all { answer ->
                            matches[answer.id.toString()] == answer.matchTarget
                        }
                    }
                    else -> {
                        // Check selected answers
                        val correctAnswerIds = question.answers
                            .filter { it.isCorrect }
                            .map { it.id.toString() }
                            .toSet()
                        submittedAnswer.selectedAnswerIds.toSet() == correctAnswerIds
                    }
                }

                if (isCorrect) {
                    score += question.points
                }
            }
        }

        // Calculate stars
        val percentage = if (maxScore > 0) (score * 100) / maxScore else 0
        val stars = when {
            percentage >= 95 -> 3
            percentage >= 80 -> 2
            percentage >= 60 -> 1
            else -> 0
        }

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

    fun getUserProgress(userId: String): List<ProgressResponse> {
        val userUUID = UUID.fromString(userId)
        val progressList = progressRepository.findByUserId(userUUID)

        return progressList.map { progress ->
            progress.toResponse(progress.test.title)
        }
    }

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
