package com.sotospeak.shared.util

import com.sotospeak.shared.contracts.ClientLogDto
import com.sotospeak.shared.platform.getPlatformName
import kotlinx.datetime.Clock

/**
 * Simple logger that works on all platforms including WASM.
 *
 * WARN/ERROR дополнительно складываются в [ClientLogQueue] (remote-sink) —
 * очередь отправляется на backend через LogUploader (OpenSpec add-client-logging).
 * Очередь подключается при старте приложения через [remoteQueue]/[remoteMeta];
 * до подключения логи идут только в консоль.
 */
object Logger {
    enum class Level { VERBOSE, DEBUG, INFO, WARN, ERROR }

    var minLevel = Level.DEBUG

    /** Метаданные remote-записи (appVersion/anonymousId; platform определяется самим логгером) */
    data class RemoteMeta(
        val appVersion: String?,
        val anonymousId: String?
    )

    /** Remote-sink: очередь WARN+ для отправки на backend; null — только консоль */
    var remoteQueue: ClientLogQueue? = null

    /** Провайдер метаданных для remote-записей (настраивается при старте приложения) */
    var remoteMeta: (() -> RemoteMeta)? = null

    /** Колбэк после enqueue remote-записи — подписчик (App.kt) запускает flush, best-effort */
    var onRemoteEnqueued: (() -> Unit)? = null

    fun d(tag: String, message: String) = log(Level.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(Level.INFO, tag, message)
    fun w(tag: String, message: String) = log(Level.WARN, tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) = log(Level.ERROR, tag, message, throwable)

    private fun log(level: Level, tag: String, message: String, throwable: Throwable? = null) {
        if (level.ordinal >= Level.WARN.ordinal) enqueueRemote(level, tag, message, throwable)
        if (level.ordinal < minLevel.ordinal) return

        val prefix = "[${level.name}] [$tag]"
        val fullMessage = if (throwable != null) {
            "$prefix $message - ${throwable.message}"
        } else {
            "$prefix $message"
        }

        println(fullMessage)
    }

    private fun enqueueRemote(level: Level, tag: String, message: String, throwable: Throwable?) {
        val queue = remoteQueue ?: return
        // runCatching: логирование не должно ронять приложение
        runCatching {
            val meta = remoteMeta?.invoke()
            queue.enqueue(
                ClientLogDto(
                    timestamp = Clock.System.now().toString(),
                    level = level.name,
                    tag = tag.take(100),
                    message = message,
                    stackTrace = throwable?.let { t -> runCatching { t.stackTraceToString() }.getOrNull() },
                    platform = platformId(),
                    appVersion = meta?.appVersion,
                    anonymousId = meta?.anonymousId
                )
            )
            onRemoteEnqueued?.invoke()
        }
    }

    /** Нормализация getPlatformName() в идентификаторы спеки: android/desktop/wasm/ios */
    private fun platformId(): String {
        val name = getPlatformName().lowercase()
        return when {
            "android" in name -> "android"
            "desktop" in name -> "desktop"
            "wasm" in name || "web" in name -> "wasm"
            else -> name
        }
    }
}
