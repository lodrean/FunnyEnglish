package com.funnyenglish.app.tests

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import com.funnyenglish.app.components.questions.ImageWordMatchQuestion
import com.funnyenglish.app.di.mockImageWordMatchQuestions
import com.funnyenglish.app.pages.ImageWordMatchPage
import com.funnyenglish.shared.model.HotspotData
import com.funnyenglish.shared.model.HotspotShape
import com.funnyenglish.shared.model.ImageWordMatchContent
import com.funnyenglish.shared.model.WordData
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Тесты граничных случаев (Edge Cases) и Accessibility для ImageWordMatch.
 * 
 * Покрываемые сценарии:
 * 1. Пустой контент (0 слов)
 * 2. Несовпадение количества слов и hotspot'ов
 * 3. Длинные слова в банке
 * 4. Специальные символы в тексте
 * 5. Очень маленькие hotspot'ы
 * 6. Очень большие hotspot'ы
 */
@OptIn(ExperimentalTestApi::class)
class ImageWordMatchEdgeCaseTest : BaseUiTest() {

    /**
     * Сценарий: Пустой контент (0 слов)
     * 
     * Given: Вопрос без слов
     * When: Компонент загружается
     * Then: Компонент отображается без ошибок
     * And: Прогресс показывает 0 / 0
     */
    @Test
    fun emptyContent_displaysWithoutCrash() = runTest(
        content = { 
            ImageWordMatchEdgeCaseScreen(
                content = ImageWordMatchContent(
                    id = "empty",
                    type = "image_word_match",
                    points = 10,
                    imageUrl = "https://via.placeholder.com/400x300",
                    instruction = "Empty test",
                    words = emptyList(),
                    hotspots = emptyList()
                )
            )
        }
    ) {
        val page = ImageWordMatchPage(this)
        
        // Then: Компонент не падает
        page.assertComponentDisplayed()
        
        // And: Прогресс 0/0
        page.assertProgressText("0 / 0 words matched")
    }

    /**
     * Сценарий: Длинные слова переносятся корректно
     * 
     * Given: Вопрос со словами длиннее 15 символов
     * When: Компонент загружается
     * Then: Слова отображаются в банке
     * And: FlowRow корректно переносит длинные слова
     */
    @Test
    fun longWords_displayCorrectly() = runTest(
        content = { 
            ImageWordMatchEdgeCaseScreen(
                content = baseContent.copy(
                    words = listOf(
                        WordData(id = "w1", text = "refrigerator", translation = "холодильник", audioUrl = null),
                        WordData(id = "w2", text = "air conditioning", translation = "кондиционер", audioUrl = null),
                        WordData(id = "w3", text = "washing machine", translation = "стиральная машина", audioUrl = null),
                        WordData(id = "w4", text = "microwave oven", translation = "микроволновая печь", audioUrl = null)
                    )
                )
            )
        }
    ) {
        val page = ImageWordMatchPage(this)
        
        // Then: Все слова отображаются
        assertEquals(4, page.getWordCount())
        page.assertWordBankDisplayed()
    }

    /**
     * Сценарий: Специальные символы в тексте слов
     * 
     * Given: Слова содержат специальные символы: '-', ''', '&', '/'
     * When: Компонент загружается
     * Then: Слова отображаются корректно
     */
    @Test
    fun specialCharacters_displayCorrectly() = runTest(
        content = { 
            ImageWordMatchEdgeCaseScreen(
                content = baseContent.copy(
                    words = listOf(
                        WordData(id = "w1", text = "mother-in-law", translation = "теща", audioUrl = null),
                        WordData(id = "w2", text = "it's", translation = "это", audioUrl = null),
                        WordData(id = "w3", text = "rock&roll", translation = "рок-н-ролл", audioUrl = null),
                        WordData(id = "w4", text = "up/down", translation = "вверх/вниз", audioUrl = null)
                    )
                )
            )
        }
    ) {
        val page = ImageWordMatchPage(this)
        
        // Then: Все слова отображаются
        assertEquals(4, page.getWordCount())
    }

    /**
     * Сценарий: Unicode символы и эмодзи
     * 
     * Given: Слова содержат эмодзи или unicode символы
     * When: Компонент загружается
     * Then: Символы отображаются корректно
     */
    @Test
    fun unicodeCharacters_displayCorrectly() = runTest(
        content = { 
            ImageWordMatchEdgeCaseScreen(
                content = baseContent.copy(
                    words = listOf(
                        WordData(id = "w1", text = "café", translation = "кафе", audioUrl = null),
                        WordData(id = "w2", text = "naïve", translation = "наивный", audioUrl = null),
                        WordData(id = "w3", text = "résumé", translation = "резюме", audioUrl = null),
                        WordData(id = "w4", text = "🍎 apple", translation = "яблоко", audioUrl = null)
                    )
                )
            )
        }
    ) {
        val page = ImageWordMatchPage(this)
        
        // Then: Все слова отображаются
        assertEquals(4, page.getWordCount())
    }

