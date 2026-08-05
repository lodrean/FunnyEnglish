package com.sotospeak.app.di

/**
 * Runtime конфигурация приложения.
 * [baseUrl] читается лениво при каждом обращении, чтобы рантайм-override
 * из debug-меню применялся без перезапуска приложения.
 */
class AppConfig(
    private val baseUrlProvider: () -> String,
    val enableNetworkLogs: Boolean,
    /** Версия приложения для клиентских логов (OpenSpec add-client-logging) */
    val appVersion: String = "dev",
    /** Debug-меню (смена backend URL и пр.): true в debug/qa-сборках */
    val debugToolsEnabled: Boolean = false
) {
    val baseUrl: String get() = baseUrlProvider()
}

/** Ключ Settings с рантайм-override base URL (debug-меню). */
const val API_BASE_URL_OVERRIDE_KEY = "api_base_url_override"

expect fun provideAppConfig(): AppConfig
