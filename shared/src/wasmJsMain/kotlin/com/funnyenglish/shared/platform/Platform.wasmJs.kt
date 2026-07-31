package com.funnyenglish.shared.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Web (WASM) implementation of AudioPlayer.
 * Note: Audio support in WASM is limited, this is a basic stub implementation.
 */
actual class AudioPlayer {
    private var onCompletion: (() -> Unit)? = null

    actual fun play(url: String) {
        log("[AudioPlayer] Play requested: $url")
    }

    actual fun pause() {
        log("[AudioPlayer] Pause requested")
    }

    actual fun stop() {
        log("[AudioPlayer] Stop requested")
    }

    actual fun release() {
        log("[AudioPlayer] Release requested")
    }

    actual fun isPlaying(): Boolean {
        return false
    }

    actual fun setOnCompletionListener(listener: () -> Unit) {
        onCompletion = listener
    }

    actual fun seekTo(position: Long) {
        log("[AudioPlayer] Seek to: $position")
    }

    actual fun getCurrentPosition(): Long {
        return 0L
    }

    actual fun getDuration(): Long {
        return 0L
    }

    actual fun setPlaybackSpeed(speed: Float) {
        log("[AudioPlayer] Set speed: $speed")
    }
}

/**
 * Web (WASM) implementation of AudioPlayerController.
 */
actual class AudioPlayerController {
    private val _state = MutableStateFlow(AudioPlayerState())
    actual val state: StateFlow<AudioPlayerState> = _state

    actual fun prepare(url: String) {
        log("[AudioPlayerController] Prepare: $url")
        _state.value = _state.value.copy(isReady = true)
    }

    actual fun play() {
        log("[AudioPlayerController] Play")
        _state.value = _state.value.copy(isPlaying = true)
    }

    actual fun pause() {
        log("[AudioPlayerController] Pause")
        _state.value = _state.value.copy(isPlaying = false)
    }

    actual fun stop() {
        log("[AudioPlayerController] Stop")
        _state.value = _state.value.copy(isPlaying = false, currentPosition = 0)
    }

    actual fun release() {
        log("[AudioPlayerController] Release")
    }

    actual fun seekTo(position: Long) {
        log("[AudioPlayerController] Seek to: $position")
        _state.value = _state.value.copy(currentPosition = position)
    }

    actual fun setPlaybackSpeed(speed: Float) {
        log("[AudioPlayerController] Set speed: $speed")
    }

    actual fun setVolume(volume: Float) {
        log("[AudioPlayerController] Set volume: $volume")
    }
}

/**
 * Web (WASM) implementation of Settings using localStorage.
 */
actual class Settings actual constructor(name: String) {
    private val prefix = "${name}_"

    actual fun getString(key: String, defaultValue: String?): String? {
        val value = localStorageGetItem(prefix + key)
        return value ?: defaultValue
    }

    actual fun putString(key: String, value: String?) {
        if (value != null) {
            localStorageSetItem(prefix + key, value)
        } else {
            localStorageRemoveItem(prefix + key)
        }
    }

    actual fun getInt(key: String, defaultValue: Int): Int {
        val value = localStorageGetItem(prefix + key)
        return value?.toIntOrNull() ?: defaultValue
    }

    actual fun putInt(key: String, value: Int) {
        localStorageSetItem(prefix + key, value.toString())
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val value = localStorageGetItem(prefix + key)
        return value?.toBooleanStrictOrNull() ?: defaultValue
    }

    actual fun putBoolean(key: String, value: Boolean) {
        localStorageSetItem(prefix + key, value.toString())
    }

    actual fun remove(key: String) {
        localStorageRemoveItem(prefix + key)
    }

    actual fun clear() {
        log("[Settings] Clear requested for prefix: $prefix")
    }
}

// JS interop declarations for localStorage
@JsFun("(key) => { try { return window.localStorage.getItem(key); } catch(e) { return null; } }")
external fun localStorageGetItem(key: String): String?

@JsFun("(key, value) => { try { window.localStorage.setItem(key, value); } catch(e) { } }")
external fun localStorageSetItem(key: String, value: String)

@JsFun("(key) => { try { window.localStorage.removeItem(key); } catch(e) { } }")
external fun localStorageRemoveItem(key: String)

// Console logging
@JsFun("(message) => { console.log(message); }")
external fun log(message: String)

actual fun getPlatformName(): String = "Web (WASM)"
