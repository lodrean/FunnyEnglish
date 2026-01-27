package com.funnyenglish.shared.platform

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer

actual class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null

    actual fun play(url: String) {
        stop()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener { start() }
            setOnCompletionListener { onCompletionListener?.invoke() }
            prepareAsync()
        }
    }

    actual fun pause() {
        mediaPlayer?.pause()
    }

    actual fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            reset()
            release()
        }
        mediaPlayer = null
    }

    actual fun release() {
        stop()
    }

    actual fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    actual fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }
}

actual class Settings actual constructor(name: String) {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context, name: String) {
        prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

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
