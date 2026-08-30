package com.sotospeak.app.di

import com.sotospeak.app.viewmodel.AuthViewModel
import com.sotospeak.app.viewmodel.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Авторизация, гостевая сессия и профиль пользователя. */
val authModule = module {
    viewModel { AuthViewModel(get(), get(), get(), get(), get(), get()) }   // AuthApi + GuestApi + …
    viewModel { ProfileViewModel(get(), get()) }
}
