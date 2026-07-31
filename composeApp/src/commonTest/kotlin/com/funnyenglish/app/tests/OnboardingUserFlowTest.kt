package com.funnyenglish.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import com.funnyenglish.app.components.LockedFeature
import com.funnyenglish.app.screens.OnboardingScreen
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * UI-тесты онбординга первого запуска и гейтинга статистики для гостей.
 */
@OptIn(ExperimentalTestApi::class)
class OnboardingUserFlowTest : BaseUiTest() {

    // ============================================
    // Онбординг: слайды
    // ============================================

    @Test
    fun onboardingShowsFirstSlide() = runTest(
        content = { OnboardingScreen(onRegister = {}, onContinueAsGuest = {}) }
    ) {
        onNodeWithTag("onboarding_screen").assertIsDisplayed()
        onNodeWithTag("onboarding_title", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains("Учим английский играючи")
        onNodeWithTag("onboarding_next_button").assertIsDisplayed()
    }

    @Test
    fun onboardingShowsSecondSlideAfterNext() = runTest(
        content = { OnboardingScreen(onRegister = {}, onContinueAsGuest = {}) }
    ) {
        onNodeWithTag("onboarding_next_button").performClick()
        waitForIdle()
        onNodeWithTag("onboarding_title", useUnmergedTree = true)
            .assertTextContains("Звёзды, уровни и ачивки")
    }

    @Test
    fun onboardingShowsModeChoiceOnLastSlide() = runTest(
        content = { OnboardingScreen(onRegister = {}, onContinueAsGuest = {}) }
    ) {
        onNodeWithTag("onboarding_next_button").performClick()
        waitForIdle()
        onNodeWithTag("onboarding_next_button").performClick()
        waitForIdle()

        onNodeWithTag("onboarding_title", useUnmergedTree = true)
            .assertTextContains("Как начнём?")
        onNodeWithTag("onboarding_register_button").assertIsDisplayed()
        onNodeWithTag("onboarding_guest_button").assertIsDisplayed()
        onNodeWithTag("onboarding_login_link").assertIsDisplayed()
    }

    @Test
    fun onboardingGuestChoiceTriggersCallback() {
        var guestChosen = false
        runTest(
            content = { OnboardingScreen(onRegister = {}, onContinueAsGuest = { guestChosen = true }) }
        ) {
            repeat(2) {
                onNodeWithTag("onboarding_next_button").performClick()
                waitForIdle()
            }
            onNodeWithTag("onboarding_guest_button").performClick()
            waitForIdle()
        }
        assertTrue(guestChosen)
    }

    @Test
    fun onboardingRegisterChoiceTriggersCallback() {
        var registerChosen = false
        runTest(
            content = { OnboardingScreen(onRegister = { registerChosen = true }, onContinueAsGuest = {}) }
        ) {
            repeat(2) {
                onNodeWithTag("onboarding_next_button").performClick()
                waitForIdle()
            }
            onNodeWithTag("onboarding_register_button").performClick()
            waitForIdle()
        }
        assertTrue(registerChosen)
    }

    // ============================================
    // Гейтинг: LockedFeature
    // ============================================

    @Test
    fun lockedFeatureShowsTitleDescriptionAndCta() = runTest(
        content = {
            LockedFeature(
                title = "Рейтинг после регистрации",
                description = "Зарегистрируйтесь, чтобы видеть рейтинг",
                onRegisterClick = {}
            )
        }
    ) {
        onNodeWithTag("locked_feature").assertIsDisplayed()
        onNodeWithTag("locked_feature_register_button").assertIsDisplayed()
    }

    @Test
    fun lockedFeatureCtaTriggersCallback() {
        var clicked = false
        runTest(
            content = {
                LockedFeature(
                    title = "Достижения после регистрации",
                    description = "Описание",
                    onRegisterClick = { clicked = true }
                )
            }
        ) {
            onNodeWithTag("locked_feature_register_button").performClick()
            waitForIdle()
        }
        assertTrue(clicked)
    }
}
