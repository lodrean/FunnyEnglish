package com.sotospeak.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.slf4j.MDC

class RequestIdFilterTest {

    private lateinit var filter: RequestIdFilter
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var filterChain: FilterChain

    // requestId, видимый в MDC на момент выполнения цепочки (контроллеры/сервисы)
    private var mdcRequestIdInChain: String? = null

    @BeforeEach
    fun setup() {
        filter = RequestIdFilter()
        request = mock(HttpServletRequest::class.java)
        response = mock(HttpServletResponse::class.java)
        filterChain = mock(FilterChain::class.java)
        mdcRequestIdInChain = null

        doAnswer {
            mdcRequestIdInChain = MDC.get(RequestIdFilter.MDC_KEY)
            null
        }.`when`(filterChain).doFilter(request, response)
    }

    @AfterEach
    fun tearDown() {
        MDC.clear()
    }

    @Test
    fun `should generate request id when header is absent`() {
        `when`(request.getHeader(RequestIdFilter.HEADER_NAME)).thenReturn(null)

        filter.doFilter(request, response, filterChain)

        verify(response).setHeader(eq(RequestIdFilter.HEADER_NAME), anyString())
        assertNotNull(mdcRequestIdInChain)
    }

    @Test
    fun `should echo valid incoming request id`() {
        `when`(request.getHeader(RequestIdFilter.HEADER_NAME)).thenReturn("client-req-123")

        filter.doFilter(request, response, filterChain)

        verify(response).setHeader(RequestIdFilter.HEADER_NAME, "client-req-123")
        assertEquals("client-req-123", mdcRequestIdInChain)
    }

    @Test
    fun `should replace invalid incoming request id to prevent log injection`() {
        `when`(request.getHeader(RequestIdFilter.HEADER_NAME)).thenReturn("bad\r\nINJECTED [FAKE]")

        filter.doFilter(request, response, filterChain)

        // CRLF-значение не должно попасть ни в ответ, ни в MDC/лог-паттерн
        verify(response).setHeader(eq(RequestIdFilter.HEADER_NAME), argThat { it != null && !it.contains("\r") && !it.contains("\n") })
        assertNotNull(mdcRequestIdInChain)
        assertNotEquals("bad\r\nINJECTED [FAKE]", mdcRequestIdInChain)
    }

    @Test
    fun `should replace too long incoming request id`() {
        `when`(request.getHeader(RequestIdFilter.HEADER_NAME)).thenReturn("a".repeat(65))

        filter.doFilter(request, response, filterChain)

        verify(response).setHeader(eq(RequestIdFilter.HEADER_NAME), argThat { it != null && it.length <= 64 })
    }

    @Test
    fun `should clear MDC after request processing`() {
        `when`(request.getHeader(RequestIdFilter.HEADER_NAME)).thenReturn(null)

        filter.doFilter(request, response, filterChain)

        assertNull(MDC.get(RequestIdFilter.MDC_KEY))
    }

    @Test
    fun `should clear MDC even when chain throws`() {
        doAnswer { throw RuntimeException("boom") }.`when`(filterChain).doFilter(request, response)

        try {
            filter.doFilter(request, response, filterChain)
        } catch (_: RuntimeException) {
            // ожидаемо
        }

        assertNull(MDC.get(RequestIdFilter.MDC_KEY))
    }
}