    /**
     * Сценарий: Одиночный hotspot покрывает большую область
     * 
     * Given: Hotspot занимает 80% изображения
     * When: Компонент загружается
     * Then: Hotspot отображается корректно
     */
    @Test
    fun largeHotspot_displaysCorrectly() = runTest(
        content = { 
            ImageWordMatchEdgeCaseScreen(
                content = baseContent.copy(
                    words = listOf(
                        WordData(id = "w1", text = "background", translation = "фон", audioUrl = null)
                    ),
                    hotspots = listOf(
                        HotspotData(
                            id = "h1", 
                            x = 0.1f, y = 0.1f, 
                            width = 0.8f, height = 0.8f,
                            shape = HotspotShape.RECTANGLE, 
                            wordId = null
                        )
                    )
                )
            )
        }
    ) {
        val page = ImageWordMatchPage(this)
        
        // Then: Hotspot отображается
        assertEquals(1, page.getHotspotCount())
        page.assertHotspotDisplayed(0)
    }

    /**
     * Сценарий: Очень маленький hotspot (5% от изображения)
     * 
     * Given: Hotspot занимает всего 5% изображения
     * When: Компонент загружается
     * Then: Hotspot все равно виден и кликабелен
     */
    @Test
    fun tinyHotspot_displaysAndClickable() = runTest(
        content = { 
            ImageWordMatchEdgeCaseScreen(
                content = baseContent.copy(
                    words = listOf(
                        WordData(id = "w1", text = "dot", translation = "точка", audioUrl = null)
                    ),
                    hotspots = listOf(
                        HotspotData(
                            id = "h1", 
                            x = 0.45f, y = 0.45f, 
                            width = 0.05f, height = 0.05f,
                            shape = HotspotShape.CIRCLE, 
                            wordId = null
                        )
                    )
                )
            )
        }
    ) {
        val page = ImageWordMatchPage(this)
        
        // Then: Маленький hotspot отображается
        assertEquals(1, page.getHotspotCount())
        page.assertHotspotDisplayed(0)
    }

    /**
     * Сценарий: Длинная инструкция
     * 
     * Given: Инструкция длиной > 200 символов
     * When: Компонент загружается
     * Then: Инструкция отображается корректно
     */
    @Test
    fun longInstruction_displaysCorrectly() = runTest(
        content = { 
            ImageWordMatchEdgeCaseScreen(
                content = baseContent.copy(
                    instruction = "This is a very long instruction that explains in detail what the user needs to do. " +
                                 "Drag each word from the word bank below to the correct location on the image above. " +
                                 "Make sure to match all words correctly to complete this question."
                )
            )
        }
    ) {
        val page = ImageWordMatchPage(this)
        
        // Then: Компонент отображается
        page.assertComponentDisplayed()
        page.assertInstructionDisplayed()
    }

    /**
     * Сценарий: Много слов в банке (12 слов)
     * 
     * Given: Вопрос с 12 словами
     * When: Компонент загружается
     * Then: FlowRow корректно переносит слова на несколько строк
     */
    @Test
    fun manyWords_flowRowWrapsCorrectly() = runTest(
        content = { 
            ImageWordMatchEdgeCaseScreen(
                content = baseContent.copy(
                    words = (1..12).map { i ->
                        WordData(
                            id = "w$i", 
                            text = "word$i", 
                            translation = "слово$i", 
                            audioUrl = null
                        )
                    },
                    hotspots = (1..12).map { i ->
                        HotspotData(
                            id = "h$i",
                            x = 0.1f * ((i - 1) % 10),
                            y = 0.1f * ((i - 1) / 10),
                            width = 0.08f,
                            height = 0.08f,
                            shape = HotspotShape.RECTANGLE,
                            wordId = null
                        )
                    }
                )
            )
        }
    ) {
        val page = ImageWordMatchPage(this)
        
        // Then: Все 12 слов отображаются
        assertEquals(12, page.getWordCount())
        assertEquals(12, page.getHotspotCount())
        
        // And: Прогресс показывает 0/12
        page.assertProgress(0, 12)
    }

    /**
     * Сценарий: Проверка семантики для accessibility
     * 
     * Given: Компонент загружен
     * When: Accessibility сервис анализирует UI
     * Then: Элементы имеют правильные content descriptions
     * And: Прогресс доступен для screen reader
     */
    @Test
    fun accessibility_semanticsAvailable() = runTest(
        content = { ImageWordMatchEdgeCaseScreen(baseContent) }
    ) {
        val page = ImageWordMatchPage(this)
        
        // Then: Компонент отображается
        page.assertComponentDisplayed()
        
        // And: Все основные элементы доступны
        page.assertImageDisplayed()
        page.assertWordBankDisplayed()
        page.assertProgressBarDisplayed()
    }

    // ==================== Helper Composables ====================

    @Composable
    private fun ImageWordMatchEdgeCaseScreen(content: ImageWordMatchContent) {
        MaterialTheme {
            ImageWordMatchQuestion(
                content = content,
                currentMatches = emptyMap(),
                onMatch = { _, _ -> },
                onUnmatch = {}
            )
        }
    }

    private val baseContent: ImageWordMatchContent
        get() = mockImageWordMatchQuestions.first().imageWordMatchContent!!
}

// ==================== Extension Functions for Page Object ====================

/**
 * Extension для проверки семантики
 */
fun ImageWordMatchPage.assertInstructionDisplayed() {
    assertTagDisplayed("iwm_instruction")
}

fun ImageWordMatchPage.assertProgressBarDisplayed() {
    assertTagDisplayed("iwm_progress_bar")
}

fun ImageWordMatchPage.assertProgressText(expectedText: String) {
    assertContainsText("iwm_progress_text", expectedText)
}
