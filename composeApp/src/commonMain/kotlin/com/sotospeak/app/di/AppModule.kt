package com.sotospeak.app.di

import org.koin.dsl.module

/**
 * Корневой DI-модуль приложения — агрегатор фичевых модулей (bd FunnyEnglish-5tf.9):
 * core (сеть/хранилище), auth, settings, messaging, speaking.
 */
val appModule = module {
    includes(
        coreModule,
        authModule,
        settingsModule,
        messagingModule,
        speakingModule
    )
}
