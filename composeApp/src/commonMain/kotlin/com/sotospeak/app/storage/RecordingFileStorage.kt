package com.sotospeak.app.storage

/**
 * Файловые операции для голосовых записей Speaking Trainer (спека Part 2 §5.1, решение R3).
 *
 * Файлы — в приватной директории приложения (`recordings/`).
 * Android/desktop actual — java.io.File; ios/wasm — стабы (Android-first, решение R6).
 */
expect class RecordingFileStorage() {
    /** Абсолютный путь директории записей (создаётся при необходимости). */
    fun recordingsDir(): String

    fun exists(path: String): Boolean

    fun readBytes(path: String): ByteArray

    fun delete(path: String): Boolean

    /** Свободное место в байтах (для проверки перед записью, порог 5 МБ). */
    fun usableSpaceBytes(): Long
}
