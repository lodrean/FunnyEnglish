package com.funnyenglish.entity.audio

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audio_test_questions")
class AudioTestQuestion(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_test_id", nullable = false)
    var audioTest: AudioTest? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 50)
    var questionType: QuestionType,

    @Column(length = 500)
    var title: String? = null,

    @Column(columnDefinition = "TEXT")
    var text: String? = null,

    @Column(name = "start_time_seconds", nullable = false)
    var startTimeSeconds: Int,

    @Column(name = "end_time_seconds", nullable = false)
    var endTimeSeconds: Int,

    @Column(nullable = false)
    var points: Int = 1,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    var answers: MutableSet<AudioTestAnswer> = mutableSetOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null
) {
    fun addAnswer(answer: AudioTestAnswer) {
        answers.add(answer)
        answer.question = this
    }

    fun removeAnswer(answer: AudioTestAnswer) {
        answers.remove(answer)
        answer.question = null
    }

    fun isActiveAtTime(currentTimeSeconds: Int): Boolean {
        return currentTimeSeconds in startTimeSeconds..endTimeSeconds
    }

    fun getDuration(): Int = endTimeSeconds - startTimeSeconds

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioTestQuestion) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
