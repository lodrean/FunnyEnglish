package com.sotospeak.service

import com.sotospeak.dto.AdminAnalyticsResponse
import com.sotospeak.dto.CategoryCompletionResponse
import com.sotospeak.dto.DailyActivityResponse
import com.sotospeak.dto.GuestAnalyticsResponse
import com.sotospeak.dto.LevelDistributionResponse
import com.sotospeak.dto.PopularTestResponse
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
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    private val guestEventRepository: GuestEventRepository
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

        return AdminAnalyticsResponse(
            totalUsers = userRepository.count(),
            totalTests = testRepository.count(),
            publishedTests = testRepository.countByIsPublishedTrue(),
            totalQuestions = questionRepository.count(),
            totalAnswers = answerRepository.count(),
            totalCompletions = progressRepository.count(),
            totalCategories = categoryRepository.count(),
            totalAchievements = achievementRepository.count(),
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
