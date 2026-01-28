package com.funnyenglish.dto

data class AdminAnalyticsResponse(
    val totalUsers: Long,
    val totalTests: Long,
    val publishedTests: Long,
    val totalQuestions: Long,
    val totalAnswers: Long,
    val totalCompletions: Long,
    val totalCategories: Long,
    val totalAchievements: Long
)

data class AdminSettingsResponse(
    val s3Endpoint: String,
    val s3Bucket: String,
    val s3Region: String,
    val maxFileSize: String,
    val maxRequestSize: String,
    val corsAllowedOrigins: List<String>
)
