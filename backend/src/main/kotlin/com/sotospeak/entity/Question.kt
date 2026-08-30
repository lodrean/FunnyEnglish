package com.sotospeak.entity

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

/**
 * Расширяемые типы вопросов
 * Легко добавлять новые типы без изменения БД благодаря JSONB content
 */
enum class QuestionType {
    // MVP Types
    TEXT_SELECT,      // Выбор текстового ответа
    IMAGE_SELECT,     // Выбор картинки/эмодзи
    AUDIO_SELECT,     // Аудирование + выбор
    DRAG_DROP_MATCH,  // Перетаскивание: соединить элементы
    DRAG_DROP_SORT,   // Перетаскивание: упорядочить
    FILL_BLANK,       // Заполнить пропуск
    IMAGE_WORD_MATCH, // NEW: Перетаскивание слов к областям на изображении
    
    // Legacy (для обратной совместимости)
    DRAG_DROP_IMAGE   // @Deprecated: используйте DRAG_DROP_MATCH
}

/**
 * Question - вопрос в тесте/уроке с гибким JSON content
 * 
 * content хранится как JSONB в PostgreSQL, что позволяет:
 * 1. Иметь разную структуру для разных типов вопросов
 * 2. Легко добавлять новые типы без миграций
 * 3. Индексировать и запрашивать внутри JSON
 */
@Entity
@Table(name = "questions")
class Question(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    val test: Test? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: QuestionType,

    @Column(nullable = false)
    var title: String,

    @Column(name = "text", columnDefinition = "TEXT")
    var text: String? = null, // Legacy field, migrated to content

    @Column(name = "audio_url")
    var audioUrl: String? = null, // Legacy field, migrated to content

    @Column(name = "image_url")
    var imageUrl: String? = null, // Legacy field

    @Column(name = "media_url")
    var mediaUrl: String? = null, // For question media (image/audio/video)

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(nullable = false)
    var points: Int = 1,

    /**
     * Гибкий JSON content - структура зависит от type
     * 
     * NOTE: Временно отключено из-за проблем с десериализацией старых данных.
     * Используйте legacy поля: text, imageUrl, audioUrl, answers
     */
    // @JdbcTypeCode(SqlTypes.JSON)
    // @Column(name = "content", columnDefinition = "jsonb")
    // val content: QuestionContent? = null,

    @Column(name = "time_limit_seconds")
    var timeLimitSeconds: Int? = null,

    @Column(name = "explanation", columnDefinition = "TEXT")
    var explanation: String? = null,

    @Column(name = "hint")
    var hint: String? = null,

    @Column(name = "grammar_note", columnDefinition = "TEXT")
    var grammarNote: String? = null,

    @Column(name = "is_published", nullable = false)
    var isPublished: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    // Legacy relationship - answers now stored in content JSON
    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val answers: MutableList<Answer> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Question) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Базовый sealed interface для content (type safety)
 * Аннотации Jackson для полиморфной десериализации JSONB
 * Используем DEDUCTION для автоматического определения типа по полям JSON
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.DEDUCTION
)
@JsonSubTypes(
    JsonSubTypes.Type(TextSelectContent::class),
    JsonSubTypes.Type(ImageSelectContent::class),
    JsonSubTypes.Type(AudioSelectContent::class),
    JsonSubTypes.Type(DragDropMatchContent::class),
    JsonSubTypes.Type(DragDropSortContent::class),
    JsonSubTypes.Type(FillBlankContent::class),
    JsonSubTypes.Type(ImageWordMatchContent::class)
)
sealed interface QuestionContent

data class TextSelectContent(
    val text: String,
    val answers: List<AnswerOptionData>
) : QuestionContent

data class ImageSelectContent(
    val text: String? = null,
    val answers: List<ImageAnswerOptionData>
) : QuestionContent

data class AudioSelectContent(
    val audioUrl: String,
    val transcript: String? = null,
    val text: String? = null,
    val answers: List<AnswerOptionData>
) : QuestionContent

data class DragDropMatchContent(
    val text: String,
    val items: List<DragItemData>,
    val targets: List<DropTargetData>
) : QuestionContent

data class DragDropSortContent(
    val text: String,
    val items: List<SortItemData>
) : QuestionContent

data class FillBlankContent(
    val textBefore: String,
    val textAfter: String,
    val answers: List<AnswerOptionData>
) : QuestionContent

// Data classes for JSON content
data class AnswerOptionData(
    val id: String,
    val text: String,
    val isCorrect: Boolean = false
)

data class ImageAnswerOptionData(
    val id: String,
    val imageUrl: String? = null,
    val emoji: String? = null,
    val text: String? = null,
    val isCorrect: Boolean = false
)

data class DragItemData(
    val id: String,
    val text: String,
    val targetId: String
)

data class DropTargetData(
    val id: String,
    val imageUrl: String? = null,
    val emoji: String? = null,
    val text: String? = null
)

data class SortItemData(
    val id: String,
    val text: String,
    val correctOrder: Int
)
