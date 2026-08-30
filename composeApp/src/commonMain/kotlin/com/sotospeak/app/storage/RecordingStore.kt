package com.sotospeak.app.storage

import com.sotospeak.shared.platform.Settings
import kotlinx.datetime.Clock
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
 *
 * Производительность (bd FunnyEnglish-5tf.7): распарсенный список кэшируется в памяти —
 * JSON читается/парсится один раз за жизнь процесса, мутации обновляют кэш и Settings
 * одной записью. Все вызовы — с главного потока (VM на viewModelScope, экраны),
 * дополнительной синхронизации нет.
 */
class RecordingStore(
    private val settings: Settings,
    private val fileStorage: RecordingFileStorage
) {
    private val json = Json { ignoreUnknownKeys = true }

    private var cache: List<RecordingMeta>? = null

    fun list(topicId: String? = null): List<RecordingMeta> {
        val all = loadAll()
        return if (topicId == null) all else all.filter { it.topicId == topicId }
    }

    /**
     * topicId с записями указанного kind — один проход по кэшу (bd 5tf.7).
     * Для бейджей прогресса Library/Topics: раньше каждый топик дёргал [list],
     * что стоило O(топики × размер JSON) парсингов.
     */
    fun recordedTopicIds(kind: RecordingKind): Set<String> =
        loadAll().asSequence().filter { it.kind == kind }.map { it.topicId }.toSet()

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

    /**
     * Чистка хранилища (bd 5tf.7); вызывается один раз при старте приложения.
     * Удаляются ТОЛЬКО файлы, на которые есть метаданные (чужие файлы не трогаем):
     * 1) метаданные, чей файл уже не существует, → мета удаляется;
     * 2) TRAINING-записи старше [TRAINING_TTL_MS] → мета + файл удаляются
     *    (privacy: «записи хранятся только на устройстве», место не растёт бесконечно;
     *    прогресс-бейдж по таким топикам обнуляется — принято, TTL консервативный).
     * Pending PRACTICE TTL не коснётся — они ждут offline-retry (спека §6.4).
     * Ошибки файловой системы не роняют старт приложения.
     */
    fun prune(nowEpochMs: Long = Clock.System.now().toEpochMilliseconds()) {
        try {
            val all = loadAll()
            val staleTraining = all.filter {
                it.kind == RecordingKind.TRAINING && nowEpochMs - it.createdAtEpochMs > TRAINING_TTL_MS
            }
            val missingFile = all.filterNot { it in staleTraining }
                .filterNot { fileStorage.exists(it.filePath) }
            val removedPaths = (staleTraining + missingFile).map { it.filePath }.toSet()
            if (removedPaths.isNotEmpty()) {
                saveAll(all.filterNot { it.filePath in removedPaths })
                staleTraining.forEach { fileStorage.delete(it.filePath) }
            }
        } catch (ignored: Exception) {
            // Платформенные стабы/ФС-ошибки: чистка не критична для работы приложения
        }
    }

    private fun loadAll(): List<RecordingMeta> {
        cache?.let { return it }
        val raw = settings.getString(KEY, null)
        val parsed = if (raw == null) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<RecordingMeta>>(raw)
            } catch (ignored: Exception) {
                emptyList() // битый JSON не должен ломать экран — начинаем с чистого списка
            }
        }
        cache = parsed
        return parsed
    }

    private fun saveAll(list: List<RecordingMeta>) {
        cache = list
        settings.putString(KEY, json.encodeToString(list))
    }

    private companion object {
        const val KEY = "speaking_recordings"

        /** TTL TRAINING-записей — 30 дней с момента создания (bd 5tf.7). */
        const val TRAINING_TTL_MS: Long = 30L * 24 * 60 * 60 * 1000
    }
}
