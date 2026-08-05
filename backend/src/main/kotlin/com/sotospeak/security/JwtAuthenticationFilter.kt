package com.sotospeak.security

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {
    
    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

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

        // Check if token is expired first
        val isExpired = jwtService.isTokenExpired(token)
        if (isExpired) {
            logger.warn("JWT Filter: Token expired for $requestUri, returning 401")
            response.status = HttpStatus.UNAUTHORIZED.value()
            response.writer.write("{\"error\":\"Token expired\",\"code\":\"TOKEN_EXPIRED\"}")
            response.contentType = "application/json"
            return
        }

        try {
            if (jwtService.validateToken(token)) {
                logger.debug("JWT Filter: Token is valid")
                val userId = jwtService.extractUserId(token)
                logger.debug("JWT Filter: Extracted userId: $userId")

                // Skip authentication if userId cannot be extracted
                if (userId != null) {
                    val role = jwtService.extractRole(token) ?: "USER"
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
            logger.warn("JWT Filter: Token expired for $requestUri")
            // Return 401 to trigger token refresh on client
            response.status = HttpStatus.UNAUTHORIZED.value()
            response.writer.write("{\"error\":\"Token expired\",\"code\":\"TOKEN_EXPIRED\"}")
            response.contentType = "application/json"
            return
        }

        filterChain.doFilter(request, response)
    }
}

data class UserPrincipal(
    val userId: String,
    val role: String
)
