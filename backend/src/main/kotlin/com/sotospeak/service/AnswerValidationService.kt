package com.sotospeak.service

import com.sotospeak.dto.*
import com.sotospeak.entity.ImageWordMatchContent
import org.springframework.stereotype.Service

/**
 * Сервис для валидации ответов на различные типы вопросов
 */
@Service
class AnswerValidationService {

    /**
     * Валидирует ответы для вопроса типа IMAGE_WORD_MATCH
     * 
     * @param content контент вопроса с правильными ответами
     * @param submittedMatches список сопоставлений слово->hotspot от пользователя
     * @return результат проверки с деталями
     */
    fun validateImageWordMatch(
        content: ImageWordMatchContent,
        submittedMatches: List<WordHotspotMatch>
    ): ImageWordMatchResultResponse {
        // Создаем мапу правильных ответов: wordId -> hotspotId
        val correctMapping = content.hotspots.associate { it.wordId to it.id }
        
        // Проверяем каждое слово
        val details = content.words.map { word ->
            val submittedMatch = submittedMatches.find { it.wordId == word.id }
            val correctHotspotId = correctMapping[word.id]
            
            if (submittedMatch != null) {
                // Пользователь сделал выбор для этого слова
                val isCorrect = submittedMatch.hotspotId == correctHotspotId
                MatchResultDetail(
                    wordId = word.id,
                    wordText = word.text,
                    selectedHotspotId = submittedMatch.hotspotId,
                    isCorrect = isCorrect,
                    correctHotspotId = if (isCorrect) null else correctHotspotId
                )
            } else {
                // Пользователь не сделал выбор для этого слова
                MatchResultDetail(
                    wordId = word.id,
                    wordText = word.text,
                    selectedHotspotId = "",
                    isCorrect = false,
                    correctHotspotId = correctHotspotId
                )
            }
        }
        
        // Подсчитываем результаты
        val correctCount = details.count { it.isCorrect }
        val totalWords = content.words.size
        val percentage = if (totalWords > 0) {
            (correctCount.toFloat() / totalWords) * 100
        } else 0f
        
        // Очки начисляются пропорционально
        val pointsPerWord = 10
        val earnedPoints = (percentage / 100 * totalWords * pointsPerWord).toInt()
        val totalPoints = totalWords * pointsPerWord
        
        return ImageWordMatchResultResponse(
            questionId = "",
            earnedPoints = earnedPoints,
            totalPoints = totalPoints,
            percentage = percentage,
            details = details
        )
    }
}
