package com.funnyenglish.shared

import com.funnyenglish.shared.model.Question
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for game logic and scoring system
 */
class GameLogicTest : FunSpec({

    context("Score Calculation") {
        
        test("calculateStars returns 3 stars for 100% score") {
            val result = calculateStars(score = 30, maxScore = 30)
            result shouldBe 3
        }
        
        test("calculateStars returns 2 stars for 66-99% score") {
            calculateStars(score = 20, maxScore = 30) shouldBe 2
            calculateStars(score = 29, maxScore = 30) shouldBe 2
        }
        
        test("calculateStars returns 1 star for 33-65% score") {
            calculateStars(score = 15, maxScore = 30) shouldBe 1
            calculateStars(score = 10, maxScore = 30) shouldBe 1
        }
        
        test("calculateStars returns 0 stars for score below 33%") {
            calculateStars(score = 9, maxScore = 30) shouldBe 0
            calculateStars(score = 0, maxScore = 30) shouldBe 0
        }
        
        test("calculateStars handles edge cases") {
            calculateStars(score = 0, maxScore = 0) shouldBe 0
        }
    }

    context("Points Calculation") {
        
        test("calculatePointsEarned gives full reward for new best score") {
            val result = calculatePointsEarned(
                stars = 3,
                testPointsReward = 10,
                isNewBestScore = true
            )
            // 10 (base) + 3*5 (bonus) = 25
            result shouldBe 25
        }
        
        test("calculatePointsEarned gives reduced reward for retake") {
            val result = calculatePointsEarned(
                stars = 3,
                testPointsReward = 10,
                isNewBestScore = false
            )
            // Only star bonus: 3*2 = 6
            result shouldBe 6
        }
        
        // Parameterized test for points calculation
        listOf(
            Triple(0, true, 0),   // 0 stars, new best = 0 points
            Triple(1, true, 15),  // 10 + 5 = 15
            Triple(2, true, 20),  // 10 + 10 = 20
            Triple(3, true, 25),  // 10 + 15 = 25
            Triple(0, false, 0),  // 0 stars, retake = 0 points
            Triple(1, false, 2),  // 1*2 = 2
            Triple(2, false, 4),  // 2*2 = 4
            Triple(3, false, 6)   // 3*2 = 6
        ).forEach { (stars, isNewBest, expected) ->
            test("calculatePointsEarned: stars=$stars, newBest=$isNewBest") {
                calculatePointsEarned(stars, 10, isNewBest) shouldBe expected
            }
        }
    }

    context("Test Validation") {
        
        test("isTestCompletable returns true when all questions answered") {
            val answers = mapOf(
                "q1" to listOf("a1"),
                "q2" to listOf("a2"),
                "q3" to listOf("a3")
            )
            val questionIds = listOf("q1", "q2", "q3")
            
            isTestCompletable(answers, questionIds) shouldBe true
        }
        
        test("isTestCompletable returns false when questions unanswered") {
            val answers = mapOf(
                "q1" to listOf("a1")
            )
            val questionIds = listOf("q1", "q2") // q2 unanswered
            
            isTestCompletable(answers, questionIds) shouldBe false
        }
        
        test("isTestCompletable returns false when answer is empty") {
            val answers = mapOf(
                "q1" to emptyList<String>()
            )
            val questionIds = listOf("q1")
            
            isTestCompletable(answers, questionIds) shouldBe false
        }
    }

    context("Progress Tracking") {
        
        test("calculateProgressPercentage returns correct value") {
            calculateProgressPercentage(
                completedTests = 5,
                totalTests = 20
            ) shouldBe 25
        }
        
        test("calculateProgressPercentage returns 0 for empty state") {
            calculateProgressPercentage(0, 0) shouldBe 0
        }
        
        test("shouldShowReviewSuggestion returns true after mistakes") {
            shouldShowReviewSuggestion(
                wrongAnswers = 3,
                totalQuestions = 10
            ) shouldBe true
        }
        
        test("shouldShowReviewSuggestion returns false for perfect score") {
            shouldShowReviewSuggestion(0, 10) shouldBe false
        }
    }
})

// ==================== Functions to test ====================

private fun calculateStars(score: Int, maxScore: Int): Int {
    if (maxScore == 0) return 0
    val percentage = (score * 100) / maxScore
    
    return when {
        percentage >= 66 -> 3
        percentage >= 33 -> 2
        percentage > 0 -> 1
        else -> 0
    }
}

private fun calculatePointsEarned(
    stars: Int,
    testPointsReward: Int,
    isNewBestScore: Boolean
): Int {
    return if (isNewBestScore) {
        testPointsReward + (stars * 5)
    } else {
        stars * 2
    }
}

private fun isTestCompletable(
    answers: Map<String, List<String>>,
    questionIds: List<String>
): Boolean {
    return questionIds.all { id ->
        val answer = answers[id]
        answer != null && answer.isNotEmpty()
    }
}

private fun calculateProgressPercentage(
    completedTests: Int,
    totalTests: Int
): Int {
    if (totalTests == 0) return 0
    return (completedTests * 100) / totalTests
}

private fun shouldShowReviewSuggestion(
    wrongAnswers: Int,
    totalQuestions: Int
): Boolean {
    return wrongAnswers > 0
}
