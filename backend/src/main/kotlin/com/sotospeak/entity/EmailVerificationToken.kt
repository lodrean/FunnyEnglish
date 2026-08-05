package com.sotospeak.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/** Одноразовый токен подтверждения email (OpenSpec add-email-verification). */
@Entity
@Table(name = "email_verification_tokens")
data class EmailVerificationToken(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(nullable = false, unique = true, length = 64)
    val token: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,

    @Column(name = "confirmed_at")
    var confirmedAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
