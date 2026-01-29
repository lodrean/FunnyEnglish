package com.funnyenglish.dto

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class AdminAnalyticsResponse(
    val totalUsers: Long,
    val totalTests: Long,
    val publishedTests: Long,
    val totalQuestions: Long,
    val totalAnswers: Long,
    val totalCompletions: Long,
    val totalCategories: Long,
    val totalAchievements: Long,
    val topCategories: List<CategoryCompletionResponse>
)

data class AdminSettingsResponse(
    val s3Endpoint: String,
    val s3Bucket: String,
    val s3Region: String,
    val maxFileSize: String,
    val maxRequestSize: String,
    val corsAllowedOrigins: List<String>
)

data class CategoryCompletionResponse(
    val categoryId: UUID,
    val categoryName: String,
    val completions: Long
)

data class DailyActivityResponse(
    val date: LocalDate,
    val newUsers: Int,
    val testsCompleted: Int,
    val achievementsEarned: Int
)

data class LevelDistributionResponse(
    val level: Int,
    val users: Long
)

data class PopularTestResponse(
    val id: String,
    val name: String,
    val completions: Int,
    val category: String
)

data class RecentActivityResponse(
    val type: String,      // NEW_USER, TEST_COMPLETED, ACHIEVEMENT
    val userName: String,
    val details: String?,
    val timestamp: Instant
)
