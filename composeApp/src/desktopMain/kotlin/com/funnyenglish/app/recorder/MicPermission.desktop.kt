package com.funnyenglish.app.recorder

import androidx.compose.runtime.Composable

/** Desktop — стаб: записи нет, разрешение считаем отклонённым, onResult не вызываем (§4.2, R6). */
@Composable
actual fun rememberMicrophonePermissionState(
    onResult: (Boolean) -> Unit
): MicPermissionState = MicPermissionState.Denied
