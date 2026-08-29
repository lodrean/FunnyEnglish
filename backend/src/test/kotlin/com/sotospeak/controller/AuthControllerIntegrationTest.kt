package com.sotospeak.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sotospeak.dto.LoginRequest
import com.sotospeak.dto.OAuthRequest
import com.sotospeak.entity.User
import com.sotospeak.repository.UserRepository
import com.sotospeak.security.JwtService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Интеграционные тесты аутентификации.
 * Проверяем, что креды из docker-compose / README (`admin@sotospeak.com / admin123`)
 * работают out-of-the-box после старта initializer.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @Test
    fun `admin login with docker-compose credentials returns token and ADMIN role`() {
        val request = LoginRequest("admin@sotospeak.com", "admin123")

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.token").exists()
            jsonPath("$.refreshToken").exists()
            jsonPath("$.user.email").value("admin@sotospeak.com")
            jsonPath("$.user.role").value("ADMIN")
        }
    }

    @Test
    fun `login with invalid password returns 401`() {
        // 401, а не 400: семантика HTTP для неверных кредов (bd FunnyEnglish-nj2.7)
        val request = LoginRequest("admin@sotospeak.com", "wrongpassword")

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.error").value("INVALID_CREDENTIALS")
        }
    }

    @Test
    fun `role is resolved from DB, not from token claim`() {
        // bd FunnyEnglish-nj2.7: токен с подделанным claim role=ADMIN для обычного пользователя
        // НЕ даёт доступа к /admin/** — фильтр берёт роль из БД.
        val email = "forged-admin@test.com"
        val user = userRepository.findByEmail(email) ?: userRepository.save(
            User(email = email, passwordHash = "x", displayName = "Forged Admin")
        )
        val forgedToken = jwtService.generateToken(user.id.toString(), user.email, "ADMIN")

        mockMvc.get("/admin/logs") {
            header("Authorization", "Bearer $forgedToken")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `oauth login is disabled by default and returns 404`() {
        // SEC Б3: endpoint отключён до реализации верификации токена у провайдера.
        val request = OAuthRequest(token = "forged-provider-token", email = "victim@sotospeak.app")

        mockMvc.post("/auth/oauth/google") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
        }
    }
}
