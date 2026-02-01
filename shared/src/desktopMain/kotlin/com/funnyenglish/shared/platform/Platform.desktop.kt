package com.funnyenglish.shared.platform

import javazoom.jl.player.advanced.AdvancedPlayer
import javazoom.jl.player.advanced.PlaybackEvent
import javazoom.jl.player.advanced.PlaybackListener
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
