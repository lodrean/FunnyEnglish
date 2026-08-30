package com.sotospeak.controller

import com.sotospeak.entity.User
import com.sotospeak.entity.speaking.Grade
import com.sotospeak.entity.speaking.Library
import com.sotospeak.entity.speaking.PracticeSubmission
import com.sotospeak.entity.speaking.Topic
import com.sotospeak.repository.UserRepository
import com.sotospeak.repository.speaking.GradeRepository
import com.sotospeak.repository.speaking.LibraryRepository
import com.sotospeak.repository.speaking.PracticeSubmissionRepository
import com.sotospeak.repository.speaking.TopicRepository
import com.sotospeak.security.JwtService
import com.sotospeak.support.PostgresContainerTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Контракты AdminController/аналитики на реальном PostgreSQL (bd FunnyEnglish-wy7.4).
 *
 * H2-грабля: native-запросы аналитики (CAST(created_at AS DATE), проекции
 * java.sql.Timestamp, JPQL nullable-параметры) зелёные на H2 и падают на
 * живом Postgres (грабли №21/№81) — поэтому весь analytics-набор гоняется
 * через Testcontainers-Postgres с Flyway-схемой (как staging/prod).
 *
 * Токены генерируются через JwtService напрямую (без /auth/login),
 * чтобы не расходовать rate-limit bucket логина (5/мин/IP, грабля №23).
 * Маппинг БЕЗ /api (context-path в MockMvc не применяется, грабля №65).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminAnalyticsPostgresIT : PostgresContainerTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var libraryRepository: LibraryRepository

    @Autowired
    private lateinit var topicRepository: TopicRepository

    @Autowired
    private lateinit var practiceSubmissionRepository: PracticeSubmissionRepository

    @Autowired
    private lateinit var gradeRepository: GradeRepository

    private lateinit var adminToken: String
    private val adminId = "66666666-6666-6666-6666-666666666666"

    @BeforeEach
    fun setup() {
        if (!userRepository.existsById(UUID.fromString(adminId))) {
            userRepository.save(
                User(
                    id = UUID.fromString(adminId),
                    email = "analytics-admin@test.com",
                    passwordHash = "password",
                    displayName = "Analytics Admin",
                    role = "ADMIN"
                )
            )
        }
        adminToken = jwtService.generateToken(adminId, "analytics-admin@test.com", "ADMIN")
    }

    @Test
    fun `admin analytics returns 200 with counters`() {
        mockMvc.get("/admin/analytics") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalUsers").exists()
            jsonPath("$.totalTests").exists()
            jsonPath("$.totalCompletions").exists()
            jsonPath("$.topCategories").isArray()
        }
    }

    @Test
    fun `daily activity returns exactly requested number of days`() {
        // Упражняет native-запросы countNewUsersByDay/countCompletionsByDay/
        // countAchievementsEarnedByDay (CAST AS DATE) на реальном Postgres.
        mockMvc.get("/admin/analytics/daily-activity") {
            header("Authorization", "Bearer $adminToken")
            param("days", "7")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()").value(7)
            jsonPath("$[0].date").exists()
            jsonPath("$[0].newUsers").exists()
        }
    }

    @Test
    fun `daily activity alias endpoint also works`() {
        mockMvc.get("/admin/analytics/activity") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()").value(7)
        }
    }

    @Test
    fun `level distribution returns 200`() {
        mockMvc.get("/admin/analytics/levels") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$").isArray()
        }
    }

    @Test
    fun `popular tests returns 200`() {
        mockMvc.get("/admin/analytics/popular-tests") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$").isArray()
        }
    }

    @Test
    fun `recent activity returns 200`() {
        // Проекция native-запроса с java.sql.Timestamp (грабля №21) — на Postgres.
        mockMvc.get("/admin/analytics/recent-activity") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$").isArray()
        }
    }

    @Test
    fun `guest analytics returns 200 with conversion rate`() {
        mockMvc.get("/admin/analytics/guests") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalGuests").exists()
            jsonPath("$.activeGuests7d").exists()
            jsonPath("$.conversionRate").exists()
        }
    }

    @Test
    fun `prd metrics returns 200 with real aggregates`() {
        // Сид: ученик с отправкой, оцененной учителем (обе метки — "сейчас" → в пределах 48ч).
        val student = userRepository.save(
            User(
                email = "prd-student@test.com",
                passwordHash = "password",
                displayName = "PRD Student"
            )
        )
        val library = libraryRepository.save(Library(title = "PRD Lib", isPublished = true))
        val topic = Topic(title = "PRD Topic", isPublished = true)
        library.addTopic(topic)
        val savedTopic = topicRepository.save(topic)
        val submission = practiceSubmissionRepository.save(
            PracticeSubmission(
                user = student,
                topic = savedTopic,
                audioUrl = "http://localhost:9000/sotospeak-test/speaking/submissions/prd/a.m4a",
                durationSec = 25
            )
        )
        gradeRepository.save(
            Grade(
                submission = submission,
                grammar = 8, vocabulary = 8, pronunciation = 8, fluency = 8,
                reviewer = userRepository.getReferenceById(UUID.fromString(adminId))
            )
        )

        mockMvc.get("/admin/analytics/prd-metrics") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.practiceSubmissionsLast7d").value(1)
            jsonPath("$.activeStudentsLast7d").value(1)
            jsonPath("$.practicePerStudentPerWeek").value(1.0)
            jsonPath("$.reviewedTotal").value(1)
            jsonPath("$.reviewedWithin48h").value(1)
            jsonPath("$.reviewedWithin48hShare").value(1.0)
            jsonPath("$.totalGuests").exists()
            jsonPath("$.guestConversionRate").exists()
        }
    }

    @Test
    fun `admin tests list returns 200`() {
        // AdminController GET /admin/tests (TestService JSONB-workaround путь, грабля №3).
        mockMvc.get("/admin/tests") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$").isArray()
        }
    }

    @Test
    fun `admin settings returns 200`() {
        mockMvc.get("/admin/settings") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.s3Bucket").exists()
        }
    }
}
