package com.sotospeak.app.tests

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sotospeak.app.screens.ProfileScreen
import com.sotospeak.app.viewmodel.ProfileState
import com.sotospeak.designsystem.theme.FunnyTheme
import com.sotospeak.shared.model.User
import com.sotospeak.shared.model.UserProfile
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * UI тесты экрана профиля (frame-profile / frame-profile-guest, Playful Coach v1.1).
 * Реальный [ProfileScreen] + моковый [ProfileState] + captured callbacks.
 *
 * Сценарии:
 * 1. Авторизованный: аватар с инициалами, имя, email, stat-карточки, «Выйти»
 * 2. Гость: stub с 📬, «Зарегистрироваться» и ссылкой «Войти»
 */
@OptIn(ExperimentalTestApi::class)
class ProfileScreenTest : BaseUiTest() {

    // ============================================
    // 1. Авторизованный профиль
    // ============================================

    @Test
    fun profileShowsUserInfoAndStats() = runTest(
        content = { ProfileScreenForTest(submissionsCount = 3, topicsCompleted = 2) }
    ) {
        onNodeWithTag("profile_screen").assertIsDisplayed()
        onNodeWithTag("profile_avatar").assertIsDisplayed()
        // Инициалы «Анна Смирнова» → «АС»
        onNodeWithTag("profile_name", useUnmergedTree = true)
            .assertTextContains("Анна Смирнова")
        onNodeWithTag("profile_email", useUnmergedTree = true)
            .assertTextContains("anna@smirnova.ru")
        // Stat-карточки: «N записи отправлено» / «N темы пройдено»
        onNodeWithTag("profile_stat_submissions").assertIsDisplayed()
        onNodeWithTag("profile_stat_topics").assertIsDisplayed()
        onNodeWithText("записи отправлено", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("темы пройдено", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("3", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("2", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun profileShowsAppbarTitleAndSubtitle() = runTest(
        content = { ProfileScreenForTest() }
    ) {
        onNodeWithText("Профиль", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Твой прогресс и записи", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun logoutButtonCallsOnLogout() = runTest(
        content = { ProfileScreenForTest() }
    ) {
        onNodeWithTag("profile_logout_button", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        waitForIdle()
        assertTrue(ProfileClicks.logout, "onLogout должен быть вызван")
    }

    // ============================================
    // 2. Гостевой профиль (GuestProfileStub)
    // ============================================

    @Test
    fun guestProfileShowsGateWithCtas() = runTest(
        content = {
            FunnyTheme {
                ProfileScreen(
                    state = ProfileState(),
                    isGuest = true,
                    onLoad = {},
                    onLogout = {},
                    onRegisterClick = { ProfileClicks.register = true },
                    onLoginClick = { ProfileClicks.login = true }
                )
            }
        }
    ) {
        onNodeWithTag("guest_profile_screen").assertIsDisplayed()
        onNodeWithText("Гостевой режим", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText(
            "Зарегистрируйся, чтобы отправлять записи учителю и видеть оценки",
            useUnmergedTree = true
        ).assertIsDisplayed()
        onNodeWithTag("guest_profile_register_button").assertIsDisplayed()
        onNodeWithTag("guest_profile_login_link", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun guestProfileRegisterAndLoginCallbacks() = runTest(
        content = {
            FunnyTheme {
                ProfileScreen(
                    state = ProfileState(),
                    isGuest = true,
                    onLoad = {},
                    onLogout = {},
                    onRegisterClick = { ProfileClicks.register = true },
                    onLoginClick = { ProfileClicks.login = true }
                )
            }
        }
    ) {
        onNodeWithTag("guest_profile_register_button").performClick()
        waitForIdle()
        assertTrue(ProfileClicks.register, "onRegisterClick должен быть вызван")

        onNodeWithTag("guest_profile_login_link", useUnmergedTree = true).performClick()
        waitForIdle()
        assertTrue(ProfileClicks.login, "onLoginClick должен быть вызван")
    }
}

// ============================================
// Test fixtures
// ============================================

/** Captured callbacks */
private object ProfileClicks {
    var logout = false
    var register = false
    var login = false
}

private val mockUserProfile = UserProfile(
    user = User(
        id = "user-1",
        email = "anna@smirnova.ru",
        displayName = "Анна Смирнова",
        avatarUrl = null,
        level = 2,
        totalPoints = 120,
        currentStreak = 3,
        role = "STUDENT",
        createdAt = "2026-01-01T00:00:00Z"
    )
)

@Composable
private fun ProfileScreenForTest(
    submissionsCount: Int = 0,
    topicsCompleted: Int = 0
) {
    FunnyTheme {
        ProfileScreen(
            state = ProfileState(userProfile = mockUserProfile),
            isGuest = false,
            submissionsCount = submissionsCount,
            topicsCompleted = topicsCompleted,
            onLoad = {},
            onLogout = { ProfileClicks.logout = true }
        )
    }
}
