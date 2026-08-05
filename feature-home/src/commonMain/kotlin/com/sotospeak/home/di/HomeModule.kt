package com.sotospeak.home.di

import com.sotospeak.home.data.HomeApi
import com.sotospeak.home.data.HomeRepositoryImpl
import com.sotospeak.home.domain.HomeRepository
import com.sotospeak.home.presentation.HomeViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val homeModule = module {
    single { HomeApi(get<HttpClient>()) }
    singleOf(::HomeRepositoryImpl) bind HomeRepository::class
    viewModelOf(::HomeViewModel)
}
