package com.sotospeak.shared.model

import kotlinx.serialization.Serializable

/**
 * Клиентская запись лога для отправки на backend (OpenSpec add-client-logging).
 * Отправляются только WARN/ERROR, без тел запросов/токенов.
 * timestamp — ISO-8601 с зоной (Clock.System.now().toString(), НЕ LocalDateTime — memory.md №52).
 */
@Serializable
data class ClientLogDto(
    val timestamp: String,
    val level: String, // WARN | ERROR
    val tag: String,
    val message: String,
    val stackTrace: String? = null,
    val platform: String, // android | desktop | wasm | admin-web
    val appVersion: String? = null,
    val anonymousId: String? = null
)

@Serializable
data class ClientLogsBatchRequest(
    val logs: List<ClientLogDto>
)

@Serializable
data class ClientLogsBatchResponse(
    val accepted: Int
)
