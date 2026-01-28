package com.funnyenglish.app.di

import com.funnyenglish.app.viewmodel.*
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.api.TokenProvider
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    val appConfig = provideAppConfig()

    // Token provider
    single<TokenProvider> { InMemoryTokenProvider() }

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
}

class InMemoryTokenProvider : TokenProvider {
    private var token: String? = null

    override fun getToken(): String? = token

    override fun setToken(token: String?) {
        this.token = token
    }
}
