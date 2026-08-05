package com.sotospeak.app.tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import com.sotospeak.app.components.LockedFeature
import com.sotospeak.app.screens.OnboardingScreen
import com.sotospeak.designsystem.theme.FunnyTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * UI-тесты онбординга первого запуска (frame-onboarding, Playful Coach v1.1)
 * и гейтинга статистики для гостей.
 *
 * Онбординг: 3 слайда value-prop, регистрации на онбординге НЕТ —
 * «Начать» на последнем слайде ведёт гостя сразу в библиотеку.
 */
@OptIn(ExperimentalTestApi::class)
class OnboardingUserFlowTest : BaseUiTest() {

    // ============================================
    // Онбординг: слайды
    // ============================================

    @Test
    fun onboardingShowsFirstSlide() = runTest(
        content = { FunnyTheme { OnboardingScreen(onFinish = {}) } }
    ) {
        onNodeWithTag("onboarding_screen").assertIsDisplayed()
        onNodeWithTag("onboarding_illustration_card").assertIsDisplayed()
        onNodeWithTag("onboarding_dots").assertIsDisplayed()
        onNodeWithTag("onboarding_title", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains("Смотри видео")
        onNodeWithTag("onboarding_next_button").assertIsDisplayed()
        onNodeWithText("Далее").assertIsDisplayed()
    }

    @Test
    fun onboardingShowsSecondSlideAfterNext() = runTest(
        content = { FunnyTheme { OnboardingScreen(onFinish = {}) } }
    ) {
        onNodeWithTag("onboarding_next_button").performClick()
        waitForIdle()
        onNodeWithTag("onboarding_title", useUnmergedTree = true)
            .assertTextContains("Тренируйся вслух")
    }

    @Test
    fun onboardingShowsThirdSlideWithStartCta() = runTest(
        content = { FunnyTheme { OnboardingScreen(onFinish = {}) } }
    ) {
        repeat(2) {
            onNodeWithTag("onboarding_next_button").performClick()
            waitForIdle()
        }
        onNodeWithTag("onboarding_title", useUnmergedTree = true)
            .assertTextContains("Отправь учителю")
        // На последнем слайде CTA — «Начать» (guest-first, без выбора режима)
        onNodeWithText("Начать").assertIsDisplayed()
    }

    @Test
    fun onboardingFinishTriggersCallback() {
        var finished = false
        runTest(
            content = { FunnyTheme { OnboardingScreen(onFinish = { finished = true }) } }
        ) {
            repeat(3) {
                onNodeWithTag("onboarding_next_button").performClick()
                waitForIdle()
            }
        }
        assertTrue(finished, "onFinish должен быть вызван на последнем слайде")
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
