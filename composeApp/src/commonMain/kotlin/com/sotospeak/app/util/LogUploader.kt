package com.sotospeak.app.util

import com.sotospeak.shared.model.ClientLogDto
import com.sotospeak.shared.util.ClientLogQueue

/**
 * Отправка накопленных клиентских логов (WARN+) на backend батчами по 50.
 * Паттерн GuestAnalytics: best-effort, очередь очищается только при успехе.
 *
 * [sendLogs] вынесен в лямбду — тестируемость (SoToSpeakApi final) и
 * изоляция от сетевого слоя.
 *
 * ВАЖНО: ошибки отправки пишем через println, НЕ через Logger — иначе ошибка
 * отправки логов порождала бы новую запись в очереди (рекурсия).
 */
class LogUploader(
    private val queue: ClientLogQueue,
    private val sendLogs: suspend (List<ClientLogDto>) -> Boolean
) {

    /** Размер локальной очереди (для debug-меню) */
    fun pendingCount(): Int = queue.size()

    /** Отправить накопленное батчами; при первой неудаче — стоп, остаток в очереди */
    suspend fun flush() {
        while (true) {
            val batch = queue.getPending().take(MAX_BATCH)
            if (batch.isEmpty()) return
            if (sendLogs(batch)) {
                queue.removeFirst(batch.size)
            } else {
                println("[WARN] [LogUploader] flush failed, ${queue.size()} entries kept")
                return
            }
        }
    }

    companion object {
        private const val MAX_BATCH = 50
    }
}
