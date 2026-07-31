package com.funnyenglish.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.model.AdaptiveLessonState
import com.funnyenglish.shared.model.DifficultyLevel
import com.funnyenglish.shared.model.FeedbackResponse
import com.funnyenglish.shared.model.SkillType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * FunnyEnglish Adaptive Lesson ViewModel
 * 
 * Manages:
 * - Lesson lifecycle (start, resume, complete)
 * - Question/answer flow
 * - Progress tracking
 * - Difficulty adjustments
 * 
 * Integrates with Backend API:
 * - POST /api/v1/adaptive-lessons/start
 * - GET /api/v1/adaptive-lessons/{id}/next
 * - POST /api/v1/adaptive-lessons/{id}/answer
 * - POST /api/v1/adaptive-lessons/{id}/break
 * - POST /api/v1/adaptive-lessons/{id}/resume
 * - POST /api/v1/adaptive-lessons/{id}/complete
 */

// ==================== UI States ====================

enum class LessonScreenState {
    IDLE,           // Not started
    LOADING,        // Starting/resuming
    QUESTION,       // Showing question
    FEEDBACK,       // Showing feedback
    BREAK,          // Break requested
    COMPLETED,      // Lesson finished
    ERROR
}

data class LessonUiState(
    val screenState: LessonScreenState = LessonScreenState.IDLE,
    val lessonId: String? = null,
    val lessonState: AdaptiveLessonState? = null,
    val currentQuestion: QuestionUiModel? = null,
    val feedback: FeedbackResponse? = null,
    val progress: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null,
    val xpEarned: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0
)

data class QuestionUiModel(
    val id: String,
    val text: String?,
    val imageUrl: String?,
    val audioUrl: String?,
    val answers: List<AnswerUiModel>,
    val difficulty: DifficultyLevel,
    val skillType: SkillType
)

data class AnswerUiModel(
    val id: String,
    val text: String?,
    val imageUrl: String?
)

data class LessonSummary(
    val totalXp: Int,
    val accuracy: Float,
    val skillImprovements: Map<SkillType, Float>,
    val weakAreas: List<String>,
    val timeSpentSeconds: Int,
    val questionsAnswered: Int
)

// ==================== ViewModel ====================

