package com.sotospeak.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "progress",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "test_id"])]
)
class Progress(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    val test: Test,

    @Column(nullable = false)
    var score: Int = 0,

    @Column(name = "max_score", nullable = false)
    var maxScore: Int,

    @Column(nullable = false)
    var stars: Int = 0,  // 1-3 based on percentage

    @Column(name = "attempts_count", nullable = false)
    var attemptsCount: Int = 1,

    @Column(name = "best_score", nullable = false)
    var bestScore: Int = 0,

    @Column(name = "time_spent_seconds")
    var timeSpentSeconds: Int? = null,

    @Column(name = "completed_at", nullable = false)
    val completedAt: Instant = Instant.now(),

    @Column(name = "last_attempt_at", nullable = false)
    var lastAttemptAt: Instant = Instant.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Progress) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
