package com.funnyenglish.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val level: Int,
    val totalPoints: Int,
    val currentStreak: Int,
    val role: String,
    val createdAt: String
)

@Serializable
data class UserProfile(
    val user: User,
    val stats: UserStats,
    val achievements: List<Achievement>
)

@Serializable
data class UserStats(
    val testsCompleted: Long,
    val totalStars: Int,
    val perfectScores: Long,
    val currentLevel: Int,
    val pointsToNextLevel: Int
)

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val level: Int,
    val totalPoints: Int
)

@Serializable
data class Leaderboard(
    val entries: List<LeaderboardEntry>,
    val userRank: Int? = null,
    val usersAbove: LeaderboardEntry? = null,
    val usersBelow: LeaderboardEntry? = null
)
