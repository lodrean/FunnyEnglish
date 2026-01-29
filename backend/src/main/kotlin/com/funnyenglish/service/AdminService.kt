package com.funnyenglish.service

import com.funnyenglish.dto.AdminAnalyticsResponse
import com.funnyenglish.dto.CategoryCompletionResponse
import com.funnyenglish.dto.DailyActivityResponse
import com.funnyenglish.dto.LevelDistributionResponse
import com.funnyenglish.dto.PopularTestResponse
import com.funnyenglish.dto.RecentActivityResponse
import com.funnyenglish.dto.UserResponse
import com.funnyenglish.dto.toResponse
import com.funnyenglish.repository.AnswerRepository
import com.funnyenglish.repository.AchievementRepository
import com.funnyenglish.repository.CategoryRepository
import com.funnyenglish.repository.ProgressRepository
import com.funnyenglish.repository.QuestionRepository
import com.funnyenglish.repository.TestRepository
import com.funnyenglish.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val testRepository: TestRepository,
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository,
    private val progressRepository: ProgressRepository,
    private val achievementRepository: AchievementRepository,
    private val categoryRepository: CategoryRepository
) {
    companion object {
        private const val TOP_CATEGORIES_LIMIT = 5
        private const val RECENT_ACTIVITY_LIMIT = 10
    }

    fun getUsers(query: String?, role: String?): List<UserResponse> {
        val normalizedQuery = query?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedRole = role?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }

        val users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))

        return users.asSequence()
            .filter { user ->
                normalizedRole == null || user.role.equals(normalizedRole, ignoreCase = true)
            }
            .filter { user ->
                normalizedQuery == null ||
                    user.displayName.contains(normalizedQuery, ignoreCase = true) ||
                    user.email.contains(normalizedQuery, ignoreCase = true)
            }
            .map { it.toResponse() }
            .toList()
    }

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

    fun getLevelDistribution(): List<LevelDistributionResponse> {
        return userRepository.countUsersByLevel().map { levelCount ->
            LevelDistributionResponse(
                level = levelCount.level,
                users = levelCount.count
            )
        }
    }

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

    fun getRecentActivity(): List<RecentActivityResponse> {
        return userRepository.findRecentActivity(RECENT_ACTIVITY_LIMIT).map { activity ->
            RecentActivityResponse(
                type = activity.type,
                userName = activity.userName,
                details = activity.details,
                timestamp = activity.timestamp
            )
        }
    }
}
