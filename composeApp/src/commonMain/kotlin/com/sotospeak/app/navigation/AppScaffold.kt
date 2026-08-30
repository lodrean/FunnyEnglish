package com.sotospeak.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sotospeak.app.viewmodel.SettingsViewModel
import com.sotospeak.designsystem.layout.MaxContentWidth
import com.sotospeak.designsystem.layout.WindowWidthSizeClass
import com.sotospeak.designsystem.layout.calculateWindowWidthSizeClass
import com.sotospeak.shared.contracts.AuthMode

/**
 * Основной каркас авторизованной/гостевой зоны: адаптивная навигация
 * (compact → NavigationBar, medium/expanded → NavigationRail) + контент
 * с ограничением ширины (MaxContentWidth) и MainNavHost внутри.
 */
@Composable
fun AppScaffold(
    currentScreen: AppScreen,
    authMode: AuthMode,
    settingsViewModel: SettingsViewModel,
    onNavigate: (AppScreen) -> Unit,
    onLogout: () -> Unit
) {
    val showBottomNav = currentScreen is AppScreen.Library ||
        currentScreen is AppScreen.MySubmissions ||
        currentScreen is AppScreen.Profile

    // M3-адаптивность (спека §5, Q4): compact → NavigationBar, medium/expanded → NavigationRail
    BoxWithConstraints {
        val useRail = showBottomNav &&
            calculateWindowWidthSizeClass(maxWidth) != WindowWidthSizeClass.COMPACT

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail) {
                SpeakingNavigationRail(
                    currentScreen = currentScreen,
                    onNavigate = onNavigate
                )
            }
            Scaffold(
                bottomBar = {
                    if (showBottomNav && !useRail) {
                        BottomNavigationBar(
                            currentScreen = currentScreen,
                            onNavigate = onNavigate
                        )
                    }
                }
            ) { padding ->
                // Экран видео сам управляет своими insets (свой Scaffold; fullscreen — edge-to-edge
                // под вырез камеры, спека Part 2 §2.3 v1.7) — внешний padding для него не применяем,
                // иначе в landscape-immersive остаётся светлая полоса шириной с display-cutout
                val isVideoScreen = currentScreen is AppScreen.Video
                Box(
                    modifier = if (isVideoScreen) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().widthIn(max = MaxContentWidth)
                    ) {
                        MainNavHost(
                            currentScreen = currentScreen,
                            authMode = authMode,
                            settingsViewModel = settingsViewModel,
                            onNavigate = onNavigate,
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}
