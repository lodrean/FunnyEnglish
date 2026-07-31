package com.funnyenglish.app.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.funnyenglish.shared.platform.AndroidContextHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Android-реализация видеоплеера на Media3 ExoPlayer (спека Part 2 §3.2).
 * Контекст — application context из AndroidContextHolder (НЕ Activity-context).
 */
actual class VideoPlayerController {

    private val _state = MutableStateFlow(VideoPlayerState())
    actual val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    private var player: ExoPlayer? = null

    // Скоуп для тикера позиции; живёт до release()
    private var scope: CoroutineScope? = null
    private var tickerJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            _state.value = _state.value.copy(
                isBuffering = playbackState == Player.STATE_BUFFERING,
                isReady = playbackState == Player.STATE_READY,
                durationMs = player?.duration?.coerceAtLeast(0) ?: 0
            )
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
            // Тикер позиции нужен только во время воспроизведения (для субтитров)
            if (isPlaying) startPositionTicker() else stopPositionTicker()
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.value = _state.value.copy(
                error = error.message,
                isPlaying = false,
                isBuffering = false
            )
        }
    }

    actual fun prepare(url: String) {
        val sanitizedUrl = url.trim()
        if (sanitizedUrl.isEmpty()) {
            _state.value = VideoPlayerState(error = "Invalid URL")
            return
        }

        val p = player ?: createPlayer()
        _state.value = VideoPlayerState(isBuffering = true)
        p.setMediaItem(MediaItem.fromUri(sanitizedUrl))
        p.prepare()
    }

    actual fun play() {
        player?.play()
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs.coerceAtLeast(0))
        // Обновляем позицию сразу, не дожидаясь следующего тика
        _state.value = _state.value.copy(positionMs = positionMs.coerceAtLeast(0))
    }

    actual fun release() {
        stopPositionTicker()
        scope?.cancel()
        scope = null
        player?.removeListener(playerListener)
        player?.release()
        player = null
        _state.value = VideoPlayerState()
    }

    /** Доступ к внутреннему ExoPlayer для привязки PlayerView (вместо cast из спеки). */
    internal fun exoPlayer(): ExoPlayer? = player

    private fun createPlayer(): ExoPlayer {
        val context = AndroidContextHolder.requireContext()
        return ExoPlayer.Builder(context).build().also {
            it.addListener(playerListener)
            player = it
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        }
    }

    private fun startPositionTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = scope?.launch {
            while (isActive) {
                player?.let { p ->
                    _state.value = _state.value.copy(
                        positionMs = p.currentPosition.coerceAtLeast(0),
                        durationMs = p.duration.coerceAtLeast(0)
                    )
                }
                delay(POSITION_TICK_MS)
            }
        }
    }

    private fun stopPositionTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private companion object {
        const val POSITION_TICK_MS = 250L
    }
}

/**
 * AndroidView-обёртка над Media3 PlayerView; стандартные контролы включены (PRD Story 2).
 */
@Composable
actual fun NativeVideoSurface(
    controller: VideoPlayerController,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = true
            }
        },
        update = { view ->
            // Привязываем/перепривязываем player (поворот экрана пересоздаёт PlayerView)
            view.player = controller.exoPlayer()
        }
    )
}
