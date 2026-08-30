package com.sotospeak.app.tests

import com.sotospeak.app.util.LogUploader
import com.sotospeak.shared.contracts.ClientLogDto
import com.sotospeak.shared.platform.Settings
import com.sotospeak.shared.util.ClientLogQueue
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Юнит-тесты клиентского логирования (OpenSpec add-client-logging):
 * очередь (cap/FIFO/битый JSON/removeFirst) + LogUploader (батчи, clear только при успехе).
 *
 * desktopTest (JVM): runBlocking недоступен в commonTest, Settings — java.util.prefs actual.
 */
class ClientLoggingTest {

    private fun newQueue(): ClientLogQueue =
        ClientLogQueue(Settings("test_client_logs_${System.nanoTime()}"))

    private fun entry(message: String, level: String = "ERROR") = ClientLogDto(
        timestamp = "2026-08-02T10:00:00Z",
        level = level,
        tag = "Test",
        message = message,
        platform = "android",
        appVersion = "1.0.0-qa",
        anonymousId = "00000000-0000-0000-0000-000000000001"
    )

    /** Минимальная запись (~100 символов JSON) — для тестов батчинга/лимитов */
    private fun minEntry(message: String) = ClientLogDto(
        timestamp = "2026-08-02T10:00:00Z",
        level = "ERROR",
        tag = "T",
        message = message,
        platform = "android"
    )

    // ==================== ClientLogQueue ====================

    @Test
    fun `queue enqueue и getPending сохраняют порядок`() {
        val queue = newQueue()
        queue.enqueue(entry("first"))
        queue.enqueue(entry("second"))

        val pending = queue.getPending()
        assertEquals(2, pending.size)
        assertEquals("first", pending[0].message)
        assertEquals("second", pending[1].message)
    }

    @Test
    fun `queue cap - FIFO-вытеснение старейших при переполнении, новейшая остаётся`() {
        val queue = newQueue()
        repeat(ClientLogQueue.MAX_PENDING_LOGS + 5) { queue.enqueue(minEntry("msg$it")) }

        val pending = queue.getPending()
        // сработал лимит по размеру JSON (7 КБ) раньше лимита по числу записей
        assertTrue(pending.size < ClientLogQueue.MAX_PENDING_LOGS + 5)
        assertEquals("msg204", pending.last().message)      // новейшая всегда остаётся
        assertTrue(pending.first().message != "msg0")       // старейшие вытеснены
    }

    @Test
    fun `queue битый JSON в Settings читается как пустая очередь`() {
        val settings = Settings("test_client_logs_${System.nanoTime()}")
        settings.putString("client_pending_logs", "{broken json[")
        val queue = ClientLogQueue(settings)

        assertTrue(queue.getPending().isEmpty())
    }

    @Test
    fun `queue removeFirst удаляет первые N записей`() {
        val queue = newQueue()
        repeat(5) { queue.enqueue(entry("msg$it")) }

        queue.removeFirst(2)
        val pending = queue.getPending()
        assertEquals(3, pending.size)
        assertEquals("msg2", pending.first().message)

        queue.removeFirst(3)
        assertTrue(queue.getPending().isEmpty())
    }

    // ==================== LogUploader ====================

    @Test
    fun `uploader отправляет всё батчами по 50 и очищает очередь`() = runBlocking {
        val queue = newQueue()
        repeat(60) { queue.enqueue(minEntry("m$it")) }

        val batches = mutableListOf<Int>()
        val uploader = LogUploader(queue) { logs ->
            batches.add(logs.size)
            true
        }
        uploader.flush()

        assertEquals(listOf(50, 10), batches)
        assertEquals(0, queue.size())
    }

    @Test
    fun `uploader при ошибке оставляет записи в очереди`() = runBlocking {
        val queue = newQueue()
        repeat(3) { queue.enqueue(entry("msg$it")) }

        val uploader = LogUploader(queue) { false }
        uploader.flush()

        assertEquals(3, queue.size())
    }

    @Test
    fun `uploader при частичной неудаче сохраняет неотправленный остаток`() = runBlocking {
        val queue = newQueue()
        repeat(60) { queue.enqueue(minEntry("m$it")) }

        var calls = 0
        val uploader = LogUploader(queue) {
            calls++
            calls == 1 // первый батч ok, второй падает
        }
        uploader.flush()

        assertEquals(2, calls)
        assertEquals(10, queue.size()) // 60 - 50 отправленных
        assertEquals("m50", queue.getPending().first().message)
    }

    @Test
    fun `uploader на пустой очереди не делает вызовов`() = runBlocking {
        val queue = newQueue()
        var calls = 0
        val uploader = LogUploader(queue) { calls++; true }
        uploader.flush()

        assertEquals(0, calls)
    }
}
