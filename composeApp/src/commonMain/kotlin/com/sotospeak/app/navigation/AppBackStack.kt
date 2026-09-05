package com.sotospeak.app.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.json.Json

/**
 * Минимальный back stack для ручной навигации (bd 5tf.3, этап 1 — БЕЗ
 * navigation-compose). Снимает два прод-бага ручного `currentScreen`:
 * отсутствие системного «назад» и потерю экрана при process death.
 *
 * Семантика:
 * - [push] — вперёд по флоу (тема → видео → вопросы, табы, конверсия гостя);
 * - [replace] — замена вершины без роста стека;
 * - [reset] — вход в новую зону без «назад» (Splash/Onboarding/Login → Library, logout → Login);
 * - [pop] — системный «назад»; false, когда стек уже в корне.
 *
 * Приоритет обработчиков «назад»: PlatformBackHandler внутри экранов composируются
 * позже app-level и перехватывают событие первыми (на Android — OnBackPressedDispatcher,
 * на wasm — реестр в PlatformBackHandler.wasmJs.kt).
 */
class AppBackStack private constructor(private val entries: SnapshotStateList<AppScreen>) {

    constructor(initial: AppScreen) : this(mutableStateListOf(initial))

    val current: AppScreen get() = entries.last()

    val canGoBack: Boolean get() = entries.size > 1

    fun push(screen: AppScreen) {
        entries.add(screen)
    }

    fun replace(screen: AppScreen) {
        entries[entries.lastIndex] = screen
    }

    fun reset(screen: AppScreen) {
        entries.clear()
        entries.add(screen)
    }

    /** Системный «назад»: true — событие поглощено стеком. */
    fun pop(): Boolean {
        if (!canGoBack) return false
        entries.removeAt(entries.lastIndex)
        return true
    }

    /** Назад, если есть куда; иначе вперёд (Register → Login при входе со стека). */
    fun popOrPush(screen: AppScreen) {
        if (!pop()) push(screen)
    }

    /** Сериализация стека (JSON на каждый экран; полиморфный sealed-сериализатор). */
    fun encode(): List<String> =
        entries.map { json.encodeToString(AppScreen.serializer(), it) }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Восстановление после process death; бросает SerializationException на мусоре. */
        fun decode(saved: List<String>): AppBackStack =
            AppBackStack(
                mutableStateListOf<AppScreen>().apply {
                    saved.forEach { add(json.decodeFromString(AppScreen.serializer(), it)) }
                }
            )

        /** Saver для rememberSaveable (процессная смерть — стек целиком). */
        val Saver = listSaver<AppBackStack, String>(
            save = { it.encode() },
            restore = { decode(it) }
        )
    }
}
