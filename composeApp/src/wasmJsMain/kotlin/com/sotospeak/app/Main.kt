package com.sotospeak.app

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.sotospeak.app.di.provideAppConfig
import com.sotospeak.app.util.Logger
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appConfig = provideAppConfig()
    
    Logger.i("Main", "=== So to speak Web (WASM) Started ===")
    Logger.i("Main", "API Base URL: ${appConfig.baseUrl}")
    Logger.i("Main", "Network Logs Enabled: ${appConfig.enableNetworkLogs}")

    // Browser history guard: навигация в приложении ручная (AppScreen state) и
    // не интегрирована с history API. Без этого guard'а кнопка «Назад» в браузере
    // уводит со страницы приложения (canvas пропадает — e2e navigation.spec).
    // SPA-поведение: «Назад» остаётся в приложении.
    setupBrowserHistoryGuard()
    
    CanvasBasedWindow(
        canvasElementId = "ComposeTarget",
        title = "So to speak Web"
    ) {
        App()

        // Сигнализируем HTML-оболочке, что Compose отрисовался и можно убрать спиннер.
        // Без этого на медленных машинах спиннер мог исчезнуть раньше первого кадра.
        DisposableEffect(Unit) {
            val handle = window.setTimeout({ markComposeReady(); null }, 500)
            onDispose { window.clearTimeout(handle) }
        }
    }
    
    Logger.i("Main", "=== So to speak Web (WASM) Closed ===")
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

/**
 * Убирает HTML-спиннер загрузки после того, как Compose отрисовал первый кадр.
 */
private fun markComposeReady(): Unit = js("(typeof window.markComposeReady === 'function') && window.markComposeReady()")
