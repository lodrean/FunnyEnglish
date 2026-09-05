package com.sotospeak.app.tests

import com.sotospeak.shared.api.ApiException
import com.sotospeak.shared.api.SoToSpeakApi
import com.sotospeak.shared.api.TokenProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Авто-refresh по 401 в [SoToSpeakApi] под контракт nj2.7 (refresh = отдельный
 * одноразовый JWT с ротацией):
 * 401 → POST /api/auth/refresh с REFRESH-токеном → новая пара (ротация) → retry;
 * refresh отклонён (4xx) → ОБА токена очищены + onSessionExpired;
 * сетевая ошибка refresh → пара сохраняется, разлогина нет;
 * logout → POST /api/auth/logout с refresh-токеном (best-effort).
 */
class SoToSpeakApiTokenRefreshTest {

    private class FakeTokenProvider(access: String?, refresh: String?) : TokenProvider {
        var access: String? = access
        var refresh: String? = refresh
        override fun getToken(): String? = access
        override fun setToken(token: String?) {
            access = token
        }
        override fun getRefreshToken(): String? = refresh
        override fun setRefreshToken(token: String?) {
            refresh = token
        }
    }

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun authResponseJson(refresh: String) = """
        {"token":"new-token","refreshToken":"$refresh","user":{"id":"u1","email":"a@b.c","displayName":"A","level":1,"totalPoints":0,"currentStreak":0,"role":"USER","createdAt":"2026-01-01T00:00:00Z"}}
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
                        refreshSucceeds -> respond(authResponseJson("new-refresh"), HttpStatusCode.OK, jsonHeaders)
                        else -> respond(
                            """{"error":"Bad request","message":"Refresh token invalid"}""",
                            HttpStatusCode.Unauthorized,
                            jsonHeaders
                        )
                    }

                path.endsWith("/api/auth/logout") ->
                    respond("{}", HttpStatusCode.OK, jsonHeaders)

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
    fun `401 triggers refresh with refresh token and retry succeeds with rotated pair`() = runTest {
        val tokenProvider = FakeTokenProvider("stale-access", "stale-refresh")
        var sessionExpired = false
        val (api, engine) = apiWith(tokenProvider, refreshSucceeds = true) { sessionExpired = true }

        val result = api.getSpeakingLibraries()

        assertTrue(result.isSuccess, "retry после refresh должен вернуть успех, было: ${result.exceptionOrNull()}")
        // Ротация: сохранены ОБА новых токена
        assertEquals("new-token", tokenProvider.access)
        assertEquals("new-refresh", tokenProvider.refresh)
        assertEquals(false, sessionExpired)
        val refreshCalls = engine.requestHistory.count { it.url.encodedPath.endsWith("/api/auth/refresh") }
        assertEquals(1, refreshCalls, "refresh должен быть вызван ровно один раз")
        // В теле refresh-запроса — REFRESH-токен, не access (регрессия nj2.9)
        val refreshBody = (engine.requestHistory
            .last { it.url.encodedPath.endsWith("/api/auth/refresh") }
            .body as? TextContent)?.text ?: ""
        assertTrue(refreshBody.contains("stale-refresh"), "в /auth/refresh должен уходить refresh-токен, было: $refreshBody")
    }

    @Test
    fun `401 with rejected refresh clears both tokens and notifies session expired`() = runTest {
        val tokenProvider = FakeTokenProvider("stale-access", "stale-refresh")
        var sessionExpired = false
        val (api, _) = apiWith(tokenProvider, refreshSucceeds = false) { sessionExpired = true }

        val result = api.getSpeakingLibraries()

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertEquals(401, assertIs<ApiException>(ex).code, "ex=$ex")
        assertEquals(null, tokenProvider.access, "access должен быть очищен")
        assertEquals(null, tokenProvider.refresh, "refresh должен быть очищен")
        assertEquals(true, sessionExpired, "должен сработать onSessionExpired")
    }

    @Test
    fun `401 without refresh token does not attempt refresh`() = runTest {
        val tokenProvider = FakeTokenProvider("some-access", null)
        val (api, engine) = apiWith(tokenProvider, refreshSucceeds = true)

        val result = api.getSpeakingLibraries()

        assertTrue(result.isFailure)
        val refreshCalls = engine.requestHistory.count { it.url.encodedPath.endsWith("/api/auth/refresh") }
        assertEquals(0, refreshCalls, "без refresh-токена refresh не вызывается")
    }

    @Test
    fun `401 with refresh network error keeps token pair and does not notify`() = runTest {
        val tokenProvider = FakeTokenProvider("stale-access", "stale-refresh")
        var sessionExpired = false
        val (api, _) = apiWith(tokenProvider, refreshSucceeds = false, refreshNetworkError = true) {
            sessionExpired = true
        }

        val result = api.getSpeakingLibraries()

        assertTrue(result.isFailure)
        assertEquals("stale-access", tokenProvider.access, "при сетевой ошибке access НЕ очищается")
        assertEquals("stale-refresh", tokenProvider.refresh, "при сетевой ошибке refresh НЕ очищается")
        assertEquals(false, sessionExpired, "onSessionExpired не вызывается при сетевой ошибке")
    }

    @Test
    fun `logout posts refresh token and is best-effort`() = runTest {
        val tokenProvider = FakeTokenProvider("some-access", "stored-refresh")
        val (api, engine) = apiWith(tokenProvider, refreshSucceeds = true)

        val result = api.logout()

        assertTrue(result.isSuccess)
        val logoutCall = engine.requestHistory.last { it.url.encodedPath.endsWith("/api/auth/logout") }
        val body = (logoutCall.body as? TextContent)?.text ?: ""
        assertTrue(body.contains("stored-refresh"), "в /auth/logout должен уходить refresh-токен, было: $body")
    }

    @Test
    fun `logout without refresh token skips network call`() = runTest {
        val tokenProvider = FakeTokenProvider(null, null)
        val (api, engine) = apiWith(tokenProvider, refreshSucceeds = true)

        val result = api.logout()

        assertTrue(result.isSuccess)
        val logoutCalls = engine.requestHistory.count { it.url.encodedPath.endsWith("/api/auth/logout") }
        assertEquals(0, logoutCalls, "без refresh-токена logout в сеть не ходит")
    }
}
