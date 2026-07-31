package com.funnyenglish.core.di

import com.funnyenglish.core.network.HttpClientFactory
import com.funnyenglish.core.settings.SettingsRepository
import com.funnyenglish.core.settings.SettingsRepositoryImpl
import com.funnyenglish.core.toggle.FeatureToggleManager
import com.funnyenglish.core.toggle.FeatureToggleManagerImpl
import com.funnyenglish.core.toggle.LocalFeatureToggleSource
import com.funnyenglish.core.toggle.RemoteFeatureToggleSource
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Core module with dependencies used across all feature modules
 */
val coreModule = module {
    // HTTP Client
    single<HttpClient> { HttpClientFactory.create(enableLogging = false) }
    
    // Feature Toggles (simplified without Settings for now)
    single<FeatureToggleManager> { 
        FeatureToggleManagerImpl(
            localSource = LocalFeatureToggleSource(),
            remoteSource = RemoteFeatureToggleSource(get(), "")
        )
    }
}
