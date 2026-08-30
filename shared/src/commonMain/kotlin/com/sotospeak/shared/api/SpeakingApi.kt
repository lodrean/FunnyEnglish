package com.sotospeak.shared.api

import com.sotospeak.shared.model.SpeakingLibrary
import com.sotospeak.shared.model.SpeakingSubmission
import com.sotospeak.shared.model.SpeakingTopicDetail
import com.sotospeak.shared.model.SpeakingTopicListItem

/**
 * Срез API: speaking-тренажёр (публичный контент + отправки Practice).
 * См. [AuthApi] — разбор монолита [SoToSpeakApi] (bd FunnyEnglish-5tf.5).
 */
interface SpeakingApi {
    /** Публичный контент (гость): библиотеки тем */
    suspend fun getSpeakingLibraries(): Result<List<SpeakingLibrary>>

    /** Публичный контент (гость): топики библиотеки */
    suspend fun getSpeakingTopics(libraryId: String): Result<List<SpeakingTopicListItem>>

    /** Публичный контент (гость): детали топика — видео + субтитры + вопросы */
    suspend fun getSpeakingTopicDetail(topicId: String): Result<SpeakingTopicDetail>

    /** Practice: загрузка голосовой записи (multipart, только авторизованным) */
    suspend fun submitSpeakingPractice(
        topicId: String,
        durationSec: Int,
        audioBytes: ByteArray,
        fileName: String = "recording.m4a"
    ): Result<SpeakingSubmission>

    /** Practice: мои отправки с оценками (только авторизованным) */
    suspend fun getMySpeakingSubmissions(): Result<List<SpeakingSubmission>>

    /** Загрузка текстового ресурса по URL (субтитры WebVTT из MinIO — не API-эндпоинт, спека Part 2 §3.3) */
    suspend fun getTextResource(url: String): Result<String>
}
