package com.sotospeak.app.player

import io.ktor.client.HttpClient
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
 * remember { VideoPlayerController(mediaClient) } + DisposableEffect { onDispose { release() } }.
 *
 * @param httpClient медиа-HTTP-клиент (Koin single named "media", см. MediaHttpClient):
 *   единый Ktor-стек стриминга (bd 4d1). Контроллер клиент НЕ закрывает (общий жизненный цикл Koin).
 */
expect class VideoPlayerController(httpClient: HttpClient) {
    val state: StateFlow<VideoPlayerState>
    /**
     * true — видео рендерится внутри canvas/иерархии Compose и поверх него можно
     * рисовать Compose-оверлей контролы (Android/Media3).
     * false — видео живёт вне canvas (WASM: DOM <video> поверх canvas), overlay-контролы
     * были бы перекрыты видео → контролы рисуются ПОД плеером.
     */
    val supportsOverlayControls: Boolean
    fun prepare(url: String)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun release()
}
