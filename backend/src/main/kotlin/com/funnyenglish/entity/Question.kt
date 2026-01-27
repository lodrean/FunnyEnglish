package com.funnyenglish.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "questions")
data class Question(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    val test: Test,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: QuestionType,

    val text: String? = null,

    @Column(name = "audio_url")
    val audioUrl: String? = null,

    @Column(name = "image_url")
    val imageUrl: String? = null,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,

    @Column(nullable = false)
    val points: Int = 1,

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val answers: MutableList<Answer> = mutableListOf()
)

enum class QuestionType {
    DRAG_DROP_IMAGE,    // Drag images to correct words
    AUDIO_SELECT,       // Listen and select correct answer
    IMAGE_SELECT,       // Select correct image
    TEXT_SELECT,        // Multiple choice text
    FILL_BLANK          // Fill in the blank
}
