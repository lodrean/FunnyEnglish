package com.funnyenglish.shared.util

/**
 * Simple logger that works on all platforms including WASM.
 */
object Logger {
    enum class Level { VERBOSE, DEBUG, INFO, WARN, ERROR }
    
    var minLevel = Level.DEBUG
    
    fun d(tag: String, message: String) = log(Level.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(Level.INFO, tag, message)
    fun w(tag: String, message: String) = log(Level.WARN, tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) = log(Level.ERROR, tag, message, throwable)
    
    private fun log(level: Level, tag: String, message: String, throwable: Throwable? = null) {
        if (level.ordinal < minLevel.ordinal) return
        
        val prefix = "[${level.name}] [$tag]"
        val fullMessage = if (throwable != null) {
            "$prefix $message - ${throwable.message}"
        } else {
            "$prefix $message"
        }
        
        println(fullMessage)
    }
}
