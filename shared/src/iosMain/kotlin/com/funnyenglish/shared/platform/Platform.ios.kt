package com.funnyenglish.shared.platform

import platform.AVFoundation.*
import platform.Foundation.*

actual class AudioPlayer {
    private var player: AVPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var endObserver: NSObjectProtocol? = null
    private var errorObserver: NSObjectProtocol? = null

    actual fun play(url: String) {
        val sanitizedUrl = url.trim()
        if (sanitizedUrl.isEmpty()) {
            stop()
            return
        }
        stop()
        val nsUrl = NSURL.URLWithString(sanitizedUrl) ?: return
        val newPlayer = AVPlayer(uRL = nsUrl)
        player = newPlayer
        newPlayer.play()

        // Setup completion observer
        val currentItem = newPlayer.currentItem
        endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = currentItem,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            handleCompletion(newPlayer)
        }
        errorObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemFailedToPlayToEndTimeNotification,
            `object` = currentItem,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            handleError(newPlayer)
        }
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun stop() {
        stopInternal()
    }

    actual fun release() {
        stop()
    }

    actual fun isPlaying(): Boolean {
        val currentPlayer = player ?: return false
        return currentPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying
    }

    actual fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    private fun handleCompletion(currentPlayer: AVPlayer) {
        val listener = if (player === currentPlayer) {
            stopInternal()
            onCompletionListener
        } else {
            null
        }
        listener?.invoke()
    }

    private fun handleError(currentPlayer: AVPlayer) {
        val listener = if (player === currentPlayer) {
            stopInternal()
            onCompletionListener
        } else {
            null
        }
        listener?.invoke()
    }

    private fun stopInternal() {
        player?.pause()
        player = null
        clearObservers()
    }

    private fun clearObservers() {
        endObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        endObserver = null
        errorObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        errorObserver = null
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
