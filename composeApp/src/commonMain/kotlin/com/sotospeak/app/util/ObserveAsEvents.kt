package com.sotospeak.app.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * Подписка на one-shot события ViewModel (Channel.receiveAsFlow()).
 * Аналог ObserveAsEvents из core/presentation (в монолите своего не было — спека Part 2 §2).
 */
@Composable
fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: suspend (T) -> Unit) {
    LaunchedEffect(flow) {
        flow.collectLatest { onEvent(it) }
    }
}
