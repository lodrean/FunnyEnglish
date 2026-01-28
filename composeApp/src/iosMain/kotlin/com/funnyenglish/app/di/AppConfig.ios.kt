package com.funnyenglish.app.di

actual fun provideAppConfig(): AppConfig {
    return AppConfig(
        baseUrl = "http://localhost:8080/",
        enableNetworkLogs = false
    )
}
