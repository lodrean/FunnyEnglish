package com.sotospeak.controller

import com.sotospeak.entity.ClientLog
import com.sotospeak.entity.ClientLogLevel
import com.sotospeak.repository.ClientLogRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

// ==================== DTO ====================

data class ClientLogRequest(
    /** Клиентское время события (ISO-8601 с зоной) */
    @field:NotNull(message = "timestamp is required")
    val timestamp: Instant,

    @field:NotBlank(message = "level is required")
    val level: String,

    @field:NotBlank(message = "tag is required")
    @field:Size(max = 100)
    val tag: String,

    @field:NotBlank(message = "message is required")
    val message: String,

    val stackTrace: String? = null,

    @field:NotBlank(message = "platform is required")
    @field:Size(max = 20)
    val platform: String,

    @field:Size(max = 50)
    val appVersion: String? = null,

    /** Обезличенный guestId устройства; отсутствует у admin-web */
    val anonymousId: String? = null
)

data class ClientLogsBatchRequest(
    @field:NotNull
    @field:Size(min = 1, max = 50, message = "Batch size must be 1..50")
    val logs: List<@Valid ClientLogRequest>
)

data class ClientLogsBatchResponse(
    val accepted: Int
)

data class ClientLogResponse(
    val id: UUID,
    val anonymousId: UUID?,
    val level: String,
    val tag: String,
    val message: String,
    val stackTrace: String?,
    val platform: String,
    val appVersion: String?,
    val clientTimestamp: Instant?,
    val createdAt: Instant
)

private const val MAX_MESSAGE_LENGTH = 4 * 1024
private const val MAX_STACK_TRACE_LENGTH = 16 * 1024

/**
 * Приём клиентских логов WARN/ERROR с устройств (OpenSpec add-client-logging).
 * Публичный endpoint (префикс public — permitAll + rate limit по IP).
 * Невалидные записи отбрасываются поштучно — пакет не отклоняется целиком.
 */
@RestController
@RequestMapping("/public/logs")
class ClientLogController(
    private val clientLogRepository: ClientLogRepository
) {

    @PostMapping
    @Transactional
    fun submitLogs(
        @Valid @RequestBody request: ClientLogsBatchRequest
    ): ResponseEntity<ClientLogsBatchResponse> {
        val entities = request.logs.mapNotNull { e ->
            // клиенту не доверяем: невалидный уровень/anonymousId — запись отбрасывается
            val level = runCatching { ClientLogLevel.valueOf(e.level.uppercase()) }
                .getOrNull() ?: return@mapNotNull null
            val anonymousId = e.anonymousId?.let {
                runCatching { UUID.fromString(it) }.getOrNull() ?: return@mapNotNull null
            }

            ClientLog(
                anonymousId = anonymousId,
                level = level,
                tag = e.tag,
                message = e.message.take(MAX_MESSAGE_LENGTH),
                stackTrace = e.stackTrace?.take(MAX_STACK_TRACE_LENGTH),
                platform = e.platform,
                appVersion = e.appVersion,
                clientTimestamp = e.timestamp,
                createdAt = Instant.now()
            )
        }

        clientLogRepository.saveAll(entities)
        return ResponseEntity.ok(ClientLogsBatchResponse(accepted = entities.size))
    }
}

/**
 * Просмотр клиентских логов администратором (ROLE_ADMIN — по правилу /admin/…).
 */
@RestController
@RequestMapping("/admin/logs")
class AdminLogController(
    private val clientLogRepository: ClientLogRepository
) {

    @GetMapping
    @Transactional(readOnly = true)
    fun getLogs(
        @RequestParam(required = false) level: String?,
        @RequestParam(required = false) platform: String?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<ClientLogResponse> {
        val parsedLevel = level?.let {
            runCatching { ClientLogLevel.valueOf(it.uppercase()) }.getOrNull()
        }
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100))
        return clientLogRepository.search(
            level = parsedLevel,
            platform = platform?.takeIf { it.isNotBlank() },
            fromTs = from,
            toTs = to,
            q = q?.takeIf { it.isNotBlank() },
            pageable = pageable
        ).map { it.toResponse() }
    }

    private fun ClientLog.toResponse() = ClientLogResponse(
        id = id,
        anonymousId = anonymousId,
        level = level.name,
        tag = tag,
        message = message,
        stackTrace = stackTrace,
        platform = platform,
        appVersion = appVersion,
        clientTimestamp = clientTimestamp,
        createdAt = createdAt
    )
}
