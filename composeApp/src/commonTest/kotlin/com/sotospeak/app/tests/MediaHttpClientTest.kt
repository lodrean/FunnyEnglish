package com.sotospeak.app.tests

import com.sotospeak.app.player.MediaHttpClient
import com.sotospeak.app.player.mediaConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Медиа-HTTP-клиент (bd 4d1): JWT не утекает на медиа-хост, редиректы CDN,
 * не-2xx не бросаются (expectSuccess=false), таймаут-контракт.
 * Тестируется ровно та конфигурация, что уходит в прод — mediaConfig() — на MockEngine.
 */
class MediaHttpClientTest {

    @Test
    fun mediaRequestHasNoAuthorizationHeader() = runTest {
        var capturedAuth: String? = "unset"
        val engine = MockEngine { request ->
            capturedAuth = request.headers[HttpHeaders.Authorization]
            respond("ok", HttpStatusCode.OK)
        }
        val client = HttpClient(engine) { mediaConfig() }
        try {
            client.get("http://cdn.example.com/video.mp4")
        } finally {
            client.close()
        }
        assertNull(capturedAuth, "JWT не должен уходить на медиа-хост")
    }

    @Test
    fun mediaClientFollowsRedirects() = runTest {
        var requestCount = 0
        val engine = MockEngine { request ->
            requestCount++
            if (request.url.encodedPath == "/video.mp4") {
                respond(
                    content = "",
                    status = HttpStatusCode.Found,
                    headers = headersOf(HttpHeaders.Location, "/final.mp4")
                )
            } else {
                respond("ok", HttpStatusCode.OK)
            }
        }
        val client = HttpClient(engine) { mediaConfig() }
        try {
            val response = client.get("http://cdn.example.com/video.mp4")
            assertEquals(HttpStatusCode.OK, response.status, "редирект должен быть пройден")
            assertTrue(requestCount >= 2, "HttpRedirect должен перевыпустить запрос на Location")
        } finally {
            client.close()
        }
    }

    @Test
    fun mediaClientDoesNotThrowOnNon2xx() = runTest {
        val engine = MockEngine { request ->
            respondError(HttpStatusCode.NotFound)
        }
        val client = HttpClient(engine) { mediaConfig() }
        try {
            val response = client.get("http://cdn.example.com/missing.mp4")
            assertEquals(HttpStatusCode.NotFound, response.status, "expectSuccess=false: статус отдаётся, а не бросается")
        } finally {
            client.close()
        }
    }

    @Test
    fun mediaTimeoutConfigHasNoHardRequestLimit() {
        // Контракт таймаутов (bd 4d1, риск R6): connect конечный (10s),
        // request/socket — без жёсткого лимита, чтобы длинные сегменты не рвались.
        assertTrue(MediaHttpClient.CONNECT_TIMEOUT_MS > 0, "connect-таймаут должен быть задан")
        assertEquals(Long.MAX_VALUE, MediaHttpClient.INFINITE_TIMEOUT_MS)
    }
}
