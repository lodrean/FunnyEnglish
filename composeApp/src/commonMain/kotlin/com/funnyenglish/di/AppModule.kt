package com.funnyenglish.di

import com.funnyenglish.data.remote.FunnyEnglishApi
import com.funnyenglish.data.repository.GamificationRepositoryImpl
import com.funnyenglish.viewmodel.AdaptiveLessonViewModel
import com.funnyenglish.viewmodel.GamificationRepository
import com.funnyenglish.viewmodel.GamificationViewModel

/**
 * Dependency Injection Module
 * 
 * Simple DI container for the app
 */

object AppModule {
    
    // API Client
    private val api by lazy {
        FunnyEnglishApi(
            baseUrl = "http://localhost:8080",
            authTokenProvider = { getAuthToken() }
        )
    }
    
    // Repository
    val gamificationRepository: GamificationRepository by lazy {
        GamificationRepositoryImpl(api)
    }
    
    // ViewModels
    fun provideGamificationViewModel(): GamificationViewModel {
        return GamificationViewModel(gamificationRepository)
    }
    
    fun provideAdaptiveLessonViewModel(): AdaptiveLessonViewModel {
        // TODO: Implement AdaptiveLessonRepository
        return AdaptiveLessonViewModel(MockAdaptiveLessonRepository())
    }
    
    // Auth token storage (simplified)
    private var authToken: String? = null
    
    fun setAuthToken(token: String) {
        authToken = token
    }
    
    fun getAuthToken(): String? = authToken
    
    fun clearAuthToken() {
        authToken = null
    }
}

// Mock repository for testing
class MockAdaptiveLessonRepository : com.funnyenglish.viewmodel.AdaptiveLessonRepository {
    override suspend fun startLesson(
        categoryId: String?,
        skillType: com.funnyenglish.shared.model.SkillType?,
        durationMinutes: Int
    ) = com.funnyenglish.viewmodel.StartLessonResponse(
        lessonId = "mock-lesson",
        segments = emptyList(),
        estimatedDurationMinutes = durationMinutes,
        targetDifficulty = com.funnyenglish.shared.model.DifficultyLevel.BEGINNER
    )
    
    override suspend fun getNextQuestion(lessonId: String) = 
        com.funnyenglish.viewmodel.NextQuestionResponse(
            question = null,
            segmentProgress = 0f,
            overallProgress = 0f,
            timeRemainingSeconds = 0,
            requiresBreak = false,
            isLastQuestion = true
        )
    
    override suspend fun submitAnswer(
        lessonId: String,
        questionId: String,
        answerId: String,
        timeSpentSeconds: Int
    ) = com.funnyenglish.shared.model.FeedbackResponse(
        isCorrect = true,
        explanation = "Mock feedback",
        grammarNote = null,
        nextQuestion = null,
        segmentComplete = false,
        xpEarned = 10,
        weakAreaIdentified = null
    )
    
    override suspend fun requestBreak(lessonId: String) = 
        com.funnyenglish.viewmodel.BreakResponse(
            breakDuration = 30,
            canResume = true
        )
    
    override suspend fun resumeLesson(lessonId: String) = 
        com.funnyenglish.viewmodel.ResumeLessonResponse(
            success = true,
            nextQuestion = null
        )
    
    override suspend fun completeLesson(lessonId: String) = 
        com.funnyenglish.viewmodel.CompleteLessonResponse(
            totalXp = 50,
            skillImprovements = emptyMap(),
            weakAreasIdentified = emptyList(),
            nextRecommendedLesson = null,
            timeSpentSeconds = 300,
            questionsAnswered = 10,
            accuracy = 0.8f
        )
}
