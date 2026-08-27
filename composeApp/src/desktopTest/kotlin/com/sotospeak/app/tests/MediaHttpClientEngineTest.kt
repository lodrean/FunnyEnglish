package com.sotospeak.app.tests

import com.sotospeak.app.player.MediaHttpClient
import io.ktor.client.engine.okhttp.OkHttpEngine
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Детерминированный тест движка медиа-клиента (bd 4d1, D2):
 * проверяем, что [MediaHttpClient.create] на desktop возвращает OkHttpEngine
 * явно, без опоры на ServiceLoader.
 */
class MediaHttpClientEngineTest {

    @Test
    fun `media client engine on desktop is OkHttpEngine`() {
        val client = MediaHttpClient.create()
        try {
            assertTrue(
                actual = client.engine is OkHttpEngine,
                message = "движок медиа-клиента должен быть OkHttpEngine (явный, не ServiceLoader)",
            )
        } finally {
            client.close()
        }
    }
}
