package com.sotospeak.app.storage

import com.sotospeak.shared.platform.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class RecordingMeta(
    val filePath: String,
    val topicId: String,
    val attemptNumber: Int,           // 1..3 для TRAINING (0 для PRACTICE)
    val kind: RecordingKind,
    val durationMs: Long,
    val timerLimitSeconds: Int,       // лимит, на котором сделана запись
    val createdAtEpochMs: Long,
    val uploaded: Boolean = false     // для PRACTICE: offline-retry (спека §6.4)
)

enum class RecordingKind { TRAINING, PRACTICE }

/**
 * Метаданные записей — JSON в [Settings] (ключ "speaking_recordings"), файлы — через
 * [RecordingFileStorage] (спека Part 2 §5.1, решение R3: Room не заведён, объём — десятки записей).
 *
 * Имена файлов: `rec_<topicId>_attempt<N>_<epochMs>.m4a` (Training),
 * `rec_<topicId>_practice_<epochMs>.m4a` (Practice).
 */
class RecordingStore(
    private val settings: Settings,
    private val fileStorage: RecordingFileStorage
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun list(topicId: String? = null): List<RecordingMeta> {
        val all = loadAll()
        return if (topicId == null) all else all.filter { it.topicId == topicId }
    }

    fun add(meta: RecordingMeta) {
        saveAll(loadAll() + meta)
    }

    /** Удаляет метаданные и сам файл. */
    fun remove(filePath: String) {
        saveAll(loadAll().filterNot { it.filePath == filePath })
        fileStorage.delete(filePath)
    }

    /**
     * Удаляет TRAINING-записи топика (Training «Начать заново с попытки 1»).
     * M2-фикс (review): pending PRACTICE-записи того же топика не трогаем —
     * они ждут offline-retry отправки учителю.
     */
    fun removeAllForTopic(topicId: String) {
        val toRemove = loadAll().filter { it.topicId == topicId && it.kind == RecordingKind.TRAINING }
        saveAll(loadAll().filterNot { it.topicId == topicId && it.kind == RecordingKind.TRAINING })
        toRemove.forEach { fileStorage.delete(it.filePath) }
    }

    fun markUploaded(filePath: String) {
        saveAll(loadAll().map { if (it.filePath == filePath) it.copy(uploaded = true) else it })
    }

    fun pendingPractice(): List<RecordingMeta> =
        loadAll().filter { it.kind == RecordingKind.PRACTICE && !it.uploaded }

    /** Имя файла новой записи (спека §5.1). */
    fun fileNameFor(topicId: String, kind: RecordingKind, attemptNumber: Int, epochMs: Long): String =
        when (kind) {
            RecordingKind.TRAINING -> "rec_${topicId}_attempt${attemptNumber}_$epochMs.m4a"
            RecordingKind.PRACTICE -> "rec_${topicId}_practice_$epochMs.m4a"
        }

    private fun loadAll(): List<RecordingMeta> {
        val raw = settings.getString(KEY, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<RecordingMeta>>(raw)
        } catch (e: Exception) {
            emptyList() // битый JSON не должен ломать экран — начинаем с чистого списка
        }
    }

    private fun saveAll(list: List<RecordingMeta>) {
        settings.putString(KEY, json.encodeToString(list))
    }

    private companion object {
        const val KEY = "speaking_recordings"
    }
}
