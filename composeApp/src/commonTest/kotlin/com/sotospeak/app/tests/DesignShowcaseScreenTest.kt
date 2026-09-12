package com.sotospeak.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.sotospeak.app.screens.showcase.DesignShowcaseScreen
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.designsystem.theme.FunnyThemePreview
import kotlin.test.Test

/**
 * UI-тесты шоукейса дизайн-системы (bd FunnyEnglish-0uo).
 * Реальный [DesignShowcaseScreen] без DI — экран stateless.
 *
 * Сценарии:
 * 1. Индекс рендерит все секции каталога
 * 2. Поиск фильтрует список по заголовку
 * 3. Клик по секции открывает демо, «Назад» возвращает на индекс
 *
 * Секции с бесконечными анимациями (RecordingKit, Loaders) в тестах не
 * открываются — только проверяется их presence в каталоге (грабля №49).
 */
@OptIn(ExperimentalTestApi::class)
class DesignShowcaseScreenTest : BaseUiTest() {

    @Test
    fun showcaseIndexShowsAllSections() = runTest(
        content = { FunnyThemePreview { DesignShowcaseScreen(onBack = {}) } }
    ) {
        onNodeWithTag("showcase_index_screen").assertIsDisplayed()
        ShowcaseSection.entries.forEach { section ->
            onNodeWithTag("showcase_section_${section.name}").assertExists()
        }
    }

    @Test
    fun showcaseSearchFiltersSections() = runTest(
        content = { FunnyThemePreview { DesignShowcaseScreen(onBack = {}) } }
    ) {
        // M3 OutlinedTextField: SetText-семантика в merged-дереве самого тегированного узла
        onNodeWithTag("showcase_search_input").performTextInput("цвет")
        onNodeWithTag("showcase_section_Colors").assertExists()
        onNodeWithTag("showcase_section_Typography").assertDoesNotExist()
    }

    @Test
    fun showcaseOpensSectionAndReturnsBack() = runTest(
        content = { FunnyThemePreview { DesignShowcaseScreen(onBack = {}) } }
    ) {
        onNodeWithTag("showcase_section_Colors").performClick()
        onNodeWithTag("showcase_section_screen").assertIsDisplayed()
        onNodeWithText("Цвета", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("primaryStrong", useUnmergedTree = true).assertExists()
        onNodeWithContentDescription("Назад", useUnmergedTree = true).performClick()
        onNodeWithTag("showcase_index_screen").assertIsDisplayed()
    }
}
