package com.funnyenglish.service

import com.funnyenglish.dto.AdminAnalyticsResponse
import com.funnyenglish.dto.UserResponse
import com.funnyenglish.dto.toResponse
import com.funnyenglish.repository.AnswerRepository
import com.funnyenglish.repository.AchievementRepository
import com.funnyenglish.repository.CategoryRepository
import com.funnyenglish.repository.ProgressRepository
import com.funnyenglish.repository.QuestionRepository
import com.funnyenglish.repository.TestRepository
import com.funnyenglish.repository.UserRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

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
        return AdminAnalyticsResponse(
            totalUsers = userRepository.count(),
            totalTests = testRepository.count(),
            publishedTests = testRepository.countByIsPublishedTrue(),
            totalQuestions = questionRepository.count(),
            totalAnswers = answerRepository.count(),
            totalCompletions = progressRepository.count(),
            totalCategories = categoryRepository.count(),
            totalAchievements = achievementRepository.count()
        )
    }
}
