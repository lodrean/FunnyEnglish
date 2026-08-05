package com.sotospeak.app.di

actual fun provideAppConfig(): AppConfig {
    val baseUrl = System.getenv("SOTOSPEAK_API_BASE_URL") ?: "http://localhost:8080"

    // Enable network logs by default in debug/development
    val enableNetworkLogs = System.getenv("SOTOSPEAK_HTTP_LOGS")?.toBoolean()
        ?: System.getProperty("sotospeak.debug")?.toBoolean()
        ?: true

    return AppConfig(
        baseUrlProvider = { baseUrl },
        enableNetworkLogs = enableNetworkLogs,
        appVersion = "1.0.0",
        debugToolsEnabled = System.getenv("SOTOSPEAK_DEBUG_TOOLS")?.toBoolean() ?: true
    )
}
