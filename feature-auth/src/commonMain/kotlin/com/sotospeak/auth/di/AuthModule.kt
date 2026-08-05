package com.sotospeak.auth.di

import com.sotospeak.auth.data.AuthApi
import com.sotospeak.auth.data.AuthRepositoryImpl
import com.sotospeak.auth.domain.AuthRepository
import com.sotospeak.auth.presentation.AuthViewModel
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
