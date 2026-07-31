package com.funnyenglish.service

import com.funnyenglish.dto.*
import com.funnyenglish.entity.AdaptiveLesson
import com.funnyenglish.entity.Answer
import com.funnyenglish.entity.LessonSegment
import com.funnyenglish.entity.Question
import com.funnyenglish.entity.QuestionType
import com.funnyenglish.entity.UserSkill
import com.funnyenglish.repository.*
import java.time.temporal.TemporalAdjusters
import com.funnyenglish.shared.model.DifficultyLevel
import com.funnyenglish.shared.model.LessonStatus
import com.funnyenglish.shared.model.SegmentPerformance
import com.funnyenglish.shared.model.SegmentType
import com.funnyenglish.shared.model.SkillType
import com.funnyenglish.shared.model.XpSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * Сервис управления адаптивными уроками
 */
@Service
class AdaptiveLessonService(
    private val adaptiveLessonRepository: AdaptiveLessonRepository,
    private val userSkillRepository: UserSkillRepository,
    private val testRepository: TestRepository,
    private val questionRepository: QuestionRepository,
    private val difficultyEngine: DifficultyEngine,
    private val xpService: XpService
) {

    companion object {
        const val SEGMENT_DURATION_SECONDS = 90 // 1.5 minutes per segment
        const val BREAK_THRESHOLD_SECONDS = 600 // 10 minutes
        const val BREAK_DURATION_SECONDS = 30 // 30 seconds break
    }

    /**
     * Начать новый адаптивный урок
     */
    @Transactional
    fun startLesson(
        userId: UUID,
        categoryId: String?,
        skillType: SkillType?,
        targetDurationMinutes: Int
    ): StartAdaptiveLessonResponse {
        // Calculate number of segments based on duration
        val numSegments = when (targetDurationMinutes) {
            5 -> 3
            7 -> 4
            10 -> 5
            else -> 4
        }

        // Get user's current skill level for initial difficulty
        val initialDifficulty = determineInitialDifficulty(userId, skillType)
        
        // Get weak areas to focus on
        val weakAreas = userSkillRepository.findWeakAreas(userId)
            .filter { skillType == null || it.skillType == skillType }

        // Generate segments
        val segments = generateSegments(numSegments, categoryId, skillType, weakAreas)

        // Create lesson entity
        val lesson = AdaptiveLesson(
            id = UUID.randomUUID(),
            userId = userId,
            status = LessonStatus.IN_PROGRESS,
            currentDifficulty = initialDifficulty,
            startedAt = Instant.now(),
            totalSegments = numSegments,
            weakAreas = weakAreas.map { it.skillType.name }
        )
        adaptiveLessonRepository.save(lesson)

        return StartAdaptiveLessonResponse(
            lessonId = lesson.id.toString(),
            segments = segments.map { it.toInfo() },
            estimatedDurationMinutes = targetDurationMinutes,
            targetDifficulty = initialDifficulty
        )
    }

    /**
     * Получить следующий вопрос
     */
    @Transactional(readOnly = true)
    fun getNextQuestion(userId: UUID, lessonId: UUID): NextQuestionResponse {
        val lesson = adaptiveLessonRepository.findByIdAndUserId(lessonId, userId)
            ?: throw NoSuchElementException("Lesson not found")

        if (lesson.status == LessonStatus.COMPLETED) {
            return NextQuestionResponse(
                question = null,
                segmentProgress = 1f,
                overallProgress = 1f,
                timeRemainingSeconds = 0,
                requiresBreak = false,
                isLastQuestion = true
            )
        }

        // Check if break required
        val timeSpent = lesson.timeSpentSeconds
        val requiresBreak = difficultyEngine.shouldRequireBreak(
            timeSpent,
            lesson.currentSegmentIndex
        )

        if (requiresBreak && lesson.status != LessonStatus.ON_BREAK) {
            lesson.status = LessonStatus.ON_BREAK
            adaptiveLessonRepository.save(lesson)
        }

        // Get or generate next question
        val question = getQuestionForLesson(lesson)
        val segmentProgress = calculateSegmentProgress(lesson)
        val overallProgress = calculateOverallProgress(lesson)
        val timeRemaining = calculateTimeRemaining(lesson)

        return NextQuestionResponse(
            question = question?.toDto(),
            segmentProgress = segmentProgress,
            overallProgress = overallProgress,
            timeRemainingSeconds = timeRemaining,
            requiresBreak = requiresBreak && lesson.status != LessonStatus.ON_BREAK,
            isLastQuestion = isLastQuestion(lesson)
        )
    }

    /**
     * Отправить ответ
     */
    @Transactional
    fun submitAnswer(
        userId: UUID,
        lessonId: UUID,
        questionId: String,
        answerId: String,
        timeSpentSeconds: Int
    ): SubmitAnswerResponse {
        val lesson = adaptiveLessonRepository.findByIdAndUserId(lessonId, userId)
            ?: throw NoSuchElementException("Lesson not found")

        // Validate answer
        val question = questionRepository.findById(UUID.fromString(questionId))
            .orElseThrow { NoSuchElementException("Question not found") }
        
        val isCorrect = question.answers.any { it.id.toString() == answerId && it.isCorrect }
        
        // Record performance
        lesson.questionsAnswered++
        if (isCorrect) lesson.correctAnswers++
        lesson.timeSpentSeconds += timeSpentSeconds
        
        // Track skill performance
        val skillType = getSkillTypeForQuestion(question)
        recordSkillPerformance(userId, skillType, isCorrect)

        // Check if difficulty adjustment needed
        val recentPerformance = getRecentPerformance(lesson)
        val difficultyAdjustment = difficultyEngine.calculateNextDifficulty(
            lesson.currentDifficulty,
            recentPerformance
        )

        val difficultyChanged = difficultyAdjustment.newDifficulty != lesson.currentDifficulty
        if (difficultyChanged) {
            lesson.currentDifficulty = difficultyAdjustment.newDifficulty
        }

        // Check if segment complete
        val segmentComplete = shouldCompleteSegment(lesson)
        if (segmentComplete) {
            lesson.currentSegmentIndex++
        }

        // Check if lesson complete
        if (isLessonComplete(lesson)) {
            lesson.status = LessonStatus.COMPLETED
            lesson.completedAt = Instant.now()
        }

        adaptiveLessonRepository.save(lesson)

        // Calculate XP
        val xpEarned = calculateXp(isCorrect, timeSpentSeconds, difficultyAdjustment.confidenceScore)
        if (xpEarned > 0) {
            xpService.addXp(
                userId = userId,
                amount = xpEarned,
                source = XpSource.LESSON_COMPLETION,
                description = "Adaptive lesson question"
            )
        }

        return SubmitAnswerResponse(
            isCorrect = isCorrect,
            explanation = if (!isCorrect) question.explanation else null,
            grammarNote = question.grammarNote,
            xpEarned = xpEarned,
            difficultyAdjusted = difficultyChanged,
            newDifficulty = if (difficultyChanged) difficultyAdjustment.newDifficulty else null,
            segmentComplete = segmentComplete,
            correctAnswer = if (!isCorrect) question.answers.find { it.isCorrect }?.toDto() else null
        )
    }

    /**
     * Запросить перерыв
     */
    @Transactional
    fun requestBreak(userId: UUID, lessonId: UUID): BreakResponse {
        val lesson = adaptiveLessonRepository.findByIdAndUserId(lessonId, userId)
            ?: throw NoSuchElementException("Lesson not found")

        lesson.status = LessonStatus.ON_BREAK
        adaptiveLessonRepository.save(lesson)

        return BreakResponse(
            breakDuration = BREAK_DURATION_SECONDS,
            canResume = true
        )
    }

    /**
     * Продолжить урок после перерыва
     */
    @Transactional
    fun resumeLesson(userId: UUID, lessonId: UUID): ResumeLessonResponse {
        val lesson = adaptiveLessonRepository.findByIdAndUserId(lessonId, userId)
            ?: throw NoSuchElementException("Lesson not found")

        if (lesson.status != LessonStatus.ON_BREAK) {
            return ResumeLessonResponse(success = false, nextQuestion = null)
        }

        lesson.status = LessonStatus.IN_PROGRESS
        adaptiveLessonRepository.save(lesson)

        val nextQuestion = getNextQuestion(userId, lessonId)
        return ResumeLessonResponse(success = true, nextQuestion = nextQuestion)
    }

    /**
     * Завершить урок
     */
    @Transactional
    fun completeLesson(userId: UUID, lessonId: UUID): CompleteLessonResponse {
        val lesson = adaptiveLessonRepository.findByIdAndUserId(lessonId, userId)
            ?: throw NoSuchElementException("Lesson not found")

        lesson.status = LessonStatus.COMPLETED
        lesson.completedAt = Instant.now()
        adaptiveLessonRepository.save(lesson)

        // Calculate skill improvements
        val skillImprovements = calculateSkillImprovements(lesson)
        
        // Identify weak areas
        val weakAreas = identifyWeakAreas(lesson)
        
        // Get recommendation
        val recommendation = generateRecommendation(userId, weakAreas)

        // Award completion XP
        val completionXp = calculateCompletionXp(lesson)
        xpService.addXp(
            userId = userId,
            amount = completionXp,
            source = XpSource.LESSON_COMPLETION,
            description = "Completed adaptive lesson"
        )

        return CompleteLessonResponse(
            totalXp = completionXp,
            skillImprovements = skillImprovements,
            weakAreasIdentified = weakAreas,
            nextRecommendedLesson = recommendation,
            timeSpentSeconds = lesson.timeSpentSeconds,
            questionsAnswered = lesson.questionsAnswered,
            accuracy = if (lesson.questionsAnswered > 0) {
                lesson.correctAnswers.toFloat() / lesson.questionsAnswered
            } else 0f
        )
    }

    /**
     * Получить состояние урока
     */
    @Transactional(readOnly = true)
    fun getLessonState(userId: UUID, lessonId: UUID): LessonStateResponse {
        val lesson = adaptiveLessonRepository.findByIdAndUserId(lessonId, userId)
            ?: throw NoSuchElementException("Lesson not found")

        return LessonStateResponse(
            lessonId = lesson.id.toString(),
            status = lesson.status.name,
            currentSegment = lesson.currentSegmentIndex,
            totalSegments = lesson.totalSegments,
            currentDifficulty = lesson.currentDifficulty,
            timeSpentSeconds = lesson.timeSpentSeconds,
            questionsAnswered = lesson.questionsAnswered,
            correctAnswers = lesson.correctAnswers
        )
    }

    /**
     * Получить слабые области
     */
    @Transactional(readOnly = true)
    fun getWeakAreas(userId: UUID): List<WeakAreaDto> {
        return userSkillRepository.findWeakAreas(userId).map { skill ->
            WeakAreaDto(
                skillType = skill.skillType,
                masteryLevel = skill.masteryLevel,
                recommendedExercises = calculateRecommendedExercises(skill.masteryLevel)
            )
        }
    }

    /**
     * Получить рекомендацию
     */
    @Transactional(readOnly = true)
    fun getRecommendation(userId: UUID): LessonRecommendation? {
        val weakAreas = userSkillRepository.findWeakAreas(userId)
        val weakestSkill = weakAreas.minByOrNull { it.masteryLevel } ?: return null

        return LessonRecommendation(
            categoryId = null,
            skillType = weakestSkill.skillType,
            difficulty = DifficultyLevel.BEGINNER,
            reason = "Focus on improving ${weakestSkill.skillType.name.lowercase()}"
        )
    }

    // ==================== Private Methods ====================

    private fun determineInitialDifficulty(userId: UUID, skillType: SkillType?): DifficultyLevel {
        val userSkills = userSkillRepository.findByUserId(userId)
        val averageMastery = if (userSkills.isNotEmpty()) {
            userSkills.map { it.masteryLevel }.average().toFloat()
        } else 0.5f

        return when {
            averageMastery < 0.4f -> DifficultyLevel.BEGINNER
            averageMastery < 0.6f -> DifficultyLevel.ELEMENTARY
            averageMastery < 0.8f -> DifficultyLevel.INTERMEDIATE
            else -> DifficultyLevel.ADVANCED
        }
    }

    private fun generateSegments(
        count: Int,
        categoryId: String?,
        skillType: SkillType?,
        weakAreas: List<UserSkill>
    ): List<LessonSegment> {
        val types = listOf(
            SegmentType.INTRO,
            SegmentType.PRACTICE,
            SegmentType.CHALLENGE,
            SegmentType.REVIEW,
            SegmentType.GRAMMAR_HINT
        )

        return (0 until count).map { index ->
            LessonSegment(
                id = UUID.randomUUID(),
                type = types.getOrElse(index) { SegmentType.PRACTICE },
                estimatedDurationSeconds = SEGMENT_DURATION_SECONDS,
                learningObjective = generateLearningObjective(index, weakAreas)
            )
        }
    }

    private fun generateLearningObjective(index: Int, weakAreas: List<UserSkill>): String {
        if (weakAreas.isNotEmpty() && index < 2) {
            return "Practice ${weakAreas[index % weakAreas.size].skillType.name.lowercase()}"
        }
        return "General practice"
    }

    private fun getQuestionForLesson(lesson: AdaptiveLesson): Question? {
        // TODO: Implement question selection based on difficulty and weak areas
        return null
    }

    private fun calculateSegmentProgress(lesson: AdaptiveLesson): Float {
        if (lesson.totalSegments == 0) return 0f
        return lesson.currentSegmentIndex.toFloat() / lesson.totalSegments
    }

    private fun calculateOverallProgress(lesson: AdaptiveLesson): Float {
        // More sophisticated calculation based on questions answered
        val segmentProgress = calculateSegmentProgress(lesson)
        val questionProgress = if (lesson.questionsAnswered > 0) {
            0.1f // Small increment per question
        } else 0f
        return (segmentProgress + questionProgress).coerceIn(0f, 1f)
    }

    private fun calculateTimeRemaining(lesson: AdaptiveLesson): Int {
        val estimatedTotal = lesson.totalSegments * SEGMENT_DURATION_SECONDS
        return (estimatedTotal - lesson.timeSpentSeconds).coerceAtLeast(0)
    }

    private fun isLastQuestion(lesson: AdaptiveLesson): Boolean {
        return lesson.currentSegmentIndex >= lesson.totalSegments - 1 &&
               lesson.questionsAnswered >= (lesson.currentSegmentIndex + 1) * 3
    }

    private fun shouldCompleteSegment(lesson: AdaptiveLesson): Boolean {
        return lesson.questionsAnswered % 3 == 0 // 3 questions per segment
    }

    private fun isLessonComplete(lesson: AdaptiveLesson): Boolean {
        return lesson.currentSegmentIndex >= lesson.totalSegments ||
               (lesson.questionsAnswered >= lesson.totalSegments * 3)
    }

    private fun getRecentPerformance(lesson: AdaptiveLesson): List<SegmentPerformance> {
        // Return recent performance from lesson history
        return emptyList() // TODO: Implement
    }

    private fun getSkillTypeForQuestion(question: Question): SkillType {
        return when (question.type) {
            QuestionType.TEXT_SELECT -> SkillType.VOCABULARY_NOUNS
            QuestionType.AUDIO_SELECT -> SkillType.LISTENING
            QuestionType.IMAGE_SELECT -> SkillType.VOCABULARY_NOUNS
            QuestionType.FILL_BLANK -> SkillType.GRAMMAR_TENSES
            QuestionType.DRAG_DROP_IMAGE -> SkillType.GRAMMAR_ARTICLES
            QuestionType.DRAG_DROP_MATCH -> SkillType.GRAMMAR_ARTICLES
            QuestionType.DRAG_DROP_SORT -> SkillType.GRAMMAR_TENSES
            QuestionType.IMAGE_WORD_MATCH -> SkillType.VOCABULARY_NOUNS
        }
    }

    private fun calculateXp(isCorrect: Boolean, timeSpent: Int, confidence: Float): Int {
        if (!isCorrect) return 0
        val baseXp = 10
        val speedBonus = if (timeSpent < 10) 5 else 0
        val difficultyBonus = (confidence * 10).toInt()
        return baseXp + speedBonus + difficultyBonus
    }

    private fun calculateCompletionXp(lesson: AdaptiveLesson): Int {
        val baseXp = 50
        val accuracyBonus = (lesson.accuracy * 50).toInt()
        return baseXp + accuracyBonus
    }

    private fun calculateSkillImprovements(lesson: AdaptiveLesson): Map<SkillType, Float> {
        // TODO: Calculate based on performance in lesson
        return emptyMap()
    }

    private fun identifyWeakAreas(lesson: AdaptiveLesson): List<WeakAreaDto> {
        // TODO: Identify from lesson performance
        return emptyList()
    }

    private fun generateRecommendation(userId: UUID, weakAreas: List<WeakAreaDto>): LessonRecommendation? {
        if (weakAreas.isEmpty()) return null
        val weakest = weakAreas.minByOrNull { it.masteryLevel } ?: return null
        
        return LessonRecommendation(
            categoryId = null,
            skillType = weakest.skillType,
            difficulty = DifficultyLevel.BEGINNER,
            reason = "Focus on ${weakest.skillType.name.lowercase()}"
        )
    }

    private fun calculateRecommendedExercises(masteryLevel: Float): Int {
        return when {
            masteryLevel < 0.3f -> 10
            masteryLevel < 0.5f -> 7
            masteryLevel < 0.7f -> 5
            else -> 3
        }
    }
    
    private fun recordSkillPerformance(userId: UUID, skillType: SkillType, isCorrect: Boolean) {
        val skill = userSkillRepository.findByUserIdAndSkillType(userId, skillType)
            ?: UserSkill(
                userId = userId,
                skillType = skillType,
                masteryLevel = 0.5f
            )
        
        skill.questionsAttempted++
        if (isCorrect) {
            skill.questionsCorrect++
        }
        
        // Update mastery level based on accuracy
        val accuracy = if (skill.questionsAttempted > 0) {
            skill.questionsCorrect.toFloat() / skill.questionsAttempted
        } else 0.5f
        
        skill.masteryLevel = accuracy
        skill.lastUpdated = java.time.Instant.now()
        
        userSkillRepository.save(skill)
    }
}

// ==================== Extension Functions ====================

private fun LessonSegment.toInfo(): SegmentInfo = SegmentInfo(
    id = this.id.toString(),
    type = this.type.name,
    estimatedDurationSeconds = this.estimatedDurationSeconds,
    learningObjective = this.learningObjective
)

private fun Question.toDto(): QuestionDto = QuestionDto(
    id = this.id.toString(),
    type = this.type.name,
    text = this.text,
    imageUrl = this.imageUrl,
    audioUrl = this.audioUrl,
    answers = this.answers.map { it.toDto() },
    difficulty = DifficultyLevel.BEGINNER, // TODO: Get from entity
    skillType = SkillType.VOCABULARY_NOUNS // TODO: Get from entity
)

private fun Answer.toDto(): AnswerDto = AnswerDto(
    id = this.id.toString(),
    text = this.text,
    imageUrl = this.imageUrl
)

private val AdaptiveLesson.accuracy: Float
    get() = if (questionsAnswered > 0) correctAnswers.toFloat() / questionsAnswered else 0f
