package com.sotospeak.app.localization

import androidx.compose.runtime.Composable
import com.sotospeak.app.viewmodel.AppLanguage

object Strings {
    @Composable
    fun get(language: AppLanguage) = when (language) {
        AppLanguage.RU -> RussianStrings
        AppLanguage.EN -> EnglishStrings
    }
}

interface AppStrings {
    val hello: String
    val continueLeaning: String
    val categories: String
    val recentTests: String
    val viewAll: String
    val settings: String
    val profile: String
    val leaderboard: String
    val achievements: String
    val logout: String
    val login: String
    val register: String
    val email: String
    val password: String
    val forgotPassword: String
    val notifications: String
    val sound: String
    val language: String
    val theme: String
    val light: String
    val dark: String
    val system: String
    val questionOf: String  // "Вопрос X из Y"
    val complete: String    // "% завершено"
    val finishTest: String
    val checkAnswer: String
    val tryAgain: String
    val backToHome: String
    val excellent: String
    val good: String
    val notBad: String
    val keepTrying: String
    val yourResult: String
    val newRecord: String
    val newLevel: String
    val newAchievements: String
}

object RussianStrings : AppStrings {
    override val hello = "Привет"
    override val continueLeaning = "Продолжить обучение"
    override val categories = "Категории"
    override val recentTests = "Недавние тесты"
    override val viewAll = "Все"
    override val settings = "Настройки"
    override val profile = "Профиль"
    override val leaderboard = "Лидеры"
    override val achievements = "Достижения"
    override val logout = "Выйти"
    override val login = "Войти"
    override val register = "Регистрация"
    override val email = "Email"
    override val password = "Пароль"
    override val forgotPassword = "Забыли пароль?"
    override val notifications = "Уведомления"
    override val sound = "Звук"
    override val language = "Язык"
    override val theme = "Тема"
    override val light = "Светлая"
    override val dark = "Тёмная"
    override val system = "Системная"
    override val questionOf = "Вопрос %d из %d"
    override val complete = "%d%% завершено"
    override val finishTest = "Завершить тест"
    override val checkAnswer = "Проверить ответ"
    override val tryAgain = "Попробовать снова"
    override val backToHome = "На главную"
    override val excellent = "Отлично!"
    override val good = "Хорошо!"
    override val notBad = "Неплохо!"
    override val keepTrying = "Попробуй ещё!"
    override val yourResult = "Ваш результат"
    override val newRecord = "Рекорд!"
    override val newLevel = "Новый уровень!"
    override val newAchievements = "Новые достижения"
}

object EnglishStrings : AppStrings {
    override val hello = "Hello"
    override val continueLeaning = "Continue Learning"
    override val categories = "Categories"
    override val recentTests = "Recent Tests"
    override val viewAll = "View All"
    override val settings = "Settings"
    override val profile = "Profile"
    override val leaderboard = "Leaderboard"
    override val achievements = "Achievements"
    override val logout = "Log Out"
    override val login = "Sign In"
    override val register = "Sign Up"
    override val email = "Email"
    override val password = "Password"
    override val forgotPassword = "Forgot Password?"
    override val notifications = "Notifications"
    override val sound = "Sound"
    override val language = "Language"
    override val theme = "Theme"
    override val light = "Light"
    override val dark = "Dark"
    override val system = "System"
    override val questionOf = "Question %d of %d"
    override val complete = "%d%% complete"
    override val finishTest = "Finish Test"
    override val checkAnswer = "Check Answer"
    override val tryAgain = "Try Again"
    override val backToHome = "Back to Home"
    override val excellent = "Excellent!"
    override val good = "Good!"
    override val notBad = "Not Bad!"
    override val keepTrying = "Keep Trying!"
    override val yourResult = "Your Result"
    override val newRecord = "New Record!"
    override val newLevel = "Level Up!"
    override val newAchievements = "New Achievements"
}
