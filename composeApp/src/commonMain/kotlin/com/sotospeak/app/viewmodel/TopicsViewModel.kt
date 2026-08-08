package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.app.storage.RecordingStore
import com.sotospeak.shared.api.SoToSpeakApi
import com.sotospeak.shared.platform.Settings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Спека Part 2 §2.2 */
data class TopicsState(
    val isLoading: Boolean = false,
    val libraryTitle: String = "",
    val topics: List<TopicUiModel> = emptyList(),
    val error: String? = null
)

/** UI-модель топика: DTO + локальный прогресс из Settings (просмотрен / есть training-записи) */
data class TopicUiModel(
    val id: String,
    val title: String,
    val durationSeconds: Int,
    val questionCount: Int,
    val hasSubtitles: Boolean,          // из DTO — иначе выбор «с субтитрами» скрыт
    val isWatched: Boolean,             // локальный флаг (Settings, ключ topic_watched_<id>)
    val hasLocalRecordings: Boolean     // есть training-записи в RecordingStore
)

sealed interface TopicsAction {
    data class OnLoad(val libraryId: String) : TopicsAction
    data object OnRefresh : TopicsAction
    data class OnTopicClick(val topicId: String) : TopicsAction        // сразу → видео (DC-5)
    data object OnBack : TopicsAction
}

sealed interface TopicsEvent {
    data class NavigateToVideo(val topicId: String, val withSubtitles: Boolean) : TopicsEvent
    data class NavigateToQuestions(val topicId: String) : TopicsEvent
    data object NavigateBack : TopicsEvent
}

class TopicsViewModel(
    private val api: SoToSpeakApi,
    private val recordingStore: RecordingStore,
    private val settings: Settings
) : ViewModel() {

    private val _state = MutableStateFlow(TopicsState())
    val state: StateFlow<TopicsState> = _state.asStateFlow()

    private val _events = Channel<TopicsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentLibraryId: String? = null

    fun onAction(action: TopicsAction) {
        when (action) {
            is TopicsAction.OnLoad -> {
                currentLibraryId = action.libraryId
                load(action.libraryId)
            }
            is TopicsAction.OnRefresh -> currentLibraryId?.let { load(it) }
            is TopicsAction.OnTopicClick -> {
                // DC-5 (V2): bottom-sheet выбора субтитров убран — сразу видео;
                // режим переключается чипами на экране видео (мокап frame-video)
                val topic = _state.value.topics.firstOrNull { it.id == action.topicId }
                _events.trySend(
                    TopicsEvent.NavigateToVideo(action.topicId, topic?.hasSubtitles == true)
                )
            }
            is TopicsAction.OnBack -> _events.trySend(TopicsEvent.NavigateBack)
        }
    }

    private fun load(libraryId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            api.getSpeakingTopics(libraryId)
                .onSuccess { topics ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        topics = topics.map { dto ->
                            TopicUiModel(
                                id = dto.id,
                                title = dto.title,
                                durationSeconds = dto.durationSeconds ?: 0,
                                questionCount = dto.questionCount,
                                hasSubtitles = dto.hasSubtitles,
                                isWatched = settings.getString("topic_watched_${dto.id}", null) == "true",
                                hasLocalRecordings = recordingStore.list(dto.id).isNotEmpty()
                            )
                        }
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
