package com.funnyenglish.shared.platform

expect class AudioPlayer() {
    fun play(url: String)
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun setOnCompletionListener(listener: () -> Unit)
}

expect class Settings(name: String) {
    fun getString(key: String, defaultValue: String?): String?
    fun putString(key: String, value: String?)
    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun remove(key: String)
    fun clear()
}

expect fun getPlatformName(): String
