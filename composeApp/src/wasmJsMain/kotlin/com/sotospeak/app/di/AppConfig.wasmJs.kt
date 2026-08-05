package com.sotospeak.app.di

import kotlinx.browser.window

/**
 * Provides app configuration for Web (WASM) target.
 * Reads configuration from window.location or uses defaults.
 */
actual fun provideAppConfig(): AppConfig {
    // In web, determine API URL based on current host
    val host = window.location.host
    val hostname = window.location.hostname
    val port = window.location.port
    val protocol = window.location.protocol

    // Allow explicit override via ?apiUrl=...
    val overrideUrl = window.location.search
        .removePrefix("?")
        .split("&")
        .mapNotNull { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2 && parts[0] == "apiUrl") parts[1] else null
        }
        .firstOrNull()

    val baseUrl = overrideUrl ?: when {
        host.contains("localhost") || host.contains("127.0.0.1") -> "http://localhost:8080"
        // Dev web server runs on :8081/:8082, backend is on :8080 on the same host
        port == "8081" || port == "8082" -> "http://${hostname}:8080"
        else -> "${protocol}//${host}" // Same origin for production
    }

    // Network logs enabled in development
    val isLocalhost = host.contains("localhost") || host.contains("127.0.0.1")
    val enableNetworkLogs = isLocalhost || window.location.search.contains("debug=true")

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

@JsFun("(message) => { console.log(message); }")
external fun log(message: String)
