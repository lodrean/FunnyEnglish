package com.funnyenglish.app.recorder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Android actual: ручной permission-flow через ActivityResultContracts (спека Part 2 §4.3).
 *
 * Если разрешение ещё не выдано — системный диалог запускается один раз при первой
 * композиции; результат приходит в [onResult]. PermanentlyDenied определяется по
 * shouldShowRequestPermissionRationale (нужна Activity — ищем её вверх по контексту).
 */
@Composable
actual fun rememberMicrophonePermissionState(
    onResult: (Boolean) -> Unit
): MicPermissionState {
    val context = LocalContext.current

    var permissionState by remember {
        mutableStateOf(
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                MicPermissionState.Granted
            } else {
                MicPermissionState.Unknown
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionState = when {
            granted -> MicPermissionState.Granted
            else -> {
                val activity = context.findActivity()
                if (activity != null &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, Manifest.permission.RECORD_AUDIO
                    )
                ) {
                    MicPermissionState.PermanentlyDenied
                } else {
                    MicPermissionState.Denied
                }
            }
        }
        onResult(granted)
    }

    // Запрашиваем разрешение один раз при первом показе, если ещё не выдано
    LaunchedEffect(Unit) {
        if (permissionState == MicPermissionState.Unknown) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    return permissionState
}

/** LocalContext обычно обёрнут (ContextThemeWrapper) — поднимаемся до Activity. */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
