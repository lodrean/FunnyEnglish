package com.sotospeak.shared.model

import kotlinx.serialization.Serializable

/**
 * Обезличенное событие гостя для анонимной аналитики.
 * anonymousId == guestId локальной сессии (случайный UUID, без имён/email).
 */
@Serializable
data class GuestEventDto(
    val anonymousId: String,
    val type: String, // SESSION_STARTED | TEST_COMPLETED
    val testId: String? = null,
    val score: Int? = null,
    val maxScore: Int? = null,
    val timeSpentSeconds: Int? = null,
    val clientTimestamp: String? = null // ISO-8601
)

@Serializable
data class GuestEventsBatchRequest(
    val events: List<GuestEventDto>
)

@Serializable
data class GuestEventsBatchResponse(
    val accepted: Int
)
