package com.sotospeak.app.navigation

import kotlinx.serialization.Serializable

/**
 * Маршруты приложения. Навигация ручная (без NavHost): [AppBackStack] в App.kt,
 * переходы — явные вызовы onNavigate/push, системный «назад» — pop (bd 5tf.3).
 *
 * @Serializable — сериализация стека в rememberSaveable (переживет process death).
 */
@Serializable
sealed class AppScreen {
    @Serializable data object Splash : AppScreen()
    @Serializable data object Onboarding : AppScreen()
    @Serializable data object Login : AppScreen()
    @Serializable data object Register : AppScreen()
    @Serializable data object Profile : AppScreen()
    @Serializable data object Settings : AppScreen()
    @Serializable data object Messages : AppScreen()

    // Speaking-тренажёр (спека Part 2 §1.2).
    // libraryId пробрасывается по цепочке; назад — pop из AppBackStack.
    // libraryTitle — для breadcrumb-подзаголовка аппбара (мокап .appbar .sub).
    @Serializable data object Library : AppScreen()
    @Serializable data class Topics(val libraryId: String, val libraryTitle: String = "") : AppScreen()
    @Serializable data class Video(
        val topicId: String,
        val libraryId: String,
        val withSubtitles: Boolean,
        val libraryTitle: String = ""
    ) : AppScreen()
    @Serializable data class Questions(
        val topicId: String,
        val libraryId: String,
        val libraryTitle: String = ""
    ) : AppScreen()
    @Serializable data class Training(
        val topicId: String,
        val libraryId: String,
        val libraryTitle: String = ""
    ) : AppScreen()
    @Serializable data class Practice(
        val topicId: String,
        val libraryId: String,
        val libraryTitle: String = ""
    ) : AppScreen()
    @Serializable data object MySubmissions : AppScreen()

    /** Скрытое debug-меню (QA/debug-сборки): вход — 7 тапов по версии в профиле */
    @Serializable data object DebugMenu : AppScreen()
}
