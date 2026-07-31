package com.funnyenglish.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Сложность слова
 */
enum class WordDifficulty {
    EASY, MEDIUM, HARD
}

/**
 * Часть речи
 */
enum class PartOfSpeech {
    NOUN, VERB, ADJECTIVE, ADVERB, PRONOUN, PREPOSITION, CONJUNCTION, INTERJECTION
}

/**
 * Word - слово в словаре
 */
@Entity
@Table(name = "words")
data class Word(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 100)
    val word: String,

    @Column(length = 200)
    val transcription: String? = null,

    @Column(nullable = false, length = 200)
    val translation: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "part_of_speech")
    val partOfSpeech: PartOfSpeech? = null,

    @Column(name = "audio_url")
    val audioUrl: String? = null,

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    val exampleSentence: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    val difficulty: WordDifficulty = WordDifficulty.MEDIUM,

    @Column(length = 100)
    val category: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)

/**
 * Статус слова в словаре пользователя
 */
enum class UserWordStatus {
    NEW,        // Новое, только добавлено
    LEARNING,   // В процессе изучения
    LEARNED,    // Выучено
    HARD        // Сложное (требует повторения)
}

/**
 * UserWord - слово в словаре пользователя с прогрессом изучения
 */
@Entity
@Table(name = "user_words")
data class UserWord(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    val word: Word,

    @Column(nullable = false)
    val progress: Int = 0, // 0-100%

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: UserWordStatus = UserWordStatus.NEW,

    @Column(name = "added_at", nullable = false)
    val addedAt: Instant = Instant.now(),

    @Column(name = "last_reviewed_at")
    val lastReviewedAt: Instant? = null,

    @Column(name = "review_count", nullable = false)
    val reviewCount: Int = 0
) {
    /**
     * Обновить прогресс изучения
     */
    fun updateProgress(additionalProgress: Int): UserWord {
        val newProgress = minOf(100, progress + additionalProgress)
        val newStatus = when {
            newProgress >= 100 -> UserWordStatus.LEARNED
            newProgress > 0 -> UserWordStatus.LEARNING
            else -> status
        }
        return copy(
            progress = newProgress,
            status = newStatus,
            lastReviewedAt = Instant.now(),
            reviewCount = reviewCount + 1
        )
    }
}
