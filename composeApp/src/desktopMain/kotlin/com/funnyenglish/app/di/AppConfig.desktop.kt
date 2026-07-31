package com.funnyenglish.app.di

import com.funnyenglish.shared.config.FeatureFlags

actual fun provideAppConfig(): AppConfig {
    val baseUrl = System.getenv("FUNNYENGLISH_API_BASE_URL") ?: "http://localhost:8080"
    
    // Enable network logs by default in debug/development
    val enableNetworkLogs = System.getenv("FUNNYENGLISH_HTTP_LOGS")?.toBoolean() 
        ?: System.getProperty("funnyenglish.debug")?.toBoolean() 
        ?: true
    
    // Feature Flags для Desktop - берём из переменных окружения
    val enableDragDrop = System.getenv("FUNNYENGLISH_ENABLE_DRAG_DROP")?.toBoolean() ?: true
    val enableImageWordMatch = System.getenv("FUNNYENGLISH_ENABLE_IMAGE_WORD_MATCH")?.toBoolean() ?: true
    val enableDebugTools = System.getenv("FUNNYENGLISH_ENABLE_DEBUG_TOOLS")?.toBoolean() ?: true
    
    // Инициализация Feature Flags
    FeatureFlags.init(
        enableDragDrop = enableDragDrop,
        enableImageWordMatch = enableImageWordMatch,
        enableNetworkLogging = enableNetworkLogs,
        enableDebugTools = enableDebugTools
    )
    
    return AppConfig(
        baseUrl = baseUrl,
        enableNetworkLogs = enableNetworkLogs
    )
}
