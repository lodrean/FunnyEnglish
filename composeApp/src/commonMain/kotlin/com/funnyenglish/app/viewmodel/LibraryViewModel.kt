package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.SpeakingLibrary
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Спека Part 2 §2.1 */
data class LibraryState(
    val isLoading: Boolean = false,
    val libraries: List<SpeakingLibrary> = emptyList(),
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
    private val api: FunnyEnglishApi
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
                    _state.value = _state.value.copy(
                        isLoading = false,
                        // Пустые темы фильтруем на клиенте как страховку (backend тоже фильтрует)
                        libraries = libraries.filter { it.topicCount > 0 }
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
