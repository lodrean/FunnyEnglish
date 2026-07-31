package com.funnyenglish.shared.platform

import javazoom.jl.player.advanced.AdvancedPlayer
import javazoom.jl.player.advanced.PlaybackEvent
import javazoom.jl.player.advanced.PlaybackListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import java.net.URL
import java.util.prefs.Preferences
import kotlin.concurrent.thread

actual class AudioPlayer {
    private val lock = Any()
    private var player: AdvancedPlayer? = null
    private var playbackThread: Thread? = null
    private var currentStream: InputStream? = null
    private var onCompletionListener: (() -> Unit)? = null
    @Volatile
    private var isCurrentlyPlaying = false
    @Volatile
    private var isPaused = false
    @Volatile
    private var playSessionId = 0L
    @Volatile
    private var currentPosition = 0
    @Volatile
    private var duration = 0

    actual fun play(url: String) {
        val sanitizedUrl = url.trim()
        if (sanitizedUrl.isEmpty()) {
            stop()
            return
        }

        val parsedUrl = runCatching { URL(sanitizedUrl) }.getOrNull()
        if (parsedUrl == null) {
            stop()
            onCompletionListener?.invoke()
            return
        }

        val sessionId = synchronized(lock) {
            isPaused = false
            playSessionId += 1
            stopLocked(interruptThread = true)
            playSessionId
        }

        playbackThread = thread(start = true, isDaemon = true) {
            try {
                val connection = parsedUrl.openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 30000
                val inputStream = connection.getInputStream().buffered()
                synchronized(lock) {
                    if (playSessionId != sessionId) {
                        inputStream.close()
                        return@thread
                    }
                    currentStream = inputStream
                }

                val newPlayer = AdvancedPlayer(inputStream)
                newPlayer.setPlayBackListener(object : PlaybackListener() {
                    override fun playbackFinished(evt: PlaybackEvent?) {
                        val shouldNotify = synchronized(lock) {
                            if (playSessionId != sessionId) {
                                return@synchronized false
                            }
                            val notify = !isPaused
                            stopLocked(interruptThread = false)
                            notify
                        }
                        if (shouldNotify) {
                            onCompletionListener?.invoke()
                        }
                    }
                })

                synchronized(lock) {
                    if (playSessionId != sessionId) {
                        newPlayer.close()
                        return@thread
                    }
                    player = newPlayer
                    isCurrentlyPlaying = true
                }
                newPlayer.play()
            } catch (e: Exception) {
                val shouldNotify = synchronized(lock) {
                    if (playSessionId != sessionId) {
                        return@synchronized false
                    }
                    stopLocked(interruptThread = false)
                    true
                }
                if (shouldNotify) {
                    println("Failed to play audio: ${e.message}")
                    onCompletionListener?.invoke()
                }
            }
        }
    }

    actual fun pause() {
        synchronized(lock) {
            isPaused = true
            playSessionId += 1
            stopLocked(interruptThread = true)
        }
    }

    actual fun stop() {
        synchronized(lock) {
            isPaused = false
            playSessionId += 1
            stopLocked(interruptThread = true)
        }
    }

    actual fun release() {
        stop()
    }

    actual fun isPlaying(): Boolean = isCurrentlyPlaying

    actual fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    actual fun seekTo(position: Long) {
        // JLayer doesn't support seeking, store for reference
        currentPosition = position.toInt()
    }

    actual fun getCurrentPosition(): Long = synchronized(lock) {
        if (isCurrentlyPlaying) currentPosition.toLong() else 0
    }

    actual fun getDuration(): Long = synchronized(lock) {
        duration.toLong()
    }

    actual fun setPlaybackSpeed(speed: Float) {
        // JLayer doesn't support playback speed
    }

    private fun stopLocked(interruptThread: Boolean) {
        isCurrentlyPlaying = false
        runCatching { player?.close() }
        player = null
        runCatching { currentStream?.close() }
        currentStream = null
        if (interruptThread) {
            playbackThread?.interrupt()
        }
        playbackThread = null
    }
}

/**
 * Desktop implementation of AudioPlayerController using JLayer
 * Note: JLayer has limited features compared to ExoPlayer/AVPlayer
 */
