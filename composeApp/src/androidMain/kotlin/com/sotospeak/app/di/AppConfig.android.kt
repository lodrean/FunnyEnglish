package com.sotospeak.app.di

import com.sotospeak.app.BuildConfig
import com.sotospeak.shared.platform.Settings

actual fun provideAppConfig(): AppConfig {
    return AppConfig(
        baseUrlProvider = {
            runCatching {
                Settings("sotospeak.preferences").getString(API_BASE_URL_OVERRIDE_KEY, null)
            }.getOrNull()?.takeIf { it.isNotBlank() } ?: BuildConfig.API_BASE_URL
        },
        enableNetworkLogs = BuildConfig.ENABLE_NETWORK_LOGS,
        appVersion = currentVersionName(),
        debugToolsEnabled = BuildConfig.ENABLE_DEBUG_TOOLS
    )
}

/** Версия из PackageManager (у library-модуля нет VERSION_NAME в BuildConfig) */
private fun currentVersionName(): String = runCatching {
    val context = com.sotospeak.shared.platform.AndroidContextHolder.requireContext()
    @Suppress("DEPRECATION")
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
}.getOrDefault("unknown")
