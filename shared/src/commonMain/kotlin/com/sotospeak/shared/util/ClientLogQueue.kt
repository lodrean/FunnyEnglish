package com.sotospeak.shared.util

import com.sotospeak.shared.contracts.ClientLogDto
import com.sotospeak.shared.platform.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Локальная очередь клиентских логов (WARN+) перед отправкой на backend.
 * Паттерн — как очередь guest-events в GuestProgressRepository:
 * Settings + JSON, cap с FIFO-вытеснением, fault-tolerant decode.
 *
 * ДВА лимита (срабатывает первый): по числу записей (200) и по размеру JSON (~3 КБ).
 * Размер важен: desktop Settings — java.util.prefs с лимитом 8 КБ на значение
 * (IllegalArgumentException), а логи со stackTrace легко его превышают.
 */
class ClientLogQueue(
    private val settings: Settings
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun enqueue(entry: ClientLogDto) {
        // Обрезаем поля при записи: одна запись не должна съесть весь лимит
        val trimmed = entry.copy(
            message = entry.message.take(MAX_ENTRY_MESSAGE_CHARS),
            stackTrace = entry.stackTrace?.take(MAX_ENTRY_STACK_CHARS)
        )
        val queue = getPending().toMutableList().apply { add(trimmed) }
        // FIFO-вытеснение старейших, пока не впишемся в лимиты
        // (queue.size > 1 — новейшую запись не вытесняем никогда)
        while (queue.size > 1 && (queue.size > MAX_PENDING_LOGS || serialize(queue).length > MAX_SERIALIZED_CHARS)) {
            queue.removeAt(0)
        }
        settings.putString(KEY_LOGS, serialize(queue))
    }

    fun getPending(): List<ClientLogDto> {
        val data = settings.getString(KEY_LOGS, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<ClientLogDto>>(data) }.getOrElse { emptyList() }
    }

    fun clearPending() {
        settings.remove(KEY_LOGS)
    }

    /** Убрать первые [count] записей (после успешной отправки батча) */
    fun removeFirst(count: Int) {
        val remaining = getPending().drop(count)
        if (remaining.isEmpty()) settings.remove(KEY_LOGS)
        else settings.putString(KEY_LOGS, serialize(remaining))
    }

    fun size(): Int = getPending().size

    private fun serialize(queue: List<ClientLogDto>): String = json.encodeToString(queue)

    companion object {
        private const val KEY_LOGS = "client_pending_logs"
        const val MAX_PENDING_LOGS = 200
        /** Лимит сериализованной очереди в символах (java.util.prefs на desktop — 8192 char на значение) */
        const val MAX_SERIALIZED_CHARS = 7 * 1024
        private const val MAX_ENTRY_MESSAGE_CHARS = 1000
        private const val MAX_ENTRY_STACK_CHARS = 2000
    }
}
