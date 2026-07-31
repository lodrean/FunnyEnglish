package com.funnyenglish.app.di

import com.funnyenglish.shared.config.FeatureFlags
import kotlinx.browser.window

/**
 * Provides app configuration for Web (WASM) target.
 * Reads configuration from window.location or uses defaults.
 */
actual fun provideAppConfig(): AppConfig {
    // In web, determine API URL based on current host
    val host = window.location.host
    val protocol = window.location.protocol
    
    // For local development, use localhost:8080
    // For production, use relative path or same origin
    val baseUrl = when {
        host.contains("localhost") || host.contains("127.0.0.1") -> "http://localhost:8080"
        else -> "${protocol}//${host}" // Same origin for production
    }
    
    // Network logs enabled in development
    val isLocalhost = host.contains("localhost") || host.contains("127.0.0.1")
    val enableNetworkLogs = isLocalhost || window.location.search.contains("debug=true")
    
    // Feature Flags для Web/WASM
    // В production (не localhost) отключаем нестабильные фичи
    val enableDragDrop = isLocalhost
    val enableImageWordMatch = isLocalhost
    val enableDebugTools = isLocalhost
    
    // Инициализация Feature Flags
    FeatureFlags.init(
        enableDragDrop = enableDragDrop,
        enableImageWordMatch = enableImageWordMatch,
        enableNetworkLogging = enableNetworkLogs,
        enableDebugTools = enableDebugTools
    )
    
    log("[AppConfig] Base URL: $baseUrl")
    log("[AppConfig] Network Logs: $enableNetworkLogs")
    log("[AppConfig] DragDrop: $enableDragDrop")
    log("[AppConfig] ImageWordMatch: $enableImageWordMatch")
    
    return AppConfig(
        baseUrl = baseUrl,
        enableNetworkLogs = enableNetworkLogs
    )
}

@JsFun("(message) => { console.log(message); }")
external fun log(message: String)
