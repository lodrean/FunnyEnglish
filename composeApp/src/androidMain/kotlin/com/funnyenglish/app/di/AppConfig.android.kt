package com.funnyenglish.app.di

import com.funnyenglish.app.BuildConfig

actual fun provideAppConfig(): AppConfig {
    return AppConfig(
        baseUrl = BuildConfig.API_BASE_URL,
        enableNetworkLogs = BuildConfig.ENABLE_NETWORK_LOGS
    )
}
