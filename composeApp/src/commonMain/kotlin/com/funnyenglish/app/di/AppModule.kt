package com.funnyenglish.app.di

import com.funnyenglish.app.storage.RecordingFileStorage
import com.funnyenglish.app.storage.RecordingStore
import com.funnyenglish.app.viewmodel.*
import com.funnyenglish.shared.api.FunnyEnglishApi
import com.funnyenglish.shared.api.TokenProvider
import com.funnyenglish.shared.platform.Settings
import com.funnyenglish.shared.repository.GuestProgressRepository
import com.funnyenglish.shared.repository.GuestProgressRepositoryImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    val appConfig = provideAppConfig()

    // Local settings storage (must be created first for TokenProvider)
    single { Settings("funnyenglish.preferences") }

    // Token provider with persistent storage
    single<TokenProvider> { PersistentTokenProvider(get()) }

    // Guest progress repository
    single<GuestProgressRepository> { GuestProgressRepositoryImpl(get()) }

    // Обезличенная аналитика гостей
    single { com.funnyenglish.app.util.GuestAnalytics(get(), get()) }

    // API
    single {
        FunnyEnglishApi(
            baseUrl = appConfig.baseUrl,
            tokenProvider = get(),
            enableNetworkLogs = appConfig.enableNetworkLogs
        )
    }

    // ViewModels
    viewModel { AuthViewModel(get(), get(), get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { TestViewModel(get(), get(), get()) }
    viewModel { CategoriesViewModel(get()) }
    viewModel { LeaderboardViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { AdaptiveLessonViewModel(get()) }
    viewModel { StreakViewModel(get()) }
    viewModel { QuestsViewModel(get()) }
    viewModel { AchievementsViewModel(get()) }
    viewModel { GroupsViewModel(get()) }
    viewModel { MessagesViewModel(get()) }

    // Speaking-тренажёр (спека Part 2 §8.1)
    single<RecordingFileStorage> { RecordingFileStorage() }
    single { RecordingStore(get(), get()) }
    factory { com.funnyenglish.shared.platform.AudioPlayer() }   // прослушивание записей
    viewModel { LibraryViewModel(get()) }
    viewModel { TopicsViewModel(get(), get(), get()) }   // api + RecordingStore + Settings
    viewModel { QuestionsViewModel(get()) }
    viewModel { VideoViewModel(get(), get()) }           // api + Settings (topic_watched_*)
    viewModel { TrainingViewModel(get(), get(), get()) } // api + RecordingStore + AudioPlayer
    viewModel { PracticeViewModel(get(), get(), get(), get()) } // + RecordingFileStorage + TokenProvider
    viewModel { MySubmissionsViewModel(get(), get(), get(), get()) }
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
