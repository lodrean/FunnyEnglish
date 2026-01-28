package com.funnyenglish.app.di

data class AppConfig(
    val baseUrl: String,
    val enableNetworkLogs: Boolean
)

expect fun provideAppConfig(): AppConfig
