package com.funnyenglish.entity

import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Контент для вопросов типа IMAGE_WORD_MATCH
 * Пользователь перетаскивает слова к областям (hotspots) на изображении
 */
@JsonTypeName("IMAGE_WORD_MATCH")
data class ImageWordMatchContent(
    val imageUrl: String,
    val instruction: String,
    val hotspots: List<HotspotData>,
    val words: List<WordData>
) {
    init {
        require(hotspots.size == words.size) {
            "Number of hotspots must match number of words"
        }
        require(hotspots.all { it.wordId in words.map { w -> w.id } }) {
            "All hotspots must be linked to valid words"
        }
        require(words.size in 2..8) {
            "Word count must be between 2 and 8"
        }
    }
}

/**
 * Область (hotspot) на изображении, к которой можно привязать слово
 * Координаты относительные (0.0 - 1.0) для масштабирования
 */
data class HotspotData(
    val id: String,
    val x: Float,           // 0.0 - 1.0 (relative to image width)
    val y: Float,           // 0.0 - 1.0 (relative to image height)
    val width: Float,       // 0.0 - 1.0 (relative to image width)
    val height: Float,      // 0.0 - 1.0 (relative to image height)
    val shape: HotspotShape = HotspotShape.RECTANGLE,
    val wordId: String      // ID связанного слова
) {
    init {
        require(x in 0.0..1.0) { "Hotspot x must be in range [0.0, 1.0]" }
        require(y in 0.0..1.0) { "Hotspot y must be in range [0.0, 1.0]" }
        require(width in 0.0..1.0) { "Hotspot width must be in range [0.0, 1.0]" }
        require(height in 0.0..1.0) { "Hotspot height must be in range [0.0, 1.0]" }
        require(width > 0.05f) { "Hotspot width must be at least 5% of image" }
        require(height > 0.05f) { "Hotspot height must be at least 5% of image" }
    }
    
    /**
     * Проверяет, содержит ли hotspot указанные относительные координаты
     */
    fun contains(rx: Float, ry: Float): Boolean {
        return when (shape) {
            HotspotShape.RECTANGLE -> {
                rx >= x && rx <= x + width && ry >= y && ry <= y + height
            }
            HotspotShape.CIRCLE -> {
                val centerX = x + width / 2
                val centerY = y + height / 2
                val radius = kotlin.math.min(width, height) / 2
                val dx = rx - centerX
                val dy = ry - centerY
                (dx * dx + dy * dy) <= (radius * radius)
            }
        }
    }
}

enum class HotspotShape {
    RECTANGLE,
    CIRCLE
}

/**
 * Слово, которое нужно сопоставить с областью на изображении
 */
data class WordData(
    val id: String,
    val text: String,           // Слово на изучаемом языке
    val translation: String? = null,  // Перевод (опционально)
    val audioUrl: String? = null      // URL аудио произношения (опционально)
) {
    init {
        require(text.isNotBlank()) { "Word text cannot be blank" }
        require(text.length <= 50) { "Word text too long (max 50 chars)" }
    }
}
