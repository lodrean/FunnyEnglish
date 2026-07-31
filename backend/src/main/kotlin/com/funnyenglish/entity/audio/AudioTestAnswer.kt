package com.funnyenglish.entity.audio

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "audio_test_answers")
class AudioTestAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    var question: AudioTestQuestion? = null,

    @Column(columnDefinition = "TEXT", nullable = false)
    var text: String,

    @Column(name = "is_correct", nullable = false)
    var isCorrect: Boolean = false,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name = "match_target", length = 255)
    var matchTarget: String? = null // For drag-drop matching if needed in future
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioTestAnswer) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
