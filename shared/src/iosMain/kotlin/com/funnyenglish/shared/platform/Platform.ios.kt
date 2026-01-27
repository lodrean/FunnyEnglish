package com.funnyenglish.shared.platform

import platform.AVFoundation.*
import platform.Foundation.*

actual class AudioPlayer {
    private var player: AVPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null

    actual fun play(url: String) {
        stop()
        val nsUrl = NSURL.URLWithString(url) ?: return
        player = AVPlayer(uRL = nsUrl)
        player?.play()

        // Setup completion observer
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = player?.currentItem,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            onCompletionListener?.invoke()
        }
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun stop() {
        player?.pause()
        player = null
    }

    actual fun release() {
        stop()
    }

    actual fun isPlaying(): Boolean {
        return player?.rate?.let { it > 0 } ?: false
    }

    actual fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }
}

actual class Settings actual constructor(name: String) {
    private val userDefaults = NSUserDefaults(suiteName = name)

    actual fun getString(key: String, defaultValue: String?): String? =
        userDefaults?.stringForKey(key) ?: defaultValue

    actual fun putString(key: String, value: String?) {
        if (value != null) {
            userDefaults?.setObject(value, key)
        } else {
            userDefaults?.removeObjectForKey(key)
        }
    }

    actual fun getInt(key: String, defaultValue: Int): Int =
        userDefaults?.integerForKey(key)?.toInt() ?: defaultValue

    actual fun putInt(key: String, value: Int) {
        userDefaults?.setInteger(value.toLong(), key)
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        userDefaults?.boolForKey(key) ?: defaultValue

    actual fun putBoolean(key: String, value: Boolean) {
        userDefaults?.setBool(value, key)
    }

    actual fun remove(key: String) {
        userDefaults?.removeObjectForKey(key)
    }

    actual fun clear() {
        userDefaults?.dictionaryRepresentation()?.keys?.forEach { key ->
            userDefaults?.removeObjectForKey(key as String)
        }
    }
}

actual fun getPlatformName(): String = "iOS"
