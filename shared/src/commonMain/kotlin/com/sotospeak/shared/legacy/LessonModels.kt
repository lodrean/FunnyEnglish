package com.sotospeak.shared.legacy

import kotlinx.serialization.Serializable

/**
 * Статус адаптивного урока
 */
@Serializable
enum class LessonStatus {
    IN_PROGRESS,
    ON_BREAK,
    COMPLETED,
    ABANDONED
}

/**
 * Тип сегмента урока
 */
@Serializable
enum class SegmentType {
    INTRO,
    PRACTICE,
    CHALLENGE,
    REVIEW,
    GRAMMAR_HINT
}

/**
 * Уровень сложности
 */
@Serializable
enum class DifficultyLevel {
    BEGINNER,
    ELEMENTARY,
    INTERMEDIATE,
    ADVANCED
}

/**
 * Тип навыка
 */
@Serializable
enum class SkillType {
    GRAMMAR_ARTICLES,
    GRAMMAR_TENSES,
    VOCABULARY_NOUNS,
    VOCABULARY_VERBS,
    VOCABULARY_ADJECTIVES,
    PRONUNCIATION,
    LISTENING,
    READING
}

/**
 * Тип игрового события
 */
@Serializable
sealed class GameEvent {
    abstract val timestamp: String
    
    @Serializable
    data class LessonCompleted(
        val lessonId: String,
        val score: Int,
        val maxScore: Int,
        val percentage: Int,
        val timeSpent: Int,
        override val timestamp: String
    ) : GameEvent()
    
    @Serializable
    data class StreakActivity(
        val streakDay: Int,
        override val timestamp: String
    ) : GameEvent()
    
    @Serializable
    data class WordMastered(
        val wordId: String,
        val masteryLevel: Float,
        override val timestamp: String
    ) : GameEvent()
    
    @Serializable
    data class ExerciseTypeTried(
        val type: QuestionType,
        override val timestamp: String
    ) : GameEvent()
    
    @Serializable
    data class QuestCompleted(
        val questId: String,
        override val timestamp: String
    ) : GameEvent()
}