actual class AudioPlayerController {
    private val _state = MutableStateFlow(AudioPlayerState())
    actual val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

    private val lock = Any()
    private var player: AdvancedPlayer? = null
    private var playbackThread: Thread? = null
    private var currentStream: InputStream? = null
    private var currentUrl: String? = null
    @Volatile
    private var isPlaying = false
    @Volatile
    private var playSessionId = 0L

    actual fun prepare(url: String) {
        val sanitizedUrl = url.trim()
        if (sanitizedUrl.isEmpty()) {
            _state.value = AudioPlayerState(error = "Invalid URL")
            return
        }

        currentUrl = sanitizedUrl
        _state.value = AudioPlayerState(isBuffering = true)

        // Desktop JLayer doesn't support true prepare without playing
        // We just store the URL and mark as ready
        _state.value = AudioPlayerState(isReady = true)
    }

    actual fun play() {
        val url = currentUrl ?: return

        val sessionId = synchronized(lock) {
            playSessionId += 1
            stopLocked(interruptThread = true)
            playSessionId
        }

        playbackThread = thread(start = true, isDaemon = true) {
            try {
                val parsedUrl = URL(url)
                val connection = parsedUrl.openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 30000
                val inputStream = connection.getInputStream().buffered()

                synchronized(lock) {
                    if (playSessionId != sessionId) {
                        inputStream.close()
                        return@thread
                    }
                    currentStream = inputStream
                }

                val newPlayer = AdvancedPlayer(inputStream)
                newPlayer.setPlayBackListener(object : PlaybackListener() {
                    override fun playbackFinished(evt: PlaybackEvent?) {
                        synchronized(lock) {
                            if (playSessionId == sessionId) {
                                isPlaying = false
                                stopLocked(interruptThread = false)
                            }
                        }
                        _state.value = _state.value.copy(isPlaying = false)
                    }
                })

                synchronized(lock) {
                    if (playSessionId != sessionId) {
                        newPlayer.close()
                        return@thread
                    }
                    player = newPlayer
                    isPlaying = true
                }

                _state.value = _state.value.copy(
                    isPlaying = true,
                    isBuffering = false,
                    isReady = true
                )

                // Simulate position updates since JLayer doesn't provide them
                val startTime = System.currentTimeMillis()
                Thread {
                    while (isPlaying && playSessionId == sessionId) {
                        val elapsed = System.currentTimeMillis() - startTime
                        _state.value = _state.value.copy(
                            currentPosition = elapsed,
                            duration = maxOf(elapsed, _state.value.duration)
                        )
                        Thread.sleep(100)
                    }
                }.start()

                newPlayer.play()
            } catch (e: Exception) {
                synchronized(lock) {
                    if (playSessionId == sessionId) {
                        stopLocked(interruptThread = false)
                    }
                }
                _state.value = _state.value.copy(
                    isPlaying = false,
                    isBuffering = false,
                    error = e.message
                )
            }
        }
    }

    actual fun pause() {
        // JLayer doesn't support pause/resume, we have to stop
        synchronized(lock) {
            playSessionId += 1
            stopLocked(interruptThread = true)
            isPlaying = false
        }
        _state.value = _state.value.copy(isPlaying = false)
    }

    actual fun stop() {
        synchronized(lock) {
            playSessionId += 1
            stopLocked(interruptThread = true)
            isPlaying = false
        }
        _state.value = AudioPlayerState()
    }

    actual fun release() {
        stop()
    }

    actual fun seekTo(position: Long) {
        // JLayer doesn't support seeking
    }

    actual fun setPlaybackSpeed(speed: Float) {
        // JLayer doesn't support playback speed
    }

    actual fun setVolume(volume: Float) {
        // JLayer doesn't support volume control
    }

    private fun stopLocked(interruptThread: Boolean) {
        runCatching { player?.close() }
        player = null
        runCatching { currentStream?.close() }
        currentStream = null
        if (interruptThread) {
            playbackThread?.interrupt()
        }
        playbackThread = null
    }
}

actual class Settings actual constructor(name: String) {
    private val prefs: Preferences = Preferences.userRoot().node(name)

    actual fun getString(key: String, defaultValue: String?): String? =
        prefs.get(key, defaultValue)

    actual fun putString(key: String, value: String?) {
        if (value == null) {
            prefs.remove(key)
        } else {
            prefs.put(key, value)
        }
        prefs.flush()
    }

    actual fun getInt(key: String, defaultValue: Int): Int =
        prefs.getInt(key, defaultValue)

    actual fun putInt(key: String, value: Int) {
        prefs.putInt(key, value)
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.putBoolean(key, value)
    }

    actual fun remove(key: String) {
        prefs.remove(key)
    }

    actual fun clear() {
        prefs.clear()
    }
}

actual fun getPlatformName(): String = "Desktop"
