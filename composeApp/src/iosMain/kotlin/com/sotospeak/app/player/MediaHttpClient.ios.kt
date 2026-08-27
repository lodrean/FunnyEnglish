package com.sotospeak.app.player

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/**
 * iOS-реализация фабрики медиа-клиента: Darwin — единственный доступный движок
 * в runtime, поэтому указывать его явно не требуется. Compile-зависимость
 * `ktor-client-darwin` не добавляется в composeApp, а приходит транзитивно
 * через shared-модуль (bd 4d1, D2).
 */
internal actual fun createPlatformMediaHttpClient(
    config: HttpClientConfig<*>.() -> Unit,
): HttpClient = HttpClient { config(this) }
