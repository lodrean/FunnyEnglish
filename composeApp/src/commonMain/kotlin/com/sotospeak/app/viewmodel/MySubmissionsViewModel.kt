package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.app.data.PracticeRetryService
import com.sotospeak.app.data.SpeakingRepository
import com.sotospeak.app.error.UiText
import com.sotospeak.app.error.toUiText
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

/** Фильтр списка отправок (bd h3l.9, §4.3.5) */
enum class SubmissionsFilter { ALL, NEW, REVIEWED }

/** Прогресс по теме: первый и последний суммарный балл оценённых попыток (bd h3l.9, §4.1.2). */
data class TopicProgress(
    val topicId: String,
    val topicTitle: String,
    val firstTotal: Double,
    val lastTotal: Double
)

/** Спека Part 2 §2.7 */
data class MySubmissionsState(
    val isLoading: Boolean = false,
    val submissions: List<SpeakingSubmission> = emptyList(),  // новые сверху
    val pendingUploads: List<RecordingMeta> = emptyList(),    // локальные неотправленные (offline retry)
    val playingAudioUrl: String? = null,
    val error: UiText? = null,
    val filter: SubmissionsFilter = SubmissionsFilter.ALL,
    /** Темы с ≥2 оценёнными попытками: 6.2 → 7.1 (новые сверху → первая попытка слева). */
    val topicsProgress: List<TopicProgress> = emptyList()
)

sealed interface MySubmissionsAction {
    data object OnRefresh : MySubmissionsAction
    data class OnRetryPending(val path: String) : MySubmissionsAction
    data class OnPlayAudio(val url: String) : MySubmissionsAction
    data object OnStopAudio : MySubmissionsAction
    data class OnFilterChange(val filter: SubmissionsFilter) : MySubmissionsAction
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
            is MySubmissionsAction.OnFilterChange -> _state.value = _state.value.copy(filter = action.filter)
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
                    val sorted = submissions.sortedByDescending { it.createdAt }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        submissions = sorted,
                        topicsProgress = computeTopicsProgress(sorted),
                        pendingUploads = repository.pendingPracticeUploads()
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        pendingUploads = repository.pendingPracticeUploads(),
                        error = error.toUiText()
                    )
                }
        }
    }

    /**
     * Прогресс по темам (bd h3l.9, §4.1.2): темы с ≥2 оценёнными попытками,
     * первый → последний балл. Список приходит «новые сверху», поэтому хронология — реверс.
     */
    private fun computeTopicsProgress(submissions: List<SpeakingSubmission>): List<TopicProgress> =
        submissions
            .filter { it.grade != null }
            .groupBy { it.topicId }
            .mapNotNull { (topicId, sameTopic) ->
                if (sameTopic.size < 2) return@mapNotNull null
                val chronological = sameTopic.sortedWith { a, b -> a.createdAt.orEmpty().compareTo(b.createdAt.orEmpty()) }
                TopicProgress(
                    topicId = topicId,
                    topicTitle = chronological.first().topicTitle,
                    firstTotal = chronological.first().grade!!.total,
                    lastTotal = chronological.last().grade!!.total
                )
            }

    private val retryService = PracticeRetryService(repository, fileStorage)

    private val inFlightUploads = mutableSetOf<String>()

    private fun retryPending(filePath: String) {
        // M3-фикс (review): in-flight guard против дублей (OnRefresh + ручной retry)
        if (!inFlightUploads.add(filePath)) return
        viewModelScope.launch {
            val sent = retryService.retryOne(filePath)
            inFlightUploads.remove(filePath)
            _state.value = _state.value.copy(pendingUploads = repository.pendingPracticeUploads())
            if (sent) {
                _events.trySend(MySubmissionsEvent.ShowMessage("Запись отправлена учителю"))
                repository.getMySubmissions().onSuccess { submissions ->
                    _state.value = _state.value.copy(
                        submissions = submissions.sortedByDescending { it.createdAt },
                        topicsProgress = computeTopicsProgress(submissions.sortedByDescending { it.createdAt })
                    )
                }
            }
        }
    }

    override fun onCleared() {
        audioPlayer.stop()
        audioPlayer.release()
        super.onCleared()
    }
}
