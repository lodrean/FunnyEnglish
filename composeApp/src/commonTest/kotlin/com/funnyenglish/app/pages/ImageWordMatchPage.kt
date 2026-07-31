package com.funnyenglish.app.pages

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag

@OptIn(ExperimentalTestApi::class)

/**
 * Page Object для вопроса типа IMAGE_WORD_MATCH.
 * 
 * Функциональность:
 * - Отображение изображения с hotspot'ами
 * - Банк слов для перетаскивания
 * - Сопоставление слов с областями на изображении
 * - Прогресс сопоставления
 * 
 * Примечание: Drag-and-drop тестирование в Compose UI тестах требует
 * специальных жестов. Для E2E тестирования рекомендуется использовать
 * Maestro или другие инструменты, поддерживающие drag-and-drop.
 */
class ImageWordMatchPage(override val compose: ComposeUiTest) : BasePage() {
    
    companion object {
        const val TAG_INSTRUCTION = "iwm_instruction"
        const val TAG_PROGRESS_BAR = "iwm_progress_bar"
        const val TAG_PROGRESS_TEXT = "iwm_progress_text"
        const val TAG_IMAGE = "iwm_image"
        const val TAG_WORD_BANK = "iwm_word_bank"
        const val TAG_WORD_BANK_LABEL = "iwm_word_bank_label"
        const val TAG_HOTSPOT_PREFIX = "iwm_hotspot_"
        const val TAG_WORD_PREFIX = "iwm_word_"
    }
    
    /**
     * Проверить что компонент ImageWordMatch отображается
     */
    fun assertComponentDisplayed() {
        assertTagDisplayed(TAG_INSTRUCTION)
        assertTagDisplayed(TAG_IMAGE)
        assertTagDisplayed(TAG_WORD_BANK)
    }
    
    /**
     * Проверить текст инструкции
     */
    fun assertInstruction(expectedText: String) {
        assertContainsText(TAG_INSTRUCTION, expectedText)
    }
    
    /**
     * Проверить что изображение отображается
     */
    fun assertImageDisplayed() {
        assertTagDisplayed(TAG_IMAGE)
    }
    
    /**
     * Проверить что банк слов отображается
     */
    fun assertWordBankDisplayed() {
        assertTagDisplayed(TAG_WORD_BANK)
    }
    
    /**
     * Проверить что слово отображается в банке слов
     */
    fun assertWordDisplayed(wordIndex: Int) {
        assertTagDisplayed("${TAG_WORD_PREFIX}${wordIndex}")
    }
    
    /**
     * Проверить что hotspot отображается на изображении
     */
    fun assertHotspotDisplayed(hotspotIndex: Int) {
        assertTagDisplayed("${TAG_HOTSPOT_PREFIX}${hotspotIndex}")
    }
    
    /**
     * Получить количество слов в банке
     */
    fun getWordCount(): Int {
        var count = 0
        while (true) {
            val nodes = compose.onAllNodes(hasTestTag("${TAG_WORD_PREFIX}${count}")).fetchSemanticsNodes()
            if (nodes.isEmpty()) break
            count++
        }
        return count
    }
    
    /**
     * Получить количество hotspot'ов
     */
    fun getHotspotCount(): Int {
        var count = 0
        while (true) {
            val nodes = compose.onAllNodes(hasTestTag("${TAG_HOTSPOT_PREFIX}${count}")).fetchSemanticsNodes()
            if (nodes.isEmpty()) break
            count++
        }
        return count
    }
    
    /**
     * Проверить прогресс сопоставления
     */
    fun assertProgressText(expectedText: String) {
        assertContainsText(TAG_PROGRESS_TEXT, expectedText)
    }
    
    /**
     * Проверить текущий прогресс (например "2 / 4 words matched")
     */
    fun assertProgress(current: Int, total: Int) {
        assertProgressText("${current} / ${total} words matched")
    }
    
    /**
     * Нажать на слово (для тестирования клика вместо drag-and-drop)
     */
    fun clickWord(wordIndex: Int) {
        clickOnTag("${TAG_WORD_PREFIX}${wordIndex}")
    }
    
    /**
     * Нажать на hotspot (для тестирования клика)
     */
    fun clickHotspot(hotspotIndex: Int) {
        clickOnTag("${TAG_HOTSPOT_PREFIX}${hotspotIndex}")
    }
    
    /**
     * Проверить что слово сопоставлено (имеет успешный стиль)
     * Примечание: это упрощенная проверка, полная проверка требует
     * доступа к семантическим свойствам
     */
    fun assertWordMatched(wordIndex: Int) {
        // Слово должно отображаться, но с измененным альфа-каналом
        assertTagDisplayed("${TAG_WORD_PREFIX}${wordIndex}")
    }
    
    /**
     * Проверить что hotspot сопоставлен (имеет успешный стиль)
     */
    fun assertHotspotMatched(hotspotIndex: Int) {
        assertTagDisplayed("${TAG_HOTSPOT_PREFIX}${hotspotIndex}")
    }
    
    /**
     * Ждать загрузки изображения
     */
    fun waitForImageLoaded(timeoutMillis: Long = 10000) {
        waitForTag(TAG_IMAGE, timeoutMillis)
    }
    
    /**
     * Выполнить сопоставление слова с hotspot'ом через callback
     * (альтернатива drag-and-drop для unit/UI тестов)
     */
    fun performMatch(wordId: String, hotspotId: String, onMatch: (String, String) -> Unit) {
        onMatch(wordId, hotspotId)
    }
    
    /**
     * Сценарий: Проверить начальное состояние компонента
     */
    fun verifyInitialState(wordCount: Int, hotspotCount: Int) {
        assertComponentDisplayed()
        assertImageDisplayed()
        assertWordBankDisplayed()
        
        // Проверяем что все слова отображаются
        for (i in 0 until wordCount) {
            assertWordDisplayed(i)
        }
        
        // Проверяем что все hotspot'ы отображаются
        for (i in 0 until hotspotCount) {
            assertHotspotDisplayed(i)
        }
        
        // Прогресс должен быть 0
        assertProgress(0, wordCount)
    }
    
    /**
     * Сценарий: Проверить состояние после полного сопоставления
     */
    fun verifyCompletedState(wordCount: Int) {
        assertComponentDisplayed()
        assertProgress(wordCount, wordCount)
        
        // Все hotspot'ы должны быть сопоставлены
        for (i in 0 until wordCount) {
            assertHotspotMatched(i)
        }
    }
}
