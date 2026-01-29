package com.funnyenglish.shared.platform

import java.net.URL
import java.util.prefs.Preferences
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

actual class AudioPlayer {
    private var clip: Clip? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var isPlaying: Boolean = false

    actual fun play(url: String) {
        stop()
        val audioStream = try {
            AudioSystem.getAudioInputStream(URL(url))
        } catch (_: Exception) {
            isPlaying = false
            return
        }

        try {
            val newClip = AudioSystem.getClip()
            newClip.open(audioStream)
            newClip.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) {
                    val finished = newClip.frameLength > 0 && newClip.framePosition >= newClip.frameLength
                    if (finished) {
                        isPlaying = false
                        onCompletionListener?.invoke()
                    }
                }
            }
            clip = newClip
            isPlaying = true
            newClip.start()
        } catch (_: Exception) {
            isPlaying = false
            clip = null
        } finally {
            runCatching { audioStream.close() }
        }
    }

    actual fun pause() {
        clip?.stop()
        isPlaying = false
    }

    actual fun stop() {
        clip?.stop()
        clip?.close()
        clip = null
        isPlaying = false
    }

    actual fun release() {
        stop()
    }

    actual fun isPlaying(): Boolean = isPlaying

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
