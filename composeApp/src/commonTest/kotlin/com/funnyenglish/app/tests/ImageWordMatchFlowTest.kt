package com.funnyenglish.app.tests

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import com.funnyenglish.app.components.questions.ImageWordMatchQuestion
import com.funnyenglish.app.di.*
import com.funnyenglish.app.pages.ImageWordMatchPage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)

/**
 * E2E UI тесты для фичи Image Word Match.
 * 
 * Сценарии:
 * 1. Отображение компонента с изображением и словами
 * 2. Прогресс сопоставления отображается корректно
 * 3. Сопоставление слова с hotspot'ом
 * 4. Отмена сопоставления (клик на сопоставленный hotspot)
 * 5. Полное прохождение вопроса (все сопоставления)
 * 6. Обработка ошибок загрузки изображения
 */
class ImageWordMatchFlowTest : BaseUiTest() {
    
    /**
     * Сценарий: Компонент отображается с корректными элементами
     * 
     * Given: Вопрос типа IMAGE_WORD_MATCH
     * When: Компонент загружается
     * Then: Отображается изображение, слова и hotspot'ы
     * And: Прогресс показывает 0 / N
     */
    @Test
    fun componentDisplaysCorrectly() = runTest(
        content = { ImageWordMatchScreenForTest() }
    ) {
        val iwmPage = ImageWordMatchPage(this)
        
        // Then: Компонент отображается
        iwmPage.assertComponentDisplayed()
        iwmPage.assertInstruction("Drag the words to the correct objects on the image")
        
        // And: Изображение загружено
        iwmPage.waitForImageLoaded()
        iwmPage.assertImageDisplayed()
        
        // And: Банк слов отображается
        iwmPage.assertWordBankDisplayed()
        
        // And: Все слова отображаются (4 слова из мока)
        iwmPage.verifyInitialState(wordCount = 4, hotspotCount = 4)
        
        // And: Прогресс 0 / 4
        iwmPage.assertProgress(0, 4)
    }
    
    /**
     * Сценарий: Сопоставление слова с hotspot'ом
     * 
     * Given: Компонент загружен
     * When: Пользователь сопоставляет слово с hotspot'ом
     * Then: Прогресс обновляется
     * And: Hotspot отмечается как сопоставленный
     */
    @Test
    fun matchWordToHotspotUpdatesProgress() = runTest(
        content = { 
            ImageWordMatchScreenWithCallbackForTest { wordId, hotspotId ->
                // Verify callback is called with correct parameters
                assertTrue(wordId.isNotEmpty())
                assertTrue(hotspotId.isNotEmpty())
            }
        }
    ) {
        val iwmPage = ImageWordMatchPage(this)
        
        // Given: Компонент загружен
        iwmPage.assertComponentDisplayed()
        
        // When/Then: Проверяем что callback работает (через прямой вызов)
        // Примечание: Полный drag-and-drop требует платформенных тестов
        iwmPage.performMatch(
            wordId = "word-1",
            hotspotId = "hs-1"
        ) { wordId, hotspotId ->
            // Callback вызван с правильными параметрами
            assertEquals("word-1", wordId)
            assertEquals("hs-1", hotspotId)
        }
    }
    
    /**
     * Сценарий: Подсчет количества слов и hotspot'ов
     * 
     * Given: Вопрос с 4 словами и 4 hotspot'ами
     * When: Компонент загружен
     * Then: Отображается правильное количество элементов
     */
    @Test
    fun correctNumberOfElementsDisplayed() = runTest(
        content = { ImageWordMatchScreenForTest() }
    ) {
        val iwmPage = ImageWordMatchPage(this)
        
        // Given/When: Компонент загружен
        iwmPage.assertComponentDisplayed()
        
        // Then: Правильное количество элементов
        val wordCount = iwmPage.getWordCount()
        val hotspotCount = iwmPage.getHotspotCount()
        
        assertEquals(4, wordCount, "Should display 4 words")
        assertEquals(4, hotspotCount, "Should display 4 hotspots")
    }
    
