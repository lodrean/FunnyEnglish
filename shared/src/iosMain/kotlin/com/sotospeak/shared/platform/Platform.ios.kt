package com.sotospeak.shared.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.*
import platform.Foundation.*
import platform.darwin.NSObjectProtocol

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

    actual fun seekTo(position: Long) {
        val time = CMTimeMake(position, 1000)
        player?.seekToTime(time)
    }

    actual fun getCurrentPosition(): Long {
        val currentTime = player?.currentTime() ?: return 0
        return (CMTimeGetSeconds(currentTime) * 1000).toLong()
    }

    actual fun getDuration(): Long {
        val duration = player?.currentItem?.duration ?: return 0
        if (CMTIME_IS_INVALID(duration)) return 0
        return (CMTimeGetSeconds(duration) * 1000).toLong()
    }

    actual fun setPlaybackSpeed(speed: Float) {
        player?.rate = speed
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

/**
 * iOS implementation of AudioPlayerController using AVPlayer
 */
actual class AudioPlayerController {
    private val _state = MutableStateFlow(AudioPlayerState())
    actual val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

    private var player: AVPlayer? = null
    private var timeObserver: NSObjectProtocol? = null
    private var endObserver: NSObjectProtocol? = null
    private var errorObserver: NSObjectProtocol? = null

    init {
        setupPlayer()
    }

    private fun setupPlayer() {
        val newPlayer = AVPlayer()
        player = newPlayer

        // Add periodic time observer
        val interval = CMTimeMake(1, 10) // Update every 100ms
        timeObserver = newPlayer.addPeriodicTimeObserverForInterval(
            interval,
            queue = NSOperationQueue.mainQueue
        ) { [weak this] _ ->
            this?.updatePosition()
        }
    }

    actual fun prepare(url: String) {
        val sanitizedUrl = url.trim()
        if (sanitizedUrl.isEmpty()) {
            _state.value = AudioPlayerState(error = "Invalid URL")
            return
        }

        _state.value = AudioPlayerState(isBuffering = true)

        val nsUrl = NSURL.URLWithString(sanitizedUrl) ?: run {
            _state.value = AudioPlayerState(error = "Invalid URL")
            return
        }

        clearObservers()

        val asset = AVAsset(uRL = nsUrl)
        val playerItem = AVPlayerItem(asset = asset)
        player?.replaceCurrentItemWithPlayerItem(playerItem)

        // Setup observers
        setupObservers(playerItem)
    }

    private fun setupObservers(playerItem: AVPlayerItem) {
        endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = playerItem,
            queue = NSOperationQueue.mainQueue
        ) { [weak this] _ ->
            this?.onPlaybackComplete()
        }

        errorObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemFailedToPlayToEndTimeNotification,
            `object` = playerItem,
            queue = NSOperationQueue.mainQueue
        ) { [weak this] _ ->
            this?.onPlaybackError()
        }
    }

    private fun clearObservers() {
        endObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        endObserver = null
        errorObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        errorObserver = null
    }

    private fun onPlaybackComplete() {
        _state.value = _state.value.copy(
            isPlaying = false,
            currentPosition = getDuration()
        )
    }

    private fun onPlaybackError() {
        _state.value = _state.value.copy(
            isPlaying = false,
            isBuffering = false,
            error = "Playback error"
        )
    }

    private fun updatePosition() {
        val currentPlayer = player ?: return
        val currentTime = currentPlayer.currentTime()
        val duration = currentPlayer.currentItem?.duration

        _state.value = _state.value.copy(
            currentPosition = (CMTimeGetSeconds(currentTime) * 1000).toLong(),
            duration = if (duration != null && !CMTIME_IS_INVALID(duration)) {
                (CMTimeGetSeconds(duration) * 1000).toLong()
            } else 0,
            isPlaying = currentPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying
        )
    }

    actual fun play() {
        player?.play()
        _state.value = _state.value.copy(isPlaying = true)
    }

    actual fun pause() {
        player?.pause()
        _state.value = _state.value.copy(isPlaying = false)
    }

    actual fun stop() {
        player?.pause()
        player?.seekToTime(CMTimeMake(0, 1))
        _state.value = AudioPlayerState()
    }

    actual fun release() {
        timeObserver?.let { player?.removeTimeObserver(it) }
        timeObserver = null
        clearObservers()
        player = null
    }

    actual fun seekTo(position: Long) {
        val time = CMTimeMake(position, 1000)
        player?.seekToTime(time)
    }

    actual fun setPlaybackSpeed(speed: Float) {
        player?.rate = speed.coerceIn(0.25f, 2.0f)
    }

    actual fun setVolume(volume: Float) {
        player?.volume = volume.coerceIn(0f, 1f)
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
