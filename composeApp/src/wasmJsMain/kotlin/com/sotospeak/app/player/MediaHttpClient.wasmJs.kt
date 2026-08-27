package com.sotospeak.app.player

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js

/**
 * wasmJs-реализация фабрики медиа-клиента: явный Js-движок (bd 4d1, D2).
 */
internal actual fun createPlatformMediaHttpClient(
    config: HttpClientConfig<*>.() -> Unit,
): HttpClient = HttpClient(Js) { config(this) }
