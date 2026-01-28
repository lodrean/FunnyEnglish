package com.funnyenglish.app

import android.app.Application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class FunnyEnglishApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.ENABLE_NETWORK_LOGS) {
            Napier.base(DebugAntilog())
        }
    }
}
