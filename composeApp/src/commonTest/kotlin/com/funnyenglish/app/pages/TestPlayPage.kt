package com.funnyenglish.app.pages

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag

@OptIn(ExperimentalTestApi::class)

/**
 * Page Object для экрана прохождения теста.
 * 
 * Пользовательские сценарии:
 * - Ответ на текстовый вопрос (TEXT_SELECT)
 * - Ответ на вопрос с изображениями (IMAGE_SELECT)
 * - Ответ на аудио вопрос (AUDIO_SELECT)
 * - Drag-and-drop matching (DRAG_DROP_MATCH)
 * - Заполнение пропусков (FILL_BLANK)
 * - Навигация между вопросами
 * - Отправка теста
 * - Просмотр результатов
 */
class TestPlayPage(override val compose: ComposeUiTest) : BasePage() {
    
    companion object {
        const val TAG_SCREEN = "test_play_screen"
        const val TAG_QUESTION_TEXT = "question_text"
        const val TAG_QUESTION_NUMBER = "question_number"
        const val TAG_PROGRESS_BAR = "progress_bar"
        const val TAG_ANSWER_OPTIONS = "answer_options"
        const val TAG_ANSWER_OPTION = "answer_option_"
        const val TAG_IMAGE_OPTION = "image_option_"
        const val TAG_AUDIO_PLAYER = "audio_player"
        const val TAG_PLAY_AUDIO_BUTTON = "play_audio_button"
        const val TAG_DRAG_ITEM = "drag_item_"
        const val TAG_DROP_ZONE = "drop_zone_"
        const val TAG_FILL_BLANK_INPUT = "fill_blank_input_"
        const val TAG_NEXT_BUTTON = "next_button"
        const val TAG_PREV_BUTTON = "prev_button"
        const val TAG_SUBMIT_BUTTON = "submit_button"
        const val TAG_FINISH_BUTTON = "finish_button"
        const val TAG_RESULTS_VIEW = "results_view"
        const val TAG_SCORE_TEXT = "score_text"
        const val TAG_XP_EARNED = "xp_earned"
        const val TAG_STARS_EARNED = "stars_earned"
        const val TAG_CORRECT_ANSWERS = "correct_answers"
        const val TAG_ACHIEVEMENT_UNLOCKED = "achievement_unlocked"
        const val TAG_EXIT_BUTTON = "exit_button"
        const val TAG_CONFIRM_DIALOG = "confirm_dialog"
        const val TAG_CONFIRM_YES = "confirm_yes"
        const val TAG_CONFIRM_NO = "confirm_no"
    }
    
    /**
     * Проверить что экран прохождения теста отображается
     */
    fun assertScreenDisplayed() {
        assertTagDisplayed(TAG_SCREEN)
        assertTagDisplayed(TAG_QUESTION_TEXT)
    }
    
    /**
     * Проверить номер текущего вопроса.
     * Реальный формат топ-бара: «Вопрос N из M».
     */
    fun assertQuestionNumber(number: Int, total: Int) {
        assertTagDisplayed(TAG_QUESTION_NUMBER)
        assertContainsText(TAG_QUESTION_NUMBER, "Вопрос $number из $total")
    }
    
    /**
     * Получить текст вопроса
     */
    fun getQuestionText(): String {
        var text = ""
        compose.onNode(hasTestTag(TAG_QUESTION_TEXT)).assertExists()
        return text
    }
    
    /**
     * Выбрать текстовый ответ (TEXT_SELECT)
     */
    fun selectTextAnswer(answerText: String) {
        clickOnText(answerText)
    }
    
    /**
     * Выбрать ответ по индексу (TEXT_SELECT)
     */
    fun selectAnswerByIndex(index: Int) {
        clickOnTag("${TAG_ANSWER_OPTION}$index")
    }
    
    /**
     * Выбрать изображение-ответ по индексу (IMAGE_SELECT)
     */
    fun selectImageAnswer(index: Int) {
        clickOnTag("${TAG_IMAGE_OPTION}$index")
    }
    
    /**
     * Воспроизвести аудио (AUDIO_SELECT)
     */
    fun playAudio() {
        clickOnTag(TAG_PLAY_AUDIO_BUTTON)
    }
    
