package com.sotospeak.app.navigation

/**
 * Маршруты приложения. Навигация ручная (без NavHost): состояние `currentScreen`
 * в App.kt, переходы — явные вызовы onNavigate.
 */
sealed class AppScreen {
    data object Splash : AppScreen()
    data object Onboarding : AppScreen()
    data object Login : AppScreen()
    data object Register : AppScreen()
    data object Profile : AppScreen()
    data object Settings : AppScreen()
    data object Messages : AppScreen()

    // Speaking-тренажёр (спека Part 2 §1.2).
    // libraryId пробрасывается по цепочке — back stack отсутствует, onBack явный.
    // libraryTitle — для breadcrumb-подзаголовка аппбара (мокап .appbar .sub).
    data object Library : AppScreen()
    data class Topics(val libraryId: String, val libraryTitle: String = "") : AppScreen()
    data class Video(
        val topicId: String,
        val libraryId: String,
        val withSubtitles: Boolean,
        val libraryTitle: String = ""
    ) : AppScreen()
    data class Questions(
        val topicId: String,
        val libraryId: String,
        val libraryTitle: String = ""
    ) : AppScreen()
    data class Training(
        val topicId: String,
        val libraryId: String,
        val libraryTitle: String = ""
    ) : AppScreen()
    data class Practice(
        val topicId: String,
        val libraryId: String,
        val libraryTitle: String = ""
    ) : AppScreen()
    data object MySubmissions : AppScreen()

    /** Скрытое debug-меню (QA/debug-сборки): вход — 7 тапов по версии в профиле */
    data object DebugMenu : AppScreen()
}
