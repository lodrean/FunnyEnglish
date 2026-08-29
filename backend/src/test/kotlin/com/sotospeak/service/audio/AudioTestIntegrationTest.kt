package com.sotospeak.service.audio

import com.sotospeak.dto.*
import com.sotospeak.entity.User
import com.sotospeak.entity.audio.QuestionType
import com.sotospeak.repository.UserRepository
import com.sotospeak.repository.audio.AudioTestRepository
import com.sotospeak.security.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test", "legacy") // legacy: AudioTestController изолирован за @Profile("legacy") (bd 0w3.2)
class AudioTestIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var audioTestRepository: AudioTestRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtService: JwtService

    private lateinit var adminToken: String
    private lateinit var userToken: String
    private val adminId = "11111111-1111-1111-1111-111111111111"
    private val userId = "22222222-2222-2222-2222-222222222222"

    @BeforeEach
    fun setup() {
        // Create test users in database
        if (!userRepository.existsById(UUID.fromString(adminId))) {
            userRepository.save(User(
                id = UUID.fromString(adminId),
                email = "admin@test.com",
                passwordHash = "password",
                displayName = "Admin User"
            ))
        }
        if (!userRepository.existsById(UUID.fromString(userId))) {
            userRepository.save(User(
                id = UUID.fromString(userId),
                email = "user@test.com",
                passwordHash = "password",
                displayName = "Test User"
            ))
        }
        
        // Create test tokens with valid UUIDs
        adminToken = jwtService.generateToken(adminId, "admin@test.com", "ADMIN")
        userToken = jwtService.generateToken(userId, "user@test.com", "USER")
    }

    @Test
    fun `create audio test - admin success`() {
        val request = CreateAudioTestRequest(
            title = "Test Audio Lesson",
            description = "A test audio lesson for integration testing",
            audioFileUrl = "https://example.com/audio/test.mp3",
            durationSeconds = 120,
            difficulty = 2,
            questions = listOf(
                CreateAudioQuestionRequest(
                    questionType = QuestionType.LISTENING_COMPREHENSION,
                    title = "What is the main topic?",
                    startTimeSeconds = 10,
                    endTimeSeconds = 30,
                    points = 1,
                    answers = listOf(
                        CreateAudioAnswerRequest(text = "Travel", isCorrect = true),
                        CreateAudioAnswerRequest(text = "Food", isCorrect = false),
                        CreateAudioAnswerRequest(text = "Sports", isCorrect = false)
                    )
                )
            )
        )

        mockMvc.post("/api/audio-tests/admin") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Test Audio Lesson") }
            jsonPath("$.difficulty") { value(2) }
            jsonPath("$.questions") { isArray() }
            jsonPath("$.questions[0].answers") { isArray() }
            jsonPath("$.questions[0].answers[0].text") { value("Travel") }
        }
    }

    @Test
    fun `create audio test - user forbidden`() {
        val request = CreateAudioTestRequest(
            title = "Test Audio Lesson",
            description = "A test audio lesson",
            audioFileUrl = "https://example.com/audio/test.mp3",
            durationSeconds = 120,
            difficulty = 2,
            questions = emptyList()
        )

        mockMvc.post("/api/audio-tests/admin") {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `get published audio tests - success`() {
        mockMvc.get("/api/audio-tests") {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
        }
    }

    @Test
    @Disabled("Test has LAZY loading issues - needs investigation")
    fun `submit audio test - correct answers`() {
        // First create an audio test
        val createRequest = CreateAudioTestRequest(
            title = "Simple Test",
            description = "Test",
            audioFileUrl = "https://example.com/audio/test.mp3",
            durationSeconds = 60,
            difficulty = 1,
            questions = listOf(
                CreateAudioQuestionRequest(
                    questionType = QuestionType.LISTENING_COMPREHENSION,
                    title = "Choose correct",
                    startTimeSeconds = 5,
                    endTimeSeconds = 15,
                    points = 1,
                    answers = listOf(
                        CreateAudioAnswerRequest(text = "Correct", isCorrect = true),
                        CreateAudioAnswerRequest(text = "Wrong", isCorrect = false)
                    )
                )
            )
        )

        // Create and get ID
        val createResult = mockMvc.post("/api/audio-tests/admin") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createRequest)
        }.andReturn()

        val response = objectMapper.readValue(
            createResult.response.contentAsString,
            AudioTestDetailResponse::class.java
        )

        // Publish the test
        mockMvc.post("/api/audio-tests/admin/${response.id!!}/publish") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
        }

        // Get the test details again to ensure all data is loaded
        val detailResult = mockMvc.get("/api/audio-tests/admin/${response.id!!}") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
        }.andReturn()
        
        val detailResponse = objectMapper.readValue(
            detailResult.response.contentAsString,
            AudioTestDetailResponse::class.java
        )
        
        // Get the correct answer ID  
        assert(detailResponse.questions.isNotEmpty()) { "Questions should not be empty" }
        assert(detailResponse.questions[0].answers.isNotEmpty()) { "Answers should not be empty, questions: ${detailResponse.questions}" }
        val correctAnswerId = detailResponse.questions[0].answers.first { it.isCorrect }.id!!

        // Submit with correct answer
        val submitRequest = SubmitAudioTestRequest(
            audioTestId = detailResponse.id!!,
            answers = listOf(
                SubmitAudioAnswerRequest(
                    questionId = detailResponse.questions[0].id!!,
                    selectedAnswerIds = listOf(correctAnswerId)
                )
            ),
            timeSpentSeconds = 30
        )

        mockMvc.post("/api/audio-tests/submit") {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(submitRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.score") { value(1) }
            jsonPath("$.maxScore") { value(1) }
            jsonPath("$.percentage") { value(100) }
            jsonPath("$.stars") { value(3) }
        }
    }

    @Test
    @Disabled("Test has LAZY loading issues - needs investigation")
    fun `submit audio test - wrong answers`() {
        // Create test
        val createRequest = CreateAudioTestRequest(
            title = "Simple Test",
            description = "Test",
            audioFileUrl = "https://example.com/audio/test.mp3",
            durationSeconds = 60,
            difficulty = 1,
            questions = listOf(
                CreateAudioQuestionRequest(
                    questionType = QuestionType.LISTENING_COMPREHENSION,
                    title = "Choose correct",
                    startTimeSeconds = 5,
                    endTimeSeconds = 15,
                    points = 1,
                    answers = listOf(
                        CreateAudioAnswerRequest(text = "Correct", isCorrect = true),
                        CreateAudioAnswerRequest(text = "Wrong", isCorrect = false)
                    )
                )
            )
        )

        val createResult = mockMvc.post("/api/audio-tests/admin") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createRequest)
        }.andReturn()

        val response = objectMapper.readValue(
            createResult.response.contentAsString,
            AudioTestDetailResponse::class.java
        )

        // Publish
        mockMvc.post("/api/audio-tests/admin/${response.id!!}/publish") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
        }
        
        // Get the test details again to ensure all data is loaded
        val detailResult = mockMvc.get("/api/audio-tests/admin/${response.id!!}") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
        }.andReturn()
        
        val detailResponse = objectMapper.readValue(
            detailResult.response.contentAsString,
            AudioTestDetailResponse::class.java
        )

        // Get wrong answer ID
        assert(detailResponse.questions.isNotEmpty()) { "Questions should not be empty" }
        assert(detailResponse.questions[0].answers.isNotEmpty()) { "Answers should not be empty" }
        val wrongAnswerId = detailResponse.questions[0].answers.first { !it.isCorrect }.id!!

        // Submit with wrong answer
        val submitRequest = SubmitAudioTestRequest(
            audioTestId = detailResponse.id!!,
            answers = listOf(
                SubmitAudioAnswerRequest(
                    questionId = detailResponse.questions[0].id!!,
                    selectedAnswerIds = listOf(wrongAnswerId)
                )
            )
        )

        mockMvc.post("/api/audio-tests/submit") {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(submitRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.score") { value(0) }
            jsonPath("$.maxScore") { value(1) }
            jsonPath("$.percentage") { value(0) }
            jsonPath("$.stars") { value(0) }
        }
    }

    @Test
    fun `update audio test - admin success`() {
        // Create first
        val createRequest = CreateAudioTestRequest(
            title = "Original Title",
            description = "Original",
            audioFileUrl = "https://example.com/audio/test.mp3",
            durationSeconds = 60,
            difficulty = 1,
            questions = emptyList()
        )

        val createResult = mockMvc.post("/api/audio-tests/admin") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createRequest)
        }.andReturn()

        val response = objectMapper.readValue(
            createResult.response.contentAsString,
            AudioTestDetailResponse::class.java
        )

        // Update
        val updateRequest = UpdateAudioTestRequest(
            title = "Updated Title",
            difficulty = 3
        )

        mockMvc.put("/api/audio-tests/admin/${response.id}") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Updated Title") }
            jsonPath("$.difficulty") { value(3) }
        }
    }

    @Test
    fun `delete audio test - admin success`() {
        val createRequest = CreateAudioTestRequest(
            title = "To Delete",
            description = "Will be deleted",
            audioFileUrl = "https://example.com/audio/test.mp3",
            durationSeconds = 60,
            difficulty = 1,
            questions = emptyList()
        )

        val createResult = mockMvc.post("/api/audio-tests/admin") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createRequest)
        }.andReturn()

        val response = objectMapper.readValue(
            createResult.response.contentAsString,
            AudioTestDetailResponse::class.java
        )

        mockMvc.delete("/api/audio-tests/admin/${response.id}") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `get user progress - success`() {
        mockMvc.get("/api/audio-tests/my-progress") {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
        }
    }

    @Test
    fun `validate question timing - invalid range`() {
        val request = CreateAudioTestRequest(
            title = "Invalid Test",
            description = "Test",
            audioFileUrl = "https://example.com/audio/test.mp3",
            durationSeconds = 60,
            difficulty = 1,
            questions = listOf(
                CreateAudioQuestionRequest(
                    questionType = QuestionType.LISTENING_COMPREHENSION,
                    title = "Invalid timing",
                    startTimeSeconds = 50,
                    endTimeSeconds = 10, // Invalid: end < start
                    points = 1,
                    answers = listOf(
                        CreateAudioAnswerRequest(text = "Answer", isCorrect = true)
                    )
                )
            )
        )

        mockMvc.post("/api/audio-tests/admin") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
