package com.funnyenglish.shared.platform

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer

actual class AudioPlayer {
    private val lock = Any()
    @Volatile
    private var mediaPlayer: MediaPlayer? = null
    @Volatile
    private var onCompletionListener: (() -> Unit)? = null

    actual fun play(url: String) {
        val sanitizedUrl = url.trim()
        if (sanitizedUrl.isEmpty()) {
            stop()
            return
        }
        val newPlayer = MediaPlayer()
        synchronized(lock) {
            stopLocked()
            mediaPlayer = newPlayer
        }
        try {
            newPlayer.setDataSource(sanitizedUrl)
            newPlayer.setOnPreparedListener {
                synchronized(lock) {
                    if (mediaPlayer !== newPlayer) {
                        return@setOnPreparedListener
                    }
                }
                newPlayer.start()
            }
            newPlayer.setOnCompletionListener {
                handleCompletion(newPlayer)
            }
            newPlayer.setOnErrorListener { _, _, _ ->
                handleError(newPlayer)
                true
            }
            newPlayer.prepareAsync()
        } catch (e: Exception) {
            handleError(newPlayer)
        }
    }

    actual fun pause() {
        synchronized(lock) {
            runCatching { mediaPlayer?.pause() }
        }
    }

    actual fun stop() {
        synchronized(lock) {
            stopLocked()
        }
    }

    actual fun release() {
        stop()
    }

    actual fun isPlaying(): Boolean = synchronized(lock) { mediaPlayer?.isPlaying ?: false }

    actual fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    private fun handleCompletion(player: MediaPlayer) {
        val listener = synchronized(lock) {
            if (mediaPlayer !== player) {
                null
            } else {
                stopLocked()
                onCompletionListener
            }
        }
        listener?.invoke()
    }

    private fun handleError(player: MediaPlayer) {
        val listener = synchronized(lock) {
            if (mediaPlayer !== player) {
                null
            } else {
                stopLocked()
                onCompletionListener
            }
        }
        listener?.invoke()
    }

    private fun stopLocked() {
        mediaPlayer?.let { player ->
            runCatching { if (player.isPlaying) player.stop() }
            runCatching { player.reset() }
            runCatching { player.release() }
        }
        mediaPlayer = null
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
