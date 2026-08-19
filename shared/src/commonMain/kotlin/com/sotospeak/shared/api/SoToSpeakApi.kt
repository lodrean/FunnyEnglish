package com.sotospeak.shared.api

import com.sotospeak.shared.model.*
import com.sotospeak.shared.util.Logger as AppLogger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class SoToSpeakApi(
    private val baseUrlProvider: () -> String,
    private val tokenProvider: TokenProvider,
    private val enableNetworkLogs: Boolean = false,
    /** Вызывается, когда refresh не удался (окно истекло/токен невалиден) — сессия сброшена, UI → гость. */
    private val onSessionExpired: (() -> Unit)? = null,
    /** Тестовый движок (MockEngine); null → платформенный по умолчанию. */
    httpClientEngine: HttpClientEngine? = null
) {
    private val refreshMutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = if (httpClientEngine != null) {
        HttpClient(httpClientEngine) { configureClient() }
    } else {
        HttpClient { configureClient() }
    }

    private fun HttpClientConfig<*>.configureClient() {
        // Бросать исключения на не-2xx: safeCall маппит их в ApiException с HTTP-кодом.
        // Без этого 401/403 уходили в generic-ветку с кодом 0 и retry-refresh не срабатывал.
        expectSuccess = true

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 30000
        }

        if (enableNetworkLogs) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        AppLogger.d("HttpClient", message)
                    }
                }
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
        }

        defaultRequest {
            url(baseUrlProvider())
            contentType(ContentType.Application.Json)
            // Токен — на КАЖДЫЙ запрос из провайдера (а не через Auth-plugin):
            // Ktor Auth кэширует результат loadTokens, и при логине в середине сессии
            // (гость → логин) запросы уходили без Authorization → 403 (баг, пойман Maestro 2026-07-31).
            // Авто-refresh по 401 реализован вручную в safeCall (refresh + один retry).
            tokenProvider.getToken()?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
        }
    }

    // Auth endpoints
    suspend fun register(request: RegisterRequest): Result<RegisterResponse> = safeCallNoRefresh {
        client.post("/api/auth/register") {
            setBody(request)
        }.body()
    }

    suspend fun resendVerification(email: String): Result<Unit> = safeCall {
        client.post("/api/auth/resend-verification") {
            contentType(ContentType.Application.Json)
            setBody(ResendVerificationRequest(email))
        }
        Unit
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> = safeCallNoRefresh {
        client.post("/api/auth/login") {
            setBody(request)
        }.body()
    }

    suspend fun oauthLogin(provider: String, request: OAuthRequest): Result<AuthResponse> = safeCallNoRefresh {
        client.post("/api/auth/oauth/$provider") {
            setBody(request)
        }.body()
    }

    /**
     * Обмен access-токена (в т.ч. истёкшего, в пределах refresh-окна backend) на новый.
     * true — токен обновлён; false — refresh не удался, токен очищен, вызван onSessionExpired.
     */
    private suspend fun refreshAccessToken(token: String): Boolean {
        return try {
            val response = client.post("/api/auth/refresh") {
                setBody(RefreshTokenRequest(token))
            }.body<AuthResponse>()
            tokenProvider.setToken(response.token)
            AppLogger.d("HttpClient", "Access token refreshed via /auth/refresh")
            true
        } catch (e: ClientRequestException) {
            // 400/401 от backend: refresh-окно истекло или токен невалиден → сессия сбрасывается.
            if (enableNetworkLogs) {
                AppLogger.e("HttpClient", "Token refresh rejected (${e.response.status}), session cleared", e)
            }
            tokenProvider.setToken(null)
            onSessionExpired?.invoke()
            false
        } catch (e: Exception) {
            // Сетевая ошибка (оффлайн и пр.) — НЕ разлогиниваем: токен остаётся, retry позже.
            if (enableNetworkLogs) {
                AppLogger.e("HttpClient", "Token refresh failed (network), session kept", e)
            }
            false
        }
    }

    // User endpoints
    suspend fun getCurrentUser(): Result<User> = safeCall {
        client.get("/api/users/me").body()
    }

    suspend fun getUserProfile(): Result<UserProfile> = safeCall {
        client.get("/api/users/me/profile").body()
    }

    // Messages (inbox ученика: сообщения/комментарии от учителя)
    suspend fun getMessages(): Result<List<Message>> = safeCall {
        client.get("/api/users/me/messages").body()
    }

    suspend fun getUnreadMessagesCount(): Result<UnreadCountResponse> = safeCall {
        client.get("/api/users/me/messages/unread-count").body()
    }

    suspend fun markMessageAsRead(messageId: String): Result<Message> = safeCall {
        client.post("/api/users/me/messages/$messageId/read").body()
    }

    // Student Groups endpoints
    suspend fun getMyStudentGroups(): Result<List<StudentGroup>> = safeCall {
        client.get("/api/groups/student/my-groups").body()
    }

    suspend fun getStudentGroupDetail(groupId: String): Result<GroupDetail> = safeCall {
        client.get("/api/groups/student/$groupId").body()
    }

    suspend fun joinGroupByCode(inviteCode: String): Result<JoinGroupResponse> = safeCall {
        client.post("/api/groups/join") {
            setBody(JoinGroupRequest(inviteCode))
        }.body()
    }

    suspend fun leaveGroup(groupId: String): Result<Unit> = safeCall {
        client.post("/api/groups/student/$groupId/leave").body()
    }

    // ==================== Public Guest endpoints ====================
    suspend fun mergeGuestProgress(request: MergeGuestProgressRequest): Result<MergeGuestProgressResponse> = safeCall {
        client.post("/api/users/me/merge-guest-progress") {
            setBody(request)
        }.body()
    }

    /**
     * Отправка обезличенных событий гостя (анонимная аналитика).
     * Публичный endpoint, best-effort — ошибки не критичны для UX.
     */
    suspend fun submitGuestEvents(events: List<GuestEventDto>): Result<GuestEventsBatchResponse> = safeCall {
        client.post("/api/public/guest-events") {
            setBody(GuestEventsBatchRequest(events))
        }.body()
    }

    /**
     * Отправка клиентских логов WARN/ERROR (OpenSpec add-client-logging).
     * Публичный endpoint, best-effort. НАМЕРЕННО без safeCall: safeCall пишет
     * сетевые ошибки в AppLogger → ошибка отправки логов породила бы новый лог
     * (рекурсия). Здесь ошибки просто возвращаются Result.failure.
     */
    suspend fun sendLogs(logs: List<ClientLogDto>): Result<ClientLogsBatchResponse> {
        return try {
            Result.success(
                client.post("/api/public/logs") {
                    setBody(ClientLogsBatchRequest(logs))
                }.body()
            )
        } catch (e: Exception) {
            Result.failure(ApiException(0, e.message ?: "Unknown error"))
        }
    }

    // ==================== Speaking Trainer endpoints ====================

    /** Публичный контент (гость): темы */
    suspend fun getSpeakingLibraries(): Result<List<SpeakingLibrary>> = safeCall {
        client.get("/api/public/speaking/libraries").body()
    }

    /** Публичный контент (гость): топики темы */
    suspend fun getSpeakingTopics(libraryId: String): Result<List<SpeakingTopicListItem>> = safeCall {
        client.get("/api/public/speaking/libraries/$libraryId/topics").body()
    }

    /** Публичный контент (гость): детали топика — видео + субтитры + вопросы */
    suspend fun getSpeakingTopicDetail(topicId: String): Result<SpeakingTopicDetail> = safeCall {
        client.get("/api/public/speaking/topics/$topicId").body()
    }

    /** Practice: загрузка голосовой записи (multipart, только авторизованным) */
    suspend fun submitSpeakingPractice(
        topicId: String,
        durationSec: Int,
        audioBytes: ByteArray,
        fileName: String = "recording.m4a"
    ): Result<SpeakingSubmission> = safeCall {
        client.submitFormWithBinaryData(
            url = "/api/speaking/submissions",
            formData = formData {
                append("topicId", topicId)
                append("durationSec", durationSec.toString())
                append("file", audioBytes, Headers.build {
                    append(HttpHeaders.ContentType, "audio/m4a")
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
            }
        ).body()
    }

    /** Practice: мои отправки с оценками (только авторизованным) */
    suspend fun getMySpeakingSubmissions(): Result<List<SpeakingSubmission>> = safeCall {
        client.get("/api/speaking/submissions/my").body()
    }

    /** Загрузка текстового ресурса по URL (субтитры WebVTT из MinIO — не API-эндпоинт, спека Part 2 §3.3) */
    suspend fun getTextResource(url: String): Result<String> = safeCall {
        client.get(url) {
            // B3-фикс (review): defaultRequest добавляет Authorization на КАЖДЫЙ запрос —
            // на медиа-хост (MinIO/S3/CDN) JWT утекать не должен
            headers.remove(HttpHeaders.Authorization)
        }.bodyAsText()
    }

    /** Обычный вызов: на 401 — один refresh + один retry (токен подхватывается в defaultRequest). */
    private suspend inline fun <reified T> safeCall(block: () -> T): Result<T> =
        safeCallWithRefresh(refreshOn401 = true, block)

    /** Для auth-эндпоинтов (login/register/oauth): 401 обрабатывает вызывающий код, refresh не нужен. */
    private suspend inline fun <reified T> safeCallNoRefresh(block: () -> T): Result<T> =
        safeCallWithRefresh(refreshOn401 = false, block)

    private suspend inline fun <reified T> safeCallWithRefresh(refreshOn401: Boolean, block: () -> T): Result<T> {
        val result = executeOnce(block)
        val error = result.exceptionOrNull() as? ApiException
        if (!refreshOn401 || error == null || error.code != 401) return result

        val staleToken = tokenProvider.getToken() ?: return result
        // Single-flight: параллельные 401 → один refresh; если токен уже сменился — просто retry.
        val refreshed = refreshMutex.withLock {
            val current = tokenProvider.getToken()
            when {
                current == null -> false            // сессию уже сбросили
                current != staleToken -> true       // другой поток уже обновил токен
                else -> refreshAccessToken(staleToken)
            }
        }
        // Retry безопасен: 401 прилетает из JWT-фильтра ДО контроллера — тело запроса не выполнялось.
        return if (refreshed) executeOnce(block) else result
    }

    private suspend inline fun <reified T> executeOnce(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: ClientRequestException) {
            val errorBody = e.response.bodyAsText()
            try {
                val error = json.decodeFromString<ErrorResponse>(errorBody)
                Result.failure(ApiException(e.response.status.value, error.message, error.error))
            } catch (_: Exception) {
                Result.failure(ApiException(e.response.status.value, errorBody))
            }
        } catch (e: Exception) {
            if (enableNetworkLogs) {
                AppLogger.e("HttpClient", "HTTP call failed", e)
            }
            Result.failure(ApiException(0, e.message ?: "Unknown error"))
        }
    }
}

interface TokenProvider {
    fun getToken(): String?
    fun setToken(token: String?)
}

class ApiException(val code: Int, override val message: String, val errorCode: String? = null) : Exception(message)
