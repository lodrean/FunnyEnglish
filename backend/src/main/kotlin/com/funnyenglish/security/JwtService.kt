package com.funnyenglish.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
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

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
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

    fun validateToken(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
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

    fun extractRole(token: String): String? {
        return try {
            extractAllClaims(token)["role"] as? String
        } catch (e: Exception) {
            null
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
