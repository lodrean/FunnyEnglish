package com.funnyenglish.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.funnyenglish.app.di.mockVisibleSpeakingLibraries
import com.funnyenglish.app.screens.LibraryScreen
import com.funnyenglish.app.viewmodel.LibraryState
import com.funnyenglish.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI тесты экрана «Библиотека» (спека Part 2 §10.1).
 * Реальный [LibraryScreen] + моковый [LibraryState] + captured callbacks.
 *
 * Сценарии:
 * 1. Карточки тем видны (library_card_<id>)
 * 2. Клик по карточке → callback с id
 * 3. Empty state (library_empty)
 * 4. Error state (ErrorMessage + retry)
 */
@OptIn(ExperimentalTestApi::class)
class LibraryScreenTest : BaseUiTest() {

    // ============================================
    // 1. Карточки тем
    // ============================================

    @Test
    fun libraryCardsAreVisible() = runTest(
        content = { LibraryScreenForTest() }
    ) {
        onNodeWithTag("library_screen").assertIsDisplayed()
        // На экран попадают уже отфильтрованные темы (topicCount > 0)
        onNodeWithTag("library_card_lib-1", useUnmergedTree = true).assertIsDisplayed()
        // Тема с topicCount=0 отфильтрована — её карточки нет
        onNodeWithTag("library_card_lib-2", useUnmergedTree = true).assertDoesNotExist()
    }

    // ============================================
    // 2. Клик по карточке
    // ============================================

    @Test
    fun clickOnLibraryCardCallsCallbackWithId() = runTest(
        content = { LibraryScreenForTest() }
    ) {
        onNodeWithTag("library_card_lib-1", useUnmergedTree = true).performClick()
        waitForIdle()
        assertEquals("lib-1", LibraryClicks.libraryId, "onLibraryClick должен получить id темы")
    }

    // ============================================
    // 3. Empty state
    // ============================================

    @Test
    fun emptyStateIsShownWhenNoLibraries() = runTest(
        content = { LibraryScreenForTest(state = LibraryState(libraries = emptyList())) }
    ) {
        onNodeWithTag("library_empty", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Пока нет доступных тем").assertIsDisplayed()
    }

    // ============================================
    // 4. Error state + retry
    // ============================================

    @Test
    fun errorStateShowsMessageAndRetry() = runTest(
        content = {
            LibraryScreenForTest(state = LibraryState(error = "504 Proxy Error"))
        }
    ) {
        // Сырой текст не показываем — маппинг в человеческий (userFriendlyError, грабля №15)
        onNodeWithText("Сервер временно недоступен. Попробуйте позже.").assertIsDisplayed()
        onNodeWithText("Попробовать снова").performClick()
        waitForIdle()
        assertTrue(LibraryClicks.load, "onLoad (retry) должен быть вызван")
    }
}

// ============================================
// Test fixtures
// ============================================

/** Captured callbacks */
private object LibraryClicks {
    var load = false
    var libraryId: String? = null
}

@androidx.compose.runtime.Composable
private fun LibraryScreenForTest(
    state: LibraryState = LibraryState(libraries = mockVisibleSpeakingLibraries)
) {
    FunnyTheme {
        LibraryScreen(
            state = state,
            onLoad = { LibraryClicks.load = true },
            onLibraryClick = { LibraryClicks.libraryId = it }
        )
    }
}
