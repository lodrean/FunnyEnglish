package com.sotospeak.security

import com.sotospeak.repository.UserRepository
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    // Кэш userId→role (bd FunnyEnglish-nj2.7): роль сверяется с БД, а не с claim токена
    // (смена/понижение роли вступает в силу без перевыпуска токена). TTL 60с — компромисс
    // между свежестью и запросом к БД на каждый HTTP-запрос (рекомендация аудита: 1–5 мин).
    private data class CachedRole(val role: String, val expiresAtMs: Long)
    private val roleCache = ConcurrentHashMap<String, CachedRole>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        val requestUri = request.requestURI

        logger.debug("JWT Filter: Processing request to $requestUri")
        logger.debug("JWT Filter: Authorization header present: ${authHeader != null}")

        if (authHeader != null) {
            logger.debug("JWT Filter: Authorization header starts with Bearer: ${authHeader.startsWith("Bearer ")}")
            logger.debug("JWT Filter: Authorization header length: ${authHeader.length}")
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("JWT Filter: No valid Authorization header, continuing as anonymous")
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)
        logger.debug("JWT Filter: Token extracted, validating...")

        // Истёкший токен НЕ роняет запрос 401 на уровне фильтра: продолжаем как аноним,
        // публичные эндпоинты (permitAll) обязаны работать с протухшим токеном.
        // Для защищённых путей 401 отдаст authorization-слой (entry point в SecurityConfig),
        // а attribute TOKEN_EXPIRED позволяет ему вернуть машиночитаемый код для клиентского refresh.
        if (jwtService.isTokenExpired(token)) {
            logger.warn("JWT Filter: Token expired for $requestUri, continuing as anonymous")
            request.setAttribute(ATTR_TOKEN_EXPIRED, true)
            filterChain.doFilter(request, response)
            return
        }

        // Refresh-токен не является access-токеном: предъявление его как Bearer → аноним (401).
        if (jwtService.extractType(token) == JwtService.TOKEN_TYPE_REFRESH) {
            logger.warn("JWT Filter: refresh token used as access token for $requestUri, continuing as anonymous")
            filterChain.doFilter(request, response)
            return
        }

        try {
            if (jwtService.validateToken(token)) {
                logger.debug("JWT Filter: Token is valid")
                val userId = jwtService.extractUserId(token)
                logger.debug("JWT Filter: Extracted userId: $userId")

                // Skip authentication if userId cannot be extracted
                if (userId != null) {
                    // Роль — из БД (не из claim токена): понижение роли/блокировка действуют
                    // в течение TTL кэша даже для уже выданных токенов. Пользователь удалён → аноним.
                    val role = resolveRole(userId)
                    if (role == null) {
                        logger.warn("JWT Filter: user $userId not found in DB, continuing as anonymous")
                        filterChain.doFilter(request, response)
                        return
                    }
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))

                    val authentication = UsernamePasswordAuthenticationToken(
                        UserPrincipal(userId, role),
                        null,
                        authorities
                    )

                    SecurityContextHolder.getContext().authentication = authentication
                    logger.debug("JWT Filter: Authentication set for user $userId")
                } else {
                    logger.warn("JWT Filter: Token valid but userId is null")
                }
            } else {
                // Token validation failed - could be expired or invalid
                logger.warn("JWT Filter: Token validation failed for $requestUri")
            }
        } catch (e: ExpiredJwtException) {
            logger.warn("JWT Filter: Token expired for $requestUri, continuing as anonymous")
            request.setAttribute(ATTR_TOKEN_EXPIRED, true)
        }

        filterChain.doFilter(request, response)
    }

    /** Роль пользователя из БД с кэшем на [ROLE_CACHE_TTL_MS]; null — пользователь не найден/ошибка. */
    private fun resolveRole(userId: String): String? {
        val now = System.currentTimeMillis()
        roleCache[userId]?.takeIf { it.expiresAtMs > now }?.let { return it.role }
        val uuid = runCatching { UUID.fromString(userId) }.getOrNull() ?: return null
        val role = runCatching { userRepository.findById(uuid).orElse(null)?.role }
            .onFailure { logger.error("JWT Filter: role lookup failed for $userId: ${it.message}") }
            .getOrNull() ?: return null
        roleCache[userId] = CachedRole(role, now + ROLE_CACHE_TTL_MS)
        return role
    }

    companion object {
        /** Request attribute: токен был, но истёк — entry point отдаёт 401 с code=TOKEN_EXPIRED. */
        const val ATTR_TOKEN_EXPIRED = "com.sotospeak.TOKEN_EXPIRED"

        /** TTL кэша userId→role (мс). */
        const val ROLE_CACHE_TTL_MS = 60_000L
    }
}

data class UserPrincipal(
    val userId: String,
    val role: String
)
