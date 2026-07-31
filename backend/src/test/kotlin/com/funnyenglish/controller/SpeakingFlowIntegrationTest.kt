package com.funnyenglish.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.funnyenglish.dto.CreateLibraryRequest
import com.funnyenglish.dto.CreateSpeakingQuestionRequest
import com.funnyenglish.dto.CreateTopicRequest
import com.funnyenglish.dto.GradeSubmissionRequest
import com.funnyenglish.dto.UpdateTopicRequest
import com.funnyenglish.entity.User
import com.funnyenglish.entity.speaking.Library
import com.funnyenglish.entity.speaking.SpeakingQuestion
import com.funnyenglish.entity.speaking.Topic
import com.funnyenglish.entity.speaking.Video
import com.funnyenglish.repository.UserRepository
import com.funnyenglish.repository.speaking.LibraryRepository
import com.funnyenglish.repository.speaking.TopicRepository
import com.funnyenglish.security.JwtService
import com.funnyenglish.service.StorageService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Сквозной флоу Speaking Trainer (Part 1 §8.3).
 * StorageService замокан (@MockitoBean) — тесты герметичны, без MinIO;
 * mock возвращает URL на базе public-url (сценарий 3).
 * Маппинг контроллеров БЕЗ /api (context-path в MockMvc не применяется).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SpeakingFlowIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var libraryRepository: LibraryRepository

    @Autowired
    private lateinit var topicRepository: TopicRepository

    @Autowired
    private lateinit var jwtService: JwtService

    /**
     * StorageService замокан через @Primary-бин (mockk умеет final Kotlin-классы,
     * Mockito-inline + Kotlin non-null params падает с NPE на stubbing).
     * Тесты герметичны, без MinIO; mock возвращает URL на базе public-url (сценарий 3).
     */
    @TestConfiguration
    class StorageServiceMockConfig {
        @Bean
        @Primary
        fun storageServiceMock(): StorageService = mockk {
            every { uploadFile(any(), any()) } returns
                "http://localhost:9000/funnyenglish-test/speaking/submissions/u_test/abc.m4a"
            every { deleteFile(any()) } returns Unit
        }
    }

    private lateinit var adminToken: String
    private lateinit var userToken: String
    private val adminId = "33333333-3333-3333-3333-333333333333"
    private val userId = "44444444-4444-4444-4444-444444444444"

    private val publicAudioUrl =
        "http://localhost:9000/funnyenglish-test/speaking/submissions/u_test/abc.m4a"

    @BeforeEach
    fun setup() {
        if (!userRepository.existsById(UUID.fromString(adminId))) {
            userRepository.save(User(
                id = UUID.fromString(adminId),
                email = "speaking-admin@test.com",
                passwordHash = "password",
                displayName = "Teacher Anna",
                role = "ADMIN"
            ))
        }
        if (!userRepository.existsById(UUID.fromString(userId))) {
            userRepository.save(User(
                id = UUID.fromString(userId),
                email = "speaking-user@test.com",
                passwordHash = "password",
                displayName = "Student Ivan"
            ))
        }
        adminToken = jwtService.generateToken(adminId, "speaking-admin@test.com", "ADMIN")
        userToken = jwtService.generateToken(userId, "speaking-user@test.com", "USER")
    }

    /** Сид: опубликованная тема + опубликованный топик с видео и вопросом */
    private fun seedPublishedContent(): Topic {
        val library = libraryRepository.save(Library(title = "Everyday Life", isPublished = true))
        val topic = Topic(title = "My Morning Routine", isPublished = true)
        library.addTopic(topic)
        topic.video = Video(
            topic = topic,
            videoUrl = "http://localhost:9000/funnyenglish-test/speaking/videos/v.mp4",
            subtitleUrl = "http://localhost:9000/funnyenglish-test/speaking/subtitles/s.vtt",
            durationSeconds = 95
        )
        topic.addQuestion(SpeakingQuestion(text = "What time do you wake up?", displayOrder = 0))
        return topicRepository.save(topic)
    }

    // 1. Гость: GET libraries → 200; неопубликованная тема отсутствует
    @Test
    fun `guest can read published libraries, unpublished are hidden`() {
        seedPublishedContent()
        libraryRepository.save(Library(title = "Draft Theme", isPublished = false))

        mockMvc.get("/public/speaking/libraries")
            .andExpect {
                status { isOk() }
                jsonPath("$[?(@.title == 'Everyday Life')]") { exists() }
                jsonPath("$[?(@.title == 'Everyday Life')].topicCount") { value(1) }
                jsonPath("$[?(@.title == 'Draft Theme')]") { doesNotExist() }
            }
    }

    // 1b. Гость: детали топика → видео + вопросы; неопубликованный → 404
    @Test
    fun `guest can read topic detail, unpublished topic returns 404`() {
        val topic = seedPublishedContent()
        val draftLibrary = libraryRepository.save(Library(title = "Draft", isPublished = false))
        val draftTopic = Topic(title = "Hidden Topic", isPublished = false)
        draftLibrary.addTopic(draftTopic)
        val hidden = topicRepository.save(draftTopic)

        mockMvc.get("/public/speaking/topics/${topic.id}")
            .andExpect {
                status { isOk() }
                jsonPath("$.title") { value("My Morning Routine") }
                jsonPath("$.video.videoUrl") { exists() }
                jsonPath("$.video.subtitleUrl") { exists() }
                jsonPath("$.questions[0].text") { value("What time do you wake up?") }
            }

        mockMvc.get("/public/speaking/topics/${hidden.id}")
            .andExpect { status { isNotFound() } }
    }

    // 2. Гость: POST submissions → 403
    @Test
    fun `guest cannot submit practice recording`() {
        val topic = seedPublishedContent()
        val file = MockMultipartFile("file", "rec.m4a", "audio/m4a", ByteArray(1024))

        mockMvc.multipart(HttpMethod.POST, "/speaking/submissions") {
            this.file(file)
            param("topicId", topic.id.toString())
            param("durationSec", "30")
        }.andExpect { status { isForbidden() } }
    }

    // 3. USER: multipart POST → 201, status NEW, audioUrl содержит public-url
    @Test
    fun `user can submit practice recording`() {
        val topic = seedPublishedContent()
        val file = MockMultipartFile("file", "rec.m4a", "audio/m4a", ByteArray(1024))

        mockMvc.multipart(HttpMethod.POST, "/speaking/submissions") {
            this.file(file)
            param("topicId", topic.id.toString())
            param("durationSec", "30")
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isCreated() }
            jsonPath("$.status") { value("NEW") }
            jsonPath("$.audioUrl") { value(publicAudioUrl) }
            jsonPath("$.topicTitle") { value("My Morning Routine") }
        }
    }

    // 4. USER: GET my → содержит созданную запись
    @Test
    fun `user sees own submission in my list`() {
        val topic = seedPublishedContent()
        submitAs(userToken, topic.id!!)

        mockMvc.get("/speaking/submissions/my") {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].topicId") { value(topic.id.toString()) }
            jsonPath("$[0].status") { value("NEW") }
            jsonPath("$[0].grade") { doesNotExist() }
        }
    }

    // 5. USER: GET admin inbox → 403
    @Test
    fun `regular user cannot access grading inbox`() {
        mockMvc.get("/admin/speaking/submissions") {
            header("Authorization", "Bearer $userToken")
        }.andExpect { status { isForbidden() } }
    }

    // 6. ADMIN: grade → 201; USER my → REVIEWED + total == среднее
    @Test
    fun `admin grades submission, user sees REVIEWED with computed total`() {
        val topic = seedPublishedContent()
        val submissionId = submitAs(userToken, topic.id!!)

        mockMvc.post("/admin/speaking/submissions/$submissionId/grade") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                GradeSubmissionRequest(grammar = 7, vocabulary = 8, pronunciation = 6, fluency = 7, comment = "Good ideas")
            )
        }.andExpect {
            status { isCreated() }
            jsonPath("$.total") { value(7.0) }
            jsonPath("$.reviewerName") { value("Teacher Anna") }
        }

        mockMvc.get("/speaking/submissions/my") {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].status") { value("REVIEWED") }
            jsonPath("$[0].grade.total") { value(7.0) }
            jsonPath("$[0].grade.comment") { value("Good ideas") }
        }
    }

    // 7. ADMIN: повторный POST grade → 400; PUT grade → 200, updatedAt присутствует
    @Test
    fun `repeat grade POST rejected, PUT edits grade`() {
        val topic = seedPublishedContent()
        val submissionId = submitAs(userToken, topic.id!!)
        gradeAs(adminToken, submissionId, GradeSubmissionRequest(5, 5, 5, 5, null))

        mockMvc.post("/admin/speaking/submissions/$submissionId/grade") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(GradeSubmissionRequest(7, 7, 7, 7, "again"))
        }.andExpect { status { isBadRequest() } }

        mockMvc.put("/admin/speaking/submissions/$submissionId/grade") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(GradeSubmissionRequest(9, 9, 9, 9, "Much better"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.total") { value(9.0) }
            jsonPath("$.comment") { value("Much better") }
            jsonPath("$.updatedAt") { exists() }
        }
    }

    // 8. ADMIN: CRUD topics — create → publish → public видит; DELETE → public 404, admin isDeleted
    @Test
    fun `admin topic lifecycle - create, publish, soft delete`() {
        // create library (admin) + контрактный тест isPublished (сценарий 9)
        val libraryResponse = mockMvc.post("/admin/speaking/libraries") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateLibraryRequest(title = "Admin Theme"))
        }.andExpect {
            status { isCreated() }
            jsonPath("$.isPublished") { value(false) } // грабля №18: поле с is-префиксом
            jsonPath("$.published") { doesNotExist() }
        }.andReturn().response.contentAsString
        val libraryId = objectMapper.readTree(libraryResponse).get("id").asText()

        // create topic
        val topicResponse = mockMvc.post("/admin/speaking/topics") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateTopicRequest(libraryId = libraryId, title = "Admin Topic"))
        }.andExpect { status { isCreated() } }
            .andReturn().response.contentAsString
        val topicId = objectMapper.readTree(topicResponse).get("id").asText()

        // add question
        mockMvc.post("/admin/speaking/topics/$topicId/questions") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateSpeakingQuestionRequest(text = "Q1?"))
        }.andExpect { status { isCreated() } }

        // publish library + topic
        mockMvc.put("/admin/speaking/libraries/$libraryId") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = """{"isPublished": true}"""
        }.andExpect { status { isOk() } }
        mockMvc.put("/admin/speaking/topics/$topicId") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateTopicRequest(isPublished = true))
        }.andExpect { status { isOk() } }

        // public видит топик
        mockMvc.get("/public/speaking/topics/$topicId")
            .andExpect {
                status { isOk() }
                jsonPath("$.video") { doesNotExist() } // видео не загружено — null допустим
                jsonPath("$.questions[0].text") { value("Q1?") }
            }

        // soft delete → public 404
        mockMvc.delete("/admin/speaking/topics/$topicId") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/public/speaking/topics/$topicId")
            .andExpect { status { isNotFound() } }

        // admin видит архивный топик с isDeleted=true
        mockMvc.get("/admin/speaking/topics") {
            header("Authorization", "Bearer $adminToken")
            param("libraryId", libraryId)
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].isDeleted") { value(true) }
        }

        // идемпотентность повторного удаления
        mockMvc.delete("/admin/speaking/topics/$topicId") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect { status { isNoContent() } }
    }

    // Inbox: фильтр по статусу + пагинация Page
    @Test
    fun `admin inbox filters by status with pagination`() {
        val topic = seedPublishedContent()
        submitAs(userToken, topic.id!!)

        mockMvc.get("/admin/speaking/submissions") {
            header("Authorization", "Bearer $adminToken")
            param("status", "NEW")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].userEmail") { value("speaking-user@test.com") }
            jsonPath("$.content[0].userDisplayName") { value("Student Ivan") }
            jsonPath("$.totalElements") { value(1) }
        }

        mockMvc.get("/admin/speaking/submissions") {
            header("Authorization", "Bearer $adminToken")
            param("status", "REVIEWED")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(0) }
        }
    }

    // ============== helpers ==============

    private fun submitAs(token: String, topicId: UUID): String {
        val file = MockMultipartFile("file", "rec.m4a", "audio/m4a", ByteArray(1024))
        val response = mockMvc.multipart(HttpMethod.POST, "/speaking/submissions") {
            this.file(file)
            param("topicId", topicId.toString())
            param("durationSec", "30")
            header("Authorization", "Bearer $token")
        }.andExpect { status { isCreated() } }
            .andReturn().response.contentAsString
        return objectMapper.readTree(response).get("id").asText()
    }

    private fun gradeAs(token: String, submissionId: String, request: GradeSubmissionRequest) {
        val result = mockMvc.post("/admin/speaking/submissions/$submissionId/grade") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andReturn().response
        check(result.status == 201) { "gradeAs failed: ${result.status} ${result.contentAsString}" }
    }
}
