package com.sotospeak.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * JPA Entity для хранения IMAGE_WORD_MATCH вопросов
 * Отдельная таблица т.к. JSONB content временно отключен
 */
@Entity
@Table(name = "image_word_match_questions")
data class ImageWordMatchQuestionEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "question_id", nullable = false, unique = true)
    val questionId: UUID,

    @Column(name = "test_id", nullable = false)
    val testId: UUID,

    @Column(name = "image_url", nullable = false, length = 500)
    val imageUrl: String,

    @Column(name = "instruction", nullable = false, length = 500)
    val instruction: String,

    @Column(name = "points", nullable = false)
    val points: Int = 10,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
)

/**
 * Слово для IMAGE_WORD_MATCH вопроса
 */
@Entity
@Table(name = "image_word_match_words")
data class ImageWordMatchWordEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "question_id", nullable = false)
    val questionId: UUID,

    @Column(name = "word_id", nullable = false, length = 50)
    val wordId: String,  // Client-generated ID

    @Column(name = "text", nullable = false, length = 100)
    val text: String,

    @Column(name = "translation", length = 100)
    val translation: String? = null,

    @Column(name = "audio_url", length = 500)
    val audioUrl: String? = null,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0
)

/**
 * Hotspot (область) для IMAGE_WORD_MATCH вопроса
 */
@Entity
@Table(name = "image_word_match_hotspots")
data class ImageWordMatchHotspotEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "question_id", nullable = false)
    val questionId: UUID,

    @Column(name = "hotspot_id", nullable = false, length = 50)
    val hotspotId: String,  // Client-generated ID

    @Column(name = "x", nullable = false)
    val x: Float,

    @Column(name = "y", nullable = false)
    val y: Float,

    @Column(name = "width", nullable = false)
    val width: Float,

    @Column(name = "height", nullable = false)
    val height: Float,

    @Enumerated(EnumType.STRING)
    @Column(name = "shape", nullable = false, length = 20)
    val shape: HotspotShape = HotspotShape.RECTANGLE,

    @Column(name = "word_id", nullable = false, length = 50)
    val wordId: String  // Reference to ImageWordMatchWordEntity.wordId
)
