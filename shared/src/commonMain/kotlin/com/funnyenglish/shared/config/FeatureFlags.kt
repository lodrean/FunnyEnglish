package com.funnyenglish.shared.config

/**
 * Feature Flags для контроля функциональности в production.
 * 
 * Использование:
 * - Отключение нестабильных фич перед релизом
 * - A/B тестирование
 * - Постепенный rollout
 */
object FeatureFlags {
    
    /**
     * Отключает DRAG_DROP_IMAGE тип вопросов
     * Причина: Необходима дополнительная отладка drag-and-drop интерфейса
     */
    var ENABLE_DRAG_DROP_QUESTIONS: Boolean = false
    
    /**
     * Отключает IMAGE_WORD_MATCH тип вопросов
     * Причина: Необходима отладка позиционирования hotspot'ов
     */
    var ENABLE_IMAGE_WORD_MATCH: Boolean = false
    
    /**
     * Включает сетевое логирование
     * В production должно быть отключено
     */
    var ENABLE_NETWORK_LOGGING: Boolean = false
    
    /**
     * Включает отладочные инструменты
     * Только для development!
     */
    var ENABLE_DEBUG_TOOLS: Boolean = false
    
    /**
     * Максимальное количество вопросов в тесте
     * null = без ограничений
     */
    var MAX_QUESTIONS_PER_TEST: Int? = null
    
    /**
     * Инициализация флагов из платформенной конфигурации
     * Вызывается при старте приложения
     */
    fun init(
        enableDragDrop: Boolean = false,
        enableImageWordMatch: Boolean = false,
        enableNetworkLogging: Boolean = false,
        enableDebugTools: Boolean = false,
        maxQuestionsPerTest: Int? = null
    ) {
        ENABLE_DRAG_DROP_QUESTIONS = enableDragDrop
        ENABLE_IMAGE_WORD_MATCH = enableImageWordMatch
        ENABLE_NETWORK_LOGGING = enableNetworkLogging
        ENABLE_DEBUG_TOOLS = enableDebugTools
        MAX_QUESTIONS_PER_TEST = maxQuestionsPerTest
    }
    
    /**
     * Проверяет, доступен ли тип вопроса
     */
    fun isQuestionTypeEnabled(questionType: String): Boolean {
        return when (questionType) {
            "DRAG_DROP_IMAGE" -> ENABLE_DRAG_DROP_QUESTIONS
            "IMAGE_WORD_MATCH" -> ENABLE_IMAGE_WORD_MATCH
            else -> true
        }
    }
}

/**
 * Аннотация для пометки экспериментальных фич
 */
@RequiresOptIn(
    message = "This is an experimental feature that may be unstable",
    level = RequiresOptIn.Level.WARNING
)
annotation class ExperimentalFeature
