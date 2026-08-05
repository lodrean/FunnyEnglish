package com.sotospeak.entity.audio

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audio_test_user_answers")
class AudioTestUserAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progress_id", nullable = false)
    var progress: AudioTestProgress? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    var question: AudioTestQuestion? = null,

    @Column(name = "selected_answer_ids")
    var selectedAnswerIds: MutableList<UUID> = mutableListOf(),

    @Column(name = "text_answer", columnDefinition = "TEXT")
    var textAnswer: String? = null, // For dictation/fill blank

    @Column(name = "is_correct", nullable = false)
    var isCorrect: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioTestUserAnswer) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
