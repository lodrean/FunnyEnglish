package com.sotospeak.core.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Collects one-time [events] inside a [LaunchedEffect] and forwards them to [onEvent].
 *
 * Use this for ViewModel [Event] flows that should be consumed by the UI once
 * (e.g. navigation, snackbars, toasts).
 */
@Composable
fun <T> ObserveAsEvents(
    events: Flow<T>,
    key1: Any? = null,
    key2: Any? = null,
    onEvent: (T) -> Unit
) {
    LaunchedEffect(key1 = key1, key2 = key2, key3 = events) {
        withContext(Dispatchers.Main.immediate) {
            events.collect { onEvent(it) }
        }
    }
}
