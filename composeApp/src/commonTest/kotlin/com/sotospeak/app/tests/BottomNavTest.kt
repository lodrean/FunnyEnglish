package com.sotospeak.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sotospeak.app.AppScreen
import com.sotospeak.app.BottomNavigationBar
import com.sotospeak.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Тесты нижней навигации: лейблы по мокапу Playful Coach v1.1
 * («Темы» / «Отправки» / «Профиль») и навигация по табам.
 */
@OptIn(ExperimentalTestApi::class)
class BottomNavTest : BaseUiTest() {

    @Test
    fun bottomNavShowsMockupLabels() = runTest(
        content = {
            FunnyTheme {
                BottomNavigationBar(currentScreen = AppScreen.Library, onNavigate = {})
            }
        }
    ) {
        onNodeWithText("Темы", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Отправки", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Профиль", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun bottomNavClickNavigates() {
        var navigated: AppScreen? = null
        runTest(
            content = {
                FunnyTheme {
                    BottomNavigationBar(
                        currentScreen = AppScreen.Library,
                        onNavigate = { navigated = it }
                    )
                }
            }
        ) {
            onNodeWithText("Отправки", useUnmergedTree = true).performClick()
            waitForIdle()
        }
        assertEquals(AppScreen.MySubmissions, navigated)
    }
}
