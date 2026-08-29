package com.sotospeak.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Refresh-токен (bd FunnyEnglish-nj2.7). Хранится только SHA-256-хэш — утечка таблицы
 * не компрометирует токены. Одноразовый: после обмена rotated_at заполнен, повторное
 * использование = reuse-detection (отзыв всей цепочки пользователя).
 */
@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(nullable = false, unique = true, length = 64)
    val jti: String,

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    val tokenHash: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,

    @Column(name = "rotated_at")
    var rotatedAt: Instant? = null,

    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
