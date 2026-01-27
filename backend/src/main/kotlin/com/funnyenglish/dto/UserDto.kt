package com.funnyenglish.dto

import com.funnyenglish.entity.User
import java.time.Instant

data class UserResponse(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val level: Int,
    val totalPoints: Int,
    val currentStreak: Int,
    val role: String,
    val createdAt: Instant
)

data class UserProfileResponse(
    val user: UserResponse,
    val stats: UserStats,
    val achievements: List<AchievementResponse>
)

data class UserStats(
    val testsCompleted: Long,
    val totalStars: Int,
    val perfectScores: Long,
    val currentLevel: Int,
    val pointsToNextLevel: Int
)

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val level: Int,
    val totalPoints: Int
)

data class LeaderboardResponse(
    val entries: List<LeaderboardEntry>,
    val userRank: Int?,
    val usersAbove: LeaderboardEntry?,
    val usersBelow: LeaderboardEntry?
)

fun User.toResponse() = UserResponse(
    id = id.toString(),
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl,
    level = level,
    totalPoints = totalPoints,
    currentStreak = currentStreak,
    role = role,
    createdAt = createdAt
)
