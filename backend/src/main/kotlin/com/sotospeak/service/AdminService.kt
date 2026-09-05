package com.sotospeak.service

import com.sotospeak.dto.AdminAnalyticsResponse
import com.sotospeak.dto.CategoryCompletionResponse
import com.sotospeak.dto.DailyActivityResponse
import com.sotospeak.dto.GuestAnalyticsResponse
import com.sotospeak.dto.LevelDistributionResponse
import com.sotospeak.dto.PopularTestResponse
import com.sotospeak.dto.PrdMetricsResponse
import com.sotospeak.dto.RecentActivityResponse
import com.sotospeak.entity.GuestEventType
import com.sotospeak.repository.AnswerRepository
import com.sotospeak.repository.AchievementRepository
import com.sotospeak.repository.CategoryRepository
import com.sotospeak.repository.GuestEventRepository
import com.sotospeak.repository.ProgressRepository
import com.sotospeak.repository.QuestionRepository
import com.sotospeak.repository.TestRepository
import com.sotospeak.repository.UserRepository
import com.sotospeak.repository.speaking.PracticeSubmissionRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val testRepository: TestRepository,
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository,
    private val progressRepository: ProgressRepository,
    private val achievementRepository: AchievementRepository,
    private val categoryRepository: CategoryRepository,
    private val guestEventRepository: GuestEventRepository,
    private val practiceSubmissionRepository: PracticeSubmissionRepository
) {
    companion object {
        private const val TOP_CATEGORIES_LIMIT = 5
        private const val RECENT_ACTIVITY_LIMIT = 10
    }

    @Transactional(readOnly = true)
    fun getAnalytics(): AdminAnalyticsResponse {
        val topCategories = progressRepository
            .findCategoryCompletions(PageRequest.of(0, TOP_CATEGORIES_LIMIT))
            .map {
                CategoryCompletionResponse(
                    categoryId = it.categoryId,
                    categoryName = it.categoryName,
                    completions = it.completions
                )
            }

        // wy7.3: 8 COUNT-ов одним запросом (scalar subselects); было 8 round-trip'ов.
        // Kotlin-проекции — методы, не свойства (грабля №105)
        val totals = progressRepository.countAdminTotals()
        return AdminAnalyticsResponse(
            totalUsers = totals.getTotalUsers(),
            totalTests = totals.getTotalTests(),
            publishedTests = totals.getPublishedTests(),
            totalQuestions = totals.getTotalQuestions(),
            totalAnswers = totals.getTotalAnswers(),
            totalCompletions = totals.getTotalCompletions(),
            totalCategories = totals.getTotalCategories(),
            totalAchievements = totals.getTotalAchievements(),
            topCategories = topCategories
        )
    }

    @Transactional(readOnly = true)
    fun getDailyActivity(days: Int): List<DailyActivityResponse> {
        val safeDays = days.coerceAtLeast(1)
        val today = LocalDate.now(ZoneId.systemDefault())
        val startDate = today.minusDays(safeDays.toLong() - 1)
        val startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()

        val newUsersByDay = userRepository.countNewUsersByDay(startInstant)
            .associate { it.date.toLocalDate() to it.count }
        val testsByDay = progressRepository.countCompletionsByDay(startInstant)
            .associate { it.date.toLocalDate() to it.count }
        val achievementsByDay = achievementRepository.countAchievementsEarnedByDay(startInstant)
            .associate { it.date.toLocalDate() to it.count }

        return (0 until safeDays).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            DailyActivityResponse(
                date = date,
                newUsers = (newUsersByDay[date] ?: 0L).toInt(),
                testsCompleted = (testsByDay[date] ?: 0L).toInt(),
                achievementsEarned = (achievementsByDay[date] ?: 0L).toInt()
            )
        }
    }

    @Transactional(readOnly = true)
    fun getLevelDistribution(): List<LevelDistributionResponse> {
        return userRepository.countUsersByLevel().map { levelCount ->
            LevelDistributionResponse(
                level = levelCount.level,
                users = levelCount.count
            )
        }
    }

    @Transactional(readOnly = true)
    fun getPopularTests(): List<PopularTestResponse> {
        return progressRepository.findPopularTests().map { test ->
            PopularTestResponse(
                id = test.id.toString(),
                name = test.name,
                completions = test.completions.toInt(),
                category = test.category
            )
        }
    }

    /** Аналитика по гостевым (обезличенным) пользователям */
    @Transactional(readOnly = true)
    fun getGuestAnalytics(): GuestAnalyticsResponse {
        val total = guestEventRepository.countDistinctGuests()
        val active7d = guestEventRepository.countDistinctGuestsActiveSince(
            Instant.now().minus(7, ChronoUnit.DAYS)
        )
        val completions = guestEventRepository.countByType(GuestEventType.TEST_COMPLETED)
        val converted = guestEventRepository.countDistinctConvertedGuests()
        return GuestAnalyticsResponse(
            totalGuests = total,
            activeGuests7d = active7d,
            guestTestCompletions = completions,
            convertedGuests = converted,
            conversionRate = if (total > 0) converted.toDouble() / total else 0.0
        )
    }

    /** Метрики PRD (Speaking Trainer §Metrics): practice/ученик/неделю, доля REVIEWED за 48ч, конверсия гость→регистрация. */
    @Transactional(readOnly = true)
    fun getPrdMetrics(): PrdMetricsResponse {
        val weekAgo = Instant.now().minus(7, ChronoUnit.DAYS)
        val submissions7d = practiceSubmissionRepository.countCreatedSince(weekAgo)
        val students7d = practiceSubmissionRepository.countDistinctSubmittersSince(weekAgo)

        val reviewed = practiceSubmissionRepository.findReviewedTimestamps()
        val within48h = reviewed.count {
            // Kotlin-интерфейс проекции: getter-методы, не свойства (h3l.3, фикс компиляции)
            Duration.between(it.getSubmittedAt(), it.getReviewedAt()) <= Duration.ofHours(48)
        }

        val totalGuests = guestEventRepository.countDistinctGuests()
        val convertedGuests = guestEventRepository.countDistinctConvertedGuests()

        return PrdMetricsResponse(
            practiceSubmissionsLast7d = submissions7d,
            activeStudentsLast7d = students7d,
            practicePerStudentPerWeek = if (students7d > 0) submissions7d.toDouble() / students7d else 0.0,
            reviewedTotal = reviewed.size.toLong(),
            reviewedWithin48h = within48h.toLong(),
            reviewedWithin48hShare = if (reviewed.isNotEmpty()) within48h.toDouble() / reviewed.size else 0.0,
            totalGuests = totalGuests,
            convertedGuests = convertedGuests,
            guestConversionRate = if (totalGuests > 0) convertedGuests.toDouble() / totalGuests else 0.0
        )
    }

    @Transactional(readOnly = true)
    fun getRecentActivity(): List<RecentActivityResponse> {
        return userRepository.findRecentActivity(RECENT_ACTIVITY_LIMIT).map { activity ->
            RecentActivityResponse(
                type = activity.type,
                userName = activity.userName,
                details = activity.details,
                timestamp = activity.timestamp.toInstant()
            )
        }
    }
}
