package com.funnyenglish.service

import com.funnyenglish.entity.Question
import com.funnyenglish.entity.QuestionType
import com.funnyenglish.shared.model.DifficultyAdjustment
import com.funnyenglish.shared.model.DifficultyLevel
import com.funnyenglish.shared.model.SegmentPerformance
import com.funnyenglish.shared.model.SkillGap
import com.funnyenglish.shared.model.SkillType
import org.springframework.stereotype.Component
import kotlin.math.*

/**
 * Движок адаптивной сложности уроков
 * Целевой success rate: 70-80%
 */
@Component
class DifficultyEngine {

    companion object {
        val TARGET_SUCCESS_RATE = 0.75 // 75% целевой показатель
        val SUCCESS_RATE_TOLERANCE = 0.05 // ±5% допуск
        val MIN_QUESTIONS_BEFORE_ADJUSTMENT = 3
    }

    /**
     * Определить следующую сложность на основе производительности
     */
    fun calculateNextDifficulty(
        currentDifficulty: DifficultyLevel,
        performanceHistory: List<SegmentPerformance>
    ): DifficultyAdjustment {
        
        if (performanceHistory.size < MIN_QUESTIONS_BEFORE_ADJUSTMENT) {
            return DifficultyAdjustment(
                newDifficulty = currentDifficulty,
                reason = "Недостаточно данных для оценки",
                confidenceScore = 0.3f
            )
        }

        // Берём последние 5 попыток для оценки
        val recentPerformance = performanceHistory.takeLast(5)
        val successRate = calculateSuccessRate(recentPerformance)
        val consistency = calculateConsistency(recentPerformance)
        
        return when {
            // Слишком легко (>85% успеха) + стабильно
            successRate > (TARGET_SUCCESS_RATE + SUCCESS_RATE_TOLERANCE) && consistency > 0.7 -> {
                DifficultyAdjustment(
                    newDifficulty = increaseDifficulty(currentDifficulty),
                    reason = "Отличная производительность (${(successRate * 100).toInt()}%). Повышаем сложность!",
                    confidenceScore = min(consistency + 0.2f, 0.9f)
                )
            }
            
            // Слишком сложно (<65% успеха)
            successRate < (TARGET_SUCCESS_RATE - SUCCESS_RATE_TOLERANCE) -> {
                DifficultyAdjustment(
                    newDifficulty = decreaseDifficulty(currentDifficulty),
                    reason = "Давай немного упростим для лучшего результата",
                    confidenceScore = min((1 - successRate) + 0.2f, 0.9f)
                )
            }
            
            // В целевом диапазоне
            else -> {
                DifficultyAdjustment(
                    newDifficulty = currentDifficulty,
                    reason = "Отличный темп! Продолжаем в том же духе",
                    confidenceScore = 0.8f
                )
            }
        }
    }

    /**
     * Подобрать вопросы под текущую сложность
     */
    fun selectQuestionsForDifficulty(
        availableQuestions: List<Question>,
        difficulty: DifficultyLevel,
        count: Int,
        weakAreas: List<SkillGap>
    ): List<Question> {
        
        // Фильтруем по сложности (если в вопросе есть метка сложности)
        val difficultyFiltered = availableQuestions.filter { question ->
            val questionDifficulty = estimateQuestionDifficulty(question)
            isDifficultyMatch(questionDifficulty, difficulty)
        }
        
        // Приоритизируем слабые области
        val prioritized = if (weakAreas.isNotEmpty()) {
            val weakAreaTypes = weakAreas.map { it.skillType }.toSet()
            difficultyFiltered.sortedByDescending { question ->
                if (weakAreaTypes.contains(getQuestionSkillType(question))) 1 else 0
            }
        } else {
            difficultyFiltered.shuffled()
        }
        
        return prioritized.take(count)
    }

    /**
     * Выявить слабые области на основе истории
     */
    fun identifyWeakAreas(performanceHistory: List<SegmentPerformance>): List<SkillGap> {
        if (performanceHistory.isEmpty()) return emptyList()
        
        // Группируем по типу навыка
        val bySkillType = performanceHistory.groupBy { 
            getSkillTypeFromSegment(it.segmentId) 
        }
        
        return bySkillType.map { (skillType, performances) ->
            val successRate = calculateSuccessRate(performances)
            SkillGap(
                skillType = skillType,
                masteryLevel = successRate,
                relatedQuestions = performances.map { it.segmentId }
            )
        }.filter { it.masteryLevel < 0.7f } // Только те, что ниже 70%
         .sortedBy { it.masteryLevel }
    }

    /**
     * Проверить, нужен ли перерыв
     */
    fun shouldRequireBreak(timeSpentSeconds: Int, segmentCount: Int): Boolean {
        // Перерыв после 10 минут или 5 сегментов
        return timeSpentSeconds >= 600 || segmentCount >= 5
    }

    /**
     * Рассчитать прогресс урока
     */
    fun calculateLessonProgress(
        completedSegments: Int,
        totalSegments: Int,
        currentSegmentProgress: Float
    ): Float {
        if (totalSegments == 0) return 0f
        
        val baseProgress = completedSegments.toFloat() / totalSegments
        val segmentContribution = (1f / totalSegments) * currentSegmentProgress
        
        return (baseProgress + segmentContribution).coerceIn(0f, 1f)
    }

