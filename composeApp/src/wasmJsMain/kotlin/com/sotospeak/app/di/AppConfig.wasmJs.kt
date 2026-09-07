package com.sotospeak.app.di

import kotlinx.browser.window

/**
 * Provides app configuration for Web (WASM) target.
 * Reads configuration from window.location or uses defaults.
 *
 * Чистая логика резолва вынесена в [parseApiUrlOverride]/[resolveApiBaseUrl]/
 * [resolveNetworkLogs] — покрыта wasmJsTest (bd qbq.7).
 */
actual fun provideAppConfig(): AppConfig {
    val host = window.location.host
    val hostname = window.location.hostname
    val port = window.location.port
    val protocol = window.location.protocol
    val search = window.location.search

    val overrideUrl = parseApiUrlOverride(search)
    val baseUrl = resolveApiBaseUrl(host, hostname, port, protocol, overrideUrl)
    val enableNetworkLogs = resolveNetworkLogs(host, search)

    log("[AppConfig] host=$host, port=$port, hostname=$hostname")
    log("[AppConfig] Base URL: $baseUrl")
    log("[AppConfig] Network Logs: $enableNetworkLogs")

    return AppConfig(
        baseUrlProvider = { baseUrl },
        enableNetworkLogs = enableNetworkLogs,
        appVersion = "1.0.0-web",
        debugToolsEnabled = false
    )
}

/** Значение параметра `apiUrl` из query string, если задан. */
internal fun parseApiUrlOverride(search: String): String? =
    search.removePrefix("?")
        .split("&")
        .mapNotNull { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2 && parts[0] == "apiUrl") parts[1] else null
        }
        .firstOrNull()

internal fun isLocalhostHost(host: String): Boolean =
    host.contains("localhost") || host.contains("127.0.0.1")

/**
 * Резолв базового URL API:
 * - явный override из `?apiUrl=` побеждает всё;
 * - localhost/127.0.0.1 → `http://localhost:8080`;
 * - dev-порты веб-сервера (8081/8082/8085, wasm-dist) → `http://<hostname>:8080`;
 * - иначе same-origin (production).
 */
internal fun resolveApiBaseUrl(
    host: String,
    hostname: String,
    port: String,
    protocol: String,
    overrideUrl: String?
): String = overrideUrl ?: when {
    isLocalhostHost(host) -> "http://localhost:8080"
    // Dev web server runs on :8081/:8082, static wasm-dist на :8085, backend on :8080 same host
    port == "8081" || port == "8082" || port == "8085" -> "http://$hostname:8080"
    else -> "$protocol//$host"
}

/** Сетевые логи: локальный хост или явный `debug=true`. */
internal fun resolveNetworkLogs(host: String, search: String): Boolean =
    isLocalhostHost(host) || search.contains("debug=true")

@JsFun("(message) => { console.log(message); }")
external fun log(message: String)
