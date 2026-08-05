package com.sotospeak.app.di

actual fun provideAppConfig(): AppConfig {
    return AppConfig(
        baseUrlProvider = { "http://localhost:8080/" },
        enableNetworkLogs = false,
        appVersion = "1.0.0-ios",
        debugToolsEnabled = false
    )
}
