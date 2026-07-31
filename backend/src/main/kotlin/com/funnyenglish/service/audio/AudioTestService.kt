package com.funnyenglish.service.audio

import com.funnyenglish.dto.*
import com.funnyenglish.entity.Category
import com.funnyenglish.entity.audio.*
import com.funnyenglish.repository.CategoryRepository
import com.funnyenglish.repository.audio.AudioTestProgressRepository
import com.funnyenglish.repository.audio.AudioTestQuestionRepository
import com.funnyenglish.repository.audio.AudioTestRepository
import com.funnyenglish.service.AchievementService
import com.funnyenglish.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AudioTestService(
    private val audioTestRepository: AudioTestRepository,
    private val audioTestQuestionRepository: AudioTestQuestionRepository,
    private val audioTestProgressRepository: AudioTestProgressRepository,
    private val categoryRepository: CategoryRepository,
    private val userService: UserService,
    private val achievementService: AchievementService
) {
    private val logger = LoggerFactory.getLogger(AudioTestService::class.java)

    // ============== Public API ==============

    fun getPublishedAudioTests(categoryId: UUID?, difficulty: Int?, pageable: Pageable): Page<AudioTestResponse> {
        return audioTestRepository.findPublishedAudioTests(categoryId, difficulty, pageable)
            .map { it.toResponse() }
    }

    fun getPublishedAudioTestById(id: UUID): AudioTestDetailResponse {
        val audioTest = audioTestRepository.findPublishedByIdWithDetails(id)
            .orElseThrow { NoSuchElementException("Audio test not found") }
        return audioTest.toDetailResponse()
    }

    fun getUserProgress(userId: UUID): List<AudioTestProgressResponse> {
        return audioTestProgressRepository.findByUserIdWithAudioTest(userId)
            .map { it.toResponse() }
    }

    fun getUserProgressForTest(userId: UUID, audioTestId: UUID): AudioTestProgressResponse? {
        return audioTestProgressRepository.findByUserIdAndAudioTestId(userId, audioTestId)
            .map { it.toResponse() }
            .orElse(null)
    }

    // ============== Admin API ==============

    fun getAllAudioTests(pageable: Pageable): Page<AudioTestResponse> {
        return audioTestRepository.findAll(pageable).map { it.toResponse() }
    }

    fun getAudioTestById(id: UUID): AudioTestDetailResponse {
        val audioTest = audioTestRepository.findByIdWithDetails(id)
            .orElseThrow { NoSuchElementException("Audio test not found") }
        return audioTest.toDetailResponse()
    }

    @Transactional
    fun createAudioTest(request: CreateAudioTestRequest): AudioTestDetailResponse {
        logger.info("Creating audio test: ${request.title}")

        val category = request.categoryId?.let {
            categoryRepository.findById(it).orElse(null)
        }

        val audioTest = AudioTest(
            title = request.title,
            description = request.description,
            audioFileUrl = request.audioFileUrl,
            durationSeconds = request.durationSeconds,
            difficulty = request.difficulty,
            category = category,
            playsLimit = request.playsLimit
        )

        // Add questions
        request.questions.forEachIndexed { index, questionReq ->
            validateQuestionTiming(questionReq, request.durationSeconds)
            
            val question = AudioTestQuestion(
                questionType = questionReq.questionType,
                title = questionReq.title,
                text = questionReq.text,
                startTimeSeconds = questionReq.startTimeSeconds,
                endTimeSeconds = questionReq.endTimeSeconds,
                points = questionReq.points,
                displayOrder = index
            )

            questionReq.answers.forEachIndexed { answerIndex, answerReq ->
                val answer = AudioTestAnswer(
                    text = answerReq.text,
                    isCorrect = answerReq.isCorrect,
                    displayOrder = answerIndex
                )
                question.addAnswer(answer)
            }

            audioTest.addQuestion(question)
        }

        // Add transcript if provided
        request.transcript?.let { transcriptReq ->
            val transcript = AudioTranscript(
                content = transcriptReq.content,
                language = transcriptReq.language,
                isGenerated = transcriptReq.isGenerated
            )
            audioTest.addTranscript(transcript)
        }

        val saved = audioTestRepository.saveAndFlush(audioTest)
        logger.info("Created audio test with id: ${saved.id}")
        
        // Reload to ensure all collections are properly loaded
        val reloaded = audioTestRepository.findByIdWithDetails(saved.id!!)
            .orElseThrow { NoSuchElementException("Created audio test not found") }
        return reloaded.toDetailResponse()
    }

    @Transactional
    fun updateAudioTest(id: UUID, request: UpdateAudioTestRequest): AudioTestDetailResponse {
        val audioTest = audioTestRepository.findByIdWithDetails(id)
            .orElseThrow { NoSuchElementException("Audio test not found") }

        request.title?.let { audioTest.title = it }
        request.description?.let { audioTest.description = it }
        request.durationSeconds?.let { audioTest.durationSeconds = it }
        request.difficulty?.let { audioTest.difficulty = it }
        request.playsLimit?.let { audioTest.playsLimit = it }
        request.isPublished?.let { audioTest.isPublished = it }
        
        request.categoryId?.let { categoryId ->
            val category = categoryRepository.findById(categoryId).orElse(null)
            audioTest.category = category
        }

        val saved = audioTestRepository.save(audioTest)
        return saved.toDetailResponse()
    }

    @Transactional
    fun deleteAudioTest(id: UUID) {
        logger.info("Deleting audio test: $id")
        audioTestRepository.deleteById(id)
    }

    @Transactional
    fun submitAudioTest(userId: String, request: SubmitAudioTestRequest): SubmitAudioTestResponse {
        logger.info("User $userId submitting audio test: ${request.audioTestId}")
        
        val userUUID = UUID.fromString(userId)
        val testUUID = UUID.fromString(request.audioTestId)
        
        val audioTest = audioTestRepository.findPublishedByIdWithDetails(testUUID)
            .orElseThrow { NoSuchElementException("Audio test not found") }

        // Check plays limit
        val progress = audioTestProgressRepository.findByUserIdAndAudioTestId(userUUID, testUUID)
            .orElse(null)
        
        if (progress != null && !progress.canPlay()) {
            throw IllegalStateException("Plays limit reached for this audio test")
        }

        // Calculate score
        var score = 0
        var maxScore = 0
        
        val questions = audioTest.questions
        
        for (question in questions) {
            maxScore += question.points
            val submittedAnswer = request.answers.find { it.questionId == question.id.toString() }
            
            if (submittedAnswer != null) {
                val isCorrect = when (question.questionType) {
                    QuestionType.LISTENING_COMPREHENSION, QuestionType.TRUE_FALSE -> {
                        val correctAnswerIds = question.answers
                            .filter { it.isCorrect }
                            .map { it.id.toString() }
                            .toSet()
                        submittedAnswer.selectedAnswerIds.toSet() == correctAnswerIds
                    }
                    QuestionType.FILL_BLANK, QuestionType.DICTATION -> {
                        // For text answers, check if it matches any correct answer
                        // or use fuzzy matching for dictation
                        val correctTexts = question.answers
                            .filter { it.isCorrect }
                            .map { it.text.lowercase().trim() }
                        val submittedText = submittedAnswer.textAnswer?.lowercase()?.trim()
                        submittedText != null && correctTexts.any { 
                            submittedText == it || 
                            (question.questionType == QuestionType.DICTATION && 
                             similarity(submittedText, it) > 0.8)
                        }
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

        // Update or create progress
        val isNewBestScore = progress == null || score > progress.bestScore
        
        val updatedProgress = if (progress != null) {
            progress.apply {
                this.score = score
                this.maxScore = maxScore
                this.stars = maxOf(this.stars, stars)
                this.attemptsCount += 1
                this.bestScore = maxOf(this.bestScore, score)
                this.timeSpentSeconds = request.timeSpentSeconds
                this.lastAttemptAt = Instant.now()
                if (percentage >= 60) {
                    this.completedAt = Instant.now()
                }
                recordPlay()
            }
            audioTestProgressRepository.save(progress)
        } else {
            val newProgress = AudioTestProgress(
                user = userService.getUserById(userId),
                audioTest = audioTest,
                score = score,
                maxScore = maxScore,
                stars = stars,
                bestScore = score,
                timeSpentSeconds = request.timeSpentSeconds,
                completedAt = if (percentage >= 60) Instant.now() else null
            ).apply { recordPlay() }
            audioTestProgressRepository.save(newProgress)
        }

        // Calculate points
        val pointsEarned = if (isNewBestScore) {
            20 + (stars * 10) // Base 20 + 10 per star
        } else {
            stars * 5
        }

        // Add points and check level up
        val (_, levelUp) = userService.addPoints(userId, pointsEarned)

        // Check achievements
        val newAchievements = achievementService.checkAndAwardAchievements(userId, percentage, stars)

        logger.info("Audio test submitted: score=$score, stars=$stars, points=$pointsEarned")

        return SubmitAudioTestResponse(
            score = score,
            maxScore = maxScore,
            percentage = percentage,
            stars = stars,
            pointsEarned = pointsEarned,
            isNewBestScore = isNewBestScore,
            levelUp = levelUp,
            newAchievements = newAchievements
        )
    }

    // ============== Private Methods ==============

    private fun validateQuestionTiming(question: CreateAudioQuestionRequest, audioDuration: Int) {
        require(question.startTimeSeconds >= 0) { "Start time cannot be negative" }
        require(question.endTimeSeconds > question.startTimeSeconds) { "End time must be after start time" }
        require(question.endTimeSeconds <= audioDuration) { "End time cannot exceed audio duration" }
        require(question.endTimeSeconds - question.startTimeSeconds >= 5) { "Question time range must be at least 5 seconds" }
    }

    private fun similarity(s1: String, s2: String): Double {
        // Simple Levenshtein distance based similarity
        val maxLen = maxOf(s1.length, s2.length)
        if (maxLen == 0) return 1.0
        val distance = levenshteinDistance(s1, s2)
        return 1.0 - (distance.toDouble() / maxLen)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    minOf(
                        dp[i - 1][j] + 1,    // deletion
                        dp[i][j - 1] + 1,    // insertion
                        dp[i - 1][j - 1] + 1 // substitution
                    )
                }
            }
        }
        
        return dp[s1.length][s2.length]
    }

    // ============== Extension Functions ==============

    private fun AudioTest.toResponse(): AudioTestResponse = AudioTestResponse(
        id = this.id!!.toString(),
        title = this.title,
        description = this.description,
        audioFileUrl = this.audioFileUrl,
        durationSeconds = this.durationSeconds,
        difficulty = this.difficulty,
        category = this.category?.let { CategoryResponse(it.id.toString(), it.name, it.description, it.iconUrl, it.tests.size) },
        isPublished = this.isPublished,
        playsLimit = this.playsLimit,
        questionCount = this.questions.size,
        createdAt = this.createdAt
    )

    private fun AudioTest.toDetailResponse(): AudioTestDetailResponse = AudioTestDetailResponse(
        id = this.id!!.toString(),
        title = this.title,
        description = this.description,
        audioFileUrl = this.audioFileUrl,
        durationSeconds = this.durationSeconds,
        difficulty = this.difficulty,
        category = this.category?.let { CategoryResponse(it.id.toString(), it.name, it.description, it.iconUrl, it.tests.size) },
        isPublished = this.isPublished,
        playsLimit = this.playsLimit,
        questions = this.questions.sortedWith(compareBy({ it.displayOrder }, { it.startTimeSeconds }))
            .map { it.toResponse() },
        transcript = this.transcripts.firstOrNull()?.toResponse(),
        createdAt = this.createdAt
    )

    private fun AudioTestQuestion.toResponse(): AudioTestQuestionResponse = AudioTestQuestionResponse(
        id = this.id!!.toString(),
        questionType = this.questionType,
        title = this.title,
        text = this.text,
        startTimeSeconds = this.startTimeSeconds,
        endTimeSeconds = this.endTimeSeconds,
        points = this.points,
        displayOrder = this.displayOrder,
        answers = this.answers.sortedBy { it.displayOrder }.map { it.toResponse() }
    )

    private fun AudioTestAnswer.toResponse(): AudioTestAnswerResponse = AudioTestAnswerResponse(
        id = this.id!!.toString(),
        text = this.text,
        isCorrect = this.isCorrect,
        displayOrder = this.displayOrder
    )

    private fun AudioTranscript.toResponse(): AudioTranscriptResponse = AudioTranscriptResponse(
        id = this.id!!.toString(),
        content = this.content,
        language = this.language,
        isGenerated = this.isGenerated
    )

    private fun AudioTestProgress.toResponse(): AudioTestProgressResponse = AudioTestProgressResponse(
        audioTestId = this.audioTest?.id.toString(),
        score = this.score,
        maxScore = this.maxScore,
        percentage = this.getPercentage(),
        stars = this.stars,
        attemptsCount = this.attemptsCount,
        bestScore = this.bestScore,
        playsUsed = this.playsUsed,
        playsLimit = this.audioTest?.playsLimit,
        canPlay = this.canPlay(),
        completedAt = this.completedAt,
        lastAttemptAt = this.lastAttemptAt
    )
}
