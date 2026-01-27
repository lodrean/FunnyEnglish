package com.funnyenglish.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "answers")
data class Answer(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    val text: String? = null,

    @Column(name = "image_url")
    val imageUrl: String? = null,

    @Column(name = "audio_url")
    val audioUrl: String? = null,

    @Column(name = "is_correct", nullable = false)
    val isCorrect: Boolean = false,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,

    @Column(name = "match_target")
    val matchTarget: String? = null  // For drag-drop: what this should match to
)
