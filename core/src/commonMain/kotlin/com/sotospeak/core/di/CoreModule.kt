package com.sotospeak.core.di

import com.sotospeak.core.network.HttpClientFactory
import com.sotospeak.core.settings.SettingsRepository
import com.sotospeak.core.settings.SettingsRepositoryImpl
import com.sotospeak.core.toggle.FeatureToggleManager
import com.sotospeak.core.toggle.FeatureToggleManagerImpl
import com.sotospeak.core.toggle.LocalFeatureToggleSource
import com.sotospeak.core.toggle.RemoteFeatureToggleSource
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
