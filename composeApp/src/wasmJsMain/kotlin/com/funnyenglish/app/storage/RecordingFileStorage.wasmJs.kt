package com.funnyenglish.app.storage

/** WASM — стаб (Android-first, решение R6 спеки Part 2). */
actual class RecordingFileStorage {

    actual fun recordingsDir(): String = unsupported()

    actual fun exists(path: String): Boolean = false

    actual fun readBytes(path: String): ByteArray = unsupported()

    actual fun delete(path: String): Boolean = false

    actual fun usableSpaceBytes(): Long = 0L

    private fun unsupported(): Nothing =
        throw UnsupportedOperationException("Запись недоступна на этой платформе")
}
