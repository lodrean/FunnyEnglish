package com.sotospeak.app.storage

import com.sotospeak.shared.platform.AndroidContextHolder
import java.io.File

actual class RecordingFileStorage {

    private val context get() = AndroidContextHolder.requireContext()

    actual fun recordingsDir(): String =
        File(context.filesDir, "recordings").apply { mkdirs() }.absolutePath

    actual fun exists(path: String): Boolean = File(path).exists()

    actual fun readBytes(path: String): ByteArray = File(path).readBytes()

    actual fun delete(path: String): Boolean = File(path).delete()

    actual fun usableSpaceBytes(): Long = File(recordingsDir()).usableSpace
}
