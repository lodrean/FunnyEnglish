package com.sotospeak.shared.platform

import android.content.Context

object AndroidContextHolder {
    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun requireContext(): Context {
        return appContext ?: error("AndroidContextHolder is not initialized")
    }
}
