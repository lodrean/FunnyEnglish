package com.sotospeak.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {
    @Value("\${app.jwt.secret}")
    private lateinit var secretKey: String

    @Value("\${app.jwt.expiration}")
    private var expiration: Long = 86400000

    /** TTL refresh-токена (отдельный долгоживущий токен, bd FunnyEnglish-nj2.7). */
    @Value("\${app.jwt.refresh-expiration:604800000}")
    private var refreshExpiration: Long = 604800000

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }
    
    private val logger = LoggerFactory.getLogger(JwtService::class.java)

    @jakarta.annotation.PostConstruct
    fun validateConfig() {
        // Fail-fast: пустой или короткий секрет = падение при старте, а не при первом логине
        require(secretKey.isNotBlank()) {
            "JWT_SECRET is not set (app.jwt.secret). Set it via env variable (min 32 chars)."
        }
        require(secretKey.toByteArray().size >= 32) {
            "JWT_SECRET is too short (${secretKey.toByteArray().size} bytes). Minimum 32 bytes required for HS256."
        }
    }

    fun generateToken(userId: String, email: String, role: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /** Сгенерированный refresh-токен: сырое значение (отдаётся клиенту), JTI и момент истечения. */
    data class RefreshTokenData(val raw: String, val jti: String, val expiresAt: java.time.Instant)

    /**
     * Refresh-токен — отдельный JWT с claim type=refresh и уникальным JTI.
     * Хранится в БД только как SHA-256-хэш (RefreshTokenService), ротируется при каждом обмене.
     */
    fun generateRefreshToken(userId: String): RefreshTokenData {
        val now = Date()
        val expiryDate = Date(now.time + refreshExpiration)
        val jti = UUID.randomUUID().toString()

        val raw = Jwts.builder()
            .subject(userId)
            .id(jti)
            .claim("type", TOKEN_TYPE_REFRESH)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
        return RefreshTokenData(raw, jti, expiryDate.toInstant())
    }

    /**
     * Парсит refresh-токен: подпись валидна, не истёк, claim type=refresh.
     * null — любая невалидность (caller отвечает 401).
     */
    fun parseRefreshToken(token: String): Claims? {
        return try {
            val claims = extractAllClaims(token)
            if (claims["type"] as? String != TOKEN_TYPE_REFRESH) null else claims
        } catch (e: Exception) {
            logger.warn("Refresh token validation failed: ${e.javaClass.simpleName} - ${e.message}")
            null
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            val isExpired = claims.expiration.before(Date())
            if (isExpired) {
                logger.warn("JWT token expired at ${claims.expiration}")
            }
            !isExpired
        } catch (e: ExpiredJwtException) {
            logger.warn("JWT token expired: ${e.message}")
            false
        } catch (e: Exception) {
            logger.warn("JWT validation failed: ${e.javaClass.simpleName} - ${e.message}")
            false
        }
    }

    fun extractUserId(token: String): String? {
        return try {
            extractAllClaims(token).subject
        } catch (e: Exception) {
            null
        }
    }

    fun extractEmail(token: String): String? {
        return try {
            extractAllClaims(token)["email"] as? String
        } catch (e: Exception) {
            null
        }
    }

    /** claim type токена (refresh-токены помечены type=refresh); null — access-токен/неизвестно. */
    fun extractType(token: String): String? {
        return try {
            extractAllClaims(token)["type"] as? String
        } catch (e: Exception) {
            null
        }
    }

    fun extractRole(token: String): String? {
        return try {
            extractAllClaims(token)["role"] as? String
        } catch (e: Exception) {
            null
        }
    }

    fun extractClaimsAllowExpired(token: String): Claims? {
        return try {
            extractAllClaims(token)
        } catch (e: ExpiredJwtException) {
            e.claims
        } catch (e: Exception) {
            null
        }
    }
    
    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            claims.expiration.before(Date())
        } catch (e: ExpiredJwtException) {
            true
        } catch (e: Exception) {
            // If we can't parse the token, treat it as expired
            logger.debug("Could not parse token to check expiration: ${e.message}")
            true
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    companion object {
        const val TOKEN_TYPE_REFRESH = "refresh"
    }
}
