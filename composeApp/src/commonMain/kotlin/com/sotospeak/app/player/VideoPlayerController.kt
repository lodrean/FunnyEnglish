package com.sotospeak.app.player

import kotlinx.coroutines.flow.StateFlow

/**
 * Состояние видеоплеера (спека Part 2 §3.2).
 * positionMs нужен для синхронизации субтитров (WebVTT).
 */
data class VideoPlayerState(
    val isReady: Boolean = false,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    /** Воспроизведение дошло до конца — показать «Начать заново» */
    val isEnded: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val error: String? = null
)

/**
 * Контроллер видеоплеера. НЕ в Koin — создаётся экраном через
 * remember { VideoPlayerController() } + DisposableEffect { onDispose { release() } }.
 */
expect class VideoPlayerController() {
    val state: StateFlow<VideoPlayerState>
    fun prepare(url: String)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun release()
}
