package com.sotospeak.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.sotospeak.app.di.mockSpeakingTopics
import com.sotospeak.app.screens.TopicsScreen
import com.sotospeak.app.viewmodel.TopicsState
import com.sotospeak.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * UI тесты экрана топиков (спека Part 2 §10.1).
 * Реальный [TopicsScreen] + моковый [TopicsState] + captured callbacks.
 *
 * Сценарии:
 * 1. Карточки топиков видны (topic_item_<id>)
 * 2. Клик по топику → callback с id (DC-5/V2: сразу видео, bottom-sheet убран)
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
    // 2. Клик по топику — сразу навигация (без bottom-sheet, DC-5)
    // ============================================

    @Test
    fun clickOnTopicCallsCallbackWithId() = runTest(
        content = { TopicsScreenForTest() }
    ) {
        onNodeWithTag("topic_item_topic-1", useUnmergedTree = true).performClick()
        waitForIdle()
        assertEquals("topic-1", TopicsClicks.topicId, "onTopicClick должен получить id топика")
    }

    @Test
    fun noSubtitleChoiceSheetAnymore() = runTest(
        content = { TopicsScreenForTest() }
    ) {
        onNodeWithTag("subtitle_choice_sheet", useUnmergedTree = true).assertDoesNotExist()
    }
}

// ============================================
// Test fixtures
// ============================================

/** Captured callbacks */
private object TopicsClicks {
    var topicId: String? = null
    var back = false
}

private fun mockTopicsState() = TopicsState(
    libraryTitle = "Знакомство",
    topics = mockSpeakingTopics
)

@androidx.compose.runtime.Composable
private fun TopicsScreenForTest(state: TopicsState = mockTopicsState()) {
    FunnyTheme {
        TopicsScreen(
            state = state,
            onTopicClick = { TopicsClicks.topicId = it },
            onRetry = {},
            onBack = { TopicsClicks.back = true }
        )
    }
}
