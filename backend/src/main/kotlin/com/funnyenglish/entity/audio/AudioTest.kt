package com.funnyenglish.entity.audio

import com.funnyenglish.entity.Category
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audio_tests")
class AudioTest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "audio_file_url", nullable = false, length = 500)
    var audioFileUrl: String,

    @Column(name = "duration_seconds", nullable = false)
    var durationSeconds: Int,

    @Column(nullable = false)
    var difficulty: Int = 1, // 1-5

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category? = null,

    @Column(name = "is_published", nullable = false)
    var isPublished: Boolean = false,

    @Column(name = "plays_limit")
    var playsLimit: Int? = null, // null means unlimited

    @OneToMany(mappedBy = "audioTest", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    var questions: MutableSet<AudioTestQuestion> = mutableSetOf(),

    @OneToMany(mappedBy = "audioTest", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var transcripts: MutableSet<AudioTranscript> = mutableSetOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
) {
    fun addQuestion(question: AudioTestQuestion) {
        questions.add(question)
        question.audioTest = this
    }

    fun removeQuestion(question: AudioTestQuestion) {
        questions.remove(question)
        question.audioTest = null
    }

    fun addTranscript(transcript: AudioTranscript) {
        transcripts.add(transcript)
        transcript.audioTest = this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioTest) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

enum class QuestionType {
    LISTENING_COMPREHENSION,
    FILL_BLANK,
    TRUE_FALSE,
    DICTATION
}
