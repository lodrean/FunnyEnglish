package com.funnyenglish.shared.api

import com.funnyenglish.shared.model.*
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class FunnyEnglishApi(
    private val baseUrl: String,
    private val tokenProvider: TokenProvider,
    private val enableNetworkLogs: Boolean = false
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(Auth) {
            bearer {
                loadTokens {
                    tokenProvider.getToken()?.let { BearerTokens(it, "") }
                }
                refreshTokens {
                    null // Token refresh not implemented yet
                }
            }
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
                        Napier.d(message = message, tag = "HttpClient")
                    }
                }
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
        }

        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
    }

    // Auth endpoints
    suspend fun register(request: RegisterRequest): Result<AuthResponse> = safeCall {
        client.post("auth/register") {
            setBody(request)
        }.body()
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> = safeCall {
        client.post("auth/login") {
            setBody(request)
        }.body()
    }

    suspend fun oauthLogin(provider: String, request: OAuthRequest): Result<AuthResponse> = safeCall {
        client.post("auth/oauth/$provider") {
            setBody(request)
        }.body()
    }

    // User endpoints
    suspend fun getCurrentUser(): Result<User> = safeCall {
        client.get("users/me").body()
    }

    suspend fun getUserProfile(): Result<UserProfile> = safeCall {
        client.get("users/me/profile").body()
    }

    suspend fun getUserProgress(): Result<List<Progress>> = safeCall {
        client.get("users/me/progress").body()
    }

    suspend fun getUserProgressSummary(): Result<ProgressSummary> = safeCall {
        client.get("users/me/progress/summary").body()
    }

    suspend fun getUserAchievements(): Result<List<Achievement>> = safeCall {
        client.get("users/me/achievements").body()
    }

    // Category endpoints
    suspend fun getCategories(): Result<List<Category>> = safeCall {
        client.get("categories").body()
    }

    suspend fun getTestsByCategory(categoryId: String): Result<List<TestListItem>> = safeCall {
        client.get("categories/$categoryId/tests").body()
    }

    // Test endpoints
    suspend fun getAllTests(): Result<List<TestListItem>> = safeCall {
        client.get("tests").body()
    }

    suspend fun getTestById(testId: String): Result<TestDetail> = safeCall {
        client.get("tests/$testId").body()
    }

    suspend fun submitTest(testId: String, request: SubmitTestRequest): Result<SubmitTestResult> = safeCall {
        client.post("tests/$testId/submit") {
            setBody(request)
        }.body()
    }

    // Leaderboard
    suspend fun getLeaderboard(limit: Int = 10): Result<Leaderboard> = safeCall {
        client.get("leaderboard") {
            parameter("limit", limit)
        }.body()
    }

    // Achievements
    suspend fun getAllAchievements(): Result<List<Achievement>> = safeCall {
        client.get("achievements").body()
    }

    private suspend inline fun <reified T> safeCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: ClientRequestException) {
            val errorBody = e.response.bodyAsText()
            try {
                val error = json.decodeFromString<ErrorResponse>(errorBody)
                Result.failure(ApiException(e.response.status.value, error.message))
            } catch (_: Exception) {
                Result.failure(ApiException(e.response.status.value, errorBody))
            }
        } catch (e: Exception) {
            if (enableNetworkLogs) {
                Napier.e(message = "HTTP call failed", throwable = e)
            }
            Result.failure(ApiException(0, e.message ?: "Unknown error"))
        }
    }
}

interface TokenProvider {
    fun getToken(): String?
    fun setToken(token: String?)
}

class ApiException(val code: Int, override val message: String) : Exception(message)
