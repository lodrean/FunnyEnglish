package com.sotospeak.shared.platform

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for audio player state
 */
data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val error: String? = null,
    val isReady: Boolean = false
)

/**
 * Expected AudioPlayer class for all platforms
 */
expect class AudioPlayer() {
    fun play(url: String)
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun setOnCompletionListener(listener: () -> Unit)
    fun seekTo(position: Long)
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun setPlaybackSpeed(speed: Float)
}

/**
 * Extended AudioPlayer interface with state flow for modern UI
 */
expect class AudioPlayerController {
    val state: StateFlow<AudioPlayerState>
    fun prepare(url: String)
    fun play()
    fun pause()
    fun stop()
    fun release()
    fun seekTo(position: Long)
    fun setPlaybackSpeed(speed: Float)
    fun setVolume(volume: Float)
}

expect class Settings(name: String) {
    fun getString(key: String, defaultValue: String?): String?
    fun putString(key: String, value: String?)
    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun remove(key: String)
    fun clear()
}

expect fun getPlatformName(): String
