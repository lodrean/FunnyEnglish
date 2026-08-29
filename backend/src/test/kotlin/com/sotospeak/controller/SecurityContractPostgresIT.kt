package com.sotospeak.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sotospeak.dto.LoginRequest
import com.sotospeak.entity.User
import com.sotospeak.repository.UserRepository
import com.sotospeak.security.JwtService
import com.sotospeak.support.PostgresContainerTest
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.util.Date
import java.util.UUID

/**
 * Security-контракты на реальном PostgreSQL (bd FunnyEnglish-wy7.4):
 * 401 UNAUTHORIZED / 401 TOKEN_EXPIRED / 403 без роли / роль из БД, а не из
 * claim токена (nj2.7) / rate-limit e2e (429 после исчерпания bucket).
 *
 * Здесь проверяется сквозной путь через JwtAuthenticationFilter → БД
 * (resolveRole) → SecurityConfig entry point — то, что H2-профиль маскирует.
 * Маппинг БЕЗ /api (context-path в MockMvc не применяется, грабля №65).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityContractPostgresIT : PostgresContainerTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    private val userId = "77777777-7777-7777-7777-777777777777"

    @BeforeEach
    fun setup() {
        if (!userRepository.existsById(UUID.fromString(userId))) {
            userRepository.save(
                User(
                    id = UUID.fromString(userId),
                    email = "security-user@test.com",
                    passwordHash = "password",
                    displayName = "Security User",
                    role = "USER"
                )
            )
        }
    }

    /** ACCESS-токен, истёкший час назад (подписан тем же секретом). */
    private fun expiredAccessToken(): String {
        val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        return Jwts.builder()
            .subject(userId)
            .claim("email", "security-user@test.com")
            .claim("role", "USER")
            .issuedAt(Date(System.currentTimeMillis() - 2 * 3600_000))
            .expiration(Date(System.currentTimeMillis() - 3600_000))
            .signWith(key)
            .compact()
    }

    @Test
    fun `anonymous on protected endpoint returns 401 UNAUTHORIZED`() {
        mockMvc.get("/users/me")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.code").value("UNAUTHORIZED")
            }
    }

    @Test
    fun `expired access token on protected endpoint returns 401 TOKEN_EXPIRED`() {
        mockMvc.get("/users/me") {
            header("Authorization", "Bearer ${expiredAccessToken()}")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.code").value("TOKEN_EXPIRED")
        }
    }

    @Test
    fun `expired access token does not break public endpoints`() {
        mockMvc.get("/public/speaking/libraries") {
            header("Authorization", "Bearer ${expiredAccessToken()}")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `refresh token presented as Bearer returns 401`() {
        val refreshRaw = jwtService.generateRefreshToken(userId).raw
        mockMvc.get("/users/me") {
            header("Authorization", "Bearer $refreshRaw")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.code").value("UNAUTHORIZED")
        }
    }

    @Test
    fun `authenticated USER gets 403 on admin endpoint`() {
        val userToken = jwtService.generateToken(userId, "security-user@test.com", "USER")
        mockMvc.get("/admin/analytics") {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `role claim in token is ignored - role comes from DB`() {
        // Токен выдан с claim role=ADMIN, но в БД пользователь USER → 403.
        // Контракт nj2.7: понижение роли действует без перевыпуска токена.
        val forgedClaimToken = jwtService.generateToken(userId, "security-user@test.com", "ADMIN")
        mockMvc.get("/admin/analytics") {
            header("Authorization", "Bearer $forgedClaimToken")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `anonymous on admin endpoint returns 401, not 403`() {
        mockMvc.get("/admin/analytics")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.code").value("UNAUTHORIZED")
            }
    }

    @Test
    fun `login rate limit e2e - bucket exhaustion returns 429 with Retry-After`() {
        // Лимит читается из env фильтром (дефолт 5/мин, dev-compose ставит 100 —
        // поэтому ёмкость вычисляем так же, а не хардкодим).
        val capacity = System.getenv("RATE_LIMIT_LOGIN_CAPACITY")?.toIntOrNull() ?: 5
        val body = objectMapper.writeValueAsString(
            LoginRequest("no-such-user@test.com", "wrong-password")
        )

        // Первые `capacity` попыток проходят фильтр и падают с 401 INVALID_CREDENTIALS.
        repeat(capacity) {
            mockMvc.post("/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isUnauthorized() }
                header { exists("X-RateLimit-Limit") }
                jsonPath("$.error").value("INVALID_CREDENTIALS")
            }
        }

        // Следующая попытка отклоняется фильтром: 429 + Retry-After + JSON-тело.
        val result = mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isTooManyRequests() }
            header { exists("Retry-After") }
        }.andReturn()

        val responseBody = result.response.contentAsString
        assertTrue(responseBody.contains("Too Many Requests")) { "429 body: $responseBody" }
        assertTrue(responseBody.contains("retryAfter")) { "429 body: $responseBody" }
        assertEquals(capacity.toString(), result.response.getHeader("X-RateLimit-Limit"))
    }
}
