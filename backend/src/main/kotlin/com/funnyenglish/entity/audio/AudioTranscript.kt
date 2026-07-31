package com.funnyenglish.entity.audio

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audio_transcripts")
class AudioTranscript(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_test_id", nullable = false)
    var audioTest: AudioTest? = null,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Column(length = 10, nullable = false)
    var language: String = "en",

    @Column(name = "is_generated", nullable = false)
    var isGenerated: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioTranscript) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
