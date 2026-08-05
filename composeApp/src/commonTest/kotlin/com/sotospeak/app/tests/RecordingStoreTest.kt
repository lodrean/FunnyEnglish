package com.sotospeak.app.tests

import com.sotospeak.app.storage.RecordingFileStorage
import com.sotospeak.app.storage.RecordingKind
import com.sotospeak.app.storage.RecordingMeta
import com.sotospeak.app.storage.RecordingStore
import com.sotospeak.shared.platform.Settings
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Юнит-тесты RecordingStore (спека Part 2 §10.1): сериализация метаданных в Settings,
 * фильтры по топику, pendingPractice, markUploaded, имена файлов.
 *
 * Работают на desktopTest: Settings desktop-actual (java.util.prefs), имя уникальное на прогон.
 */
class RecordingStoreTest {

    private fun newStore(): RecordingStore {
        val settings = Settings("test_speaking_${Clock.System.now().toEpochMilliseconds()}")
        return RecordingStore(settings, RecordingFileStorage())
    }

    private fun meta(
        topicId: String = "topic1",
        attempt: Int = 1,
        kind: RecordingKind = RecordingKind.TRAINING,
        uploaded: Boolean = false,
        path: String = "/tmp/rec_${topicId}_${attempt}_${Clock.System.now().toEpochMilliseconds()}.m4a"
    ) = RecordingMeta(
        filePath = path,
        topicId = topicId,
        attemptNumber = attempt,
        kind = kind,
        durationMs = 12_000,
        timerLimitSeconds = 80,
        createdAtEpochMs = 1_700_000_000_000,
        uploaded = uploaded
    )

    @Test
    fun addAndListByTopic() {
        val store = newStore()
        store.add(meta(topicId = "t1", attempt = 1))
        store.add(meta(topicId = "t1", attempt = 2))
        store.add(meta(topicId = "t2", attempt = 1))

        assertEquals(3, store.list().size)
        assertEquals(2, store.list("t1").size)
        assertEquals(1, store.list("t2").size)
    }

    @Test
    fun removeDeletesMeta() {
        val store = newStore()
        val m = meta()
        store.add(m)
        assertEquals(1, store.list().size)

        store.remove(m.filePath)   // файл не существует — delete вернёт false, мета всё равно удаляется
        assertEquals(0, store.list().size)
    }

    @Test
    fun removeAllForTopic() {
        val store = newStore()
        store.add(meta(topicId = "t1", attempt = 1))
        store.add(meta(topicId = "t1", attempt = 2))
        store.add(meta(topicId = "t2", attempt = 1))

        store.removeAllForTopic("t1")
        assertEquals(0, store.list("t1").size)
        assertEquals(1, store.list("t2").size)
    }

    @Test
    fun pendingPracticeOnlyNotUploaded() {
        val store = newStore()
        val pending = meta(kind = RecordingKind.PRACTICE, attempt = 0, uploaded = false)
        val sent = meta(kind = RecordingKind.PRACTICE, attempt = 0, uploaded = true)
        val training = meta(kind = RecordingKind.TRAINING)
        store.add(pending)
        store.add(sent)
        store.add(training)

        val result = store.pendingPractice()
        assertEquals(1, result.size)
        assertEquals(pending.filePath, result.first().filePath)
    }

    @Test
    fun markUploadedFlipsFlag() {
        val store = newStore()
        val m = meta(kind = RecordingKind.PRACTICE, attempt = 0)
        store.add(m)
        assertFalse(store.pendingPractice().isEmpty())

        store.markUploaded(m.filePath)
        assertTrue(store.pendingPractice().isEmpty())
        assertTrue(store.list().first { it.filePath == m.filePath }.uploaded)
    }

    @Test
    fun fileNameFormats() {
        val store = newStore()
        assertEquals(
            "rec_t1_attempt2_123.m4a",
            store.fileNameFor("t1", RecordingKind.TRAINING, 2, 123)
        )
        assertEquals(
            "rec_t1_practice_123.m4a",
            store.fileNameFor("t1", RecordingKind.PRACTICE, 0, 123)
        )
    }
}
