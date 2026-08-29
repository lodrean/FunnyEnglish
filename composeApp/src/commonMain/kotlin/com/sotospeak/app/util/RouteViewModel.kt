package com.sotospeak.app.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import org.koin.compose.viewmodel.koinViewModel

/**
 * ViewModel со скоупом маршрута (PROJECT-REVIEW-2026-08-28 §2.2, К3; bd FunnyEnglish-5tf.2).
 *
 * `koinViewModel()` без параметров резолвится в ViewModelStore Activity — все VM
 * переживают переходы между экранами, из-за чего по VM разбросан компенсаторный
 * паттерн «ручной сброс состояния в load()», а таймеры/джобы живут между визитами.
 *
 * Здесь каждому значению [key] (маршрут с параметрами, например `AppScreen.Topics(...)`
 * или `"practice:$topicId"`) выделяется собственный [ViewModelStore]: VM создаётся
 * при входе на маршрут и уничтожается (`onCleared`, отмена viewModelScope/таймеров)
 * при уходе с маршрута или смене ключа. Повторный вход на маршрут — чистая VM.
 *
 * App-уровневые VM (AuthViewModel, SettingsViewModel) осознанно остаются
 * на Activity-скоупе через обычный `koinViewModel()`.
 */
@Composable
inline fun <reified VM : ViewModel> routeViewModel(key: Any): VM {
    val owner = remember(key) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    return koinViewModel(viewModelStoreOwner = owner)
}
