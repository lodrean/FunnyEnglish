package com.sotospeak.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sotospeak.dto.LoginRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
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

    @Test
    fun `admin login with docker-compose credentials returns token and ADMIN role`() {
        val request = LoginRequest("admin@sotospeak.com", "admin123")

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.token").exists()
            jsonPath("$.user.email").value("admin@sotospeak.com")
            jsonPath("$.user.role").value("ADMIN")
        }
    }

    @Test
    fun `login with invalid password returns 400`() {
        val request = LoginRequest("admin@sotospeak.com", "wrongpassword")

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
