package com.funnyenglish.app.di

import com.funnyenglish.app.viewmodel.*
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.api.TokenProvider
import com.funnyenglish.shared.platform.Settings
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    val appConfig = provideAppConfig()

    // Local settings storage (must be created first for TokenProvider)
    single { Settings("funnyenglish.preferences") }

    // Token provider with persistent storage
    single<TokenProvider> { PersistentTokenProvider(get()) }

    // API
    single {
        FunnyEnglishApi(
            baseUrl = appConfig.baseUrl,
            tokenProvider = get(),
            enableNetworkLogs = appConfig.enableNetworkLogs
        )
    }

    // ViewModels
    viewModel { AuthViewModel(get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { TestViewModel(get()) }
    viewModel { CategoriesViewModel(get()) }
    viewModel { LeaderboardViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}

class PersistentTokenProvider(private val settings: Settings) : TokenProvider {
    private var cachedToken: String? = null

    init {
        // Load token from persistent storage on init
        cachedToken = settings.getString(KEY_AUTH_TOKEN, null)
    }

    override fun getToken(): String? = cachedToken

    override fun setToken(token: String?) {
        cachedToken = token
        if (token != null) {
            settings.putString(KEY_AUTH_TOKEN, token)
        } else {
            settings.remove(KEY_AUTH_TOKEN)
        }
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
}
