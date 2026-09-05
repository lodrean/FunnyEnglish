package com.sotospeak.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sotospeak.config.SPEAKING_PUBLIC_LIBRARIES
import com.sotospeak.config.SPEAKING_PUBLIC_TOPICS
import com.sotospeak.config.SPEAKING_PUBLIC_TOPIC_DETAILS
import com.sotospeak.dto.CreateLibraryRequest
import com.sotospeak.dto.CreateSpeakingQuestionRequest
import com.sotospeak.dto.CreateTopicRequest
import com.sotospeak.dto.GradeSubmissionRequest
import com.sotospeak.dto.UpdateTopicRequest
import com.sotospeak.entity.User
import com.sotospeak.entity.speaking.Library
import com.sotospeak.entity.speaking.SpeakingQuestion
import com.sotospeak.entity.speaking.Topic
import com.sotospeak.entity.speaking.Video
import com.sotospeak.repository.UserRepository
import com.sotospeak.repository.speaking.LibraryRepository
import com.sotospeak.repository.speaking.TopicRepository
import com.sotospeak.security.JwtService
import com.sotospeak.service.StorageService
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
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
import org.springframework.test.web.servlet.patch
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
    // Валидный m4a-контент: ftyp-подпись на смещении 4 (bd FunnyEnglish-nj2.8, magic-bytes)
    @Suppress("MagicNumber")
    private fun validM4a(): ByteArray {
        val b = ByteArray(1024)
        b[4] = 'f'.code.toByte(); b[5] = 't'.code.toByte(); b[6] = 'y'.code.toByte(); b[7] = 'p'.code.toByte()
        b[8] = 'M'.code.toByte(); b[9] = '4'.code.toByte(); b[10] = 'A'.code.toByte(); b[11] = ' '.code.toByte()
        return b
    }



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

    @Autowired
    private lateinit var cacheManager: CacheManager

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
                "http://localhost:9000/sotospeak-test/speaking/submissions/u_test/abc.m4a"
            every { deleteFile(any()) } returns Unit
        }
    }

    private lateinit var adminToken: String
    private lateinit var userToken: String
    private val adminId = "33333333-3333-3333-3333-333333333333"
    private val userId = "44444444-4444-4444-4444-444444444444"

    private val publicAudioUrl =
        "http://localhost:9000/sotospeak-test/speaking/submissions/u_test/abc.m4a"

    @BeforeEach
    fun setup() {
        // Caffeine-кэш публичного контента (bd wy7.7) живёт вне транзакции и НЕ
        // откатывается вместе с тестовой tx — чистим, иначе cross-test staleness
        // (сиды через репозиторий кэш не инвалидируют).
        listOf(SPEAKING_PUBLIC_LIBRARIES, SPEAKING_PUBLIC_TOPICS, SPEAKING_PUBLIC_TOPIC_DETAILS)
            .forEach { cacheManager.getCache(it)?.clear() }
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
            videoUrl = "http://localhost:9000/sotospeak-test/speaking/videos/v.mp4",
            subtitleUrl = "http://localhost:9000/sotospeak-test/speaking/subtitles/s.vtt",
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

    // 2. Гость: POST submissions → 401 (bd FunnyEnglish-nj2.7, раньше 403)
    @Test
    fun `guest cannot submit practice recording`() {
        val topic = seedPublishedContent()
        val file = MockMultipartFile("file", "rec.m4a", "audio/m4a", validM4a())

        mockMvc.multipart(HttpMethod.POST, "/speaking/submissions") {
            this.file(file)
            param("topicId", topic.id.toString())
            param("durationSec", "30")
        }.andExpect { status { isUnauthorized() } }
    }

    // 3. USER: multipart POST → 201, status NEW, audioUrl содержит public-url
    @Test
    fun `user can submit practice recording`() {
        val topic = seedPublishedContent()
        val file = MockMultipartFile("file", "rec.m4a", "audio/m4a", validM4a())

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

    // 4b. USER: повторный POST по тому же топику → 409 DUPLICATE_SUBMISSION
    @Test
    fun `user cannot submit practice twice for same topic`() {
        val topic = seedPublishedContent()
        submitAs(userToken, topic.id!!)

        val file = MockMultipartFile("file", "rec.m4a", "audio/m4a", validM4a())
        mockMvc.multipart(HttpMethod.POST, "/speaking/submissions") {
            this.file(file)
            param("topicId", topic.id.toString())
            param("durationSec", "30")
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("DUPLICATE_SUBMISSION") }
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

    // 9. ADMIN: GET topics/{id} — детали черновика без N+1 (deep-link); unknown id → 404
    @Test
    fun `admin can get topic by id including drafts`() {
        val draftLibrary = libraryRepository.save(Library(title = "Draft Lib", isPublished = false))
        val draftTopic = Topic(title = "Draft Topic", isPublished = false)
        draftLibrary.addTopic(draftTopic)
        draftTopic.addQuestion(SpeakingQuestion(text = "Q draft?", displayOrder = 0))
        val saved = topicRepository.save(draftTopic)

        mockMvc.get("/admin/speaking/topics/${saved.id}") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Draft Topic") }
            jsonPath("$.isPublished") { value(false) }
            jsonPath("$.isDeleted") { value(false) }
            jsonPath("$.questions[0].text") { value("Q draft?") }
        }

        mockMvc.get("/admin/speaking/topics/${UUID.randomUUID()}") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect { status { isNotFound() } }
    }

    // 10. ADMIN: PATCH publish — точечный publish/unpublish library и topic (Part 3 §3.3)
    @Test
    fun `admin can publish and unpublish via PATCH publish`() {
        val library = libraryRepository.save(Library(title = "Patch Lib", isPublished = false))
        val topic = Topic(title = "Patch Topic", isPublished = false)
        library.addTopic(topic)
        val savedTopic = topicRepository.save(topic)

        mockMvc.patch("/admin/speaking/libraries/${library.id}/publish") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = """{"isPublished": true}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.isPublished") { value(true) }
        }

        mockMvc.patch("/admin/speaking/topics/${savedTopic.id}/publish") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = """{"isPublished": true}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.isPublished") { value(true) }
            jsonPath("$.published") { doesNotExist() } // грабля №18
        }

        // unpublish топика обратно
        mockMvc.patch("/admin/speaking/topics/${savedTopic.id}/publish") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = """{"isPublished": false}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.isPublished") { value(false) }
        }
    }

    // 11. ADMIN: batch-reorder вопросов — displayOrder = индекс; чужой/неполный набор id → 400
    @Test
    fun `admin can batch reorder topic questions`() {
        val library = libraryRepository.save(Library(title = "Reorder Lib", isPublished = false))
        val topic = Topic(title = "Reorder Topic", isPublished = false)
        library.addTopic(topic)
        topic.addQuestion(SpeakingQuestion(text = "Q1", displayOrder = 0))
        topic.addQuestion(SpeakingQuestion(text = "Q2", displayOrder = 1))
        topic.addQuestion(SpeakingQuestion(text = "Q3", displayOrder = 2))
        val saved = topicRepository.save(topic)
        val ids = saved.questions.sortedBy { it.displayOrder }.map { it.id.toString() }
        val reversed = ids.reversed()

        mockMvc.post("/admin/speaking/topics/${saved.id}/questions/reorder") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("questionIds" to reversed))
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/admin/speaking/topics/${saved.id}") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.questions[0].id") { value(reversed[0]) }
            jsonPath("$.questions[0].displayOrder") { value(0) }
            jsonPath("$.questions[2].id") { value(reversed[2]) }
            jsonPath("$.questions[2].displayOrder") { value(2) }
        }

        // неполный набор id → 400
        mockMvc.post("/admin/speaking/topics/${saved.id}/questions/reorder") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("questionIds" to ids.drop(1)))
        }.andExpect { status { isBadRequest() } }
    }

    // 12. ADMIN: GET submissions/{id} (deep-link) + GET submissions/count?status= (badge)
    @Test
    fun `admin can get submission by id and count by status`() {
        val topic = seedPublishedContent()
        val submissionId = submitAs(userToken, topic.id!!)

        mockMvc.get("/admin/speaking/submissions/$submissionId") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(submissionId) }
            jsonPath("$.userEmail") { value("speaking-user@test.com") }
            jsonPath("$.topicTitle") { value("My Morning Routine") }
            jsonPath("$.status") { value("NEW") }
            jsonPath("$.audioUrl") { value(publicAudioUrl) }
        }

        mockMvc.get("/admin/speaking/submissions/count") {
            header("Authorization", "Bearer $adminToken")
            param("status", "NEW")
        }.andExpect {
            status { isOk() }
            jsonPath("$.count") { value(1) }
        }

        gradeAs(adminToken, submissionId, GradeSubmissionRequest(8, 8, 8, 8, null))

        mockMvc.get("/admin/speaking/submissions/count") {
            header("Authorization", "Bearer $adminToken")
            param("status", "NEW")
        }.andExpect { jsonPath("$.count") { value(0) } }
        mockMvc.get("/admin/speaking/submissions/count") {
            header("Authorization", "Bearer $adminToken")
            param("status", "REVIEWED")
        }.andExpect { jsonPath("$.count") { value(1) } }

        mockMvc.get("/admin/speaking/submissions/${UUID.randomUUID()}") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect { status { isNotFound() } }
    }

    // 13. Public HTTP-кэш (bd wy7.7, §4.3.3): ETag + Cache-Control; If-None-Match → 304
    @Test
    fun `public endpoints return cache headers and honor If-None-Match`() {
        val topic = seedPublishedContent()

        val response = mockMvc.get("/public/speaking/topics/${topic.id}")
            .andExpect {
                status { isOk() }
                header { exists("ETag") }
                header { string("Cache-Control", containsString("max-age=60")) }
                header { string("Cache-Control", containsString("public")) }
            }.andReturn().response
        val etag = response.getHeader("ETag")!!

        mockMvc.get("/public/speaking/topics/${topic.id}") {
            header("If-None-Match", etag)
        }.andExpect { status { isNotModified() } }

        // изменённый контент (другой сид → другой id) даёт другой ETag
        val other = seedPublishedContent()
        mockMvc.get("/public/speaking/topics/${other.id}")
            .andExpect { header { exists("ETag") } }
    }

    // 14. Caffeine-кэш детали топика: повторный GET отдаётся из кэша (bd wy7.7)
    @Test
    fun `public topic detail is served from caffeine cache`() {
        val topic = seedPublishedContent()

        mockMvc.get("/public/speaking/topics/${topic.id}")
            .andExpect { jsonPath("$.title") { value("My Morning Routine") } }

        // прямая мутация через репозиторий (минуя сервис) кэш НЕ инвалидирует
        val managed = topicRepository.findById(topic.id!!).get()
        managed.title = "Changed Directly"
        topicRepository.save(managed)

        mockMvc.get("/public/speaking/topics/${topic.id}")
            .andExpect { jsonPath("$.title") { value("My Morning Routine") } }
    }

    // 15. Publish/unpublish через admin API инвалидирует публичный кэш (bd wy7.7)
    @Test
    fun `publish and unpublish invalidate public content cache`() {
        val libraryResponse = mockMvc.post("/admin/speaking/libraries") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateLibraryRequest(title = "Cache Lib"))
        }.andExpect { status { isCreated() } }
            .andReturn().response.contentAsString
        val libraryId = objectMapper.readTree(libraryResponse).get("id").asText()

        val topicResponse = mockMvc.post("/admin/speaking/topics") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateTopicRequest(libraryId = libraryId, title = "Cache Topic"))
        }.andExpect { status { isCreated() } }
            .andReturn().response.contentAsString
        val topicId = objectMapper.readTree(topicResponse).get("id").asText()

        fun publishTopic(value: Boolean) {
            mockMvc.patch("/admin/speaking/topics/$topicId/publish") {
                header("Authorization", "Bearer $adminToken")
                contentType = MediaType.APPLICATION_JSON
                content = """{"isPublished": $value}"""
            }.andExpect { status { isOk() } }
        }

        // публичный detail требует опубликованные И библиотеку, И топик
        mockMvc.patch("/admin/speaking/libraries/$libraryId/publish") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = """{"isPublished": true}"""
        }.andExpect { status { isOk() } }

        // publish → public видит → detail закэширован
        publishTopic(true)
        mockMvc.get("/public/speaking/topics/$topicId")
            .andExpect { status { isOk() } }

        // unpublish → кэш инвалидирован → 404 (без инвалидации вернулся бы закэшированный 200)
        publishTopic(false)
        mockMvc.get("/public/speaking/topics/$topicId")
            .andExpect { status { isNotFound() } }

        // повторный publish → снова виден
        publishTopic(true)
        mockMvc.get("/public/speaking/topics/$topicId")
            .andExpect { status { isOk() } }
    }

    // ============== helpers ==============

    private fun submitAs(token: String, topicId: UUID): String {
        val file = MockMultipartFile("file", "rec.m4a", "audio/m4a", validM4a())
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
