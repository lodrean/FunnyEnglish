package com.sotospeak.app.tests

import com.sotospeak.app.storage.RecordingFileStorage
import com.sotospeak.app.storage.RecordingKind
import com.sotospeak.app.storage.RecordingMeta
import com.sotospeak.app.storage.RecordingStore
import com.sotospeak.shared.platform.Settings
import kotlinx.datetime.Clock
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Файловые сценарии RecordingStore.prune (bd FunnyEnglish-5tf.7) — desktopTest, т.к.
 * commonTest не имеет доступа к java.io.File. Файлы создаются в реальной директории
 * записей desktop-actual с уникальными именами и удаляются после теста;
 * prune удаляет только файлы, на которые есть метаданные в тестовом store.
 */
class RecordingStorePruneFileTest {

    private val fileStorage = RecordingFileStorage()
    private val createdFiles = mutableListOf<File>()

    private fun newStore(): RecordingStore {
        val settings = Settings("test_prune_${Clock.System.now().toEpochMilliseconds()}")
        return RecordingStore(settings, fileStorage)
    }

    private fun createRecordingFile(name: String): File {
        val file = File(fileStorage.recordingsDir(), name)
        file.writeBytes(ByteArray(16))
        createdFiles += file
        return file
    }

    private fun meta(
        file: File,
        topicId: String = "t1",
        kind: RecordingKind = RecordingKind.TRAINING,
        createdAtEpochMs: Long = 1_700_000_000_000
    ) = RecordingMeta(
        filePath = file.absolutePath,
        topicId = topicId,
        attemptNumber = if (kind == RecordingKind.TRAINING) 1 else 0,
        kind = kind,
        durationMs = 12_000,
        timerLimitSeconds = 80,
        createdAtEpochMs = createdAtEpochMs,
        uploaded = false
    )

    @AfterTest
    fun cleanup() {
        createdFiles.forEach { it.delete() }
        createdFiles.clear()
    }

    @Test
    fun pruneKeepsFreshTrainingWithExistingFile() {
        val store = newStore()
        val now = Clock.System.now().toEpochMilliseconds()
        val file = createRecordingFile("rec_test_fresh_$now.m4a")
        store.add(meta(file, createdAtEpochMs = now))

        store.prune(now)
        assertEquals(1, store.list().size)
        assertTrue(file.exists())
    }

    @Test
    fun pruneRemovesStaleTrainingMetaAndFile() {
        val store = newStore()
        val now = Clock.System.now().toEpochMilliseconds()
        val file = createRecordingFile("rec_test_stale_$now.m4a")
        store.add(meta(file, createdAtEpochMs = now - STALE_AGE_MS))

        store.prune(now)
        assertTrue(store.list().isEmpty())
        assertFalse(file.exists())
    }

    @Test
    fun pruneKeepsOldPendingPractice() {
        val store = newStore()
        val now = Clock.System.now().toEpochMilliseconds()
        val file = createRecordingFile("rec_test_practice_$now.m4a")
        // Старая, но PRACTICE и неотправленная — offline-retry (спека §6.4), TTL не действует
        store.add(meta(file, kind = RecordingKind.PRACTICE, createdAtEpochMs = now - STALE_AGE_MS))

        store.prune(now)
        assertEquals(1, store.pendingPractice().size)
        assertTrue(file.exists())
    }

    private companion object {
        /** Старше TTL TRAINING-записей (30 дней) с запасом. */
        const val STALE_AGE_MS: Long = 31L * 24 * 60 * 60 * 1000
    }
}
