package com.sotospeak.app.tests

import com.sotospeak.shared.api.ApiException
import com.sotospeak.shared.api.SoToSpeakApi
import com.sotospeak.shared.api.TokenProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Авто-refresh по 401 в [SoToSpeakApi] (bd FunnyEnglish-db9):
 * 401 → POST /api/auth/refresh старым токеном → retry с новым;
 * refresh отклонён (4xx) → токен очищен + onSessionExpired;
 * сетевая ошибка refresh → токен сохраняется, разлогина нет.
 */
class SoToSpeakApiTokenRefreshTest {

    private class FakeTokenProvider(initial: String?) : TokenProvider {
        private var stored: String? = initial
        val current: String? get() = stored
        override fun getToken(): String? = stored
        override fun setToken(token: String?) {
            stored = token
        }
    }

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private val authResponseJson = """
        {"token":"new-token","user":{"id":"u1","email":"a@b.c","displayName":"A","level":1,"totalPoints":0,"currentStreak":0,"role":"USER","createdAt":"2026-01-01T00:00:00Z"}}
    """.trimIndent()

    private val expired401 = """{"error":"Token expired","code":"TOKEN_EXPIRED"}"""

    private fun apiWith(
        tokenProvider: FakeTokenProvider,
        refreshSucceeds: Boolean,
        refreshNetworkError: Boolean = false,
        onSessionExpired: () -> Unit = {}
    ): Pair<SoToSpeakApi, MockEngine> {
        val engine = MockEngine { request ->
            val path = request.url.encodedPath
            when {
                path.endsWith("/api/auth/refresh") ->
                    when {
                        refreshNetworkError -> throw java.io.IOException("network down")
                        refreshSucceeds -> respond(authResponseJson, HttpStatusCode.OK, jsonHeaders)
                        else -> respond(
                            """{"error":"Bad request","message":"Refresh window expired"}""",
                            HttpStatusCode.BadRequest,
                            jsonHeaders
                        )
                    }

                path.endsWith("/api/public/speaking/libraries") -> {
                    val auth = request.headers[HttpHeaders.Authorization]
                    if (auth == "Bearer new-token") {
                        respond("[]", HttpStatusCode.OK, jsonHeaders)
                    } else {
                        respond(expired401, HttpStatusCode.Unauthorized, jsonHeaders)
                    }
                }

                else -> respond("{}", HttpStatusCode.NotFound, jsonHeaders)
            }
        }
        val api = SoToSpeakApi(
            baseUrlProvider = { "http://localhost" },
            tokenProvider = tokenProvider,
            onSessionExpired = onSessionExpired,
            httpClientEngine = engine
        )
        return api to engine
    }

    @Test
    fun `401 triggers refresh and retry succeeds with new token`() = runTest {
        val tokenProvider = FakeTokenProvider("stale-token")
        var sessionExpired = false
        val (api, engine) = apiWith(tokenProvider, refreshSucceeds = true) { sessionExpired = true }

        val result = api.getSpeakingLibraries()

        assertTrue(result.isSuccess, "retry после refresh должен вернуть успех, было: ${result.exceptionOrNull()}")
        assertEquals("new-token", tokenProvider.current)
        assertEquals(false, sessionExpired)
        val refreshCalls = engine.requestHistory.count { it.url.encodedPath.endsWith("/api/auth/refresh") }
        assertEquals(1, refreshCalls, "refresh должен быть вызван ровно один раз")
    }

    @Test
    fun `401 with rejected refresh clears token and notifies session expired`() = runTest {
        val tokenProvider = FakeTokenProvider("stale-token")
        var sessionExpired = false
        val (api, _) = apiWith(tokenProvider, refreshSucceeds = false) { sessionExpired = true }

        val result = api.getSpeakingLibraries()

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertEquals(401, assertIs<ApiException>(ex).code, "ex=$ex")
        assertEquals(null, tokenProvider.current, "токен должен быть очищен")
        assertEquals(true, sessionExpired, "должен сработать onSessionExpired")
    }

    @Test
    fun `401 without token does not attempt refresh`() = runTest {
        val tokenProvider = FakeTokenProvider(null)
        val (api, engine) = apiWith(tokenProvider, refreshSucceeds = true)

        val result = api.getSpeakingLibraries()

        assertTrue(result.isFailure)
        val refreshCalls = engine.requestHistory.count { it.url.encodedPath.endsWith("/api/auth/refresh") }
        assertEquals(0, refreshCalls, "без токена refresh не вызывается")
    }

    @Test
    fun `401 with refresh network error keeps token and does not notify`() = runTest {
        val tokenProvider = FakeTokenProvider("stale-token")
        var sessionExpired = false
        val (api, _) = apiWith(tokenProvider, refreshSucceeds = false, refreshNetworkError = true) {
            sessionExpired = true
        }

        val result = api.getSpeakingLibraries()

        assertTrue(result.isFailure)
        assertEquals("stale-token", tokenProvider.current, "при сетевой ошибке refresh токен НЕ очищается")
        assertEquals(false, sessionExpired, "onSessionExpired не вызывается при сетевой ошибке")
    }
}
