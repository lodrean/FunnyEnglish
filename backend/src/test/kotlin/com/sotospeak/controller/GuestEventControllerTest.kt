package com.sotospeak.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sotospeak.entity.GuestEventType
import com.sotospeak.repository.GuestEventRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GuestEventControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var guestEventRepository: GuestEventRepository

    @BeforeEach
    fun cleanup() {
        guestEventRepository.deleteAll()
    }

    @Test
    fun `принимает batch событий и сохраняет в БД`() {
        val anonymousId = UUID.randomUUID().toString()
        val testId = UUID.randomUUID().toString()
        val payload = mapOf(
            "events" to listOf(
                mapOf("anonymousId" to anonymousId, "type" to "SESSION_STARTED"),
                mapOf(
                    "anonymousId" to anonymousId, "type" to "TEST_COMPLETED",
                    "testId" to testId, "score" to 8, "maxScore" to 10, "timeSpentSeconds" to 60
                )
            )
        )

        mockMvc.post("/public/guest-events") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isOk() }
            jsonPath("$.accepted") { value(2) }
        }

        val saved = guestEventRepository.findAll()
        assertEquals(2, saved.size)
        val completion = saved.first { it.type == GuestEventType.TEST_COMPLETED }
        assertEquals(8, completion.score)
        assertEquals(UUID.fromString(testId), completion.testId)
    }

    @Test
    fun `отбрасывает события с невалидным anonymousId и score больше maxScore`() {
        val validId = UUID.randomUUID().toString()
        val payload = mapOf(
            "events" to listOf(
                mapOf("anonymousId" to "not-a-uuid", "type" to "SESSION_STARTED"),
                mapOf("anonymousId" to validId, "type" to "TEST_COMPLETED", "score" to 15, "maxScore" to 10),
                mapOf("anonymousId" to validId, "type" to "SESSION_STARTED")
            )
        )

        mockMvc.post("/public/guest-events") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isOk() }
            jsonPath("$.accepted") { value(1) }
        }

        assertEquals(1, guestEventRepository.count())
    }

    @Test
    fun `batch больше 50 событий отклоняется`() {
        val validId = UUID.randomUUID().toString()
        val payload = mapOf(
            "events" to (1..51).map { mapOf("anonymousId" to validId, "type" to "SESSION_STARTED") }
        )

        mockMvc.post("/public/guest-events") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    fun `аналитика гостей считает агрегаты и конверсию`() {
        val guest1 = UUID.randomUUID()
        val guest2 = UUID.randomUUID()
        val userId = UUID.randomUUID()

        guestEventRepository.save(
            com.sotospeak.entity.GuestEvent(anonymousId = guest1, type = GuestEventType.SESSION_STARTED)
        )
        guestEventRepository.save(
            com.sotospeak.entity.GuestEvent(anonymousId = guest1, type = GuestEventType.TEST_COMPLETED, score = 5, maxScore = 10)
        )
        guestEventRepository.save(
            com.sotospeak.entity.GuestEvent(anonymousId = guest2, type = GuestEventType.SESSION_STARTED)
        )

        assertEquals(2, guestEventRepository.countDistinctGuests())
        assertEquals(1, guestEventRepository.countByType(GuestEventType.TEST_COMPLETED))

        guestEventRepository.markConverted(guest1, userId)
        assertEquals(1, guestEventRepository.countDistinctConvertedGuests())
    }
}
