package com.sotospeak.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Lesson entity - структурированный урок для обучения
 */
@Entity
@Table(name = "lessons")
data class Lesson(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    val category: Category? = null,

    @Column(name = "icon_url")
    val iconUrl: String? = null,

    @Column(length = 10)
    val emoji: String? = null,

    @Column(name = "duration_minutes")
    val durationMinutes: Int = 5,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,

    @Column(name = "is_published", nullable = false)
    val isPublished: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
)

/**
 * Learning Path - путь обучения (последовательность уроков)
 */
@Entity
@Table(name = "learning_paths")
data class LearningPath(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "learningPath", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    val pathLessons: MutableList<PathLesson> = mutableListOf()
)

/**
 * Связь LearningPath с Lesson (порядок уроков в пути)
 */
@Entity
@Table(name = "path_lessons")
data class PathLesson(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "path_id", nullable = false)
    val learningPath: LearningPath,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    val lesson: Lesson,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,

    @Column(name = "is_required", nullable = false)
    val isRequired: Boolean = true
)

/**
 * Прогресс пользователя по пути обучения
 */
@Entity
@Table(name = "user_path_progress")
data class UserPathProgress(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "path_id", nullable = false)
    val learningPath: LearningPath,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_lesson_id")
    val currentLesson: Lesson? = null,

    @Column(name = "completed_lessons", nullable = false)
    val completedLessons: Int = 0,

    @Column(name = "total_lessons", nullable = false)
    val totalLessons: Int = 0,

    @Column(name = "started_at")
    val startedAt: Instant? = null,

    @Column(name = "completed_at")
    val completedAt: Instant? = null,

    @Column(name = "last_activity_at")
    val lastActivityAt: Instant? = null
)

/**
 * Завершенный урок пользователем
 */
@Entity
@Table(name = "completed_lessons")
data class CompletedLesson(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    val lesson: Lesson,

    @Column(name = "completed_at", nullable = false)
    val completedAt: Instant = Instant.now(),

    @Column(name = "xp_earned")
    val xpEarned: Int = 0,

    @Column(name = "time_spent_seconds")
    val timeSpentSeconds: Int = 0
)