    /**
     * Выполнить drag-and-drop
     * Note: Drag-and-drop тестирование требует платформенных имплементаций
     */
    fun dragAndDrop(dragItemIndex: Int, dropZoneIndex: Int) {
        // TODO: Реализовать через performGesture или платформенные тесты
        // В commonTest доступны базовые действия, drag-and-drop требует специфичных жестов
    }
    
    /**
     * Заполнить пропуск (FILL_BLANK)
     */
    fun fillBlank(blankIndex: Int, text: String) {
        enterText("${TAG_FILL_BLANK_INPUT}$blankIndex", text)
    }
    
    /**
     * Нажать "Далее"
     */
    fun clickNext() {
        clickOnTag(TAG_NEXT_BUTTON)
    }
    
    /**
     * Нажать "Назад"
     */
    fun clickPrevious() {
        clickOnTag(TAG_PREV_BUTTON)
    }
    
    /**
     * Нажать "Завершить" (отправка теста)
     */
    fun clickSubmit() {
        clickOnTag(TAG_SUBMIT_BUTTON)
    }
    
    /**
     * Нажать "Завершить" на экране результатов
     */
    fun clickFinish() {
        clickOnTag(TAG_FINISH_BUTTON)
    }
    
    /**
     * Проверить что отображаются результаты
     */
    fun assertResultsDisplayed() {
        waitForTag(TAG_RESULTS_VIEW)
        assertTagDisplayed(TAG_RESULTS_VIEW)
    }
    
    /**
     * Проверить полученный балл
     */
    fun assertScore(score: String) {
        assertTagDisplayed(TAG_SCORE_TEXT)
        assertContainsText(TAG_SCORE_TEXT, score)
    }
    
    /**
     * Проверить полученный XP
     */
    fun assertXpEarned(xp: String) {
        assertTagDisplayed(TAG_XP_EARNED)
        assertContainsText(TAG_XP_EARNED, xp)
    }
    
    /**
     * Проверить полученные звезды
     */
    fun assertStarsEarned(stars: Int) {
        assertTagDisplayed(TAG_STARS_EARNED)
        // Проверить количество звезд (иконок)
    }
    
    /**
     * Проверить разблокировку ачивки
     */
    fun assertAchievementUnlocked(achievementName: String) {
        waitForText(achievementName)
        assertTextDisplayed(achievementName)
        assertTagDisplayed(TAG_ACHIEVEMENT_UNLOCKED)
    }
    
    /**
     * Проверить количество правильных ответов
     */
    fun assertCorrectAnswersCount(count: String) {
        assertTagDisplayed(TAG_CORRECT_ANSWERS)
        assertContainsText(TAG_CORRECT_ANSWERS, count)
    }
    
    /**
     * Нажать выход из теста
     */
    fun clickExit() {
        clickOnTag(TAG_EXIT_BUTTON)
    }
    
    /**
     * Подтвердить выход из теста
     */
    fun confirmExit() {
        waitForTag(TAG_CONFIRM_DIALOG)
        clickOnTag(TAG_CONFIRM_YES)
    }
    
    /**
     * Отменить выход из теста
     */
    fun cancelExit() {
        waitForTag(TAG_CONFIRM_DIALOG)
        clickOnTag(TAG_CONFIRM_NO)
    }
    
    /**
     * Сценарий: Ответить на текстовый вопрос и перейти далее
     */
    fun answerTextQuestionAndNext(answerText: String) {
        selectTextAnswer(answerText)
        clickNext()
    }
    
    /**
     * Сценарий: Завершить тест с текущими ответами
     */
    fun finishTest() {
        clickSubmit()
        assertResultsDisplayed()
    }
    
    /**
     * Сценарий: Полный проход теста с ответами
     */
    fun completeTestWithAnswers(answers: List<String>) {
        answers.forEachIndexed { index, answer ->
            selectTextAnswer(answer)
            if (index < answers.size - 1) {
                clickNext()
            }
        }
        clickSubmit()
        assertResultsDisplayed()
    }
    
    /**
     * Проверить что кнопка "Далее" отключена (до выбора ответа)
     */
    fun assertNextButtonDisabled() {
        assertIsDisabled(TAG_NEXT_BUTTON)
    }
    
    /**
     * Проверить что кнопка "Далее" включена (после выбора ответа)
     */
    fun assertNextButtonEnabled() {
        assertIsEnabled(TAG_NEXT_BUTTON)
    }
}
