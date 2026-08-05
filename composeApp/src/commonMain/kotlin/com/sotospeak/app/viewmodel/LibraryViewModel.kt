package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.app.storage.RecordingKind
import com.sotospeak.app.storage.RecordingStore
import com.sotospeak.shared.api.SoToSpeakApi
import com.sotospeak.shared.model.SpeakingLibrary
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Спека Part 2 §2.1 */
data class LibraryState(
    val isLoading: Boolean = false,
    val libraries: List<SpeakingLibrary> = emptyList(),
    /** libraryId → число топиков с training-записями (DC-2: бейдж «N пройдено», прогресс-бар) */
    val completedTopics: Map<String, Int> = emptyMap(),
    val error: String? = null
)

sealed interface LibraryAction {
    data object OnRefresh : LibraryAction
    data class OnLibraryClick(val libraryId: String) : LibraryAction
    data object OnClearError : LibraryAction
}

sealed interface LibraryEvent {
    data class NavigateToTopics(val libraryId: String) : LibraryEvent
}

class LibraryViewModel(
    private val api: SoToSpeakApi,
    private val recordingStore: RecordingStore
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private val _events = Channel<LibraryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.OnRefresh -> load()
            is LibraryAction.OnLibraryClick ->
                _events.trySend(LibraryEvent.NavigateToTopics(action.libraryId))
            is LibraryAction.OnClearError ->
                _state.value = _state.value.copy(error = null)
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            api.getSpeakingLibraries()
                .onSuccess { libraries ->
                    // Пустые темы фильтруем на клиенте как страховку (backend тоже фильтрует)
                    val visible = libraries.filter { it.topicCount > 0 }
                    _state.value = _state.value.copy(isLoading = false, libraries = visible)
                    loadProgress(visible)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка загрузки"
                    )
                }
        }
    }

    /**
     * DC-2: прогресс по темам — топики с хотя бы одной TRAINING-записью (RecordingStore).
     * Топики грузим параллельно по каждой библиотеке; ошибки не роняют экран — бейджи/бар скрыты.
     */
    private fun loadProgress(libraries: List<SpeakingLibrary>) {
        viewModelScope.launch {
            val completed = coroutineScope {
                libraries.map { library ->
                    async {
                        val done = api.getSpeakingTopics(library.id)
                            .getOrNull()
                            ?.count { topic ->
                                recordingStore.list(topic.id).any { it.kind == RecordingKind.TRAINING }
                            } ?: 0
                        library.id to done
                    }
                }.awaitAll().toMap()
            }
            _state.value = _state.value.copy(completedTopics = completed)
        }
    }
}
