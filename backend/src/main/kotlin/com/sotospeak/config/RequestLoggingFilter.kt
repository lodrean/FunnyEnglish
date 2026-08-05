package com.sotospeak.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Access-log: метод, путь, статус, длительность каждого запроса.
 * Тела запросов/ответов НЕ логируются (пароли/токены).
 * Health-check пропускаем — иначе docker healthcheck шумит каждые 30с.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2) // после RateLimitingFilter, до auth
class RequestLoggingFilter : OncePerRequestFilter() {

    private val accessLogger = LoggerFactory.getLogger("com.sotospeak.accesslog")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val start = System.nanoTime()
        try {
            filterChain.doFilter(request, response)
        } finally {
            val durationMs = (System.nanoTime() - start) / 1_000_000
            accessLogger.info(
                "{} {} -> {} ({} ms)",
                request.method,
                request.requestURI,
                response.status,
                durationMs
            )
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.requestURI.contains("/actuator/health")
}
