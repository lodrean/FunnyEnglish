package com.sotospeak.app.storage

import java.io.File

actual class RecordingFileStorage {

    actual fun recordingsDir(): String =
        File(System.getProperty("user.home"), ".sotospeak/recordings")
            .apply { mkdirs() }
            .absolutePath

    actual fun exists(path: String): Boolean = File(path).exists()

    actual fun readBytes(path: String): ByteArray = File(path).readBytes()

    actual fun delete(path: String): Boolean = File(path).delete()

    actual fun usableSpaceBytes(): Long = File(recordingsDir()).usableSpace
}
