package com.sotospeak.app.wasm

import com.sotospeak.app.di.isLocalhostHost
import com.sotospeak.app.di.parseApiUrlOverride
import com.sotospeak.app.di.resolveApiBaseUrl
import com.sotospeak.app.di.resolveNetworkLogs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Wasm-тесты чистой логики AppConfig (bd qbq.7): раньше wasmJsTest srcDirs были
 * обнулены (kotest не поддерживается) — теперь только kotlin.test и фильтр
 * com.sotospeak.app.wasm.* в Gradle-задаче.
 */
class AppConfigResolveTest {

    // ==================== parseApiUrlOverride ====================

    @Test
    fun apiUrlOverrideParsed() {
        assertEquals("http://192.168.1.5:8080", parseApiUrlOverride("?apiUrl=http://192.168.1.5:8080"))
        assertEquals(
            "http://x",
            parseApiUrlOverride("?debug=true&apiUrl=http://x&foo=1")
        )
    }

    @Test
    fun apiUrlOverrideAbsent() {
        assertNull(parseApiUrlOverride(""))
        assertNull(parseApiUrlOverride("?debug=true"))
        assertNull(parseApiUrlOverride("?apiUrl")) // без "=" — не override
    }

    // ==================== resolveApiBaseUrl ====================

    @Test
    fun localhostResolvesToLocalBackend() {
        assertEquals(
            "http://localhost:8080",
            resolveApiBaseUrl("localhost:3000", "localhost", "3000", "http:", null)
        )
        assertEquals(
            "http://localhost:8080",
            resolveApiBaseUrl("127.0.0.1:8081", "127.0.0.1", "8081", "http:", null)
        )
    }

    @Test
    fun devPortsResolveToHostBackend() {
        for (port in listOf("8081", "8082", "8085")) {
            assertEquals(
                "http://192.168.1.148:8080",
                resolveApiBaseUrl("192.168.1.148:$port", "192.168.1.148", port, "http:", null),
                "port $port"
            )
        }
    }

    @Test
    fun productionResolvesToSameOrigin() {
        // window.location.protocol содержит ":" — same-origin склейка даёт "https://host"
        assertEquals(
            "https://sotospeak.app",
            resolveApiBaseUrl("sotospeak.app", "sotospeak.app", "", "https:", null)
        )
    }

    @Test
    fun overrideBeatsEverything() {
        assertEquals(
            "http://10.0.0.2:9999",
            resolveApiBaseUrl("localhost:3000", "localhost", "3000", "http:", "http://10.0.0.2:9999")
        )
    }

    // ==================== resolveNetworkLogs ====================

    @Test
    fun networkLogsOnLocalhostOrDebugParam() {
        assertTrue(resolveNetworkLogs("localhost:3000", ""))
        assertTrue(resolveNetworkLogs("sotospeak.app", "?debug=true"))
        assertFalse(resolveNetworkLogs("sotospeak.app", ""))
    }

    // ==================== isLocalhostHost ====================

    @Test
    fun localhostDetection() {
        assertTrue(isLocalhostHost("localhost:8080"))
        assertTrue(isLocalhostHost("127.0.0.1:3000"))
        assertFalse(isLocalhostHost("192.168.1.148:8081"))
        assertFalse(isLocalhostHost("sotospeak.app"))
    }
}