class AdaptiveLessonViewModel(
    private val repository: AdaptiveLessonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    // Track if user is on break
    private var isOnBreak = false

    // ==================== Lesson Lifecycle ====================

    fun startLesson(
        categoryId: String? = null,
        skillType: SkillType? = null,
        durationMinutes: Int = 7
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                screenState = LessonScreenState.LOADING,
                isLoading = true,
                error = null
            )
            
            try {
                val response = repository.startLesson(
                    categoryId = categoryId,
                    skillType = skillType,
                    durationMinutes = durationMinutes
                )
                
                _uiState.value = _uiState.value.copy(
                    lessonId = response.lessonId,
                    screenState = LessonScreenState.QUESTION,
                    isLoading = false
                )
                
                // Load first question
                loadNextQuestion()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = LessonScreenState.ERROR,
                    isLoading = false,
                    error = e.message ?: "Failed to start lesson"
                )
            }
        }
    }

    fun resumeLesson() {
        val lessonId = _uiState.value.lessonId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                repository.resumeLesson(lessonId)
                isOnBreak = false
                _uiState.value = _uiState.value.copy(
                    screenState = LessonScreenState.QUESTION,
                    isLoading = false
                )
                loadNextQuestion()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to resume lesson"
                )
            }
        }
    }

    fun completeLesson() {
        val lessonId = _uiState.value.lessonId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = repository.completeLesson(lessonId)
                
                _uiState.value = _uiState.value.copy(
                    screenState = LessonScreenState.COMPLETED,
                    isLoading = false,
                    xpEarned = result.totalXp
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to complete lesson"
                )
            }
        }
    }

    // ==================== Question Flow ====================

    private fun loadNextQuestion() {
        val lessonId = _uiState.value.lessonId ?: return
        
        viewModelScope.launch {
            try {
                val response = repository.getNextQuestion(lessonId)
                
                if (response.question == null || response.isLastQuestion) {
                    // Lesson complete
                    completeLesson()
                    return@launch
                }
                
                val questionModel = QuestionUiModel(
                    id = response.question.id,
                    text = response.question.text,
                    imageUrl = response.question.imageUrl,
                    audioUrl = response.question.audioUrl,
                    answers = response.question.answers.map { 
                        AnswerUiModel(
                            id = it.id,
                            text = it.text,
                            imageUrl = it.imageUrl
                        )
                    },
                    difficulty = response.question.difficulty,
                    skillType = response.question.skillType
                )
                
                _uiState.value = _uiState.value.copy(
                    currentQuestion = questionModel,
                    progress = response.overallProgress,
                    screenState = LessonScreenState.QUESTION
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load question"
                )
            }
        }
    }

    fun submitAnswer(answerId: String, timeSpentSeconds: Int = 0) {
        val lessonId = _uiState.value.lessonId ?: return
        val questionId = _uiState.value.currentQuestion?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val feedback = repository.submitAnswer(
                    lessonId = lessonId,
                    questionId = questionId,
                    answerId = answerId,
                    timeSpentSeconds = timeSpentSeconds
                )
                
                // Update stats
                val newCorrectCount = if (feedback.isCorrect) {
                    _uiState.value.correctAnswers + 1
                } else {
                    _uiState.value.correctAnswers
                }
                
                _uiState.value = _uiState.value.copy(
                    feedback = feedback,
                    correctAnswers = newCorrectCount,
                    totalQuestions = _uiState.value.totalQuestions + 1,
                    screenState = LessonScreenState.FEEDBACK,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to submit answer"
                )
            }
        }
    }

    fun nextQuestion() {
        _uiState.value = _uiState.value.copy(
            feedback = null,
            currentQuestion = null
        )
        loadNextQuestion()
    }

    // ==================== Break ====================

    fun requestBreak() {
        val lessonId = _uiState.value.lessonId ?: return
        
        viewModelScope.launch {
            try {
                repository.requestBreak(lessonId)
                isOnBreak = true
                _uiState.value = _uiState.value.copy(
                    screenState = LessonScreenState.BREAK
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to request break"
                )
            }
        }
    }

    // ==================== Utilities ====================

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = LessonUiState()
        isOnBreak = false
    }

    // Calculate accuracy
    fun getAccuracy(): Float {
        val state = _uiState.value
        return if (state.totalQuestions > 0) {
            state.correctAnswers.toFloat() / state.totalQuestions.toFloat()
        } else 0f
    }
}

// ==================== Repository Interface ====================

interface AdaptiveLessonRepository {
    suspend fun startLesson(
        categoryId: String?,
        skillType: SkillType?,
        durationMinutes: Int
    ): StartLessonResponse
    
    suspend fun getNextQuestion(lessonId: String): NextQuestionResponse
    
    suspend fun submitAnswer(
        lessonId: String,
        questionId: String,
        answerId: String,
        timeSpentSeconds: Int
    ): FeedbackResponse
    
    suspend fun requestBreak(lessonId: String): BreakResponse
    suspend fun resumeLesson(lessonId: String): ResumeLessonResponse
    suspend fun completeLesson(lessonId: String): CompleteLessonResponse
}

// Response DTOs (should match backend)
data class StartLessonResponse(
    val lessonId: String,
    val segments: List<SegmentInfo>,
    val estimatedDurationMinutes: Int,
    val targetDifficulty: DifficultyLevel
)

data class SegmentInfo(
    val id: String,
    val type: String,
    val estimatedDurationSeconds: Int,
    val learningObjective: String
)

data class NextQuestionResponse(
    val question: QuestionUiModel?,
    val segmentProgress: Float,
    val overallProgress: Float,
    val timeRemainingSeconds: Int,
    val requiresBreak: Boolean,
    val isLastQuestion: Boolean
)

data class BreakResponse(
    val breakDuration: Int,
    val canResume: Boolean
)

data class ResumeLessonResponse(
    val success: Boolean,
    val nextQuestion: NextQuestionResponse?
)

data class CompleteLessonResponse(
    val totalXp: Int,
    val skillImprovements: Map<SkillType, Float>,
    val weakAreasIdentified: List<WeakAreaDto>,
    val nextRecommendedLesson: LessonRecommendation?,
    val timeSpentSeconds: Int,
    val questionsAnswered: Int,
    val accuracy: Float
)

data class WeakAreaDto(
    val skillType: SkillType,
    val masteryLevel: Float,
    val recommendedExercises: Int
)

data class LessonRecommendation(
    val categoryId: String?,
    val skillType: SkillType?,
    val difficulty: DifficultyLevel,
    val reason: String
)
