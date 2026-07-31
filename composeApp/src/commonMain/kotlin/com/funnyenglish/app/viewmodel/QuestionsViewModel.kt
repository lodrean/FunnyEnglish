package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.SpeakingQuestion
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Спека Part 2 §2.4 */
data class QuestionsState(
    val isLoading: Boolean = false,
    val topicTitle: String = "",
    val questions: List<SpeakingQuestion> = emptyList(),
    val isGuest: Boolean = false,          // Practice заблокирован для гостя (PRD Story 3)
    val error: String? = null
)

sealed interface QuestionsAction {
    data class OnLoad(val topicId: String, val isGuest: Boolean) : QuestionsAction
    data object OnStartTraining : QuestionsAction
    data object OnStartPractice : QuestionsAction           // гость → событие ShowLoginCta
    data object OnBack : QuestionsAction
}

sealed interface QuestionsEvent {
    data class NavigateToTraining(val topicId: String) : QuestionsEvent
    data class NavigateToPractice(val topicId: String) : QuestionsEvent
    data object ShowLoginCta : QuestionsEvent               // диалог «Войти/Зарегистрироваться»
    data object NavigateBack : QuestionsEvent
}

class QuestionsViewModel(
    private val api: FunnyEnglishApi
) : ViewModel() {

    private val _state = MutableStateFlow(QuestionsState())
    val state: StateFlow<QuestionsState> = _state.asStateFlow()

    private val _events = Channel<QuestionsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentTopicId: String? = null

    fun onAction(action: QuestionsAction) {
        when (action) {
            is QuestionsAction.OnLoad -> {
                currentTopicId = action.topicId
                _state.value = _state.value.copy(isGuest = action.isGuest)
                load(action.topicId)
            }
            is QuestionsAction.OnStartTraining -> currentTopicId?.let {
                _events.trySend(QuestionsEvent.NavigateToTraining(it))
            }
            is QuestionsAction.OnStartPractice -> {
                if (_state.value.isGuest) {
                    _events.trySend(QuestionsEvent.ShowLoginCta)
                } else {
                    currentTopicId?.let { _events.trySend(QuestionsEvent.NavigateToPractice(it)) }
                }
            }
            is QuestionsAction.OnBack -> _events.trySend(QuestionsEvent.NavigateBack)
        }
    }

    private fun load(topicId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            api.getSpeakingTopicDetail(topicId)
                .onSuccess { detail ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        topicTitle = detail.title,
                        questions = detail.questions.sortedBy { it.displayOrder }
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка загрузки"
                    )
                }
        }
    }
}
