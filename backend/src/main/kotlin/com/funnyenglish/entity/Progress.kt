package com.funnyenglish.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "progress",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "test_id"])]
)
data class Progress(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    val test: Test,

    @Column(nullable = false)
    val score: Int = 0,

    @Column(name = "max_score", nullable = false)
    val maxScore: Int,

    @Column(nullable = false)
    val stars: Int = 0,  // 1-3 based on percentage

    @Column(name = "attempts_count", nullable = false)
    val attemptsCount: Int = 1,

    @Column(name = "best_score", nullable = false)
    val bestScore: Int = 0,

    @Column(name = "time_spent_seconds")
    val timeSpentSeconds: Int? = null,

    @Column(name = "completed_at", nullable = false)
    val completedAt: Instant = Instant.now(),

    @Column(name = "last_attempt_at", nullable = false)
    val lastAttemptAt: Instant = Instant.now()
)
