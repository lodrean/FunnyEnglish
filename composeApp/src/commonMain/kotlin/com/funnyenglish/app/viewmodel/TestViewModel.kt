package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TestPlayState(
    val isLoading: Boolean = false,
    val test: TestDetail? = null,
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, SubmitAnswer> = emptyMap(),
    val timeElapsed: Int = 0,
    val isSubmitting: Boolean = false,
    val result: SubmitTestResult? = null,
    val error: String? = null
)

class TestViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _state = MutableStateFlow(TestPlayState())
    val state: StateFlow<TestPlayState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun loadTest(testId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            api.getTestById(testId)
                .onSuccess { test ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        test = test
                    )
                    startTimer()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _state.value = _state.value.copy(
                    timeElapsed = _state.value.timeElapsed + 1
                )

                // Check time limit
                val timeLimit = _state.value.test?.timeLimitSeconds
                if (timeLimit != null && _state.value.timeElapsed >= timeLimit) {
                    submitTest()
                    break
                }
            }
        }
    }

    fun selectAnswer(questionId: String, answerId: String) {
        val currentAnswer = _state.value.answers[questionId]
        val question = _state.value.test?.questions?.find { it.id == questionId }

        val newAnswer = when (question?.type) {
            QuestionType.DRAG_DROP_IMAGE -> {
                // For drag-drop, we handle this differently
                currentAnswer ?: SubmitAnswer(questionId)
            }
            else -> {
                // For single/multiple choice
                val selectedIds = if (currentAnswer?.selectedAnswerIds?.contains(answerId) == true) {
                    currentAnswer.selectedAnswerIds - answerId
                } else {
                    listOf(answerId) // Single selection for most types
                }
                SubmitAnswer(questionId, selectedIds)
            }
        }

        _state.value = _state.value.copy(
            answers = _state.value.answers + (questionId to newAnswer)
        )
    }

    fun setDragDropMatch(questionId: String, answerId: String, matchTarget: String) {
        val currentAnswer = _state.value.answers[questionId] ?: SubmitAnswer(questionId)
        val newMatches = (currentAnswer.dragDropMatches ?: emptyMap()) + (answerId to matchTarget)

        val newAnswer = currentAnswer.copy(dragDropMatches = newMatches)
        _state.value = _state.value.copy(
            answers = _state.value.answers + (questionId to newAnswer)
        )
    }

    fun goToNextQuestion() {
        val currentIndex = _state.value.currentQuestionIndex
        val questionsCount = _state.value.test?.questions?.size ?: 0

        if (currentIndex < questionsCount - 1) {
            _state.value = _state.value.copy(currentQuestionIndex = currentIndex + 1)
        }
    }

    fun goToPreviousQuestion() {
        val currentIndex = _state.value.currentQuestionIndex
        if (currentIndex > 0) {
            _state.value = _state.value.copy(currentQuestionIndex = currentIndex - 1)
        }
    }

    fun goToQuestion(index: Int) {
        val questionsCount = _state.value.test?.questions?.size ?: 0
        if (index in 0 until questionsCount) {
            _state.value = _state.value.copy(currentQuestionIndex = index)
        }
    }

    fun submitTest() {
        val test = _state.value.test ?: return

        timerJob?.cancel()

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null)

            val request = SubmitTestRequest(
                testId = test.id,
                answers = _state.value.answers.values.toList(),
                timeSpentSeconds = _state.value.timeElapsed
            )

            api.submitTest(test.id, request)
                .onSuccess { result ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        result = result
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        error = error.message
                    )
                }
        }
    }

    fun resetTest() {
        timerJob?.cancel()
        _state.value = TestPlayState()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
