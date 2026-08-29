package com.sotospeak.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sotospeak.entity.ClientLogLevel
import com.sotospeak.entity.User
import com.sotospeak.repository.ClientLogRepository
import com.sotospeak.repository.UserRepository
import com.sotospeak.security.JwtService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.UUID

/**
 * OpenSpec add-client-logging: приём клиентских логов (public) + просмотр админом.
 * URL без /api-префикса — MockMvc не применяет context-path (memory.md №65).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientLogControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var clientLogRepository: ClientLogRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtService: JwtService

    private lateinit var adminToken: String
    private val adminId = "55555555-5555-5555-5555-555555555555"

    @BeforeEach
    fun setup() {
        clientLogRepository.deleteAll()
        if (!userRepository.existsById(UUID.fromString(adminId))) {
            userRepository.save(
                User(
                    id = UUID.fromString(adminId),
                    email = "logs-admin@test.com",
                    passwordHash = "password",
                    displayName = "Logs Admin",
                    role = "ADMIN"
                )
            )
        }
        adminToken = jwtService.generateToken(adminId, "logs-admin@test.com", "ADMIN")
    }

    private fun logEntry(
        level: String = "ERROR",
        anonymousId: String? = UUID.randomUUID().toString(),
        message: String = "boom"
    ): Map<String, Any?> = mapOf(
        "timestamp" to Instant.now().toString(),
        "level" to level,
        "tag" to "HttpClient",
        "message" to message,
        "stackTrace" to "java.lang.IllegalStateException: boom",
        "platform" to "android",
        "appVersion" to "1.0.0-qa",
        "anonymousId" to anonymousId
    )

    @Test
    fun `принимает batch логов без авторизации и сохраняет в БД`() {
        val payload = mapOf("logs" to listOf(logEntry(), logEntry(level = "WARN", message = "warn!")))

        mockMvc.post("/public/logs") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isOk() }
            jsonPath("$.accepted") { value(2) }
        }

        val saved = clientLogRepository.findAll()
        assertEquals(2, saved.size)
        assertEquals(ClientLogLevel.ERROR, saved.first { it.message == "boom" }.level)
        assertEquals("android", saved.first().platform)
        assertNotNull(saved.first().clientTimestamp)
    }

    @Test
    fun `отбрасывает записи с невалидным level и anonymousId, пакет не отклоняется`() {
        val payload = mapOf(
            "logs" to listOf(
                logEntry(level = "VERBOSE"),                    // невалидный уровень
                logEntry(anonymousId = "not-a-uuid"),           // невалидный uuid
                logEntry(message = "valid one")
            )
        )

        mockMvc.post("/public/logs") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isOk() }
            jsonPath("$.accepted") { value(1) }
        }

        assertEquals(1, clientLogRepository.count())
        assertEquals("valid one", clientLogRepository.findAll().first().message)
    }

    @Test
    fun `batch больше 50 или пустой отклоняется`() {
        val big = mapOf("logs" to (1..51).map { logEntry() })
        mockMvc.post("/public/logs") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(big)
        }.andExpect { status { isBadRequest() } }

        val empty = mapOf("logs" to emptyList<Any>())
        mockMvc.post("/public/logs") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(empty)
        }.andExpect { status { isBadRequest() } }

        assertEquals(0, clientLogRepository.count())
    }

    @Test
    fun `длинные message и stackTrace обрезаются до лимитов`() {
        val payload = mapOf(
            "logs" to listOf(
                logEntry(message = "m".repeat(10_000)).toMutableMap().apply {
                    put("stackTrace", "s".repeat(100_000))
                }
            )
        )

        mockMvc.post("/public/logs") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isOk() }
            jsonPath("$.accepted") { value(1) }
        }

        val saved = clientLogRepository.findAll().first()
        assertEquals(4 * 1024, saved.message.length)
        assertEquals(16 * 1024, saved.stackTrace!!.length)
    }

    @Test
    fun `admin logs требует ADMIN и фильтрует по level и platform`() {
        // аноним — 401 (bd FunnyEnglish-nj2.7, раньше 403)
        mockMvc.get("/admin/logs").andExpect { status { isUnauthorized() } }

        // seed: 2 записи
        mockMvc.post("/public/logs") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "logs" to listOf(
                        logEntry(level = "ERROR", message = "android error"),
                        logEntry(level = "WARN", message = "wasm warn").toMutableMap()
                            .apply { put("platform", "wasm") }
                    )
                )
            )
        }.andExpect { status { isOk() } }

        mockMvc.get("/admin/logs?level=ERROR&platform=android") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(1) }
            jsonPath("$.content[0].message") { value("android error") }
        }

        // поиск по подстроке
        mockMvc.get("/admin/logs?q=wasm") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(1) }
        }
    }
}
