package com.funnyenglish.service

import com.funnyenglish.dto.GuestTestProgressDto
import com.funnyenglish.dto.MergeGuestProgressRequest
import com.funnyenglish.entity.Category
import com.funnyenglish.entity.Progress
import com.funnyenglish.entity.User
import com.funnyenglish.entity.Test as TestEntity
import com.funnyenglish.repository.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class UserServiceMergeGuestProgressTest {

    private lateinit var userRepository: UserRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var achievementService: AchievementService
    private lateinit var testRepository: TestRepository
    private lateinit var guestEventRepository: GuestEventRepository
    private lateinit var userService: UserService

    private val userId = UUID.randomUUID()
    private val testId = UUID.randomUUID()
    private val categoryId = UUID.randomUUID()

    private val user = User(
        id = userId,
        email = "test@example.com",
        displayName = "Test User",
        totalPoints = 50,
        level = 1
    )

    private val category = Category(
        id = categoryId,
        name = "Test Category",
        displayOrder = 1
    )

    private val testEntity = TestEntity(
        id = testId,
        category = category,
        title = "Test Quiz",
        pointsReward = 10
    )

    @BeforeEach
    fun setup() {
        userRepository = mockk(relaxed = true)
        progressRepository = mockk(relaxed = true)
        achievementRepository = mockk(relaxed = true)
        achievementService = mockk(relaxed = true)
        testRepository = mockk(relaxed = true)
        guestEventRepository = mockk(relaxed = true)

        userService = UserService(
            userRepository = userRepository,
            progressRepository = progressRepository,
            achievementRepository = achievementRepository,
            achievementService = achievementService,
            testRepository = testRepository,
            guestEventRepository = guestEventRepository
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { testRepository.findById(testId) } returns Optional.of(testEntity)
        every { progressRepository.save(any()) } answers { firstArg() }
        every { userRepository.save(any()) } answers { firstArg() }
    }

    @Test
    fun `mergeGuestProgress should create new progress when no existing progress`() {
        // Given
        every { progressRepository.findByUserIdAndTestId(userId, testId) } returns null
        every { achievementService.checkAndAwardAchievements(any(), any(), any()) } returns emptyList()

        val request = MergeGuestProgressRequest(
            testProgress = listOf(
                GuestTestProgressDto(
                    testId = testId.toString(),
                    score = 8,
                    maxScore = 10,
                    stars = 2,
                    timeSpentSeconds = 120
                )
            )
        )

        // When
        val result = userService.mergeGuestProgress(userId.toString(), request)

        // Then
        assertEquals(1, result.mergedTests)
        assertEquals(20, result.totalXpAdded) // 10 base + 2*5 stars = 20
        assertNull(result.levelUp)
        verify { progressRepository.save(any()) }
    }

    @Test
    fun `mergeGuestProgress should update progress when guest score is better`() {
        // Given
        val existingProgress = Progress(
            user = user,
            test = testEntity,
            score = 5,
            maxScore = 10,
            stars = 1,
            bestScore = 5
        )
        every { progressRepository.findByUserIdAndTestId(userId, testId) } returns existingProgress
        every { achievementService.checkAndAwardAchievements(any(), any(), any()) } returns emptyList()

        // Guest score 9/10 = 90% → 2 stars (server recalculates, ignores DTO stars)
        val request = MergeGuestProgressRequest(
            testProgress = listOf(
                GuestTestProgressDto(
                    testId = testId.toString(),
                    score = 9,
                    maxScore = 10,
                    stars = 3, // ignored by server
                    timeSpentSeconds = 120
                )
            )
        )

        // When
        val result = userService.mergeGuestProgress(userId.toString(), request)

        // Then
        assertEquals(1, result.mergedTests)
        // Old XP: 10 + 1*5 = 15, New XP: 10 + 2*5 = 20, diff = 5
        assertEquals(5, result.totalXpAdded)
    }

    @Test
    fun `mergeGuestProgress should skip when guest score is not better`() {
        // Given
        val existingProgress = Progress(
            user = user,
            test = testEntity,
            score = 8,
            maxScore = 10,
            stars = 2,
            bestScore = 8
        )
        every { progressRepository.findByUserIdAndTestId(userId, testId) } returns existingProgress

        val request = MergeGuestProgressRequest(
            testProgress = listOf(
                GuestTestProgressDto(
                    testId = testId.toString(),
                    score = 6,
                    maxScore = 10,
                    stars = 1,
                    timeSpentSeconds = 120
                )
            )
        )

        // When
        val result = userService.mergeGuestProgress(userId.toString(), request)

        // Then
        assertEquals(0, result.mergedTests)
        assertEquals(0, result.totalXpAdded)
        verify(exactly = 0) { progressRepository.save(any()) }
    }

    @Test
    fun `mergeGuestProgress should skip invalid scores`() {
        // Given
        val request = MergeGuestProgressRequest(
            testProgress = listOf(
                GuestTestProgressDto(
                    testId = testId.toString(),
                    score = 15, // > maxScore
                    maxScore = 10,
                    stars = 3,
                    timeSpentSeconds = 120
                ),
                GuestTestProgressDto(
                    testId = testId.toString(),
                    score = 5,
                    maxScore = 0, // invalid maxScore
                    stars = 1,
                    timeSpentSeconds = 120
                )
            )
        )

        // When
        val result = userService.mergeGuestProgress(userId.toString(), request)

        // Then
        assertEquals(0, result.mergedTests)
        verify(exactly = 0) { progressRepository.save(any()) }
    }

    @Test
    fun `mergeGuestProgress should handle nonexistent test`() {
        // Given
        val unknownTestId = UUID.randomUUID()
        every { testRepository.findById(unknownTestId) } returns Optional.empty()

        val request = MergeGuestProgressRequest(
            testProgress = listOf(
                GuestTestProgressDto(
                    testId = unknownTestId.toString(),
                    score = 8,
                    maxScore = 10,
                    stars = 2,
                    timeSpentSeconds = 120
                )
            )
        )

        // When
        val result = userService.mergeGuestProgress(userId.toString(), request)

        // Then
        assertEquals(0, result.mergedTests)
        verify(exactly = 0) { progressRepository.save(any()) }
    }

    @Test
    fun `mergeGuestProgress should award achievements`() {
        // Given
        every { progressRepository.findByUserIdAndTestId(userId, testId) } returns null
        every { achievementService.checkAndAwardAchievements(userId.toString(), 80, 2) } returns listOf(
            com.funnyenglish.dto.AchievementResponse(
                id = "ach-1",
                code = "FIRST_TEST",
                name = "Первый тест",
                description = "Пройдите первый тест",
                iconUrl = null,
                pointsReward = 50,
                earned = true
            )
        )

        val request = MergeGuestProgressRequest(
            testProgress = listOf(
                GuestTestProgressDto(
                    testId = testId.toString(),
                    score = 8,
                    maxScore = 10,
                    stars = 2,
                    timeSpentSeconds = 120
                )
            )
        )

        // When
        val result = userService.mergeGuestProgress(userId.toString(), request)

        // Then
        assertEquals(1, result.mergedTests)
        assertEquals(1, result.newAchievements.size)
        assertEquals("ach-1", result.newAchievements.first().id)
    }

    @Test
    fun `mergeGuestProgress should trigger level up when enough XP`() {
        // Given
        every { progressRepository.findByUserIdAndTestId(userId, testId) } returns null
        every { achievementService.checkAndAwardAchievements(any(), any(), any()) } returns emptyList()

        // User at 50 XP, needs 100 for level 2. Award 60 XP to cross threshold.
        val highRewardTest = testEntity.copy(pointsReward = 50)
        every { testRepository.findById(testId) } returns Optional.of(highRewardTest)

        val request = MergeGuestProgressRequest(
            testProgress = listOf(
                GuestTestProgressDto(
                    testId = testId.toString(),
                    score = 10,
                    maxScore = 10,
                    stars = 3,
                    timeSpentSeconds = 120
                )
            )
        )

        // When
        val result = userService.mergeGuestProgress(userId.toString(), request)

        // Then
        assertEquals(1, result.mergedTests)
        assertEquals(65, result.totalXpAdded) // 50 + 3*5 = 65
        assertNotNull(result.levelUp)
        assertEquals(1, result.levelUp?.previousLevel)
        assertEquals(2, result.levelUp?.newLevel)
    }
}
