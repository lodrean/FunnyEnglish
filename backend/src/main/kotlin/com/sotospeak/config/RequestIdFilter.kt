package com.sotospeak.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Request-id трейсинг (bd FunnyEnglish-wy7.8): каждому запросу присваивается
 * идентификатор, который кладётся в MDC (ключ `requestId` — попадает во все
 * строки лога за время обработки, см. %X{requestId} в logging.pattern) и
 * возвращается клиенту в заголовке `X-Request-Id`.
 *
 * Входящий `X-Request-Id` (от прокси/клиента) принимается, но валидируется —
 * произвольные байты в заголовке недопустимы (log injection через CRLF/паттерн).
 * Невалидный или отсутствующий идентификатор заменяется сгенерированным UUID.
 *
 * Порядок — HIGHEST_PRECEDENCE: до RateLimitingFilter (+1) и RequestLoggingFilter (+2),
 * чтобы requestId был в MDC уже на момент их логирования.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestIdFilter : OncePerRequestFilter() {

    companion object {
        const val HEADER_NAME = "X-Request-Id"
        const val MDC_KEY = "requestId"

        // Допустимые символы — чтобы requestId было безопасно писать в лог-паттерн
        private val VALID_ID = Regex("^[A-Za-z0-9._-]{1,64}$")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val incoming = request.getHeader(HEADER_NAME)
        val requestId = if (incoming != null && VALID_ID.matches(incoming)) {
            incoming
        } else {
            UUID.randomUUID().toString()
        }

        MDC.put(MDC_KEY, requestId)
        try {
            response.setHeader(HEADER_NAME, requestId)
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(MDC_KEY)
        }
    }
}
