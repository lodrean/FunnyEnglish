package com.sotospeak.app.error

import com.sotospeak.shared.api.ApiException

/**
 * Типизированная пользовательская ошибка (bd FunnyEnglish-5tf.6, §2.2 предложение 5
 * PROJECT-REVIEW-2026-08-28): state экранов хранит `error: UiText?` вместо `error: String?`.
 *
 * Сырые технические сообщения (дампы Ktor-исключений, URL, стектрейсы) в UI не попадают:
 * перевод Throwable → UiText делается ОДИН раз — в [toUiText] на уровне data/VM,
 * а не в компонентах (раньше `ErrorMessage` сам переводил строки — грабля №15/№55).
 */
sealed interface UiText {
    /** Готовый user-facing текст: локальные ошибки (файл, запись) и человеческие сообщения backend'а. */
    data class Message(val value: String) : UiText

    /** Сетевой сбой до HTTP: нет соединения, DNS, таймаут. */
    data object NoConnection : UiText

    /** 5xx / прокси-ошибка — сервер временно недоступен. */
    data object ServerUnavailable : UiText

    /** 401 — сессия истекла (refresh не удался). */
    data object SessionExpired : UiText

    /** 403 — нет доступа к данным. */
    data object Forbidden : UiText

    /** 404 — данные не найдены. */
    data object NotFound : UiText

    /** Прочее: десериализация, неожиданный ответ, неизвестная ошибка. */
    data object Unknown : UiText
}

/**
 * Текст для UI. Строки захардкожены по-русски — как и весь остальной UI приложения
 * (локализации пока нет); при её появлении UiText расширяется StringResource-вариантом.
 */
fun UiText.asString(): String = when (this) {
    is UiText.Message -> value
    UiText.NoConnection -> "Нет соединения с сервером. Проверьте интернет."
    UiText.ServerUnavailable -> "Сервер временно недоступен. Попробуйте позже."
    UiText.SessionExpired -> "Сессия истекла. Войдите снова."
    UiText.Forbidden -> "Нет доступа к этим данным."
    UiText.NotFound -> "Данные не найдены."
    UiText.Unknown -> "Не удалось загрузить данные. Попробуйте ещё раз."
}

/**
 * Маппинг ошибок сетевого слоя в типизированный [UiText] — единственная точка перевода
 * технических сообщений (замена бывшего `userFriendlyError` из components/Common.kt).
 */
fun Throwable.toUiText(): UiText {
    if (this !is ApiException) return UiText.Unknown
    return when (code) {
        // code = 0 — сбой до HTTP (ApiException(0, e.message)): сеть или десериализация
        0 -> if (message.isNetworkFailure()) UiText.NoConnection else UiText.Unknown
        // Неверные креды на логине — это НЕ истёкшая сессия: показываем сообщение backend'а
        401 -> if (errorCode == "INVALID_CREDENTIALS") UiText.Message(message) else UiText.SessionExpired
        403 -> UiText.Forbidden
        404 -> UiText.NotFound
        in 500..599 -> UiText.ServerUnavailable
        // Прочие 4xx: структурированный ErrorResponse (errorCode != null — тело распарсилось)
        // несёт человеческое сообщение backend'а («Email уже занят» и т.п.) — показываем его.
        else -> if (errorCode != null && message.isNotBlank()) UiText.Message(message) else UiText.Unknown
    }
}

/** Признаки сетевого сбоя в сообщении платформенного исключения (Ktor/JVM/OkHttp). */
private fun String.isNetworkFailure(): Boolean {
    val lower = lowercase()
    return "unable to resolve host" in lower || "connection refused" in lower ||
        "failed to connect" in lower || "timeout" in lower
}
