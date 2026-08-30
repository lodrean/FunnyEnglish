package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.app.data.SpeakingRepository
import com.sotospeak.app.storage.RecordingFileStorage
import com.sotospeak.app.storage.RecordingMeta
import com.sotospeak.shared.contracts.SpeakingSubmission
import com.sotospeak.shared.platform.AudioPlayer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Спека Part 2 §2.7 */
data class MySubmissionsState(
    val isLoading: Boolean = false,
    val submissions: List<SpeakingSubmission> = emptyList(),  // новые сверху
    val pendingUploads: List<RecordingMeta> = emptyList(),    // локальные неотправленные (offline retry)
    val playingAudioUrl: String? = null,
    val error: String? = null
)

sealed interface MySubmissionsAction {
    data object OnRefresh : MySubmissionsAction
    data class OnRetryPending(val path: String) : MySubmissionsAction
    data class OnPlayAudio(val url: String) : MySubmissionsAction
    data object OnStopAudio : MySubmissionsAction
    data object OnBack : MySubmissionsAction
}

sealed interface MySubmissionsEvent {
    data object NavigateBack : MySubmissionsEvent
    data class ShowMessage(val text: String) : MySubmissionsEvent
}

class MySubmissionsViewModel(
    private val repository: SpeakingRepository,
    private val fileStorage: RecordingFileStorage,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _state = MutableStateFlow(MySubmissionsState())
    val state: StateFlow<MySubmissionsState> = _state.asStateFlow()

    private val _events = Channel<MySubmissionsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        audioPlayer.setOnCompletionListener {
            _state.value = _state.value.copy(playingAudioUrl = null)
        }
    }

    fun onAction(action: MySubmissionsAction) {
        when (action) {
            is MySubmissionsAction.OnRefresh -> refresh()
            is MySubmissionsAction.OnRetryPending -> retryPending(action.path)
            is MySubmissionsAction.OnPlayAudio -> {
                audioPlayer.stop()
                audioPlayer.play(action.url)
                _state.value = _state.value.copy(playingAudioUrl = action.url)
            }
            is MySubmissionsAction.OnStopAudio -> {
                audioPlayer.stop()
                _state.value = _state.value.copy(playingAudioUrl = null)
            }
            is MySubmissionsAction.OnBack -> _events.trySend(MySubmissionsEvent.NavigateBack)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            // Автоматический retry неотправленных при входе на экран (спека §6.4)
            repository.pendingPracticeUploads().forEach { retryPending(it.filePath) }
            repository.getMySubmissions()
                .onSuccess { submissions ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        submissions = submissions.sortedByDescending { it.createdAt },
                        pendingUploads = repository.pendingPracticeUploads()
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        pendingUploads = repository.pendingPracticeUploads(),
                        error = error.message ?: "Ошибка загрузки"
                    )
                }
        }
    }

    private val inFlightUploads = mutableSetOf<String>()

    private fun retryPending(filePath: String) {
        // M3-фикс (review): без in-flight guard быстрые повторные вызовы (OnRefresh +
        // ручной retry) создавали дубли submissions на backend
        if (!inFlightUploads.add(filePath)) return
        val meta = repository.findRecording(filePath) ?: run {
            inFlightUploads.remove(filePath)
            return
        }
        viewModelScope.launch {
            try {
                val bytes = try {
                    fileStorage.readBytes(filePath)
                } catch (e: Exception) {
                    _events.trySend(MySubmissionsEvent.ShowMessage("Файл записи не найден"))
                    repository.removeRecording(filePath)   // файла нет — чистим мету
                    return@launch
                }
                repository.submitPractice(
                    topicId = meta.topicId,
                    durationSec = (meta.durationMs / 1000).toInt().coerceAtLeast(1),
                    audioBytes = bytes,
                    fileName = filePath.substringAfterLast('/')
                )
                    .onSuccess {
                        repository.markRecordingUploaded(filePath)
                        repository.removeRecording(filePath)   // уже в MinIO — освобождаем место
                        _state.value = _state.value.copy(
                            pendingUploads = repository.pendingPracticeUploads()
                        )
                        _events.trySend(MySubmissionsEvent.ShowMessage("Запись отправлена учителю"))
                        // Обновим список — новая отправка должна появиться
                        repository.getMySubmissions().onSuccess { submissions ->
                            _state.value = _state.value.copy(
                                submissions = submissions.sortedByDescending { it.createdAt }
                            )
                        }
                    }
                    .onFailure {
                        _state.value = _state.value.copy(
                            pendingUploads = repository.pendingPracticeUploads()
                        )
                    }
            } finally {
                inFlightUploads.remove(filePath)
            }
        }
    }

    override fun onCleared() {
        audioPlayer.stop()
        audioPlayer.release()
        super.onCleared()
    }
}