    /**
     * Оценить, готов ли пользователь к следующему уровню
     */
    fun isReadyForNextLevel(
        currentLevel: DifficultyLevel,
        performanceHistory: List<SegmentPerformance>
    ): Boolean {
        if (performanceHistory.size < 10) return false
        
        val recentSuccessRate = calculateSuccessRate(performanceHistory.takeLast(10))
        val consistency = calculateConsistency(performanceHistory.takeLast(10))
        
        // Готов если >80% успеха и стабильность >0.8
        return recentSuccessRate > 0.8 && consistency > 0.8
    }

    // ==================== Private Methods ====================

    private fun calculateSuccessRate(performances: List<SegmentPerformance>): Float {
        if (performances.isEmpty()) return 0f
        
        val totalCorrect = performances.sumOf { it.correctAnswers }
        val totalQuestions = performances.sumOf { it.totalAnswers }
        
        return if (totalQuestions > 0) {
            totalCorrect.toFloat() / totalQuestions
        } else {
            0f
        }
    }

    private fun calculateConsistency(performances: List<SegmentPerformance>): Float {
        if (performances.size < 2) return 1f
        
        // Рассчитываем стандартное отклонение успеха
        val successRates = performances.map { 
            if (it.totalAnswers > 0) it.correctAnswers.toFloat() / it.totalAnswers else 0f
        }
        
        val mean = successRates.average().toFloat()
        val variance = successRates.map { (it - mean).pow(2) }.average().toFloat()
        val stdDev = sqrt(variance.toDouble()).toFloat()
        
        // Конвертируем в consistency score (ниже stdDev = выше consistency)
        return (1f - stdDev).coerceIn(0f, 1f)
    }

    private fun increaseDifficulty(current: DifficultyLevel): DifficultyLevel {
        return when (current) {
            DifficultyLevel.BEGINNER -> DifficultyLevel.ELEMENTARY
            DifficultyLevel.ELEMENTARY -> DifficultyLevel.INTERMEDIATE
            DifficultyLevel.INTERMEDIATE -> DifficultyLevel.ADVANCED
            DifficultyLevel.ADVANCED -> DifficultyLevel.ADVANCED // Максимум
        }
    }

    private fun decreaseDifficulty(current: DifficultyLevel): DifficultyLevel {
        return when (current) {
            DifficultyLevel.BEGINNER -> DifficultyLevel.BEGINNER // Минимум
            DifficultyLevel.ELEMENTARY -> DifficultyLevel.BEGINNER
            DifficultyLevel.INTERMEDIATE -> DifficultyLevel.ELEMENTARY
            DifficultyLevel.ADVANCED -> DifficultyLevel.INTERMEDIATE
        }
    }

    private fun estimateQuestionDifficulty(question: Question): DifficultyLevel {
        // Эвристика на основе длины текста, типа вопроса и т.д.
        val textLength = question.text?.length ?: 0
        val answerCount = question.answers.size
        
        return when {
            textLength < 30 && answerCount <= 3 -> DifficultyLevel.BEGINNER
            textLength < 60 && answerCount <= 4 -> DifficultyLevel.ELEMENTARY
            textLength < 100 -> DifficultyLevel.INTERMEDIATE
            else -> DifficultyLevel.ADVANCED
        }
    }

    private fun isDifficultyMatch(
        questionDifficulty: DifficultyLevel,
        targetDifficulty: DifficultyLevel
    ): Boolean {
        // Допускаем соседние уровни сложности
        val levels = DifficultyLevel.entries
        val questionIndex = levels.indexOf(questionDifficulty)
        val targetIndex = levels.indexOf(targetDifficulty)
        
        return abs(questionIndex - targetIndex) <= 1
    }

    private fun getQuestionSkillType(question: Question): SkillType {
        return when (question.type) {
            QuestionType.TEXT_SELECT -> SkillType.VOCABULARY_NOUNS
            QuestionType.AUDIO_SELECT -> SkillType.LISTENING
            QuestionType.IMAGE_SELECT -> SkillType.VOCABULARY_NOUNS
            QuestionType.FILL_BLANK -> SkillType.GRAMMAR_TENSES
            QuestionType.DRAG_DROP_IMAGE -> SkillType.GRAMMAR_ARTICLES
            QuestionType.DRAG_DROP_MATCH -> SkillType.GRAMMAR_ARTICLES
            QuestionType.DRAG_DROP_SORT -> SkillType.GRAMMAR_TENSES
            QuestionType.IMAGE_WORD_MATCH -> SkillType.VOCABULARY_NOUNS
        }
    }

    private fun getSkillTypeFromSegment(segmentId: String): SkillType {
        // Парсим тип навыка из ID сегмента
        return when {
            segmentId.contains("grammar") -> SkillType.GRAMMAR_TENSES
            segmentId.contains("vocab") -> SkillType.VOCABULARY_NOUNS
            segmentId.contains("listen") -> SkillType.LISTENING
            else -> SkillType.VOCABULARY_NOUNS
        }
    }
}
