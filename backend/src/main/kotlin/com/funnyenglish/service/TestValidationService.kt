package com.funnyenglish.service

import com.funnyenglish.dto.SubmitAnswerRequest
import com.funnyenglish.entity.QuestionType
import com.funnyenglish.repository.ImageWordMatchHotspotRepository
import com.funnyenglish.repository.ImageWordMatchWordRepository
import com.funnyenglish.repository.QuestionRepository
import com.funnyenglish.repository.TestRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TestValidationService(
    private val testRepository: TestRepository,
    private val questionRepository: QuestionRepository,
    private val iwHotspotRepository: ImageWordMatchHotspotRepository,
    private val iwWordRepository: ImageWordMatchWordRepository
) {
    private val logger = LoggerFactory.getLogger(TestValidationService::class.java)

    data class ValidationResult(
        val score: Int,
        val maxScore: Int,
        val percentage: Int,
        val stars: Int
    )

    fun validateTest(testId: UUID, answers: List<SubmitAnswerRequest>): ValidationResult {
        val test = testRepository.findByIdWithQuestions(testId)
            ?: throw NoSuchElementException("Test not found")

        val questions = questionRepository.findByTestIdWithAnswers(testId)

        var score = 0
        var maxScore = 0

        for (question in questions) {
            maxScore += question.points
            val submittedAnswer = answers.find { it.questionId == question.id.toString() }

            logger.debug("Question ${question.id}: type=${question.type}, submittedAnswer=${submittedAnswer != null}")

            if (submittedAnswer != null) {
                val isCorrect = when (question.type) {
                    QuestionType.DRAG_DROP_IMAGE -> {
                        val matches = submittedAnswer.dragDropMatches ?: emptyMap()
                        val correctAnswers = question.answers.filter { it.isCorrect }
                        correctAnswers.all { answer ->
                            matches[answer.id.toString()] == answer.matchTarget
                        }
                    }
                    QuestionType.IMAGE_WORD_MATCH -> {
                        val submittedMatches = submittedAnswer.imageWordMatches ?: emptyMap()
                        val hotspots = iwHotspotRepository.findByQuestionId(question.id!!)
                        val correctMapping = hotspots.associate { it.wordId to it.hotspotId }
                        val words = iwWordRepository.findByQuestionId(question.id)
                        words.all { word ->
                            submittedMatches[word.wordId] == correctMapping[word.wordId]
                        }
                    }
                    else -> {
                        val correctAnswerIds = question.answers
                            .filter { it.isCorrect }
                            .map { it.id.toString() }
                            .toSet()
                        submittedAnswer.selectedAnswerIds.toSet() == correctAnswerIds
                    }
                }

                logger.debug("Question ${question.id}: isCorrect=$isCorrect")

                if (isCorrect) {
                    score += question.points
                }
            } else {
                logger.debug("Question ${question.id}: no submitted answer")
            }
        }

        logger.info("Test validation result: score=$score, maxScore=$maxScore")

        val percentage = if (maxScore > 0) (score * 100) / maxScore else 0
        val stars = when {
            percentage >= 95 -> 3
            percentage >= 80 -> 2
            percentage >= 60 -> 1
            else -> 0
        }

        return ValidationResult(score, maxScore, percentage, stars)
    }
}
