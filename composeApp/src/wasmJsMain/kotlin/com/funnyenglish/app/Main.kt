package com.funnyenglish.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.funnyenglish.app.di.provideAppConfig
import com.funnyenglish.app.util.Logger
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appConfig = provideAppConfig()
    
    Logger.i("Main", "=== FunnyEnglish Web (WASM) Started ===")
    Logger.i("Main", "API Base URL: ${appConfig.baseUrl}")
    Logger.i("Main", "Network Logs Enabled: ${appConfig.enableNetworkLogs}")

    // Browser history guard: навигация в приложении ручная (AppScreen state) и
    // не интегрирована с history API. Без этого guard'а кнопка «Назад» в браузере
    // уводит со страницы приложения (canvas пропадает — e2e navigation.spec).
    // SPA-поведение: «Назад» остаётся в приложении.
    setupBrowserHistoryGuard()
    
    CanvasBasedWindow(
        canvasElementId = "ComposeTarget",
        title = "FunnyEnglish Web"
    ) {
        App()
    }
    
    Logger.i("Main", "=== FunnyEnglish Web (WASM) Closed ===")
}

/**
 * Пушит фиктивную history-запись и при popstate (кнопка «Назад»)
 * пушит её снова — пользователь остаётся в приложении.
 */
private fun setupBrowserHistoryGuard() {
    window.history.pushState(null, "", window.location.href)
    window.onpopstate = {
        window.history.pushState(null, "", window.location.href)
    }
}
