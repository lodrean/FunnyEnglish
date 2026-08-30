package com.sotospeak.app.di

import com.sotospeak.app.player.MediaHttpClient
import com.sotospeak.shared.api.AuthApi
import com.sotospeak.shared.api.GuestApi
import com.sotospeak.shared.api.MessagingApi
import com.sotospeak.shared.api.SoToSpeakApi
import com.sotospeak.shared.api.SpeakingApi
import com.sotospeak.shared.api.TokenProvider
import com.sotospeak.shared.platform.Settings
import com.sotospeak.shared.repository.GuestProgressRepository
import com.sotospeak.shared.repository.GuestProgressRepositoryImpl
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Ядро приложения: локальное хранилище, токены, сеть (единый Ktor-клиент +
 * узкие API-срезы), клиентские логи, гостевая аналитика, медиа-клиент.
 */
val coreModule = module {
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
        val guestApi = get<GuestApi>()
        com.sotospeak.app.util.LogUploader(get()) { logs -> guestApi.sendLogs(logs).isSuccess }
    }

    // Событие «сессия истекла» (refresh не удался): API (single) → AuthViewModel (viewModel), без циклической зависимости
    single { SessionEvents() }

    // API: единый Ktor-клиент + узкие интерфейсы-срезы поверх него (bd FunnyEnglish-5tf.5)
    single {
        val sessionEvents = get<SessionEvents>()
        SoToSpeakApi(
            baseUrlProvider = { appConfig.baseUrl },
            tokenProvider = get(),
            enableNetworkLogs = appConfig.enableNetworkLogs,
            onSessionExpired = { sessionEvents.listener?.invoke() }
        )
    }
    single<AuthApi> { get<SoToSpeakApi>() }
    single<SpeakingApi> { get<SoToSpeakApi>() }
    single<MessagingApi> { get<SoToSpeakApi>() }
    single<GuestApi> { get<SoToSpeakApi>() }

    // Медиа-клиент для стриминга видео (bd 4d1): единый Ktor-стек, БЕЗ auth/JSON —
    // JWT на медиа-хост не уходит (принцип getTextResource); KtorDataSource сам
    // обрабатывает статусы (expectSuccess=false). Живёт в Koin (single), контроллер
    // плеера его НЕ закрывает.
    single<HttpClient>(named("media")) { MediaHttpClient.create() }
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
