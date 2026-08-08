package com.sotospeak.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.window
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

/**
 * WASM: системной кнопки «назад» нет (history guard держит SPA), аппбар без стрелки (мокап).
 * «Назад» — клавиши Escape / BrowserBack.
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    DisposableEffect(enabled, onBack) {
        val listener: (Event) -> Unit = { e ->
            val key = (e as? KeyboardEvent)?.key
            if (enabled && (key == "Escape" || key == "BrowserBack")) onBack()
        }
        window.addEventListener("keydown", listener)
        onDispose { window.removeEventListener("keydown", listener) }
    }
}
