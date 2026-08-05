package com.sotospeak.app.util

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.sotospeak.shared.platform.AndroidContextHolder

actual fun openAppSettings() {
    val context = AndroidContextHolder.requireContext()
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
