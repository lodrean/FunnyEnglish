package com.funnyenglish.app.di

import com.funnyenglish.shared.config.FeatureFlags

actual fun provideAppConfig(): AppConfig {
    // iOS: Feature Flags для production (нестабильные фичи отключены)
    FeatureFlags.init(
        enableDragDrop = false,      // Отключено до стабилизации
        enableImageWordMatch = false, // Отключено до стабилизации
        enableNetworkLogging = false,
        enableDebugTools = false
    )
    
    return AppConfig(
        baseUrl = "http://localhost:8080/",
        enableNetworkLogs = false
    )
}
