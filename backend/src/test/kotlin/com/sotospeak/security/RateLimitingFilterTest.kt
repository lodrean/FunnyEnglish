package com.sotospeak.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.PrintWriter
import java.io.StringWriter

class RateLimitingFilterTest {

    private lateinit var filter: RateLimitingFilter
    private lateinit var objectMapper: ObjectMapper
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var filterChain: FilterChain
    private lateinit var responseWriter: StringWriter

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
        filter = RateLimitingFilter(objectMapper)
        request = mock(HttpServletRequest::class.java)
        response = mock(HttpServletResponse::class.java)
        filterChain = mock(FilterChain::class.java)
        
        responseWriter = StringWriter()
        `when`(response.writer).thenReturn(PrintWriter(responseWriter))
    }

    @Test
    fun `should allow request when under rate limit`() {
        // Given
        `when`(request.requestURI).thenReturn("/auth/login")
        `when`(request.method).thenReturn("POST")
        `when`(request.remoteAddr).thenReturn("127.0.0.1")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(filterChain).doFilter(request, response)
        verify(response).addHeader("X-RateLimit-Limit", "5")
        verify(response).addHeader(eq("X-RateLimit-Remaining"), anyString())
    }

    @Test
    fun `should block request when rate limit exceeded`() {
        // Given
        `when`(request.requestURI).thenReturn("/auth/login")
        `when`(request.method).thenReturn("POST")
        `when`(request.remoteAddr).thenReturn("127.0.0.2")

        // When - Make 6 requests (exceeds limit of 5)
        repeat(6) {
            filter.doFilter(request, response, filterChain)
        }

        // Then - Last request should be blocked
        verify(response, atLeastOnce()).status = 429
        verify(response, atLeastOnce()).addHeader(eq("Retry-After"), anyString())
    }

    @Test
    fun `should extract IP from X-Forwarded-For header`() {
        // Given
        `when`(request.requestURI).thenReturn("/auth/login")
        `when`(request.method).thenReturn("POST")
        `when`(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should extract IP from X-Real-IP header`() {
        // Given
        `when`(request.requestURI).thenReturn("/auth/login")
        `when`(request.method).thenReturn("POST")
        `when`(request.getHeader("X-Forwarded-For")).thenReturn(null)
        `when`(request.getHeader("X-Real-IP")).thenReturn("192.168.1.2")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should not apply rate limiting to GET requests`() {
        // Given
        `when`(request.requestURI).thenReturn("/auth/login")
        `when`(request.method).thenReturn("GET")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(filterChain).doFilter(request, response)
        verify(response, never()).addHeader(eq("X-RateLimit-Limit"), anyString())
    }

    @Test
    fun `should not apply rate limiting to non-auth endpoints`() {
        // Given
        `when`(request.requestURI).thenReturn("/api/categories")
        `when`(request.method).thenReturn("POST")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(filterChain).doFilter(request, response)
        verify(response, never()).addHeader(eq("X-RateLimit-Limit"), anyString())
    }

    @Test
    fun `should apply stricter limit to register endpoint`() {
        // Given
        `when`(request.requestURI).thenReturn("/auth/register")
        `when`(request.method).thenReturn("POST")
        `when`(request.remoteAddr).thenReturn("127.0.0.3")

        // When - First request
        filter.doFilter(request, response, filterChain)

        // Then - Should have limit of 3
        verify(response).addHeader("X-RateLimit-Limit", "3")
    }

    @Test
    fun `should return correct error response for rate limit`() {
        // Given
        `when`(request.requestURI).thenReturn("/auth/login")
        `when`(request.method).thenReturn("POST")
        `when`(request.remoteAddr).thenReturn("127.0.0.4")

        // When - Exhaust rate limit
        repeat(10) {
            filter.doFilter(request, response, filterChain)
        }

        // Then - Verify error response structure
        val responseContent = responseWriter.toString()
        assertTrue(responseContent.contains("Too Many Requests"))
        assertTrue(responseContent.contains("Rate limit exceeded"))
        assertTrue(responseContent.contains("retryAfter"))
    }

    @Test
    fun `should add rate limit headers to successful response`() {
        // Given
        `when`(request.requestURI).thenReturn("/auth/login")
        `when`(request.method).thenReturn("POST")
        `when`(request.remoteAddr).thenReturn("127.0.0.5")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(response).addHeader("X-RateLimit-Limit", "5")
        verify(response).addHeader(eq("X-RateLimit-Remaining"), anyString())
        verify(response).addHeader(eq("X-RateLimit-Reset"), anyString())
    }
}
