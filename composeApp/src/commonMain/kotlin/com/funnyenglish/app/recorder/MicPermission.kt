package com.funnyenglish.app.recorder

import androidx.compose.runtime.Composable

/** Состояние разрешения RECORD_AUDIO (спека Part 2 §4.3). */
enum class MicPermissionState { Unknown, Granted, Denied, PermanentlyDenied }

/**
 * Composable-обёртка над системным разрешением на микрофон.
 * Без accompanist — ручная реализация на androidx.activity.compose (спека Part 2 §4.3).
 *
 * [onResult] вызывается по итогу системного диалога; возвращаемое значение —
 * текущее состояние разрешения (рекомпозируется при изменении).
 */
@Composable
expect fun rememberMicrophonePermissionState(
    onResult: (Boolean) -> Unit
): MicPermissionState
