package com.funnyenglish.auth.di

import com.funnyenglish.auth.data.AuthApi
import com.funnyenglish.auth.data.AuthRepositoryImpl
import com.funnyenglish.auth.domain.AuthRepository
import com.funnyenglish.auth.presentation.AuthViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    single { AuthApi(get<HttpClient>()) }
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    viewModelOf(::AuthViewModel)
}
