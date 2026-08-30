package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.app.data.SpeakingRepository
import com.sotospeak.app.recorder.MicPermissionState
import com.sotospeak.app.recorder.RecordingSessionController
import com.sotospeak.app.storage.RecordingFileStorage
import com.sotospeak.app.storage.RecordingKind
import com.sotospeak.app.storage.RecordingMeta
import com.sotospeak.shared.api.ApiException
import com.sotospeak.shared.api.TokenProvider
import com.sotospeak.shared.model.SpeakingQuestion
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Спека Part 2 §2.6. Practice: 30 секунд, один тейк на ВСЕ вопросы.
 * Дизайн v1.0: фазы Review и ручной отправки НЕТ — после остановки запись уходит автоматически.
 */
data class PracticeState(
    val isLoading: Boolean = false,
    val topicTitle: String = "",
    val questions: List<SpeakingQuestion> = emptyList(),   // списком — отвечать на все
    val phase: PracticePhase = PracticePhase.Ready,
    val remainingSeconds: Int = PRACTICE_LIMIT_SECONDS,
    val takeFilePath: String? = null,                      // единственный тейк (файл для upload)
    val uploadProgress: Int = 0,                           // 0..100 для панели «Отправка учителю…»
    val uploadError: Boolean = false,                      // retry; файл не теряется (PRD Story 5)
    val micPermission: MicPermissionState = MicPermissionState.Unknown,
    val hasSubmitted: Boolean = false,                     // уже есть успешная отправка по топику
    val error: String? = null
) {
    companion object {
        const val PRACTICE_LIMIT_SECONDS = 30
        const val PRACTICE_MIN_DURATION_MS = 5000L // 5 сек — минимальная длительность для отправки
    }
}

enum class PracticePhase { Ready, Recording, Uploading, Sent }

sealed interface PracticeAction {
    data class OnLoad(val topicId: String) : PracticeAction
    data object OnStart : PracticeAction                       // запуск записи + таймер 30с
    data object OnStopEarly : PracticeAction                   // ручная остановка → немедленная автоотправка
    /** Экран подтверждает: VoiceRecorder начал запись */
    data object OnRecorderStarted : PracticeAction
    /** Экран сообщает: VoiceRecorder.state = Stopped(filePath) → автоотправка */
    data class OnRecorderStopped(val filePath: String) : PracticeAction
    data class OnRecorderError(val message: String) : PracticeAction
    data object OnRetryUpload : PracticeAction
    data object OnInterruption : PracticeAction                // автостоп → автоотправка
    data class OnPermissionResult(val granted: Boolean) : PracticeAction
    data class OnPermissionState(val state: MicPermissionState) : PracticeAction
    data object OnBack : PracticeAction                        // заблокирован в Recording/Uploading (диалог)
}

sealed interface PracticeEvent {
    data object NavigateToMySubmissions : PracticeEvent        // после успешной отправки
    data object NavigateBack : PracticeEvent
    data class ShowMessage(val text: String) : PracticeEvent
}

