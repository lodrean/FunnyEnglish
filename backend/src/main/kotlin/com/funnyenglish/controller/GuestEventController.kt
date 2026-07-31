package com.funnyenglish.controller

import com.funnyenglish.entity.GuestEvent
import com.funnyenglish.entity.GuestEventType
import com.funnyenglish.repository.GuestEventRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

// ==================== DTO ====================

data class GuestEventRequest(
    @field:NotBlank(message = "anonymousId is required")
    val anonymousId: String,

    @field:NotNull(message = "type is required")
    val type: GuestEventType,

    val testId: String? = null,

    @field:Min(0) val score: Int? = null,
    @field:Min(1) val maxScore: Int? = null,
    @field:Min(0) @field:Max(86400) val timeSpentSeconds: Int? = null,

    /** Клиентское время события (ISO-8601); если отсутствует — серверное */
    val clientTimestamp: Instant? = null
)

data class GuestEventsBatchRequest(
    @field:NotNull
    @field:Size(min = 1, max = 50, message = "Batch size must be 1..50")
    val events: List<@Valid GuestEventRequest>
)

data class GuestEventsBatchResponse(
    val accepted: Int
)

// ==================== Controller ====================

/**
 * Приём обезличенных событий гостевых пользователей.
 * Публичный endpoint (префикс public — permitAll + rate limit).
 */
@RestController
@RequestMapping("/public/guest-events")
class GuestEventController(
    private val guestEventRepository: GuestEventRepository
) {

    @PostMapping
    @Transactional
    fun submitEvents(
        @Valid @RequestBody request: GuestEventsBatchRequest
    ): ResponseEntity<GuestEventsBatchResponse> {
        val entities = request.events.mapNotNull { e ->
            val anonymousId = runCatching { UUID.fromString(e.anonymousId) }.getOrNull() ?: return@mapNotNull null
            // score не должен превышать maxScore (клиенту не доверяем)
            if (e.score != null && e.maxScore != null && e.score > e.maxScore) return@mapNotNull null

            GuestEvent(
                anonymousId = anonymousId,
                type = e.type,
                testId = e.testId?.let { runCatching { UUID.fromString(it) }.getOrNull() },
                score = e.score,
                maxScore = e.maxScore,
                timeSpentSeconds = e.timeSpentSeconds,
                createdAt = e.clientTimestamp ?: Instant.now()
            )
        }

        guestEventRepository.saveAll(entities)
        return ResponseEntity.ok(GuestEventsBatchResponse(accepted = entities.size))
    }
}
