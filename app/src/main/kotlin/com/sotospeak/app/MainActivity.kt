package com.sotospeak.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sotospeak.app.di.InitializeCoil
import com.sotospeak.shared.platform.Settings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pre-set the window background to the app's selected theme so that rotation/config
        // changes don't flash the default (light) window background before Compose draws.
        val isDarkTheme = resolveInitialDarkTheme()
        window.setBackgroundDrawableResource(
            if (isDarkTheme) R.color.splash_background_dark else R.color.splash_background_light
        )

        enableEdgeToEdge()
        setContent {
            InitializeCoil()
            App()
        }
    }

    private fun resolveInitialDarkTheme(): Boolean {
        val settings = Settings(PREFS_NAME)
        return when (settings.getString(KEY_THEME_MODE, DEFAULT_THEME_MODE)) {
            "dark" -> true
            "light" -> false
            else -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        }
    }

    companion object {
        private const val PREFS_NAME = "sotospeak.preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val DEFAULT_THEME_MODE = "system"
    }
}
