package com.sotospeak.app.di

import com.sotospeak.app.player.MediaHttpClient
import com.sotospeak.app.storage.RecordingFileStorage
import com.sotospeak.app.storage.RecordingStore
import com.sotospeak.app.viewmodel.*
import com.sotospeak.shared.api.SoToSpeakApi
import com.sotospeak.shared.api.TokenProvider
import com.sotospeak.shared.platform.Settings
import com.sotospeak.shared.repository.GuestProgressRepository
import com.sotospeak.shared.repository.GuestProgressRepositoryImpl
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    val appConfig = provideAppConfig()

    // Local settings storage (must be created first for TokenProvider)
    single { Settings("sotospeak.preferences") }

    // Token provider with persistent storage
    single<TokenProvider> { PersistentTokenProvider(get()) }

    // Guest progress repository
    single<GuestProgressRepository> { GuestProgressRepositoryImpl(get()) }

    // Обезличенная аналитика гостей
    single { com.sotospeak.app.util.GuestAnalytics(get(), get()) }

    // Клиентские логи WARN+ → backend (OpenSpec add-client-logging)
    single { appConfig }
    single { com.sotospeak.shared.util.ClientLogQueue(get()) }
    single {
        val api = get<SoToSpeakApi>()
        com.sotospeak.app.util.LogUploader(get()) { logs -> api.sendLogs(logs).isSuccess }
    }

    // Событие «сессия истекла» (refresh не удался): API (single) → AuthViewModel (viewModel), без циклической зависимости
    single { SessionEvents() }

    // API
    single {
        val sessionEvents = get<SessionEvents>()
        SoToSpeakApi(
            baseUrlProvider = { appConfig.baseUrl },
            tokenProvider = get(),
            enableNetworkLogs = appConfig.enableNetworkLogs,
            onSessionExpired = { sessionEvents.listener?.invoke() }
        )
    }

    // Медиа-клиент для стриминга видео (bd 4d1): единый Ktor-стек, БЕЗ auth/JSON —
    // JWT на медиа-хост не уходит (принцип getTextResource); KtorDataSource сам
    // обрабатывает статусы (expectSuccess=false). Живёт в Koin (single), контроллер
    // плеера его НЕ закрывает.
    single<HttpClient>(named("media")) { MediaHttpClient.create() }

    // ViewModels
    viewModel { AuthViewModel(get(), get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { GroupsViewModel(get()) }
    viewModel { MessagesViewModel(get()) }

    // Speaking-тренажёр (спека Part 2 §8.1)
    single<RecordingFileStorage> { RecordingFileStorage() }
    single { RecordingStore(get(), get()) }
    factory { com.sotospeak.shared.platform.AudioPlayer() }   // прослушивание записей
    viewModel { LibraryViewModel(get(), get()) }   // api + RecordingStore (DC-2: прогресс тем)
    viewModel { TopicsViewModel(get(), get(), get()) }   // api + RecordingStore + Settings
    viewModel { QuestionsViewModel(get()) }
    viewModel { VideoViewModel(get(), get()) }           // api + Settings (topic_watched_*)
    viewModel { TrainingViewModel(get(), get(), get()) } // api + RecordingStore + AudioPlayer
    viewModel { PracticeViewModel(get(), get(), get(), get()) } // + RecordingFileStorage + TokenProvider
    viewModel { MySubmissionsViewModel(get(), get(), get(), get()) }
}

/** Лёгкий мост SoToSpeakApi (single) → AuthViewModel (viewModel) для события «сессия истекла». */
class SessionEvents {
    var listener: (() -> Unit)? = null
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
