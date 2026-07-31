package com.funnyenglish.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class GuestEventType {
    SESSION_STARTED,   // старт гостевой сессии (раз в день на устройстве)
    TEST_COMPLETED     // гость прошёл тест
}

/**
 * Обезличенное событие гостевого пользователя.
 *
 * anonymousId — случайный UUID, генерируемый на устройстве при первом запуске
 * (совпадает с guestId локальной GuestSession). Никаких имён/email/IP —
 * связать с конкретным человеком невозможно.
 *
 * convertedUserId проставляется при регистрации (merge-guest-progress) —
 * используется ТОЛЬКО для метрики конверсии.
 */
@Entity
@Table(name = "guest_events")
data class GuestEvent(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "anonymous_id", nullable = false)
    val anonymousId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: GuestEventType,

    @Column(name = "test_id")
    val testId: UUID? = null,

    val score: Int? = null,

    @Column(name = "max_score")
    val maxScore: Int? = null,

    @Column(name = "time_spent_seconds")
    val timeSpentSeconds: Int? = null,

    @Column(name = "converted_user_id")
    var convertedUserId: UUID? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
