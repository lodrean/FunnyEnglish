package com.funnyenglish.app.di

actual fun provideAppConfig(): AppConfig {
    val baseUrl = System.getenv("FUNNYENGLISH_API_BASE_URL") ?: "http://localhost:8080/"
    val enableNetworkLogs = System.getenv("FUNNYENGLISH_HTTP_LOGS")?.toBoolean() ?: false
    return AppConfig(
        baseUrl = baseUrl,
        enableNetworkLogs = enableNetworkLogs
    )
}
