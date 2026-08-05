package com.sotospeak.core.data.network

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating HTTP clients used by data layer.
 */
object HttpClientFactory {

    fun create(enableLogging: Boolean = false): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 30_000
            }

            if (enableLogging) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Napier.d(message, tag = "HttpClient")
                        }
                    }
                    level = LogLevel.ALL
                }
            }
        }
    }

    fun createAuthenticated(
        baseUrl: String,
        tokenProvider: () -> String?,
        enableLogging: Boolean = false
    ): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 30_000
            }

            defaultRequest {
                url(baseUrl)
                contentType(ContentType.Application.Json)
                tokenProvider()?.let { token ->
                    headers.append("Authorization", "Bearer $token")
                }
            }

            if (enableLogging) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Napier.d(message, tag = "HttpClient")
                        }
                    }
                    level = LogLevel.ALL
                }
            }
        }
    }
}
