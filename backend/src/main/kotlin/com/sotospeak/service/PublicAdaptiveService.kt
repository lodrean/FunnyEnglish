package com.sotospeak.service

import com.sotospeak.entity.QuestionType
import com.sotospeak.repository.*
import com.sotospeak.shared.model.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PublicAdaptiveService(
    private val testRepository: TestRepository,
    private val questionRepository: QuestionRepository,
    private val imageWordMatchQuestionRepository: ImageWordMatchQuestionRepository,
    private val imageWordMatchWordRepository: ImageWordMatchWordRepository,
    private val imageWordMatchHotspotRepository: ImageWordMatchHotspotRepository
) {

    @Transactional(readOnly = true)
    fun createRandomLesson(categoryId: String?, durationMinutes: Int): AdaptiveLessonState {
        val tests = if (categoryId != null) {
            testRepository.findByCategoryIdAndIsPublishedTrueOrderByDisplayOrder(UUID.fromString(categoryId))
        } else {
            testRepository.findByIsPublishedTrueOrderByDisplayOrder()
        }

        val selectedTests = if (tests.size <= 2) tests else tests.shuffled().take(2)

        val allQuestions = selectedTests.flatMap { test ->
            questionRepository.findByTestIdWithAnswers(test.id!!)
                .filter { it.isPublished }
                .shuffled()
                .take(6)
        }.shuffled()

        val numSegments = when (durationMinutes) {
            5 -> 3
            7 -> 4
            10 -> 5
            else -> 3
        }

        val questionsPerSegment = maxOf(1, allQuestions.size / numSegments)
        val segments = (0 until numSegments).map { segmentIndex ->
            val segmentQuestions = allQuestions
                .drop(segmentIndex * questionsPerSegment)
                .take(questionsPerSegment)
                .map { it.toSharedQuestion() }

            MicroLessonSegment(
                id = UUID.randomUUID().toString(),
                type = when (segmentIndex) {
                    0 -> SegmentType.INTRO
                    1 -> SegmentType.PRACTICE
                    2 -> SegmentType.CHALLENGE
                    else -> SegmentType.REVIEW
                },
                questions = segmentQuestions,
                estimatedDurationSeconds = 90,
                learningObjective = "Practice general skills",
                grammarHint = null
            )
        }.filter { it.questions.isNotEmpty() }

        val effectiveSegments = if (segments.isEmpty()) {
            listOf(
                MicroLessonSegment(
                    id = UUID.randomUUID().toString(),
                    type = SegmentType.PRACTICE,
                    questions = emptyList(),
                    estimatedDurationSeconds = 90,
                    learningObjective = "No questions available"
                )
            )
        } else segments

        return AdaptiveLessonState(
            lessonId = "guest_lesson_${UUID.randomUUID()}",
            currentSegmentIndex = 0,
            totalSegments = effectiveSegments.size,
            currentDifficulty = DifficultyLevel.BEGINNER,
            segments = effectiveSegments,
            timeSpentSeconds = 0,
            requiresBreak = false,
            weakAreas = emptyList(),
            performanceHistory = emptyList()
        )
    }

    @Transactional(readOnly = true)
    fun validateAnswer(questionId: String, answerId: String): FeedbackResponse {
        val question = questionRepository.findById(UUID.fromString(questionId))
            .orElseThrow { NoSuchElementException("Question not found") }

        val isCorrect = when (question.type) {
            QuestionType.IMAGE_WORD_MATCH -> {
                // For IMAGE_WORD_MATCH in single-answer validation, we cannot determine correctness
                // without the full match mapping. Treat as incorrect for safety.
                false
            }
            else -> {
                question.answers.any { it.id.toString() == answerId && it.isCorrect }
            }
        }

        return FeedbackResponse(
            isCorrect = isCorrect,
            explanation = if (!isCorrect) question.explanation else null,
            grammarNote = question.grammarNote,
            nextQuestion = null,
            segmentComplete = false,
            xpEarned = if (isCorrect) 10 else 0,
            weakAreaIdentified = null,
            requiresBreak = false
        )
    }

    private fun com.sotospeak.entity.Question.toSharedQuestion(): Question {
        val imageWordContent = if (this.type == QuestionType.IMAGE_WORD_MATCH) {
            val entity = imageWordMatchQuestionRepository.findByQuestionId(this.id!!)
            entity?.let {
                val hotspots = imageWordMatchHotspotRepository.findByQuestionId(this.id)
                val words = imageWordMatchWordRepository.findByQuestionId(this.id)
                ImageWordMatchContent(
                    id = it.id.toString(),
                    type = "image_word_match",
                    points = this.points,
                    imageUrl = it.imageUrl,
                    instruction = it.instruction,
                    hotspots = hotspots.map { h ->
                        com.sotospeak.shared.model.HotspotData(
                            id = h.hotspotId,
                            x = h.x,
                            y = h.y,
                            width = h.width,
                            height = h.height,
                            shape = com.sotospeak.shared.model.HotspotShape.RECTANGLE,
                            wordId = h.wordId
                        )
                    },
                    words = words.map { w ->
                        com.sotospeak.shared.model.WordData(
                            id = w.wordId,
                            text = w.text,
                            translation = w.translation,
                            audioUrl = w.audioUrl
                        )
                    }
                )
            }
        } else null

        return Question(
            id = this.id.toString(),
            type = when (this.type) {
                QuestionType.TEXT_SELECT -> com.sotospeak.shared.model.QuestionType.TEXT_SELECT
                QuestionType.AUDIO_SELECT -> com.sotospeak.shared.model.QuestionType.AUDIO_SELECT
                QuestionType.IMAGE_SELECT -> com.sotospeak.shared.model.QuestionType.IMAGE_SELECT
                QuestionType.FILL_BLANK -> com.sotospeak.shared.model.QuestionType.FILL_BLANK
                QuestionType.DRAG_DROP_IMAGE -> com.sotospeak.shared.model.QuestionType.DRAG_DROP_IMAGE
                QuestionType.IMAGE_WORD_MATCH -> com.sotospeak.shared.model.QuestionType.IMAGE_WORD_MATCH
                else -> com.sotospeak.shared.model.QuestionType.TEXT_SELECT
            },
            title = this.title,
            text = this.text,
            audioUrl = this.audioUrl,
            imageUrl = this.imageUrl,
            points = this.points,
            answers = this.answers.map { it.toSharedAnswer() },
            imageWordMatchContent = imageWordContent
        )
    }

    private fun com.sotospeak.entity.Answer.toSharedAnswer(): Answer {
        return Answer(
            id = this.id.toString(),
            text = this.text,
            imageUrl = this.imageUrl,
            audioUrl = this.audioUrl,
            matchTarget = this.matchTarget
        )
    }
}
