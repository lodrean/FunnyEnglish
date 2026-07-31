package com.funnyenglish.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.funnyenglish.app.di.mockSpeakingTopics
import com.funnyenglish.app.screens.TopicsScreen
import com.funnyenglish.app.viewmodel.TopicsState
import com.funnyenglish.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI тесты экрана топиков (спека Part 2 §10.1).
 * Реальный [TopicsScreen] + моковый [TopicsState] + captured callbacks.
 *
 * Сценарии:
 * 1. Карточки топиков видны (topic_item_<id>)
 * 2. Клик по топику → callback с id
 * 3. Bottom-sheet выбора субтитров (state.subtitleChoiceTopicId):
 *    - subtitle_with (только если hasSubtitles) / subtitle_without / skip_video_button
 * 4. Для топика без субтитров кнопки subtitle_with нет
 */
@OptIn(ExperimentalTestApi::class)
class TopicsScreenTest : BaseUiTest() {

    // ============================================
    // 1. Список топиков
    // ============================================

    @Test
    fun topicItemsAreVisible() = runTest(
        content = { TopicsScreenForTest() }
    ) {
        onNodeWithTag("topics_screen").assertIsDisplayed()
        onNodeWithTag("topic_item_topic-1", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("topic_item_topic-2", useUnmergedTree = true).assertIsDisplayed()
    }

    // ============================================
    // 2. Клик по топику
    // ============================================

    @Test
    fun clickOnTopicCallsCallbackWithId() = runTest(
        content = { TopicsScreenForTest() }
    ) {
        onNodeWithTag("topic_item_topic-1", useUnmergedTree = true).performClick()
        waitForIdle()
        assertEquals("topic-1", TopicsClicks.topicId, "onTopicClick должен получить id топика")
    }

    // ============================================
    // 3. Bottom-sheet выбора субтитров
    // ============================================

    @Test
    fun subtitleChoiceSheetIsShown() = runTest(
        content = {
            TopicsScreenForTest(state = mockTopicsState(subtitleChoiceTopicId = "topic-1"))
        }
    ) {
        onNodeWithTag("subtitle_choice_sheet", useUnmergedTree = true).assertIsDisplayed()
        // topic-1 имеет субтитры — обе кнопки выбора + пропуск
        onNodeWithTag("subtitle_with", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("subtitle_without", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("skip_video_button", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun subtitleWithButtonCallsCallbackWithTrue() = runTest(
        content = {
            TopicsScreenForTest(state = mockTopicsState(subtitleChoiceTopicId = "topic-1"))
        }
    ) {
        onNodeWithTag("subtitle_with", useUnmergedTree = true).performClick()
        waitForIdle()
        assertEquals("topic-1" to true, TopicsClicks.subtitleChoice)
    }

    @Test
    fun subtitleWithoutButtonCallsCallbackWithFalse() = runTest(
        content = {
            TopicsScreenForTest(state = mockTopicsState(subtitleChoiceTopicId = "topic-1"))
        }
    ) {
        onNodeWithTag("subtitle_without", useUnmergedTree = true).performClick()
        waitForIdle()
        assertEquals("topic-1" to false, TopicsClicks.subtitleChoice)
    }

    @Test
    fun skipVideoButtonCallsCallbackWithTopicId() = runTest(
        content = {
            TopicsScreenForTest(state = mockTopicsState(subtitleChoiceTopicId = "topic-1"))
        }
    ) {
        onNodeWithTag("skip_video_button", useUnmergedTree = true).performClick()
        waitForIdle()
        assertEquals("topic-1", TopicsClicks.skipVideoTopicId)
    }

    // ============================================
    // 4. Топик без субтитров — кнопки «С субтитрами» нет
    // ============================================

    @Test
    fun noSubtitleWithButtonForTopicWithoutSubtitles() = runTest(
        content = {
            TopicsScreenForTest(state = mockTopicsState(subtitleChoiceTopicId = "topic-2"))
        }
    ) {
        onNodeWithTag("subtitle_choice_sheet", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("subtitle_with", useUnmergedTree = true).assertDoesNotExist()
        onNodeWithTag("subtitle_without", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("skip_video_button", useUnmergedTree = true).assertIsDisplayed()
    }
}

// ============================================
// Test fixtures
// ============================================

/** Captured callbacks */
private object TopicsClicks {
    var topicId: String? = null
    var subtitleChoice: Pair<String, Boolean>? = null
    var skipVideoTopicId: String? = null
    var dismissed = false
    var back = false
}

private fun mockTopicsState(subtitleChoiceTopicId: String? = null) = TopicsState(
    libraryTitle = "Знакомство",
    topics = mockSpeakingTopics,
    subtitleChoiceTopicId = subtitleChoiceTopicId
)

@androidx.compose.runtime.Composable
private fun TopicsScreenForTest(state: TopicsState = mockTopicsState()) {
    FunnyTheme {
        TopicsScreen(
            state = state,
            onTopicClick = { TopicsClicks.topicId = it },
            onSubtitleChoice = { id, with -> TopicsClicks.subtitleChoice = id to with },
            onSkipVideo = { TopicsClicks.skipVideoTopicId = it },
            onDismissSubtitleChoice = { TopicsClicks.dismissed = true },
            onRetry = {},
            onBack = { TopicsClicks.back = true }
        )
    }
}
