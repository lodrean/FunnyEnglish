package com.sotospeak.app.di

import com.sotospeak.app.viewmodel.GroupsViewModel
import com.sotospeak.app.viewmodel.MessagesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Сообщения и учебные группы (legacy-фичи, UI недостижим из навигации). */
val messagingModule = module {
    viewModel { GroupsViewModel(get()) }
    viewModel { MessagesViewModel(get()) }
}
