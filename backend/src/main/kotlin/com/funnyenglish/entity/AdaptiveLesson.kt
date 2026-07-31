package com.funnyenglish.entity

import com.funnyenglish.shared.model.DifficultyLevel
import com.funnyenglish.shared.model.LessonStatus
import com.funnyenglish.shared.model.SegmentType
import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * Адаптивный урок - сущность для отслеживания прогресса
 */
@Entity
@Table(name = "adaptive_lessons")
data class AdaptiveLesson(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: LessonStatus = LessonStatus.IN_PROGRESS,

    @Enumerated(EnumType.STRING)
    @Column(name = "current_difficulty", nullable = false)
    var currentDifficulty: DifficultyLevel = DifficultyLevel.BEGINNER,

    @Column(name = "started_at", nullable = false)
    val startedAt: Instant = Instant.now(),

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @Column(name = "total_segments", nullable = false)
    val totalSegments: Int = 4,

    @Column(name = "current_segment_index", nullable = false)
    var currentSegmentIndex: Int = 0,

    @Column(name = "time_spent_seconds", nullable = false)
    var timeSpentSeconds: Int = 0,

    @Column(name = "questions_answered", nullable = false)
    var questionsAnswered: Int = 0,

    @Column(name = "correct_answers", nullable = false)
    var correctAnswers: Int = 0,

    @ElementCollection
    @CollectionTable(
        name = "adaptive_lesson_weak_areas",
        joinColumns = [JoinColumn(name = "lesson_id")]
    )
    @Column(name = "skill_type")
    var weakAreas: List<String> = emptyList(),

    @OneToMany(mappedBy = "lesson", cascade = [CascadeType.ALL], orphanRemoval = true)
    var segments: MutableList<LessonSegment> = mutableListOf(),

    @OneToMany(mappedBy = "lesson", cascade = [CascadeType.ALL], orphanRemoval = true)
    var questionHistory: MutableList<LessonQuestionHistory> = mutableListOf()
)

/**
 * Сегмент адаптивного урока
 */
@Entity
@Table(name = "lesson_segments")
data class LessonSegment(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    val lesson: AdaptiveLesson? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: SegmentType,

    @Column(name = "estimated_duration_seconds", nullable = false)
    val estimatedDurationSeconds: Int,

    @Column(name = "learning_objective")
    val learningObjective: String,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,

    @Column(name = "completed_at")
    var completedAt: Instant? = null
)

/**
 * История ответов на вопросы в уроке
 */
@Entity
@Table(name = "lesson_question_history")
data class LessonQuestionHistory(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    val lesson: AdaptiveLesson? = null,

    @Column(name = "question_id", nullable = false)
    val questionId: UUID,

    @Column(name = "answer_id")
    val answerId: UUID? = null,

    @Column(name = "is_correct", nullable = false)
    val isCorrect: Boolean = false,

    @Column(name = "time_spent_seconds", nullable = false)
    val timeSpentSeconds: Int = 0,

    @Column(name = "answered_at", nullable = false)
    val answeredAt: Instant = Instant.now(),

    @Column(name = "difficulty_at_time", nullable = false)
    val difficultyAtTime: String
)

/**
 * Пользовательские навыки (для отслеживания прогресса)
 */
@Entity
@Table(name = "user_skills")
data class UserSkill(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_type", nullable = false)
    val skillType: com.funnyenglish.shared.model.SkillType,

    @Column(name = "mastery_level", nullable = false)
    var masteryLevel: Float = 0.5f, // 0.0 - 1.0

    @Column(name = "questions_attempted", nullable = false)
    var questionsAttempted: Int = 0,

    @Column(name = "questions_correct", nullable = false)
    var questionsCorrect: Int = 0,

    @Column(name = "last_updated", nullable = false)
    var lastUpdated: Instant = Instant.now()
)
