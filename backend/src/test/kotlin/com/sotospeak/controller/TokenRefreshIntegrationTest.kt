package com.sotospeak.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sotospeak.dto.RefreshTokenRequest
import com.sotospeak.entity.User
import com.sotospeak.repository.UserRepository
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
 * Регрессия bd FunnyEnglish-db9 (логи 2026-08-18): истёкший JWT не должен ронять
 * публичные эндпоинты (401 TOKEN_EXPIRED из фильтра до authorization-слоя).
 * Плюс контракт /auth/refresh: истёкший токен в пределах окна → новый токен; за окном → 400.
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

    /** Подписанный тем же секретом токен с заданным моментом истечения. */
    private fun tokenExpiringAt(expiration: Date): String {
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

    /** Истёк час назад — в пределах refresh-окна (7 дней). */
    private fun expiredWithinWindow(): String =
        tokenExpiringAt(Date(System.currentTimeMillis() - 3600_000))

    /** Истёк 8 дней назад — за пределами refresh-окна. */
    private fun expiredBeyondWindow(): String =
        tokenExpiringAt(Date(System.currentTimeMillis() - 8L * 24 * 3600_000))

    @Test
    fun `expired token on public endpoint returns 200, not 401`() {
        mockMvc.get("/public/speaking/libraries") {
            header("Authorization", "Bearer ${expiredWithinWindow()}")
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
            header("Authorization", "Bearer ${expiredWithinWindow()}")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.code").value("TOKEN_EXPIRED")
        }
    }

    @Test
    fun `refresh with expired token within window returns new working token`() {
        val result = mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest(expiredWithinWindow()))
        }.andExpect {
            status { isOk() }
            jsonPath("$.token").exists()
            jsonPath("$.user.email").value("refresh-user@test.com")
        }.andReturn()

        val newToken = objectMapper.readTree(result.response.contentAsString).get("token").asText()

        // Новый токен работает на защищённом эндпоинте
        mockMvc.get("/users/me") {
            header("Authorization", "Bearer $newToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.email").value("refresh-user@test.com")
        }
    }

    @Test
    fun `refresh with token expired beyond window returns 400`() {
        mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest(expiredBeyondWindow()))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `refresh with garbage token returns 400`() {
        mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest("not-a-jwt"))
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
