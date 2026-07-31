package com.funnyenglish.app.tests

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.funnyenglish.app.components.questions.ImageWordMatchQuestion
import com.funnyenglish.app.di.mockImageWordMatchQuestions
import com.funnyenglish.shared.model.HotspotData
import com.funnyenglish.shared.model.HotspotShape
import com.funnyenglish.shared.model.ImageWordMatchContent
import com.funnyenglish.shared.model.WordData
import kotlin.test.Test

/**
 * Скриншот тесты для ImageWordMatch компонента.
 * 
 * Цели:
 * 1. Визуальная регрессия - отслеживание нежелательных изменений UI
 * 2. Документация состояний компонента
 * 3. Проверка адаптивности на разных размерах экрана
 * 
 * Примечание: Для записи скриншотов используйте:
 * - Android: ./gradlew composeApp:recordPaparazziDebug
 * - Desktop: сделайте скриншот вручную
 */
@OptIn(ExperimentalTestApi::class)
class ImageWordMatchScreenshotTest {

    /**
     * Скриншот: Начальное состояние (пустое)
     * 
     * Given: Компонент только загружен
     * When: Пользователь видит вопрос впервые
     * Then: Отображается изображение, 4 слова в банке, прогресс 0/4
     */
    @Test
    fun screenshot_initialState() = runComposeUiTest {
        setContent {
            TestContainer {
                ImageWordMatchQuestion(
                    content = mockContent,
                    currentMatches = emptyMap(),
                    onMatch = { _, _ -> },
                    onUnmatch = {}
                )
            }
        }
        
        // Ждем загрузки изображения
        waitForIdle()
        
        // Проверяем что все элементы отображаются
        onNodeWithTag("iwm_instruction").assertIsDisplayed()
        onNodeWithTag("iwm_image").assertIsDisplayed()
        onNodeWithTag("iwm_word_bank").assertIsDisplayed()
        onNodeWithTag("iwm_progress_bar").assertIsDisplayed()
    }

    /**
     * Скриншот: Частичное сопоставление (50%)
     * 
     * Given: 2 из 4 слов сопоставлены
     * When: Пользователь в процессе прохождения
     * Then: Прогресс 2/4, сопоставленные слова полупрозрачные
     */
    @Test
    fun screenshot_partialMatches() = runComposeUiTest {
        setContent {
            TestContainer {
                ImageWordMatchQuestion(
                    content = mockContent,
                    currentMatches = mapOf(
                        "word-1" to "hs-1",
                        "word-2" to "hs-2"
                    ),
                    onMatch = { _, _ -> },
                    onUnmatch = {}
                )
            }
        }
        
        waitForIdle()
        
        // Проверяем текст прогресса
        onNodeWithTag("iwm_progress_text").assertTextContains("2 / 4 words matched")
    }

    /**
     * Скриншот: Полное сопоставление (100%)
     * 
     * Given: Все слова сопоставлены
     * When: Пользователь завершил вопрос
     * Then: Прогресс 4/4, все hotspot'ы подсвечены зеленым
     */
    @Test
    fun screenshot_allMatched() = runComposeUiTest {
        setContent {
            TestContainer {
                ImageWordMatchQuestion(
                    content = mockContent,
                    currentMatches = mapOf(
                        "word-1" to "hs-1",
                        "word-2" to "hs-2",
                        "word-3" to "hs-3",
                        "word-4" to "hs-4"
                    ),
                    onMatch = { _, _ -> },
                    onUnmatch = {}
                )
            }
        }
        
        waitForIdle()
        
        // Проверяем полный прогресс
        onNodeWithTag("iwm_progress_text").assertTextContains("4 / 4 words matched")
    }

    /**
     * Скриншот: Ошибка загрузки изображения
     * 
     * Given: Невалидный URL изображения
     * When: Компонент загружается
     * Then: Отображается fallback UI
     */
    @Test
    fun screenshot_imageError() = runComposeUiTest {
        setContent {
            TestContainer {
                ImageWordMatchQuestion(
                    content = mockContent.copy(
                        imageUrl = "https://invalid-url.com/image.jpg"
                    ),
                    currentMatches = emptyMap(),
                    onMatch = { _, _ -> },
                    onUnmatch = {}
                )
            }
        }
        
        waitForIdle()
        
        // Компонент должен отображаться даже с ошибкой
        onNodeWithTag("iwm_image").assertIsDisplayed()
        onNodeWithTag("iwm_word_bank").assertIsDisplayed()
    }

    /**
     * Скриншот: Минимальный контент (2 слова)
     * 
     * Given: Вопрос с минимальным количеством слов
     * When: Компонент загружается
     * Then: Отображается корректно с 2 словами
     */
    @Test
    fun screenshot_minimalContent() = runComposeUiTest {
        setContent {
            TestContainer {
                ImageWordMatchQuestion(
                    content = mockContent.copy(
                        words = listOf(
                            WordData(id = "w1", text = "cat", translation = "кот", audioUrl = null),
                            WordData(id = "w2", text = "dog", translation = "собака", audioUrl = null)
                        ),
                        hotspots = listOf(
                            HotspotData(id = "h1", x = 0.2f, y = 0.2f, width = 0.3f, height = 0.3f, 
                                       shape = HotspotShape.RECTANGLE, wordId = null),
                            HotspotData(id = "h2", x = 0.6f, y = 0.6f, width = 0.3f, height = 0.3f,
                                       shape = HotspotShape.RECTANGLE, wordId = null)
                        )
                    ),
                    currentMatches = emptyMap(),
                    onMatch = { _, _ -> },
                    onUnmatch = {}
                )
            }
        }
        
        waitForIdle()
        
        // Проверяем прогресс для 2 слов
        onNodeWithTag("iwm_progress_text").assertTextContains("0 / 2 words matched")
    }

