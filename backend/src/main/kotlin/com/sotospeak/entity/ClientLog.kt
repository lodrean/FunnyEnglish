package com.sotospeak.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class ClientLogLevel { WARN, ERROR }

/**
 * Клиентская запись лога (WARN/ERROR), присланная устройством пользователя.
 * anonymousId — обезличенный guestId (как в guest_events), может отсутствовать.
 * PII (тела запросов, токены) клиент не отправляет — см. OpenSpec add-client-logging.
 */
@Entity
@Table(name = "client_logs")
data class ClientLog(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "anonymous_id")
    val anonymousId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val level: ClientLogLevel,

    @Column(nullable = false, length = 100)
    val tag: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val message: String,

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    val stackTrace: String? = null,

    @Column(nullable = false, length = 20)
    val platform: String,

    @Column(name = "app_version", length = 50)
    val appVersion: String? = null,

    /** Клиентское время события (может отставать от created_at при офлайн-накоплении) */
    @Column(name = "client_timestamp")
    val clientTimestamp: Instant? = null,

    /** Серверное время приёма */
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
