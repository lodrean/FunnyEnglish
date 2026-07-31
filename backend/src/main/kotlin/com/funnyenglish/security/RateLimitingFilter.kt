package com.funnyenglish.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // After CORS, before auth
class RateLimitingFilter(
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(RateLimitingFilter::class.java)

    // Rate limit configuration per endpoint
    data class RateLimitConfig(
        val capacity: Int,
        val refillRate: Int,      // tokens per refill period
        val refillPeriodSeconds: Long
    )

    // Token bucket state
    data class TokenBucket(
        val tokens: AtomicInteger,
        val lastRefillTime: AtomicLong
    )

    // In-memory storage for buckets - each IP+path gets its own bucket
    private val buckets = ConcurrentHashMap<String, TokenBucket>()

    // Rate limit configurations
    // Лимиты настраиваются через env (E2E-сьюты делают десятки логинов с одного IP
    // и упираются в 5/мин → флаки). В prod env НЕ выставлять — останутся безопасные дефолты.
    private fun envInt(name: String, default: Int): Int = System.getenv(name)?.toIntOrNull() ?: default

    private val loginConfig = RateLimitConfig(envInt("RATE_LIMIT_LOGIN_CAPACITY", 5), 1, 12)      // 5 per minute, refill 1 per 12s
    private val registerConfig = RateLimitConfig(envInt("RATE_LIMIT_REGISTER_CAPACITY", 3), 1, 20)   // 3 per minute, refill 1 per 20s
    private val mergeConfig = RateLimitConfig(3, 1, 10)      // burst 3, then 1 per 10s
    private val defaultConfig = RateLimitConfig(100, 10, 6)  // 100 per minute, refill 10 per 6s

    init {
        // Schedule cleanup of old buckets every hour
        val scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor()
        scheduler.scheduleAtFixedRate(::cleanupOldBuckets, 1, 1, TimeUnit.HOURS)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI
        val method = request.method

        // Only apply rate limiting to specific endpoints
        if (!shouldApplyRateLimiting(path, method)) {
            filterChain.doFilter(request, response)
            return
        }

        val clientIp = extractClientIp(request)
        val bucketKey = "$clientIp:$path"
        val config = getConfigForPath(path)
        
        // Get or create bucket
        val bucket = buckets.computeIfAbsent(bucketKey) { _ ->
            TokenBucket(
                tokens = AtomicInteger(config.capacity),
                lastRefillTime = AtomicLong(Instant.now().epochSecond)
            )
        }

        // Refill tokens based on elapsed time
        refillTokens(bucket, config)

        // Try to consume a token
        val remainingTokens = bucket.tokens.decrementAndGet()

        // Add rate limit headers
        response.addHeader("X-RateLimit-Limit", config.capacity.toString())
        response.addHeader("X-RateLimit-Remaining", maxOf(0, remainingTokens).toString())
        
        val resetTime = bucket.lastRefillTime.get() + config.refillPeriodSeconds
        response.addHeader("X-RateLimit-Reset", resetTime.toString())

        if (remainingTokens >= 0) {
            // Request allowed
            filterChain.doFilter(request, response)
        } else {
            // Rate limit exceeded - restore the token
            bucket.tokens.incrementAndGet()
            
            val waitTimeSeconds = calculateWaitTime(bucket, config)
            
            logger.warn("Rate limit exceeded for IP: $clientIp, Path: $path")
            
            response.status = 429
            response.addHeader("Retry-After", waitTimeSeconds.toString())
            response.contentType = "application/json"
            
            val errorResponse = RateLimitError(
                error = "Too Many Requests",
                message = "Rate limit exceeded. Try again in $waitTimeSeconds seconds.",
                retryAfter = waitTimeSeconds,
                limit = config.capacity,
                remaining = 0
            )
            
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
        }
    }

    private fun refillTokens(bucket: TokenBucket, config: RateLimitConfig) {
        val now = Instant.now().epochSecond
        val lastRefill = bucket.lastRefillTime.get()
        val elapsedSeconds = now - lastRefill
        
        if (elapsedSeconds >= config.refillPeriodSeconds) {
            val periods = elapsedSeconds / config.refillPeriodSeconds
            val tokensToAdd = (periods * config.refillRate).toInt()
            
            if (tokensToAdd > 0) {
                val newTokens = minOf(config.capacity, bucket.tokens.get() + tokensToAdd)
                bucket.tokens.set(newTokens)
                bucket.lastRefillTime.set(now)
            }
        }
    }

    private fun calculateWaitTime(bucket: TokenBucket, config: RateLimitConfig): Long {
        val now = Instant.now().epochSecond
        val nextRefill = bucket.lastRefillTime.get() + config.refillPeriodSeconds
        return maxOf(1, nextRefill - now)
    }

    private fun shouldApplyRateLimiting(path: String, method: String): Boolean {
        // Rate limit POST requests to auth endpoints
        if (method == "POST" && (path.contains("/auth/login") ||
                path.contains("/auth/register") ||
                path.contains("/auth/refresh"))) {
            return true
        }

        // Rate limit public endpoints to prevent abuse
        if (path.contains("/public/")) {
            return true
        }

        // Rate limit merge endpoint
        if (method == "POST" && path.contains("/merge-guest-progress")) {
            return true
        }

        return false
    }

    private fun extractClientIp(request: HttpServletRequest): String {
        // Check for X-Forwarded-For header (when behind proxy/load balancer)
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            // Take the first IP in the chain
            return xForwardedFor.split(",")[0].trim()
        }

        // Check for X-Real-IP header
        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp.trim()
        }

        // Fall back to remote address
        return request.remoteAddr ?: "unknown"
    }

    private fun getConfigForPath(path: String): RateLimitConfig {
        return when {
            path.contains("/auth/login") -> loginConfig
            path.contains("/auth/register") -> registerConfig
            path.contains("/merge-guest-progress") -> mergeConfig
            else -> defaultConfig
        }
    }

    private fun cleanupOldBuckets() {
        val now = Instant.now().epochSecond
        val oneHour = 60 * 60
        
        val keysToRemove = buckets.entries
            .filter { now - it.value.lastRefillTime.get() > oneHour }
            .map { it.key }
        
        keysToRemove.forEach { key ->
            buckets.remove(key)
        }
        
        if (keysToRemove.isNotEmpty()) {
            logger.debug("Cleaned up ${keysToRemove.size} old rate limit buckets")
        }
    }

    data class RateLimitError(
        val error: String,
        val message: String,
        val retryAfter: Long,
        val limit: Int,
        val remaining: Int
    )
}
