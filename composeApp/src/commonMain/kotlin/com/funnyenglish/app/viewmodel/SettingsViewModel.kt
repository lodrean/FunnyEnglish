package com.funnyenglish.app.viewmodel

import androidx.lifecycle.ViewModel
import com.funnyenglish.shared.platform.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String) {
    RU("ru"),
    EN("en")
}

enum class AppThemeMode(val code: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark")
}

data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val autoPlayAudio: Boolean = false,
    val language: AppLanguage = AppLanguage.RU,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)

class SettingsViewModel(
    private val settings: Settings
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val languageCode = settings.getString(KEY_LANGUAGE, AppLanguage.RU.code)
        val themeCode = settings.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.code)
        _state.value = SettingsState(
            notificationsEnabled = settings.getBoolean(KEY_NOTIFICATIONS, true),
            soundEnabled = settings.getBoolean(KEY_SOUND, true),
            hapticsEnabled = settings.getBoolean(KEY_HAPTICS, true),
            autoPlayAudio = settings.getBoolean(KEY_AUTOPLAY_AUDIO, false),
            language = AppLanguage.values().firstOrNull { it.code == languageCode } ?: AppLanguage.RU,
            themeMode = AppThemeMode.values().firstOrNull { it.code == themeCode } ?: AppThemeMode.SYSTEM
        )
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_NOTIFICATIONS, enabled)
        _state.value = _state.value.copy(notificationsEnabled = enabled)
    }

    fun setSoundEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_SOUND, enabled)
        _state.value = _state.value.copy(soundEnabled = enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_HAPTICS, enabled)
        _state.value = _state.value.copy(hapticsEnabled = enabled)
    }

    fun setAutoPlayAudio(enabled: Boolean) {
        settings.putBoolean(KEY_AUTOPLAY_AUDIO, enabled)
        _state.value = _state.value.copy(autoPlayAudio = enabled)
    }

    fun setLanguage(language: AppLanguage) {
        settings.putString(KEY_LANGUAGE, language.code)
        _state.value = _state.value.copy(language = language)
    }

    fun setThemeMode(mode: AppThemeMode) {
        settings.putString(KEY_THEME_MODE, mode.code)
        _state.value = _state.value.copy(themeMode = mode)
    }

    companion object {
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_AUTOPLAY_AUDIO = "autoplay_audio"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
