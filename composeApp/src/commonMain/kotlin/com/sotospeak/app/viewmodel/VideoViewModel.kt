package com.sotospeak.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotospeak.app.data.SpeakingRepository
import com.sotospeak.app.error.UiText
import com.sotospeak.app.error.toUiText
import com.sotospeak.app.subtitles.SubtitleCue
import com.sotospeak.app.subtitles.WebVttParser
import com.sotospeak.shared.contracts.SpeakingTopicDetail
import com.sotospeak.shared.platform.Settings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Спека Part 2 §2.3.
 * playerState живёт в контроллере плеера (экран собирает его отдельно и передаёт в UI) —
 * в VideoState дублируется только ошибка видео для плашки retry.
 */
data class VideoState(
    val isLoading: Boolean = false,
    val topic: SpeakingTopicDetail? = null,
    val subtitlesEnabled: Boolean = false,
    val subtitleCues: List<SubtitleCue> = emptyList(),
    val videoError: Boolean = false,          // «видео не загружается» — retry + «К вопросам»
    val reloadNonce: Int = 0,                 // M1-фикс (review): ключ перезапуска плеера при retry
    val error: UiText? = null
)

sealed interface VideoAction {
    data class OnLoad(val topicId: String, val withSubtitles: Boolean) : VideoAction
    data object OnToggleSubtitles : VideoAction               // переключатель во время просмотра
    data object OnRetryVideo : VideoAction
    data class OnVideoStarted(val topicId: String) : VideoAction  // старт воспроизведения → флаг watched
    data object OnGoToQuestions : VideoAction
    data object OnBack : VideoAction
}

sealed interface VideoEvent {
    data class NavigateToQuestions(val topicId: String) : VideoEvent
    data object NavigateBack : VideoEvent
}

class VideoViewModel(
    private val repository: SpeakingRepository,
    private val settings: Settings
) : ViewModel() {

    private val _state = MutableStateFlow(VideoState())
    val state: StateFlow<VideoState> = _state.asStateFlow()

    private val _events = Channel<VideoEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentTopicId: String? = null
    private var withSubtitlesRequested: Boolean = false

    fun onAction(action: VideoAction) {
        when (action) {
            is VideoAction.OnLoad -> {
                currentTopicId = action.topicId
                withSubtitlesRequested = action.withSubtitles
                load(action.topicId)
            }
            is VideoAction.OnToggleSubtitles -> {
                // Тоггл доступен только если субтитры есть и загружены
                if (_state.value.subtitleCues.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        subtitlesEnabled = !_state.value.subtitlesEnabled
                    )
                }
            }
            is VideoAction.OnRetryVideo -> {
                _state.value = _state.value.copy(
                    videoError = false,
                    reloadNonce = _state.value.reloadNonce + 1
                )
                currentTopicId?.let { load(it) }
            }
            is VideoAction.OnVideoStarted -> markWatched(action.topicId)
            is VideoAction.OnGoToQuestions -> currentTopicId?.let {
                markWatched(it)
                _events.trySend(VideoEvent.NavigateToQuestions(it))
            }
            is VideoAction.OnBack -> _events.trySend(VideoEvent.NavigateBack)
        }
    }

    fun onVideoError() {
        _state.value = _state.value.copy(videoError = true)
    }

    private fun load(topicId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, videoError = false)
            repository.getTopicDetail(topicId)
                .onSuccess { detail ->
                    _state.value = _state.value.copy(isLoading = false, topic = detail)
                    val subtitleUrl = detail.video?.subtitleUrl
                    if (withSubtitlesRequested && subtitleUrl != null) {
                        loadSubtitles(subtitleUrl)
                    }
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.toUiText()
                    )
                }
        }
    }

    private fun loadSubtitles(url: String) {
        viewModelScope.launch {
            repository.getTextResource(url)
                .onSuccess { vtt ->
                    val cues = WebVttParser.parse(vtt)
                    _state.value = _state.value.copy(
                        subtitleCues = cues,
                        subtitlesEnabled = cues.isNotEmpty()
                    )
                }
                // Ошибка субтитров не блокирует видео — просто смотрим без них
        }
    }

    private fun markWatched(topicId: String) {
        settings.putString("topic_watched_$topicId", "true")
    }
}
