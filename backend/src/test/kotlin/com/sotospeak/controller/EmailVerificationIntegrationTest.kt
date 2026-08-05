package com.sotospeak.controller

import com.sotospeak.repository.EmailVerificationTokenRepository
import com.sotospeak.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import java.util.UUID

/**
 * Email-верификация при flag=on (OpenSpec add-email-verification, политика login-block):
 * register → без токена → login 403 EMAIL_NOT_VERIFIED → verify-email → login 200; resend anti-enumeration.
 */
@SpringBootTest(properties = ["app.email-verification.enabled=true"])
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmailVerificationIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var tokenRepository: EmailVerificationTokenRepository

    @Autowired
    private lateinit var mailSender: JavaMailSender

    /** JavaMailSender мокаем через @Primary-бин (mockk — грабля №33: Mockito-inline + Kotlin NPE). */
    @TestConfiguration
    class MailSenderMockConfig {
        @Bean
        @Primary
        fun javaMailSenderMock(): JavaMailSender = mockk {
            every { send(any<SimpleMailMessage>()) } returns Unit
        }
    }

    private fun registerBody(email: String) = """
        {"email": "$email", "password": "secret123", "displayName": "Test User"}
    """.trimIndent()

    private fun loginBody(email: String) = """
        {"email": "$email", "password": "secret123"}
    """.trimIndent()

    @Test
    fun `register без auto-login, login 403 до подтверждения, 200 после verify`() {
        val email = "verify_${UUID.randomUUID()}@example.com"

        // 1. Register → 200, emailSent=true, БЕЗ token
        mockMvc.post("/auth/register") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = registerBody(email)
        }.andDo { print() }.andExpect {
            status { isOk() }
            jsonPath("$.emailSent") { value(true) }
            jsonPath("$.token") { doesNotExist() }
            jsonPath("$.user.email") { value(email) }
        }

        // Письмо отправлено (async — но @Async proxy замокан на уровне sender, ждать не нужно:
        // отправка идёт через EmailService → mock; verify с таймаутом не делаем — см. resend-тест)

        // 2. Login до подтверждения → 403 EMAIL_NOT_VERIFIED
        mockMvc.post("/auth/login") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = loginBody(email)
        }.andExpect {
            status { isForbidden() }
            jsonPath("$.error") { value("EMAIL_NOT_VERIFIED") }
        }

        // 3. Токен верификации создан
        val user = userRepository.findByEmail(email)!!
        val token = tokenRepository.findAll().first { it.userId == user.id }

        // 4. verify-email → HTML «Почта подтверждена»
        mockMvc.get("/auth/verify-email") { param("token", token.token) }
            .andExpect {
                status { isOk() }
                content { string(org.hamcrest.Matchers.containsString("Почта подтверждена")) }
            }

        // 5. Повторное использование токена → страница ошибки
        mockMvc.get("/auth/verify-email") { param("token", token.token) }
            .andExpect {
                status { isOk() }
                content { string(org.hamcrest.Matchers.containsString("недействительна")) }
            }

        // 6. Login после подтверждения → 200 с токеном
        mockMvc.post("/auth/login") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = loginBody(email)
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { isString() }
        }
    }

    @Test
    fun `resend — 200 для любого email (anti-enumeration), новый токен для неподтверждённого`() {
        val email = "resend_${UUID.randomUUID()}@example.com"
        mockMvc.post("/auth/register") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = registerBody(email)
        }.andExpect { status { isOk() } }

        // Несуществующий email → тоже 200
        mockMvc.post("/auth/resend-verification") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"email": "nobody_${UUID.randomUUID()}@example.com"}"""
        }.andExpect { status { isOk() } }

        // Существующий неподтверждённый → 200, токен перевыпущен
        val user = userRepository.findByEmail(email)!!
        val oldToken = tokenRepository.findAll().first { it.userId == user.id }.token

        mockMvc.post("/auth/resend-verification") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"email": "$email"}"""
        }.andExpect { status { isOk() } }

        val newToken = tokenRepository.findAll().first { it.userId == user.id }.token
        assert(newToken != oldToken) { "Resend должен перевыпустить токен" }

        // Старый токен инвалидирован
        mockMvc.get("/auth/verify-email") { param("token", oldToken) }
            .andExpect {
                status { isOk() }
                content { string(org.hamcrest.Matchers.containsString("недействительна")) }
            }

        // Письмо уходило (register + resend)
        verify(atLeast = 1) { mailSender.send(any<SimpleMailMessage>()) }
    }
}
