package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.app.recorder.MicPermissionState
import com.sotospeak.app.storage.RecordingKind
import com.sotospeak.app.storage.RecordingMeta
import com.sotospeak.app.storage.RecordingStore
import com.sotospeak.shared.api.SoToSpeakApi
import com.sotospeak.shared.model.SpeakingQuestion
import com.sotospeak.shared.platform.AudioPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Спека Part 2 §2.5. Training: 3 попытки на топик (80/50/30), каждая попытка —
 * одна запись на ВСЕ вопросы; без удаления — только прослушивание, авто-✅.
 */
data class TrainingState(
    val isLoading: Boolean = false,
    val topicTitle: String = "",
    val questions: List<SpeakingQuestion> = emptyList(),   // весь список виден на экране
    val attempts: List<RecordingMeta> = emptyList(),       // попытки топика (макс. 3)
    val recorder: RecorderUiState = RecorderUiState.Idle,
    val attemptNumber: Int = 1,                            // 1..3; лимит = timerLimitFor(attemptNumber)
    val remainingSeconds: Int = 0,                         // видимый обратный отсчёт
    val isFinished: Boolean = false,                       // true после 3-й попытки → финальные CTA
    val playingRecordingPath: String? = null,
    val micPermission: MicPermissionState = MicPermissionState.Unknown,
    val error: String? = null
)


sealed interface RecorderUiState {
    data object Idle : RecorderUiState
    data object RequestingPermission : RecorderUiState
    data class Recording(val startedAtMs: Long) : RecorderUiState
    data object Saving : RecorderUiState
    data class Error(val message: String) : RecorderUiState       // микрофон занят / нет места
}

sealed interface TrainingAction {
    data class OnLoad(val topicId: String) : TrainingAction
    data object OnStartRecording : TrainingAction
    data object OnStopRecording : TrainingAction                 // досрочный стоп — попытка засчитывается
    /** Экран подтверждает: VoiceRecorder реально начал запись → старт таймера */
    data object OnRecorderStarted : TrainingAction
    /** Экран сообщает: VoiceRecorder.state = Stopped(filePath) → сохранить попытку */
    data class OnRecorderStopped(val filePath: String) : TrainingAction
    data class OnRecorderError(val message: String) : TrainingAction
    data class OnPlayRecording(val path: String) : TrainingAction
    data object OnStopPlayback : TrainingAction
    // удаления/перезаписи попыток НЕТ (дизайн v1.1): записи только прослушиваются, ✅ автоматически
    data object OnGoToPractice : TrainingAction                  // финальный CTA «Перейти к практике»
    data object OnRestartAttempts : TrainingAction               // «Начать заново с попытки 1»
    data object OnInterruption : TrainingAction                  // звонок/сворачивание → автостоп, попытка засчитывается
    data class OnPermissionResult(val granted: Boolean) : TrainingAction
    data class OnPermissionState(val state: MicPermissionState) : TrainingAction
    data object OnBackToLibrary : TrainingAction
    data object OnBack : TrainingAction
}

sealed interface TrainingEvent {
    data object NavigateToLibrary : TrainingEvent
    data class NavigateToPractice(val topicId: String) : TrainingEvent
    data object NavigateBack : TrainingEvent
    data class ShowMessage(val text: String) : TrainingEvent       // snackbar
}

