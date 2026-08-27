package com.sotospeak.app.player

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig

/**
 * Медиа-HTTP-клиент для стриминга видео (bd 4d1): единый Ktor-стек вместо
 * встроенного DefaultHttpDataSource (HttpURLConnection) внутри ExoPlayer.
 *
 * Намеренные отличия от API-клиента [com.sotospeak.shared.api.SoToSpeakApi]:
 * - БЕЗ ContentNegotiation/JSON и БЕЗ defaultRequest/auth — JWT на медиа-хост
 *   не уходит (тот же принцип, что в getTextResource: Authorization снимается);
 * - expectSuccess=false — KtorDataSource сам обрабатывает HTTP-статусы
 *   (не-2xx → HttpDataSourceException → PlaybackException → error-overlay/retry);
 * - HttpRedirect — редиректы CDN (3xx);
 * - таймауты: connect 10s, request/socket — INFINITE (долгие сегменты не рвутся).
 *
 * Движок теперь задаётся явно через expect/actual-фабрику [createPlatformMediaHttpClient]:
 * - OkHttp на androidMain/desktopMain;
 * - Js на wasmJsMain;
 * - Darwin на iosMain (единственный движок в runtime, compile-зависимость не
 *   добавляется в composeApp).
 * Клиент живёт в Koin (single, named "media") и НЕ закрывается контроллером
 * плеера (R10).
 */
object MediaHttpClient {

    /** Таймаут установки соединения, мс. */
    const val CONNECT_TIMEOUT_MS = 10_000L

    /** Request/socket-таймауты: без жёсткого лимита (длинные/медленные сегменты). */
    const val INFINITE_TIMEOUT_MS = HttpTimeoutConfig.INFINITE_TIMEOUT_MS

    /** Создаёт клиент с явным платформенным движком. */
    fun create(): HttpClient = createPlatformMediaHttpClient { mediaConfig() }
}

/**
 * Платформенная фабрика HTTP-движка для медиа-клиента (bd 4d1).
 *
 * Ожидаемая реализация:
 * - androidMain/desktopMain → OkHttp;
 * - wasmJsMain → Js;
 * - iosMain → Darwin (неявно, без добавления compile-зависимости в composeApp).
 */
internal expect fun createPlatformMediaHttpClient(
    config: HttpClientConfig<*>.() -> Unit,
): HttpClient

/**
 * Общая конфигурация медиа-клиента — переиспользуется тестами с MockEngine,
 * чтобы тестировать ровно ту конфигурацию, что уходит в прод.
 */
internal fun HttpClientConfig<*>.mediaConfig() {
    expectSuccess = false
    install(HttpRedirect)
    install(HttpTimeout) {
        connectTimeoutMillis = MediaHttpClient.CONNECT_TIMEOUT_MS
        // 0 в Ktor запрещён (require > 0); бесконечность = INFINITE_TIMEOUT_MS
        requestTimeoutMillis = MediaHttpClient.INFINITE_TIMEOUT_MS
        socketTimeoutMillis = MediaHttpClient.INFINITE_TIMEOUT_MS
    }
}
