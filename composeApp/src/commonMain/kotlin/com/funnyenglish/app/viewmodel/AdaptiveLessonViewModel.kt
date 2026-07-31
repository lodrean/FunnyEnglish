package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.*
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel для адаптивных микро-уроков
 *
 * Управляет состоянием урока с динамической сложностью,
 * отслеживает прогресс и обрабатывает перерывы.
 * Поддерживает как авторизованный, так и гостевой режим.
 */

data class AdaptiveLessonUiState(
    val isLoading: Boolean = false,
    val lesson: AdaptiveLessonState? = null,
    val currentQuestion: Question? = null,
    val currentQuestionIndex: Int = 0,
    val showBreak: Boolean = false,
    val isComplete: Boolean = false,
    val earnedXp: Int = 0,
    val totalCorrect: Int = 0,
    val totalAnswered: Int = 0,
    val weakAreasImproved: List<SkillGap> = emptyList(),
    val error: String? = null,
    val feedback: FeedbackResponse? = null,
    val isSubmitting: Boolean = false
)

class AdaptiveLessonViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdaptiveLessonUiState())
    val uiState: StateFlow<AdaptiveLessonUiState> = _uiState.asStateFlow()

    private var lessonStartTime: Long = 0
    private var isGuestSession: Boolean = false

    /**
     * Начать новый адаптивный урок
     */
    fun startLesson(categoryId: String? = null, targetDurationMinutes: Int = 5, isGuest: Boolean = false) {
        isGuestSession = isGuest
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val apiCall = if (isGuest) {
                api.getPublicAdaptiveLesson(categoryId, targetDurationMinutes)
            } else {
                api.startAdaptiveLesson(categoryId, targetDurationMinutes)
            }

            apiCall
                .onSuccess { lesson ->
                    lessonStartTime = 0L
                    val firstQuestion = lesson.segments.firstOrNull()?.questions?.firstOrNull()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lesson = lesson,
                            currentQuestion = firstQuestion,
                            currentQuestionIndex = 0
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to start lesson"
                        )
                    }
                }
        }
    }

    /**
     * Отправить ответ на текущий вопрос
     */
    fun submitAnswer(answerId: String) {
        val currentState = _uiState.value
        val question = currentState.currentQuestion ?: return
        val lesson = currentState.lesson ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val apiCall = if (isGuestSession) {
                api.validateAdaptiveAnswer(question.id, answerId)
            } else {
                api.submitAdaptiveAnswer(
                    lessonId = lesson.lessonId,
                    questionId = question.id,
                    answerId = answerId
                )
            }

            apiCall
                .onSuccess { feedback ->
                    val newTotalCorrect = currentState.totalCorrect + if (feedback.isCorrect) 1 else 0
                    val newTotalAnswered = currentState.totalAnswered + 1

                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            feedback = feedback,
                            totalCorrect = newTotalCorrect,
                            totalAnswered = newTotalAnswered,
                            earnedXp = it.earnedXp + feedback.xpEarned
                        )
                    }

                    // Check if break is required
                    if (feedback.requiresBreak) {
                        _uiState.update { it.copy(showBreak = true) }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = error.message ?: "Failed to submit answer"
                        )
                    }
                }
        }
    }

    /**
     * Перейти к следующему вопросу
     */
    fun moveToNextQuestion() {
        val currentState = _uiState.value
        val lesson = currentState.lesson ?: return
        val currentSegment = lesson.segments.getOrNull(lesson.currentSegmentIndex) ?: return

        val nextQuestionIndex = currentState.currentQuestionIndex + 1

        if (nextQuestionIndex < currentSegment.questions.size) {
            // Next question in current segment
            _uiState.update {
                it.copy(
                    currentQuestion = currentSegment.questions[nextQuestionIndex],
                    currentQuestionIndex = nextQuestionIndex,
                    feedback = null
                )
            }
        } else {
            // Move to next segment
            moveToNextSegment()
        }
    }

    /**
     * Перейти к следующему сегменту
     */
    private fun moveToNextSegment() {
        val currentState = _uiState.value
        val lesson = currentState.lesson ?: return

        val nextSegmentIndex = lesson.currentSegmentIndex + 1

        if (nextSegmentIndex < lesson.segments.size) {
            val nextSegment = lesson.segments[nextSegmentIndex]
            _uiState.update {
                it.copy(
                    lesson = lesson.copy(currentSegmentIndex = nextSegmentIndex),
                    currentQuestion = nextSegment.questions.firstOrNull(),
                    currentQuestionIndex = 0,
                    feedback = null
                )
            }
        } else {
            // Lesson complete
            completeLesson()
        }
    }

    /**
     * Запросить перерыв
     */
    fun requestBreak() {
        _uiState.update { it.copy(showBreak = true) }
    }

    /**
     * Продолжить после перерыва
     */
    fun resumeLesson() {
        _uiState.update { it.copy(showBreak = false) }
    }

    /**
     * Завершить урок
     */
    fun completeLesson() {
        val currentState = _uiState.value
        val lesson = currentState.lesson ?: return

        if (isGuestSession) {
            _uiState.update {
                it.copy(
                    isComplete = true,
                    earnedXp = it.earnedXp + 20 // completion bonus for guest
                )
            }
            return
        }

        viewModelScope.launch {
            api.completeAdaptiveLesson(lesson.lessonId)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isComplete = true,
                            earnedXp = result.totalXp,
                            weakAreasImproved = result.improvedSkills
                        )
                    }
                }
                .onFailure {
                    // Still mark as complete locally even if API fails
                    _uiState.update { it.copy(isComplete = true) }
                }
        }
    }

    /**
     * Пропустить текущий вопрос (с пометкой как неправильный)
     */
    fun skipQuestion() {
        _uiState.update {
            it.copy(
                totalAnswered = it.totalAnswered + 1,
                feedback = FeedbackResponse(
                    isCorrect = false,
                    explanation = "Пропущено",
                    grammarNote = null,
                    nextQuestion = null,
                    segmentComplete = false,
                    xpEarned = 0,
                    weakAreaIdentified = null
                )
            )
        }
    }

    /**
     * Сбросить состояние ошибки
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Получить прогресс текущего сегмента (0.0 - 1.0)
     */
    fun getSegmentProgress(): Float {
        val state = _uiState.value
        val lesson = state.lesson ?: return 0f
        val currentSegment = lesson.segments.getOrNull(lesson.currentSegmentIndex) ?: return 0f

        return if (currentSegment.questions.isNotEmpty()) {
            (state.currentQuestionIndex + 1).toFloat() / currentSegment.questions.size
        } else 0f
    }

    /**
     * Получить общий прогресс урока (0.0 - 1.0)
     */
    fun getOverallProgress(): Float {
        val state = _uiState.value
        val lesson = state.lesson ?: return 0f

        val completedSegments = lesson.currentSegmentIndex.toFloat()
        val currentSegmentProgress = getSegmentProgress()

        return (completedSegments + currentSegmentProgress) / lesson.totalSegments
    }

    /**
     * Получить оставшееся время в секундах
     */
    fun getRemainingTimeSeconds(): Int {
        val lesson = _uiState.value.lesson ?: return 0
        return lesson.segments.sumOf { it.estimatedDurationSeconds } - lesson.timeSpentSeconds
    }
}
