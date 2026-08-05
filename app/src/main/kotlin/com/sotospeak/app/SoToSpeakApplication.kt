package com.sotospeak.app

import android.app.Application
import com.sotospeak.shared.platform.AndroidContextHolder
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class SoToSpeakApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContextHolder.init(this)
        if (BuildConfig.ENABLE_NETWORK_LOGS) {
            Napier.base(DebugAntilog())
        }
    }
}
