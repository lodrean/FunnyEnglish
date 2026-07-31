package com.funnyenglish.app.util

/**
 * Открыть системные настройки приложения (для PermanentlyDenied разрешений, спека §4.3).
 * Android — ACTION_APPLICATION_DETAILS_SETTINGS; остальные платформы — no-op (Android-first, R6).
 */
expect fun openAppSettings()
