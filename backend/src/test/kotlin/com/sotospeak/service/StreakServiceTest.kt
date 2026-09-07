package com.sotospeak.service

import com.sotospeak.entity.UserStreak
import com.sotospeak.entity.XpHistory
import com.sotospeak.repository.UserStreakRepository
import com.sotospeak.repository.XpHistoryRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Юнит-тесты реальной активности в StreakService (bd wy7.9):
 * getRecentActivities читает XpHistory, weeklyCalendar проставляет xpEarned по дням.
 */
@Suppress("MagicNumber")
class StreakServiceTest {

    private val userStreakRepository: UserStreakRepository = mockk(relaxed = true)
    private val xpHistoryRepository: XpHistoryRepository = mockk()
    private val userId = UUID.randomUUID()

    private lateinit var service: StreakService

    @BeforeEach
    fun setup() {
        every { userStreakRepository.save(any<UserStreak>()) } answers { firstArg() }
        service = StreakService(userStreakRepository, xpHistoryRepository)
    }

    private fun xp(amount: Int, createdAt: Instant) =
        XpHistory(userId = userId, amount = amount, source = "TEST", createdAt = createdAt)

    private fun noonUtc(date: LocalDate): Instant =
        date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().plusSeconds(43200)

    private fun dateStr(daysAgo: Long): String = LocalDate.now().minusDays(daysAgo).toString()

    @Test
    fun weeklyCalendarPopulatesXpEarnedFromXpHistory() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        every { userStreakRepository.findByUserId(userId) } returns null
        every { xpHistoryRepository.findRecentByUserId(userId, 200) } returns listOf(
            xp(25, noonUtc(today)),
            xp(10, noonUtc(today)),
            xp(50, noonUtc(yesterday))
        )

        val data = service.getStreakData(userId)
        val byDate = data.weeklyCalendar.associateBy { it.date }

        assertEquals(35, byDate[dateStr(0)]?.xpEarned)
        assertEquals(50, byDate[dateStr(1)]?.xpEarned)
        assertEquals(0, byDate[dateStr(6)]?.xpEarned)
    }

    @Test
    fun activitiesOutsideWindowDoNotReachCalendar() {
        every { userStreakRepository.findByUserId(userId) } returns null
        // findRecentByUserId отдаёт последние 200 записей; фильтр окна 30 дней — в сервисе
        every { xpHistoryRepository.findRecentByUserId(userId, 200) } returns listOf(
            xp(100, Instant.parse("2020-01-01T12:00:00Z"))
        )

        val data = service.getStreakData(userId)
        val totalXp = data.weeklyCalendar.sumOf { it.xpEarned }
        assertEquals(0, totalXp, "XP старше окна не должен попадать в недельный календарь")
    }
}
