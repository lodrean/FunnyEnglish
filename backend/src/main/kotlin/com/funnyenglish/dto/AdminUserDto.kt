package com.funnyenglish.dto

import java.time.Instant

data class AdminUserSummaryResponse(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val role: String,
    val level: Int,
    val totalPoints: Int,
    val currentStreak: Int,
    val createdAt: Instant,
    val stats: UserStats
)

data class AdminUserDetailResponse(
    val user: UserResponse,
    val stats: UserStats,
    val achievements: List<AchievementResponse>,
    val progressSummary: UserProgressSummary,
    val progress: List<ProgressResponse>
)
