package com.sotospeak.controller

import com.sotospeak.dto.GradeSubmissionRequest
import com.sotospeak.entity.User
import com.sotospeak.entity.speaking.Grade
import com.sotospeak.entity.speaking.GradeAudit
import com.sotospeak.entity.speaking.Library
import com.sotospeak.entity.speaking.PracticeSubmission
import com.sotospeak.entity.speaking.SubmissionStatus
import com.sotospeak.entity.speaking.Topic
import com.sotospeak.repository.UserRepository
import com.sotospeak.repository.speaking.GradeAuditRepository
import com.sotospeak.repository.speaking.GradeRepository
import com.sotospeak.repository.speaking.LibraryRepository
import com.sotospeak.repository.speaking.PracticeSubmissionRepository
import com.sotospeak.repository.speaking.TopicRepository
import com.sotospeak.security.JwtService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Grading-аналитика и аудит-лог оценок (bd h3l.4, h3l.10).
 * Сеیدинг через репозитории: HTTP-upload'ы в общем сьюте упираются в
 * upload-лимитер (10/мин/IP) — грабля, пойманная на 429 в 4-м тесте подряд.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SpeakingGradingAnalyticsIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var libraryRepository: LibraryRepository

    @Autowired
    private lateinit var topicRepository: TopicRepository

    @Autowired
    private lateinit var practiceSubmissionRepository: PracticeSubmissionRepository

    @Autowired
    private lateinit var gradeRepository: GradeRepository

    @Autowired
    private lateinit var gradeAuditRepository: GradeAuditRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var objectMapper: com.fasterxml.jackson.databind.ObjectMapper

    private lateinit var adminToken: String
    private lateinit var userToken: String
    private val adminId = "33333333-3333-3333-3333-333333333333"
    private val userId = "44444444-4444-4444-4444-444444444444"

    @BeforeEach
    fun setup() {
        if (!userRepository.existsById(UUID.fromString(adminId))) {
            userRepository.save(
                User(
                    id = UUID.fromString(adminId),
                    email = "speaking-admin@test.com",
                    passwordHash = "password",
                    displayName = "Teacher Anna",
                    role = "ADMIN"
                )
            )
        }
        if (!userRepository.existsById(UUID.fromString(userId))) {
            userRepository.save(
                User(
                    id = UUID.fromString(userId),
                    email = "speaking-user@test.com",
                    passwordHash = "password",
                    displayName = "Student Ivan"
                )
            )
        }
        adminToken = jwtService.generateToken(adminId, "speaking-admin@test.com", "ADMIN")
        userToken = jwtService.generateToken(userId, "speaking-user@test.com", "USER")
    }

    private fun seedPublishedContent(): Topic {
        val library = libraryRepository.save(Library(title = "Everyday Life", isPublished = true))
        val topic = Topic(title = "My Morning Routine", isPublished = true)
        library.addTopic(topic)
        return topicRepository.save(topic)
    }

    @Suppress("LongParameterList")
    private fun seedGradedSubmission(
        topic: Topic,
        grammar: Int,
        vocabulary: Int,
        pronunciation: Int,
        fluency: Int,
        comment: String? = null
    ): PracticeSubmission {
        val user = userRepository.findByEmail("speaking-user@test.com")!!
        val submission = practiceSubmissionRepository.save(
            PracticeSubmission(
                user = user,
                topic = topic,
                audioUrl = "http://localhost:9000/sotospeak-test/speaking/submissions/u_test/abc.m4a",
                durationSec = 30,
                status = SubmissionStatus.REVIEWED
            )
        )
        val admin = userRepository.findByEmail("speaking-admin@test.com")!!
        val grade = gradeRepository.save(
            Grade(
                submission = submission,
                grammar = grammar,
                vocabulary = vocabulary,
                pronunciation = pronunciation,
                fluency = fluency,
                comment = comment,
                reviewer = admin
            )
        )
        // обе стороны OneToOne выставляем явно: inverse-side сам не подтянется в том же PC
        submission.grade = grade
        return practiceSubmissionRepository.saveAndFlush(submission)
    }

    // bd h3l.4: grading-аналитика — средние по рубрике, NEW-очередь, распределение по топикам
    @Test
    @Suppress("MagicNumber")
    fun gradingAnalyticsReflectsGradedSubmissions() {
        val topic = seedPublishedContent()
        seedGradedSubmission(topic, grammar = 7, vocabulary = 8, pronunciation = 6, fluency = 7)

        mockMvc.get("/admin/speaking/grading/analytics") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.averages.grammar") { value(7.0) }
            jsonPath("$.averages.vocabulary") { value(8.0) }
            jsonPath("$.averages.pronunciation") { value(6.0) }
            jsonPath("$.averages.fluency") { value(7.0) }
            jsonPath("$.averages.total") { value(7.0) }
            jsonPath("$.newCount") { value(0) }
            jsonPath("$.reviewedCount") { value(1) }
            jsonPath("$.avgReviewTimeMinutes") { value(0.0) }
            jsonPath("$.byTopic[0].topicId") { value(topic.id.toString()) }
            jsonPath("$.byTopic[0].gradeCount") { value(1) }
            jsonPath("$.byTopic[0].avgTotal") { value(7.0) }
        }

        // USER → 403 (admin-поверхность)
        mockMvc.get("/admin/speaking/grading/analytics") {
            header("Authorization", "Bearer $userToken")
        }.andExpect { status { isForbidden() } }
    }

    // bd h3l.4: CSV-экспорт оценённых записей (включая экранирование кавычек в комментарии)
    @Test
    @Suppress("LongMethod", "MagicNumber")
    fun gradingCsvExportContainsHeaderAndGradedRow() {
        val topic = seedPublishedContent()
        val submission = seedGradedSubmission(
            topic, grammar = 8, vocabulary = 7, pronunciation = 7, fluency = 8,
            comment = """Ok "pace" man, keep going"""
        )

        val result = mockMvc.get("/admin/speaking/grading/export.csv") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            content { contentTypeCompatibleWith("text/csv") }
        }.andReturn().response

        val body = result.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)
        check(body.startsWith("\uFEFF")) { "CSV must start with BOM" }
        val csvHeader = "submission_id,topic,user_email,submitted_at,reviewed_at," +
            "grammar,vocabulary,pronunciation,fluency,total,comment"
        check(body.contains(csvHeader)) { "header missing: $body" }
        check(body.contains(submission.id.toString())) { "submission row missing" }
        check(body.contains("My Morning Routine")) { "topic title missing" }
        // кавычки в комментарии экранируются удвоением, поле берётся в кавычки из-за запятой
        val escapedNeedle = "\"Ok \"\"pace\"\" man, keep going\""
        check(body.contains(escapedNeedle)) { "comment escaping: $body" }
    }

    // bd h3l.4: CSV-экспорт пуст при отсутствии оценок (только заголовок)
    @Test
    fun gradingCsvExportEmptyWithoutGrades() {
        mockMvc.get("/admin/speaking/grading/export.csv") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
        }.andReturn().response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8).let { body ->
            check(body.count { it == '\n' } == 1) { "expected header-only CSV, got: $body" }
        }
    }

    // bd h3l.10: аудит-лог оценок — CREATE после первичной оценки, EDIT после правки
    @Test
    @Suppress("MagicNumber")
    fun gradeHistoryRecordsCreateAndEdit() {
        val topic = seedPublishedContent()
        val submission = seedGradedSubmission(
            topic, grammar = 7, vocabulary = 7, pronunciation = 7, fluency = 7, comment = "first"
        )
        gradeAuditRepository.save(
            GradeAudit(
                submission = submission,
                reviewer = submission.grade?.reviewer,
                action = GradeAudit.ACTION_CREATE,
                grammar = 7, vocabulary = 7, pronunciation = 7, fluency = 7,
                comment = "first"
            )
        )
        val submissionId = submission.id.toString()

        mockMvc.put("/admin/speaking/submissions/$submissionId/grade") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(GradeSubmissionRequest(9, 8, 9, 8, "revised"))
        }.andExpect { status { isOk() } }

        mockMvc.get("/admin/speaking/submissions/$submissionId/grade/history") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].action") { value("EDIT") }
            jsonPath("$[0].grammar") { value(9) }
            jsonPath("$[0].comment") { value("revised") }
            jsonPath("$[0].total") { value(8.5) }
            jsonPath("$[0].reviewerName") { value("Teacher Anna") }
            jsonPath("$[1].action") { value("CREATE") }
            jsonPath("$[1].grammar") { value(7) }
            jsonPath("$[1].comment") { value("first") }
        }

        // USER → 403
        mockMvc.get("/admin/speaking/submissions/$submissionId/grade/history") {
            header("Authorization", "Bearer $userToken")
        }.andExpect { status { isForbidden() } }
    }
}