class PracticeViewModel(
    private val repository: SpeakingRepository,
    private val fileStorage: RecordingFileStorage,
    private val tokenProvider: TokenProvider
) : ViewModel() {

    private val _state = MutableStateFlow(PracticeState())
    val state: StateFlow<PracticeState> = _state.asStateFlow()

    private val _events = Channel<PracticeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentTopicId: String? = null
    // Общий с Training механизм: таймер 30с + длительность записи (bd FunnyEnglish-5tf.5)
    private val recordingSession = RecordingSessionController(viewModelScope)

    fun onAction(action: PracticeAction) {
        when (action) {
            is PracticeAction.OnLoad -> {
                currentTopicId = action.topicId
                load(action.topicId)
            }
            is PracticeAction.OnStart -> {
                // Дополнительная защита гейтинга (спека §6.2): backend тоже ограничивает
                if (tokenProvider.getToken() == null) {
                    _events.trySend(PracticeEvent.ShowMessage("Требуется вход"))
                    return
                }
                if (_state.value.phase != PracticePhase.Ready) return
                if (_state.value.hasSubmitted) {
                    _events.trySend(PracticeEvent.ShowMessage("Вы уже отправляли ответ по этой теме"))
                    return
                }
                // Экран: permission → recorder.start → OnRecorderStarted
                _state.value = _state.value.copy(phase = PracticePhase.Recording)
            }
            is PracticeAction.OnRecorderStarted -> {
                recordingSession.markRecordingStarted()
                recordingSession.startTimer(PracticeState.PRACTICE_LIMIT_SECONDS) { remaining ->
                    _state.value = _state.value.copy(remainingSeconds = remaining)
                }
            }
            is PracticeAction.OnStopEarly, is PracticeAction.OnInterruption -> {
                // Экран останавливает VoiceRecorder → OnRecorderStopped → автоотправка
            }
            is PracticeAction.OnRecorderStopped -> {
                recordingSession.stopTimer()
                val path = action.filePath
                val durationMs = recordingSession.elapsedMs()

                // Короткие записи не считаются попыткой — даём перезаписать
                if (durationMs < PracticeState.PRACTICE_MIN_DURATION_MS) {
                    fileStorage.delete(path) // освобождаем место
                    _state.value = _state.value.copy(
                        phase = PracticePhase.Ready,
                        takeFilePath = null,
                        error = null
                    )
                    _events.trySend(PracticeEvent.ShowMessage("Запись слишком короткая — попробуйте ещё раз"))
                    return
                }

                val meta = RecordingMeta(
                    filePath = path,
                    topicId = currentTopicId.orEmpty(),
                    attemptNumber = 0,
                    kind = RecordingKind.PRACTICE,
                    durationMs = durationMs,
                    timerLimitSeconds = PracticeState.PRACTICE_LIMIT_SECONDS,
                    createdAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                    uploaded = false
                )
                repository.addRecording(meta)
                _state.value = _state.value.copy(takeFilePath = path)
                upload(path, (durationMs / 1000).toInt().coerceAtLeast(1))
            }
            is PracticeAction.OnRecorderError -> {
                recordingSession.stopTimer()
                _state.value = _state.value.copy(
                    phase = PracticePhase.Ready,
                    error = action.message
                )
            }
            is PracticeAction.OnRetryUpload -> {
                val path = _state.value.takeFilePath ?: return
                val durationSec = repository.findRecording(path)
                    ?.let { (it.durationMs / 1000).toInt().coerceAtLeast(1) } ?: 1
                upload(path, durationSec)
            }
            is PracticeAction.OnPermissionResult -> {
                _state.value = _state.value.copy(
                    micPermission = if (action.granted) MicPermissionState.Granted
                    else MicPermissionState.Denied,
                    phase = if (action.granted) _state.value.phase else PracticePhase.Ready
                )
            }
            is PracticeAction.OnPermissionState ->
                _state.value = _state.value.copy(micPermission = action.state)
            is PracticeAction.OnBack -> {
                // В фазах Recording/Uploading экран показывает диалог-подтверждение;
                // сюда попадаем только при реальном уходе
                _events.trySend(PracticeEvent.NavigateBack)
            }
        }
    }

    private fun load(topicId: String) {
        viewModelScope.launch {
            // B2-фикс (review): повторный вход на Practice — полный сброс фазовой машины,
            // иначе после Sent экран навсегда показывал SentPhase; а после ухода во время
            // записи (B1: VoiceRecorder уже release'нут экраном) — кирпич «Recording».
            recordingSession.reset()
            _state.value = PracticeState(
                isLoading = true,
                micPermission = _state.value.micPermission
            )
            val detailDeferred = async { repository.getTopicDetail(topicId) }
            val hasSubmitted = repository.getMySubmissions()
                .getOrNull()
                ?.any { it.topicId == topicId }
                ?: false
            detailDeferred.await()
                .onSuccess { detail ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        topicTitle = detail.title,
                        questions = detail.questions.sortedBy { it.displayOrder },
                        hasSubmitted = hasSubmitted
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

    /** Автоотправка сразу после остановки записи (дизайн v1.0 — без Review). */
    private var uploadInFlight = false

    private fun upload(filePath: String, durationSec: Int) {
        if (uploadInFlight) return   // M3-фикс (review): дубли при повторных тапах retry
        uploadInFlight = true
        val topicId = currentTopicId ?: run { uploadInFlight = false; return }
        viewModelScope.launch {
            try {
            _state.value = _state.value.copy(
                phase = PracticePhase.Uploading,
                uploadError = false,
                uploadProgress = 30   // индикатор без точного прогресса (Ktor onUpload — доработка T11)
            )
            val bytes = try {
                fileStorage.readBytes(filePath)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    phase = PracticePhase.Ready,
                    uploadError = true,
                    error = "Не удалось прочитать файл записи"
                )
                return@launch
            }
            _state.value = _state.value.copy(uploadProgress = 60)
            repository.submitPractice(
                topicId = topicId,
                durationSec = durationSec,
                audioBytes = bytes,
                fileName = filePath.substringAfterLast('/')
            )
                .onSuccess {
                    repository.markRecordingUploaded(filePath)
                    // Локальный файл уже в MinIO — освобождаем место (спека §6.4)
                    repository.removeRecording(filePath)
                    _state.value = _state.value.copy(
                        phase = PracticePhase.Sent,
                        uploadProgress = 100,
                        hasSubmitted = true
                    )
                    // Автоперехода нет: Sent-экран с CTA «Вернуться в библиотеку» (мокап, маэстро-флоу §10.2)
                }
                .onFailure { error ->
                    // Дубль — backend отклонил повторную отправку (Part 2 §2.6)
                    if (error is ApiException && error.errorCode == "DUPLICATE_SUBMISSION") {
                        fileStorage.delete(filePath)
                        repository.removeRecording(filePath)
                        _state.value = _state.value.copy(
                            phase = PracticePhase.Ready,
                            uploadError = false,
                            uploadProgress = 0,
                            hasSubmitted = true,
                            error = "Вы уже отправляли ответ по этой теме"
                        )
                        _events.trySend(PracticeEvent.ShowMessage("Вы уже отправляли ответ по этой теме"))
                    } else {
                        // Файл не теряется: мета uploaded=false остаётся → retry (спека §6.4)
                        _state.value = _state.value.copy(
                            phase = PracticePhase.Ready,
                            uploadError = true,
                            uploadProgress = 0,
                            error = error.message ?: "Ошибка отправки"
                        )
                    }
                }
            } finally {
                uploadInFlight = false
            }
        }
    }

    override fun onCleared() {
        recordingSession.stopTimer()
        super.onCleared()
    }
}
