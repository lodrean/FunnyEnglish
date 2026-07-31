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

/**
 * Состояние экрана Audio Test
 */
data class AudioTestScreenState(
    val isLoading: Boolean = false,
    val audioTest: AudioTestDetail? = null,
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(), // questionId -> selectedOptionId
    val playsUsed: Int = 0,
    val isPlaying: Boolean = false,
    val result: SubmitAudioTestResult? = null,
    val error: String? = null,
    val timeSpentSeconds: Int = 0
)

class AudioTestViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _state = MutableStateFlow(AudioTestScreenState())
    val state: StateFlow<AudioTestScreenState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var hasPlayStarted = false
    private var isGuestSession: Boolean = false

    fun loadAudioTest(audioTestId: String, isGuest: Boolean = false) {
        isGuestSession = isGuest
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            api.getAudioTestById(audioTestId)
                .onSuccess { audioTest ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        audioTest = audioTest
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
                val currentTime = _state.value.timeSpentSeconds + 1
                _state.value = _state.value.copy(timeSpentSeconds = currentTime)

                // Audio tests track play count, not time limit
            }
        }
    }

    fun onPlayStarted() {
        if (!hasPlayStarted) {
            hasPlayStarted = true
            _state.value = _state.value.copy(
                playsUsed = _state.value.playsUsed + 1,
                isPlaying = true
            )
        }
    }

    fun onPlayCompleted() {
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun selectAnswer(questionId: String, optionId: String) {
        _state.value = _state.value.copy(
            answers = _state.value.answers + (questionId to optionId)
        )
    }

    fun goToNextQuestion() {
        val currentIndex = _state.value.currentQuestionIndex
        val questionsCount = _state.value.audioTest?.questions?.size ?: 0

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
        val questionsCount = _state.value.audioTest?.questions?.size ?: 0
        if (index in 0 until questionsCount) {
            _state.value = _state.value.copy(currentQuestionIndex = index)
        }
    }

    fun submitTest() {
        val audioTest = _state.value.audioTest ?: return

        // Check if all questions are answered
        if (_state.value.answers.size < audioTest.questions.size) {
            _state.value = _state.value.copy(
                error = "Ответьте на все вопросы перед завершением"
            )
            return
        }

        timerJob?.cancel()

        if (isGuestSession) {
            val result = calculateLocalResult(audioTest, _state.value.answers)
            _state.value = _state.value.copy(result = result)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val answers = audioTest.questions.map { question ->
                SubmitAudioAnswerRequest(
                    questionId = question.id,
                    selectedAnswerIds = _state.value.answers[question.id]?.let { listOf(it) } ?: emptyList()
                )
            }

            val request = SubmitAudioTestRequest(
                audioTestId = audioTest.id,
                answers = answers,
                timeSpentSeconds = _state.value.timeSpentSeconds
            )

            api.submitAudioTest(request)
                .onSuccess { result ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        result = result
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    private fun calculateLocalResult(
        audioTest: AudioTestDetail,
        answers: Map<String, String>
    ): SubmitAudioTestResult {
        var score = 0
        var maxScore = 0
        audioTest.questions.forEach { question ->
            maxScore += question.points
            val selectedAnswerId = answers[question.id]
            val isCorrect = question.answers.any { it.id == selectedAnswerId && it.isCorrect }
            if (isCorrect) score += question.points
        }
        val percentage = if (maxScore > 0) (score * 100) / maxScore else 0
        val stars = when {
            percentage >= 95 -> 3
            percentage >= 80 -> 2
            percentage >= 60 -> 1
            else -> 0
        }
        return SubmitAudioTestResult(
            score = score,
            maxScore = maxScore,
            percentage = percentage,
            stars = stars,
            pointsEarned = 0,
            isNewBestScore = false,
            levelUp = null,
            newAchievements = emptyList()
        )
    }

    fun resetTest() {
        timerJob?.cancel()
        hasPlayStarted = false
        _state.value = AudioTestScreenState()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
