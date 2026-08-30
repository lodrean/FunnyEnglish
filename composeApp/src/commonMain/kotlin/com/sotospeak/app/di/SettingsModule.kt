package com.sotospeak.app.di

import com.sotospeak.app.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Настройки приложения (тема, звук, уведомления) — app-уровневая VM. */
val settingsModule = module {
    viewModel { SettingsViewModel(get()) }
}
