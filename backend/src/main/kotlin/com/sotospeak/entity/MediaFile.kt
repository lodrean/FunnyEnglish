package com.sotospeak.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Тип медиа файла
 */
enum class MediaType {
    IMAGE,
    AUDIO,
    VIDEO
}

/**
 * Media File - загруженные файлы преподавателя
 * Хранятся в S3/MinIO, в БД - метаданные
 */
@Entity
@Table(name = "media_files")
data class MediaFile(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val filename: String,

    @Column(nullable = false, length = 500)
    val url: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val type: MediaType,

    @Column(name = "content_type")
    val contentType: String? = null, // image/png, audio/mpeg, etc.

    @Column(name = "size_bytes")
    val sizeBytes: Long = 0,

    @Column
    val width: Int? = null, // Для изображений

    @Column
    val height: Int? = null, // Для изображений

    @Column(name = "duration_seconds")
    val durationSeconds: Int? = null, // Для аудио/видео

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    val uploadedBy: User? = null,

    @Column(name = "uploaded_at", nullable = false)
    val uploadedAt: Instant = Instant.now(),

    @Column(name = "folder")
    val folder: String? = null, // questions, answers, content, etc.

    @Column(name = "is_archived", nullable = false)
    val isArchived: Boolean = false
)

/**
 * Media Library - быстрый доступ к часто используемым медиа
 * Позволяет преподавателю переиспользовать картинки между вопросами
 */
@Entity
@Table(name = "media_library_items")
data class MediaLibraryItem(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    val mediaFile: MediaFile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column
    val tag: String? = null, // Для поиска: "fruits", "animals", etc.

    @Column(name = "usage_count", nullable = false)
    val usageCount: Int = 0,

    @Column(name = "added_at", nullable = false)
    val addedAt: Instant = Instant.now()
)
