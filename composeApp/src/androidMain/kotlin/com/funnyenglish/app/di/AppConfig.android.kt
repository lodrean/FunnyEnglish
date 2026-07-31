package com.funnyenglish.app.di

import com.funnyenglish.app.BuildConfig
import com.funnyenglish.shared.config.FeatureFlags

actual fun provideAppConfig(): AppConfig {
    // Инициализация Feature Flags из BuildConfig
    FeatureFlags.init(
        enableDragDrop = BuildConfig.ENABLE_DRAG_DROP_QUESTIONS,
        enableImageWordMatch = BuildConfig.ENABLE_IMAGE_WORD_MATCH,
        enableNetworkLogging = BuildConfig.ENABLE_NETWORK_LOGS,
        enableDebugTools = BuildConfig.ENABLE_DEBUG_TOOLS
    )
    
    return AppConfig(
        baseUrl = BuildConfig.API_BASE_URL,
        enableNetworkLogs = BuildConfig.ENABLE_NETWORK_LOGS
    )
}
