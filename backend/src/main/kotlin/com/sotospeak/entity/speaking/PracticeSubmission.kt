package com.sotospeak.entity.speaking

import com.sotospeak.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

enum class SubmissionStatus { NEW, REVIEWED }

@Entity
@Table(name = "practice_submissions")
class PracticeSubmission(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    var topic: Topic? = null,

    @Column(name = "audio_url", nullable = false, length = 500)
    var audioUrl: String,

    @Column(name = "duration_sec", nullable = false)
    var durationSec: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: SubmissionStatus = SubmissionStatus.NEW,

    @OneToOne(mappedBy = "submission", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var grade: Grade? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PracticeSubmission) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
