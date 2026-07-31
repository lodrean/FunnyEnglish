package com.funnyenglish.app.util

import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.model.GuestEventDto
import com.funnyenglish.shared.repository.GuestProgressRepository

/**
 * Отправка обезличенных событий гостя на backend.
 *
 * События сначала попадают в локальную очередь (Settings), затем best-effort
 * отправляются batch'ем. При ошибке сети события остаются в очереди до
 * следующей попытки. Никаких имён/email — только случайный anonymousId.
 */
class GuestAnalytics(
    private val api: FunnyEnglishApi,
    private val guestRepo: GuestProgressRepository
) {

    /** Поставить событие в очередь и попробовать отправить накопленное */
    suspend fun track(event: GuestEventDto) {
        guestRepo.enqueueEvent(event)
        flush()
    }

    /** Отправить все накопленные события; при успехе очистить очередь */
    suspend fun flush() {
        val pending = guestRepo.getPendingEvents()
        if (pending.isEmpty()) return
        api.submitGuestEvents(pending)
            .onSuccess { guestRepo.clearPendingEvents() }
        // Ошибки игнорируем намеренно: аналитика не должна влиять на UX
    }
}
