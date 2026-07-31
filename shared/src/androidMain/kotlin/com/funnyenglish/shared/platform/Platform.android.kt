package com.funnyenglish.shared.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Legacy AudioPlayer implementation using ExoPlayer
 */
actual class AudioPlayer {
    private val lock = Any()
    @Volatile
    private var exoPlayer: ExoPlayer? = null
    @Volatile
    private var onCompletionListener: (() -> Unit)? = null

    actual fun play(url: String) {
        val sanitizedUrl = url.trim()
        if (sanitizedUrl.isEmpty()) {
            stop()
            return
        }

        synchronized(lock) {
            // Reuse existing player if available
            var player = exoPlayer
            if (player == null) {
                player = createExoPlayer()
                exoPlayer = player
            }

            val mediaItem = MediaItem.fromUri(sanitizedUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    actual fun pause() {
        synchronized(lock) {
            exoPlayer?.pause()
        }
    }

    actual fun stop() {
        synchronized(lock) {
            exoPlayer?.stop()
        }
    }

    actual fun release() {
        synchronized(lock) {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    actual fun isPlaying(): Boolean = synchronized(lock) {
        exoPlayer?.isPlaying ?: false
    }

    actual fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    actual fun seekTo(position: Long) {
        synchronized(lock) {
            exoPlayer?.seekTo(position)
        }
    }

    actual fun getCurrentPosition(): Long = synchronized(lock) {
        exoPlayer?.currentPosition ?: 0
    }

    actual fun getDuration(): Long = synchronized(lock) {
        exoPlayer?.duration ?: 0
    }

    actual fun setPlaybackSpeed(speed: Float) {
        synchronized(lock) {
            exoPlayer?.setPlaybackSpeed(speed)
        }
    }

    private fun createExoPlayer(): ExoPlayer {
        val context = AndroidContextHolder.requireContext()
        val player = ExoPlayer.Builder(context).build()
        
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onCompletionListener?.invoke()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                onCompletionListener?.invoke()
            }
        })
        
        return player
    }
}

/**
 * Modern AudioPlayerController with StateFlow for reactive UI
 */
actual class AudioPlayerController {
    private val _state = MutableStateFlow(AudioPlayerState())
    actual val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            _state.value = _state.value.copy(
                isBuffering = playbackState == Player.STATE_BUFFERING,
                isReady = playbackState == Player.STATE_READY,
                isPlaying = exoPlayer?.isPlaying ?: false,
                duration = exoPlayer?.duration?.coerceAtLeast(0) ?: 0
            )
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.value = _state.value.copy(
                error = error.message,
                isPlaying = false,
                isBuffering = false
            )
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            _state.value = _state.value.copy(
                currentPosition = exoPlayer?.currentPosition?.coerceAtLeast(0) ?: 0
            )
        }
    }

    init {
        val context = AndroidContextHolder.requireContext()
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(playerListener)
        }
    }

    actual fun prepare(url: String) {
        val sanitizedUrl = url.trim()
        if (sanitizedUrl.isEmpty()) {
            _state.value = AudioPlayerState(error = "Invalid URL")
            return
        }

        _state.value = AudioPlayerState(isBuffering = true)
        
        val mediaItem = MediaItem.fromUri(sanitizedUrl)
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
        }
    }

    actual fun play() {
        exoPlayer?.play()
    }

    actual fun pause() {
        exoPlayer?.pause()
    }

    actual fun stop() {
        exoPlayer?.stop()
        _state.value = AudioPlayerState()
    }

    actual fun release() {
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }

    actual fun seekTo(position: Long) {
        exoPlayer?.seekTo(position.coerceAtLeast(0))
    }

    actual fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed.coerceIn(0.25f, 2.0f))
    }

    actual fun setVolume(volume: Float) {
        exoPlayer?.volume = volume.coerceIn(0f, 1f)
    }

    /**
     * Update current position - call this periodically for progress updates
     */
    fun updatePosition() {
        exoPlayer?.let { player ->
            _state.value = _state.value.copy(
                currentPosition = player.currentPosition.coerceAtLeast(0),
                duration = player.duration.coerceAtLeast(0)
            )
        }
    }
}

actual class Settings actual constructor(name: String) {
    private val prefs: SharedPreferences = AndroidContextHolder.requireContext()
        .getSharedPreferences(name, Context.MODE_PRIVATE)

    actual fun getString(key: String, defaultValue: String?): String? =
        prefs.getString(key, defaultValue)

    actual fun putString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getInt(key: String, defaultValue: Int): Int =
        prefs.getInt(key, defaultValue)

    actual fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    actual fun clear() {
        prefs.edit().clear().apply()
    }
}

actual fun getPlatformName(): String = "Android"