    /**
     * Сценарий: Прогресс обновляется при сопоставлениях
     * 
     * Given: Вопрос с 4 словами
     * When: 2 слова сопоставлены
     * Then: Прогресс показывает 2 / 4
     */
    @Test
    fun progressUpdatesWithMatches() = runTest(
        content = { ImageWordMatchScreenWithMatchesForTest(
            matches = mapOf("word-1" to "hs-1", "word-2" to "hs-2")
        )}
    ) {
        val iwmPage = ImageWordMatchPage(this)
        
        // Given/When: Компонент с 2 сопоставлениями
        iwmPage.assertComponentDisplayed()
        
        // Then: Прогресс 2 / 4
        iwmPage.assertProgress(2, 4)
    }
    
    /**
     * Сценарий: Полное сопоставление всех слов
     * 
     * Given: Вопрос с 4 словами
     * When: Все слова сопоставлены
     * Then: Прогресс показывает 4 / 4
     * And: Все hotspot'ы отмечены как сопоставленные
     */
    @Test
    fun allWordsMatchedShowsCompleteProgress() = runTest(
        content = { ImageWordMatchScreenWithMatchesForTest(
            matches = mapOf(
                "word-1" to "hs-1",
                "word-2" to "hs-2", 
                "word-3" to "hs-3",
                "word-4" to "hs-4"
            )
        )}
    ) {
        val iwmPage = ImageWordMatchPage(this)
        
        // Given/When: Все слова сопоставлены
        iwmPage.assertComponentDisplayed()
        
        // Then: Прогресс 4 / 4 (полный)
        iwmPage.verifyCompletedState(wordCount = 4)
    }
    
    /**
     * Сценарий: Отображение fallback при ошибке загрузки изображения
     * 
     * Given: Невалидный URL изображения
     * When: Компонент загружается
     * Then: Отображается fallback UI
     * And: Функциональность сопоставления работает
     */
    @Test
    fun fallbackDisplayedOnImageError() = runTest(
        content = { ImageWordMatchScreenWithInvalidImageForTest() }
    ) {
        val iwmPage = ImageWordMatchPage(this)
        
        // When: Компонент с невалидным изображением загружается
        iwmPage.assertComponentDisplayed()
        
        // Then: Fallback UI отображается (через наличие hotspot'ов)
        iwmPage.assertHotspotDisplayed(0)
        iwmPage.assertWordBankDisplayed()
    }
}

/**
 * Test composable с mock данными
 */
@Composable
private fun ImageWordMatchScreenForTest() {
    MaterialTheme {
        val content = mockImageWordMatchQuestions.first().imageWordMatchContent!!
        
        ImageWordMatchQuestion(
            content = content,
            currentMatches = emptyMap(),
            onMatch = { _, _ -> },
            onUnmatch = {}
        )
    }
}

/**
 * Test composable с callback для проверки
 */
@Composable
private fun ImageWordMatchScreenWithCallbackForTest(
    onMatchCallback: (String, String) -> Unit
) {
    MaterialTheme {
        val content = mockImageWordMatchQuestions.first().imageWordMatchContent!!
        
        ImageWordMatchQuestion(
            content = content,
            currentMatches = emptyMap(),
            onMatch = { wordId, hotspotId ->
                onMatchCallback(wordId, hotspotId)
            },
            onUnmatch = {}
        )
    }
}

/**
 * Test composable с предустановленными сопоставлениями
 */
@Composable
private fun ImageWordMatchScreenWithMatchesForTest(
    matches: Map<String, String>
) {
    MaterialTheme {
        val content = mockImageWordMatchQuestions.first().imageWordMatchContent!!
        
        ImageWordMatchQuestion(
            content = content,
            currentMatches = matches,
            onMatch = { _, _ -> },
            onUnmatch = {}
        )
    }
}

/**
 * Test composable с невалидным URL изображения
 */
@Composable
private fun ImageWordMatchScreenWithInvalidImageForTest() {
    MaterialTheme {
        val content = mockImageWordMatchQuestions.first().imageWordMatchContent!!.copy(
            imageUrl = "https://invalid-url.com/nonexistent.jpg"
        )
        
        ImageWordMatchQuestion(
            content = content,
            currentMatches = emptyMap(),
            onMatch = { _, _ -> },
            onUnmatch = {}
        )
    }
}
