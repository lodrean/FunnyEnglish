package com.sotospeak.shared.api

import com.sotospeak.shared.model.ClientLogDto
import com.sotospeak.shared.model.ClientLogsBatchResponse
import com.sotospeak.shared.model.GuestEventDto
import com.sotospeak.shared.model.GuestEventsBatchResponse
import com.sotospeak.shared.model.MergeGuestProgressRequest
import com.sotospeak.shared.model.MergeGuestProgressResponse

/**
 * Срез API: гостевой режим и публичные best-effort эндпоинты
 * (анонимная аналитика, клиентские логи, merge прогресса при регистрации).
 * См. [AuthApi] — разбор монолита [SoToSpeakApi] (bd FunnyEnglish-5tf.5).
 */
interface GuestApi {
    suspend fun mergeGuestProgress(request: MergeGuestProgressRequest): Result<MergeGuestProgressResponse>

    /** Отправка обезличенных событий гостя (анонимная аналитика), best-effort. */
    suspend fun submitGuestEvents(events: List<GuestEventDto>): Result<GuestEventsBatchResponse>

    /** Отправка клиентских логов WARN/ERROR (OpenSpec add-client-logging), best-effort. */
    suspend fun sendLogs(logs: List<ClientLogDto>): Result<ClientLogsBatchResponse>
}
