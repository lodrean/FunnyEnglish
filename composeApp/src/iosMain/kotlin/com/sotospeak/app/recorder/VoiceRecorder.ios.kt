package com.sotospeak.app.recorder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** iOS — стаб (Android-first, решение R6 спеки Part 2 §4.2). */
actual class VoiceRecorder {

    private val _state = MutableStateFlow<VoiceRecorderState>(VoiceRecorderState.Idle)
    actual val state: StateFlow<VoiceRecorderState> = _state.asStateFlow()

    actual fun start(outputFileName: String) {
        _state.value = VoiceRecorderState.Error("Запись недоступна на этой платформе")
    }

    actual fun stop(): String? = null

    actual fun cancel() {
        _state.value = VoiceRecorderState.Idle
    }

    actual fun release() {
        _state.value = VoiceRecorderState.Idle
    }
}
