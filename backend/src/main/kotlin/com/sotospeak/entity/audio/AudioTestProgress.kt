package com.sotospeak.entity.audio

import com.sotospeak.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audio_test_progress")
class AudioTestProgress(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_test_id", nullable = false)
    var audioTest: AudioTest? = null,

    @Column(nullable = false)
    var score: Int = 0,

    @Column(name = "max_score", nullable = false)
    var maxScore: Int = 0,

    @Column(nullable = false)
    var stars: Int = 0,

    @Column(name = "attempts_count", nullable = false)
    var attemptsCount: Int = 0,

    @Column(name = "best_score", nullable = false)
    var bestScore: Int = 0,

    @Column(name = "time_spent_seconds")
    var timeSpentSeconds: Int? = null,

    @Column(name = "plays_used", nullable = false)
    var playsUsed: Int = 0,

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "last_attempt_at", nullable = false)
    var lastAttemptAt: Instant? = null
) {
    fun incrementAttempts() {
        attemptsCount++
    }

    fun recordPlay() {
        playsUsed++
    }

    fun canPlay(): Boolean {
        val limit = audioTest?.playsLimit
        return limit == null || playsUsed < limit
    }

    fun getPercentage(): Int {
        return if (maxScore > 0) (score * 100) / maxScore else 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioTestProgress) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