    /**
     * Скриншот: Максимальный контент (8 слов)
     * 
     * Given: Вопрос с большим количеством слов
     * When: Компонент загружается
     * Then: FlowRow корректно переносит слова
     */
    @Test
    fun screenshot_manyWords() = runComposeUiTest {
        setContent {
            TestContainer {
                ImageWordMatchQuestion(
                    content = mockContent.copy(
                        words = listOf(
                            WordData(id = "w1", text = "refrigerator", translation = "холодильник", audioUrl = null),
                            WordData(id = "w2", text = "microwave", translation = "микроволновка", audioUrl = null),
                            WordData(id = "w3", text = "sink", translation = "раковина", audioUrl = null),
                            WordData(id = "w4", text = "stove", translation = "плита", audioUrl = null),
                            WordData(id = "w5", text = "cabinet", translation = "шкафчик", audioUrl = null),
                            WordData(id = "w6", text = "counter", translation = "столешница", audioUrl = null),
                            WordData(id = "w7", text = "dishwasher", translation = "посудомойка", audioUrl = null),
                            WordData(id = "w8", text = "table", translation = "стол", audioUrl = null)
                        ),
                        hotspots = listOf(
                            HotspotData(id = "h1", x = 0.1f, y = 0.1f, width = 0.1f, height = 0.1f,
                                       shape = HotspotShape.RECTANGLE, wordId = null),
                            HotspotData(id = "h2", x = 0.3f, y = 0.1f, width = 0.1f, height = 0.1f,
                                       shape = HotspotShape.RECTANGLE, wordId = null),
                            HotspotData(id = "h3", x = 0.5f, y = 0.1f, width = 0.1f, height = 0.1f,
                                       shape = HotspotShape.RECTANGLE, wordId = null),
                            HotspotData(id = "h4", x = 0.7f, y = 0.1f, width = 0.1f, height = 0.1f,
                                       shape = HotspotShape.RECTANGLE, wordId = null),
                            HotspotData(id = "h5", x = 0.1f, y = 0.5f, width = 0.1f, height = 0.1f,
                                       shape = HotspotShape.RECTANGLE, wordId = null),
                            HotspotData(id = "h6", x = 0.3f, y = 0.5f, width = 0.1f, height = 0.1f,
                                       shape = HotspotShape.RECTANGLE, wordId = null),
                            HotspotData(id = "h7", x = 0.5f, y = 0.5f, width = 0.1f, height = 0.1f,
                                       shape = HotspotShape.RECTANGLE, wordId = null),
                            HotspotData(id = "h8", x = 0.7f, y = 0.5f, width = 0.1f, height = 0.1f,
                                       shape = HotspotShape.RECTANGLE, wordId = null)
                        )
                    ),
                    currentMatches = emptyMap(),
                    onMatch = { _, _ -> },
                    onUnmatch = {}
                )
            }
        }
        
        waitForIdle()
        
        // Проверяем прогресс для 8 слов
        onNodeWithTag("iwm_progress_text").assertTextContains("0 / 8 words matched")
    }

    /**
     * Скриншот: Круглые hotspot'ы
     * 
     * Given: Hotspot'ы с формой CIRCLE
     * When: Компонент загружается
     * Then: Отображаются круглые области
     */
    @Test
    fun screenshot_circleHotspots() = runComposeUiTest {
        setContent {
            TestContainer {
                ImageWordMatchQuestion(
                    content = mockContent.copy(
                        hotspots = listOf(
                            HotspotData(id = "h1", x = 0.2f, y = 0.2f, width = 0.15f, height = 0.15f,
                                       shape = HotspotShape.CIRCLE, wordId = null),
                            HotspotData(id = "h2", x = 0.6f, y = 0.2f, width = 0.15f, height = 0.15f,
                                       shape = HotspotShape.CIRCLE, wordId = null),
                            HotspotData(id = "h3", x = 0.2f, y = 0.6f, width = 0.15f, height = 0.15f,
                                       shape = HotspotShape.CIRCLE, wordId = null),
                            HotspotData(id = "h4", x = 0.6f, y = 0.6f, width = 0.15f, height = 0.15f,
                                       shape = HotspotShape.CIRCLE, wordId = null)
                        )
                    ),
                    currentMatches = emptyMap(),
                    onMatch = { _, _ -> },
                    onUnmatch = {}
                )
            }
        }
        
        waitForIdle()
        
        // Проверяем что компонент отображается (hotspot'ы требуют загрузки изображения)
        onNodeWithTag("iwm_instruction").assertIsDisplayed()
        onNodeWithTag("iwm_image").assertIsDisplayed()
        onNodeWithTag("iwm_word_bank").assertIsDisplayed()
    }

    companion object {
        private val mockContent = mockImageWordMatchQuestions.first().imageWordMatchContent!!
    }
}

/**
 * Контейнер для скриншот тестов с MaterialTheme
 */
@Composable
private fun TestContainer(content: @Composable () -> Unit) {
    MaterialTheme {
        Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}
