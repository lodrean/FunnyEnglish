package com.sotospeak.entity.speaking

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "topics")
class Topic(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    var library: Library? = null,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name = "is_published", nullable = false)
    var isPublished: Boolean = false,

    /** Soft delete: не-null = топик архивирован, записи учеников сохраняются */
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,

    @OneToOne(mappedBy = "topic", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var video: Video? = null,

    @OneToMany(mappedBy = "topic", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    var questions: MutableSet<SpeakingQuestion> = mutableSetOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
) {
    fun addQuestion(question: SpeakingQuestion) {
        questions.add(question)
        question.topic = this
    }

    fun removeQuestion(question: SpeakingQuestion) {
        questions.remove(question)
        question.topic = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Topic) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
