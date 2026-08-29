package com.sotospeak.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sotospeak.dto.RefreshTokenRequest
import com.sotospeak.dto.RegisterRequest
import com.sotospeak.entity.User
import com.sotospeak.repository.UserRepository
import com.sotospeak.security.JwtService
import com.sotospeak.service.RefreshTokenService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.util.Date
import java.util.UUID

/**
 * Контракт refresh-аутентификации (bd FunnyEnglish-nj2.7, SEC AR-6):
 * отдельный refresh-токен (JTI + хэш в БД), ротация при обмене, отзыв через logout,
 * reuse-detection с отзывом всей цепочки; аноним/невалидный токен → 401.
 * Плюс регрессия bd FunnyEnglish-db9: истёкший ACCESS-токен не роняет публичные эндпоинты.
 * Маппинг без /api (context-path в MockMvc не применяется).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TokenRefreshIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var refreshTokenService: RefreshTokenService

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    private val userId = "55555555-5555-5555-5555-555555555555"

    @BeforeEach
    fun setup() {
        if (!userRepository.existsById(UUID.fromString(userId))) {
            userRepository.save(
                User(
                    id = UUID.fromString(userId),
                    email = "refresh-user@test.com",
                    passwordHash = "password",
                    displayName = "Refresh User"
                )
            )
        }
    }

    /** Подписанный тем же секретом ACCESS-токен с заданным моментом истечения. */
    private fun accessTokenExpiringAt(expiration: Date): String {
        val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        return Jwts.builder()
            .subject(userId)
            .claim("email", "refresh-user@test.com")
            .claim("role", "USER")
            .issuedAt(Date(expiration.time - 3600_000))
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    /** Истёк час назад access-токен. */
    private fun expiredAccessToken(): String =
        accessTokenExpiringAt(Date(System.currentTimeMillis() - 3600_000))

    // --- Регрессия db9: истёкший access-токен не ломает публичные эндпоинты ---

    @Test
    fun `expired token on public endpoint returns 200, not 401`() {
        mockMvc.get("/public/speaking/libraries") {
            header("Authorization", "Bearer ${expiredAccessToken()}")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `no token on public endpoint still returns 200`() {
        mockMvc.get("/public/speaking/libraries")
            .andExpect { status { isOk() } }
    }

    @Test
    fun `expired token on protected endpoint returns 401 with TOKEN_EXPIRED code`() {
        mockMvc.get("/users/me") {
            header("Authorization", "Bearer ${expiredAccessToken()}")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.code").value("TOKEN_EXPIRED")
        }
    }

    @Test
    fun `anonymous request to protected endpoint returns 401`() {
        mockMvc.get("/users/me")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.code").value("UNAUTHORIZED")
            }
    }

    // --- Refresh-токены: выдача, ротация, reuse-detection, отзыв ---

    @Test
    fun `register returns refresh token, refresh rotates it, reuse revokes the chain`() {
        // 1. Регистрация → access + refresh
        val registerResult = mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RegisterRequest("rotate-user@test.com", "password123", "Rotate User")
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.token").exists()
            jsonPath("$.refreshToken").exists()
        }.andReturn()

        val registerBody = objectMapper.readTree(registerResult.response.contentAsString)
        val oldRefresh = registerBody.get("refreshToken").asText()

        // Refresh-токен НЕ работает как access-токен на защищённом эндпоинте
        mockMvc.get("/users/me") {
            header("Authorization", "Bearer $oldRefresh")
        }.andExpect {
            status { isUnauthorized() }
        }

        // 2. Обмен → новая пара, refresh-токен ротирован (отличается от старого)
        val refreshResult = mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest(oldRefresh))
        }.andExpect {
            status { isOk() }
            jsonPath("$.token").exists()
            jsonPath("$.refreshToken").exists()
            jsonPath("$.user.email").value("rotate-user@test.com")
        }.andReturn()

        val refreshBody = objectMapper.readTree(refreshResult.response.contentAsString)
        val newAccess = refreshBody.get("token").asText()
        val newRefresh = refreshBody.get("refreshToken").asText()
        assert(newRefresh != oldRefresh) { "Refresh token must rotate on every exchange" }

        // Новый access-токен работает на защищённом эндпоинте
        mockMvc.get("/users/me") {
            header("Authorization", "Bearer $newAccess")
        }.andExpect {
            status { isOk() }
            jsonPath("$.email").value("rotate-user@test.com")
        }

        // 3. Reuse-detection: повторное использование СТАРОГО токена → 401 + отзыв всей цепочки
        mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest(oldRefresh))
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.error").value("INVALID_REFRESH_TOKEN")
        }

        // Цепочка отозвана: и НОВЫЙ токен больше не работает
        mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest(newRefresh))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `access token cannot be used as refresh token`() {
        val accessToken = jwtService.generateToken(userId, "refresh-user@test.com", "USER")
        mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest(accessToken))
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.error").value("INVALID_REFRESH_TOKEN")
        }
    }

    @Test
    fun `refresh with garbage token returns 401`() {
        mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest("not-a-jwt"))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `refresh with expired refresh token returns 401`() {
        val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val expiredRefresh = Jwts.builder()
            .subject(userId)
            .id(UUID.randomUUID().toString())
            .claim("type", JwtService.TOKEN_TYPE_REFRESH)
            .issuedAt(Date(System.currentTimeMillis() - 8L * 24 * 3600_000))
            .expiration(Date(System.currentTimeMillis() - 7L * 24 * 3600_000))
            .signWith(key)
            .compact()

        mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest(expiredRefresh))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `logout revokes refresh token, subsequent refresh returns 401`() {
        val user = userRepository.findById(UUID.fromString(userId)).get()
        val rawRefresh = refreshTokenService.issue(user)

        mockMvc.post("/auth/logout") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest(rawRefresh))
        }.andExpect {
            status { isOk() }
        }

        mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest(rawRefresh))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `logout is idempotent for unknown or invalid tokens`() {
        mockMvc.post("/auth/logout") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest("not-a-jwt"))
        }.andExpect {
            status { isOk() }
        }
    }
}
