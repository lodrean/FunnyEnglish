package com.sotospeak.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sotospeak.dto.*
import com.sotospeak.entity.Answer
import com.sotospeak.entity.Category
import com.sotospeak.entity.Question
import com.sotospeak.entity.QuestionType
import com.sotospeak.entity.Test as TestEntity
import com.sotospeak.entity.User
import com.sotospeak.repository.*
import com.sotospeak.security.JwtService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.get
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test", "legacy") // legacy: /public/tests/** изолирован за @Profile("legacy") (bd 0w3.2)
class GuestFlowE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var testRepository: TestRepository

    @Autowired
    private lateinit var questionRepository: QuestionRepository

    @Autowired
    private lateinit var answerRepository: AnswerRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var progressRepository: ProgressRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var category: Category
    private lateinit var testEntity: TestEntity
    private lateinit var question: Question
    private lateinit var correctAnswer: Answer

    @BeforeEach
    fun setup() {
        // Clean up
        progressRepository.deleteAll()
        userRepository.deleteAll()
        answerRepository.deleteAll()
        questionRepository.deleteAll()
        testRepository.deleteAll()
        categoryRepository.deleteAll()

        // Create test data
        category = categoryRepository.save(
            Category(
                name = "Test Category",
                displayOrder = 1
            )
        )

        testEntity = testRepository.save(
            TestEntity(
                category = category,
                title = "Guest Flow Test",
                pointsReward = 10
            )
        )

        question = questionRepository.save(
            Question(
                test = testEntity,
                type = QuestionType.TEXT_SELECT,
                title = "Test Question",
                text = "What is the answer?",
                points = 10,
                displayOrder = 1
            )
        )

        correctAnswer = answerRepository.save(
            Answer(
                question = question,
                text = "Correct",
                isCorrect = true,
                displayOrder = 1
            )
        )

        answerRepository.save(
            Answer(
                question = question,
                text = "Wrong",
                isCorrect = false,
                displayOrder = 2
            )
        )
    }

    @Test
    fun `guest takes test then merges after registration`() {
        // Step 1: Guest validates test via public endpoint
        val validateRequest = SubmitTestRequest(
            testId = testEntity.id.toString(),
            answers = listOf(
                SubmitAnswerRequest(
                    questionId = question.id.toString(),
                    selectedAnswerIds = listOf(correctAnswer.id.toString())
                )
            )
        )

        mockMvc.post("/public/tests/${testEntity.id}/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validateRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.score") { value(10) }
            jsonPath("$.maxScore") { value(10) }
            jsonPath("$.percentage") { value(100) }
            jsonPath("$.stars") { value(3) }
        }

        // Step 2: Create user and generate token directly
        val user = userRepository.save(
            User(
                email = "guestflow@test.com",
                passwordHash = passwordEncoder.encode("password123"),
                displayName = "Guest Flow User"
            )
        )
        val token = jwtService.generateToken(user.id.toString(), user.email, user.role)

        // Step 3: Merge guest progress
        val mergeRequest = MergeGuestProgressRequest(
            testProgress = listOf(
                GuestTestProgressDto(
                    testId = testEntity.id.toString(),
                    score = 10,
                    maxScore = 10,
                    stars = 3,
                    timeSpentSeconds = 60
                )
            )
        )

        mockMvc.post("/users/me/merge-guest-progress") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mergeRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.mergedTests") { value(1) }
            jsonPath("$.totalXpAdded") { value(25) } // 10 base + 3*5 stars = 25
        }

        // Step 4: Verify user progress exists
        mockMvc.get("/users/me/progress") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.size()") { value(1) }
        }
    }

    @Test
    fun `public validate endpoint does not create progress in database`() {
        val initialProgressCount = progressRepository.count()

        val validateRequest = SubmitTestRequest(
            testId = testEntity.id.toString(),
            answers = listOf(
                SubmitAnswerRequest(
                    questionId = question.id.toString(),
                    selectedAnswerIds = listOf(correctAnswer.id.toString())
                )
            )
        )

        mockMvc.post("/public/tests/${testEntity.id}/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validateRequest)
        }.andExpect {
            status { isOk() }
        }

        // Verify no progress was created in the database
        val finalProgressCount = progressRepository.count()
        assert(finalProgressCount == initialProgressCount) {
            "Public validation should not create database entries, but count changed from $initialProgressCount to $finalProgressCount"
        }
    }
}
