package com.sotospeak.app.data

import com.sotospeak.app.storage.RecordingFileStorage

/**
 * Фоновый retry неотправленных Practice-записей (bd FunnyEnglish-h3l.19, спека §6.4).
 * Общая логика для MySubmissionsViewModel (ручной/при-входе retry) и
 * Android WorkManager-воркера (периодический фоновый retry).
 * Возвращает число успешно отправленных записей за вызов.
 */
class PracticeRetryService(
    private val repository: SpeakingRepository,
    private val fileStorage: RecordingFileStorage
) {
    /** Отправляет все pending-записи; возвращает число успешных отправок. */
    suspend fun retryAll(): Int {
        var uploaded = 0
        repository.pendingPracticeUploads().forEach { meta ->
            if (retryOne(meta.filePath)) uploaded++
        }
        return uploaded
    }

    /** Retry одной записи; true — отправлена (мета удалена), false — осталась в очереди. */
    suspend fun retryOne(filePath: String): Boolean {
        val meta = repository.findRecording(filePath) ?: return false
        val bytes = try {
            fileStorage.readBytes(filePath)
        } catch (e: Exception) {
            repository.removeRecording(filePath)   // файла нет — чистим мету
            return false
        }
        val sent = repository.submitPractice(
            topicId = meta.topicId,
            durationSec = (meta.durationMs / 1000).toInt().coerceAtLeast(1),
            audioBytes = bytes,
            fileName = filePath.substringAfterLast('/')
        ).isSuccess
        if (sent) {
            repository.markRecordingUploaded(filePath)
            repository.removeRecording(filePath)   // уже в MinIO — освобождаем место
        }
        return sent
    }
}
