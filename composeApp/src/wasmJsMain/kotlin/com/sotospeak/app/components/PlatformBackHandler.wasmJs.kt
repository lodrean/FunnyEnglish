package com.sotospeak.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.window
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

/**
 * WASM: системной кнопки «назад» нет (history guard держит SPA), аппбар без стрелки (мокап).
 * «Назад» — клавиши Escape / BrowserBack.
 *
 * Активных обработчиков может быть несколько (экранный + app-level back stack, bd 5tf.3),
 * поэтому событие получает ТОЛЬКО последний зарегистрированный включённый обработчик —
 * семантика Android OnBackPressedDispatcher (внутренний приоритет). Раньше каждый хук
 * вешал свой window-listener и один Escape срабатывал на всех уровнях сразу.
 */
private val backHandlers = mutableListOf<Pair<() -> Boolean, () -> Unit>>()

private val backKeyListener: (Event) -> Unit = { e ->
    val key = (e as? KeyboardEvent)?.key
    if (key == "Escape" || key == "BrowserBack") {
        backHandlers.lastOrNull()?.let { (isEnabled, onBack) ->
            if (isEnabled()) onBack()
        }
    }
}

private var listenerInstalled = false

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    DisposableEffect(enabled, onBack) {
        if (!listenerInstalled) {
            window.addEventListener("keydown", backKeyListener)
            listenerInstalled = true
        }
        val entry = { enabled } to onBack
        backHandlers.add(entry)
        onDispose {
            backHandlers.remove(entry)
            // Общий keydown-листенер не снимается: он один на процесс и переживает
            // перерегистрации обработчиков при навигации.
        }
    }
}
