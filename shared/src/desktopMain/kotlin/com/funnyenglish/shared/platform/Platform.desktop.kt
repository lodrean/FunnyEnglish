package com.funnyenglish.shared.platform

import javazoom.jl.player.advanced.AdvancedPlayer
import javazoom.jl.player.advanced.PlaybackEvent
import javazoom.jl.player.advanced.PlaybackListener
import java.net.URL
import java.util.prefs.Preferences
import kotlin.concurrent.thread

actual class AudioPlayer {
    private var player: AdvancedPlayer? = null
    private var playbackThread: Thread? = null
    private var onCompletionListener: (() -> Unit)? = null
    @Volatile
    private var isCurrentlyPlaying = false
    @Volatile
    private var isPaused = false
    private var currentUrl: String? = null

    actual fun play(url: String) {
        stop()
        currentUrl = url

        playbackThread = thread(start = true, isDaemon = true) {
            try {
                val connection = URL(url).openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 30000
                val inputStream = connection.getInputStream().buffered()

                val newPlayer = AdvancedPlayer(inputStream)
                newPlayer.setPlayBackListener(object : PlaybackListener() {
                    override fun playbackFinished(evt: PlaybackEvent?) {
                        isCurrentlyPlaying = false
                        if (!isPaused) {
                            onCompletionListener?.invoke()
                        }
                    }
                })

                player = newPlayer
                isCurrentlyPlaying = true
                isPaused = false
                newPlayer.play()
            } catch (e: Exception) {
                isCurrentlyPlaying = false
                println("Failed to play audio: ${e.message}")
            }
        }
    }

    actual fun pause() {
        isPaused = true
        isCurrentlyPlaying = false
        player?.close()
        player = null
        playbackThread?.interrupt()
        playbackThread = null
    }

    actual fun stop() {
        isPaused = false
        isCurrentlyPlaying = false
        player?.close()
        player = null
        playbackThread?.interrupt()
        playbackThread = null
        currentUrl = null
    }

    actual fun release() {
        stop()
    }

    actual fun isPlaying(): Boolean = isCurrentlyPlaying

    actual fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
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