class TrainingViewModel(
    private val api: SoToSpeakApi,
    private val recordingStore: RecordingStore,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _state = MutableStateFlow(TrainingState())
    val state: StateFlow<TrainingState> = _state.asStateFlow()

    private val _events = Channel<TrainingEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentTopicId: String? = null
    private var timerJob: Job? = null

    init {
        audioPlayer.setOnCompletionListener {
            _state.value = _state.value.copy(playingRecordingPath = null)
        }
    }

    fun onAction(action: TrainingAction) {
        when (action) {
            is TrainingAction.OnLoad -> {
                currentTopicId = action.topicId
                load(action.topicId)
            }
            is TrainingAction.OnStartRecording -> {
                if (_state.value.isFinished) return
                if (_state.value.recorder is RecorderUiState.Recording) return
                _state.value = _state.value.copy(recorder = RecorderUiState.RequestingPermission)
            }
            is TrainingAction.OnRecorderStarted -> {
                val limit = timerLimitFor(_state.value.attemptNumber)
                _state.value = _state.value.copy(
                    recorder = RecorderUiState.Recording(Clock.System.now().toEpochMilliseconds()),
                    remainingSeconds = limit
                )
                startTimer(limit)
            }
            is TrainingAction.OnStopRecording, is TrainingAction.OnInterruption -> {
                // Экран останавливает VoiceRecorder (stop, НЕ cancel — попытка засчитывается);
                // сохранение произойдёт в OnRecorderStopped
                if (action is TrainingAction.OnInterruption &&
                    _state.value.recorder is RecorderUiState.Recording
                ) {
                    _events.trySend(TrainingEvent.ShowMessage("Запись остановлена и сохранена"))
                }
            }
            is TrainingAction.OnRecorderStopped -> saveAttempt(action.filePath)
            is TrainingAction.OnRecorderError -> {
                stopTimer()
                _state.value = _state.value.copy(
                    recorder = RecorderUiState.Error(action.message)
                )
            }
            is TrainingAction.OnPlayRecording -> {
                audioPlayer.stop()
                audioPlayer.play(action.path)
                _state.value = _state.value.copy(playingRecordingPath = action.path)
            }
            is TrainingAction.OnStopPlayback -> {
                audioPlayer.stop()
                _state.value = _state.value.copy(playingRecordingPath = null)
            }
            is TrainingAction.OnGoToPractice -> currentTopicId?.let {
                _events.trySend(TrainingEvent.NavigateToPractice(it))
            }
            is TrainingAction.OnRestartAttempts -> {
                currentTopicId?.let { recordingStore.removeAllForTopic(it) }
                _state.value = _state.value.copy(
                    attempts = emptyList(),
                    attemptNumber = 1,
                    isFinished = false,
                    recorder = RecorderUiState.Idle
                )
            }
            is TrainingAction.OnPermissionResult -> {
                _state.value = _state.value.copy(
                    micPermission = if (action.granted) MicPermissionState.Granted
                    else MicPermissionState.Denied,
                    recorder = if (action.granted) _state.value.recorder else RecorderUiState.Idle
                )
            }
            is TrainingAction.OnPermissionState ->
                _state.value = _state.value.copy(micPermission = action.state)
            is TrainingAction.OnBackToLibrary -> _events.trySend(TrainingEvent.NavigateToLibrary)
            is TrainingAction.OnBack -> _events.trySend(TrainingEvent.NavigateBack)
        }
    }

    private fun load(topicId: String) {
        viewModelScope.launch {
            stopTimer()
            // B1-фикс (review): повторный вход на экран — VoiceRecorder пересоздан экраном,
            // поэтому застрявший recorder-state (Recording/Saving после ухода во время
            // записи или поворота) сбрасываем в Idle; попытки перечитываем из store.
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                recorder = RecorderUiState.Idle,
                remainingSeconds = 0,
                playingRecordingPath = null
            )
            api.getSpeakingTopicDetail(topicId)
                .onSuccess { detail ->
                    val attempts = recordingStore.list(topicId)
                        .filter { it.kind == RecordingKind.TRAINING }
                        .sortedBy { it.attemptNumber }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        topicTitle = detail.title,
                        questions = detail.questions.sortedBy { it.displayOrder },
                        attempts = attempts,
                        attemptNumber = (attempts.size + 1).coerceAtMost(MAX_ATTEMPTS),
                        isFinished = attempts.size >= MAX_ATTEMPTS
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

    private fun startTimer(limitSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = limitSeconds
            while (isActive && remaining > 0) {
                delay(1000)
                remaining--
                _state.value = _state.value.copy(remainingSeconds = remaining)
            }
            // Таймер истёк → экран увидит remainingSeconds == 0 и остановит VoiceRecorder
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun saveAttempt(filePath: String) {
        stopTimer()
        val topicId = currentTopicId ?: return
        val s = _state.value
        val startedAt = (s.recorder as? RecorderUiState.Recording)?.startedAtMs
        val now = Clock.System.now().toEpochMilliseconds()
        val durationMs = if (startedAt != null) (now - startedAt).coerceAtLeast(0) else 0L
        val limit = timerLimitFor(s.attemptNumber)

        _state.value = s.copy(recorder = RecorderUiState.Saving)

        val meta = RecordingMeta(
            filePath = filePath,
            topicId = topicId,
            attemptNumber = s.attemptNumber,
            kind = RecordingKind.TRAINING,
            durationMs = durationMs,
            timerLimitSeconds = limit,
            createdAtEpochMs = now
        )
        recordingStore.add(meta)

        val attempts = (s.attempts + meta).sortedBy { it.attemptNumber }
        val finished = attempts.size >= MAX_ATTEMPTS
        _state.value = _state.value.copy(
            attempts = attempts,
            attemptNumber = (attempts.size + 1).coerceAtMost(MAX_ATTEMPTS),
            isFinished = finished,
            recorder = RecorderUiState.Idle,
            remainingSeconds = 0
        )
        _events.trySend(
            TrainingEvent.ShowMessage(
                if (finished) "Третья попытка принята! Молодец!" else "Попытка принята ✅"
            )
        )
    }

    override fun onCleared() {
        stopTimer()
        audioPlayer.stop()
        audioPlayer.release()
        super.onCleared()
    }

    companion object {
        const val MAX_ATTEMPTS = 3

        /**
         * Лимит попытки (PRD Story 4, дизайн v1.1):
         *   попытка 1 → 80с, попытка 2 → 50с, попытка 3 → 30с.
         */
        fun timerLimitFor(attemptNumber: Int): Int = when (attemptNumber) {
            1 -> 80
            2 -> 50
            else -> 30
        }
    }
}
