package com.sotospeak.app.data

import com.sotospeak.app.storage.RecordingKind
import com.sotospeak.app.storage.RecordingMeta
import com.sotospeak.app.storage.RecordingStore
import com.sotospeak.shared.api.SpeakingApi
import com.sotospeak.shared.contracts.SpeakingLibrary
import com.sotospeak.shared.contracts.SpeakingSubmission
import com.sotospeak.shared.contracts.SpeakingTopicDetail
import com.sotospeak.shared.contracts.SpeakingTopicListItem

/**
 * Speaking-домен (библиотеки/топики/вопросы/записи): единая точка доступа VM
 * к сети ([SpeakingApi]) и локальным метаданным записей ([RecordingStore]).
 *
 * Умеренный разбор монолита SoToSpeakApi (bd FunnyEnglish-5tf.5, К4 §2.2
 * PROJECT-REVIEW-2026-08-28): VM больше не ходят в сеть напрямую.
 * Репозиторий намеренно тонкий. Маппинг ошибок ApiException → UiText —
 * `Throwable.toUiText()` (app/error/UiText.kt, bd FunnyEnglish-5tf.6,
 * предложение 5 того же обзора), применяется VM в onFailure.
 */
class SpeakingRepository(
    private val api: SpeakingApi,
    private val recordingStore: RecordingStore
) {
    // ---- Сеть ----

    suspend fun getLibraries(): Result<List<SpeakingLibrary>> = api.getSpeakingLibraries()

    suspend fun getTopics(libraryId: String): Result<List<SpeakingTopicListItem>> =
        api.getSpeakingTopics(libraryId)

    suspend fun getTopicDetail(topicId: String): Result<SpeakingTopicDetail> =
        api.getSpeakingTopicDetail(topicId)

    suspend fun getMySubmissions(): Result<List<SpeakingSubmission>> =
        api.getMySpeakingSubmissions()

    suspend fun submitPractice(
        topicId: String,
        durationSec: Int,
        audioBytes: ByteArray,
        fileName: String
    ): Result<SpeakingSubmission> =
        api.submitSpeakingPractice(topicId, durationSec, audioBytes, fileName)

    /** Субтитры WebVTT по URL (медиа-хост, не API — см. SpeakingApi.getTextResource). */
    suspend fun getTextResource(url: String): Result<String> = api.getTextResource(url)

    // ---- Локальные записи (RecordingStore) ----

    fun listRecordings(topicId: String? = null): List<RecordingMeta> = recordingStore.list(topicId)

    /**
     * topicId с TRAINING-записями — один снапшот кэша store на весь проход (bd 5tf.7),
     * вместо list() на каждый топик в LibraryViewModel.loadProgress.
     */
    fun trainingTopicIds(): Set<String> = recordingStore.recordedTopicIds(RecordingKind.TRAINING)

    fun findRecording(filePath: String): RecordingMeta? =
        recordingStore.list().firstOrNull { it.filePath == filePath }

    fun addRecording(meta: RecordingMeta) = recordingStore.add(meta)

    /** Удаляет метаданные и сам файл. */
    fun removeRecording(filePath: String) = recordingStore.remove(filePath)

    /** «Начать заново» в Training: удаляет TRAINING-записи топика (PRACTICE не трогает). */
    fun removeTrainingAttempts(topicId: String) = recordingStore.removeAllForTopic(topicId)

    fun markRecordingUploaded(filePath: String) = recordingStore.markUploaded(filePath)

    /** Неотправленные PRACTICE-записи (offline retry, спека §6.4). */
    fun pendingPracticeUploads(): List<RecordingMeta> = recordingStore.pendingPractice()
}
