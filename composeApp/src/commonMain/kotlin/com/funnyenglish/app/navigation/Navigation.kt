package com.funnyenglish.app.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Categories : Screen("categories")
    data object CategoryTests : Screen("categories/{categoryId}/tests") {
        fun createRoute(categoryId: String) = "categories/$categoryId/tests"
    }
    data object TestDetail : Screen("tests/{testId}") {
        fun createRoute(testId: String) = "tests/$testId"
    }
    data object TestPlay : Screen("tests/{testId}/play") {
        fun createRoute(testId: String) = "tests/$testId/play"
    }
    data object TestResult : Screen("tests/{testId}/result") {
        fun createRoute(testId: String) = "tests/$testId/result"
    }
    data object Profile : Screen("profile")
    data object Achievements : Screen("achievements")
    data object Leaderboard : Screen("leaderboard")
    data object Settings : Screen("settings")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val iconName: String
) {
    data object Home : BottomNavItem("home", "Главная", "home")
    data object Categories : BottomNavItem("categories", "Уроки", "school")
    data object Leaderboard : BottomNavItem("leaderboard", "Рейтинг", "leaderboard")
    data object Profile : BottomNavItem("profile", "Профиль", "person")
}
