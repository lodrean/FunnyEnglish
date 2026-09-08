package com.sotospeak.app

import android.app.Application
import com.sotospeak.app.work.PracticeRetryScheduling
import com.sotospeak.shared.platform.AndroidContextHolder
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class SoToSpeakApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContextHolder.init(this)
        if (BuildConfig.ENABLE_NETWORK_LOGS) {
            Napier.base(DebugAntilog())
        }
        // Фоновый retry Practice-записей (bd h3l.19): Koin нужен и воркеру.
        // App()-композируемый код стартует Koin с guard'ом (грабля №53) — дубли не будет.
        if (GlobalContext.getOrNull() == null) {
            startKoin { modules(com.sotospeak.app.di.appModule) }
        }
        PracticeRetryScheduling.register(this)
    }
}
