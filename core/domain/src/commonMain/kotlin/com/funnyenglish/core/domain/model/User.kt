package com.funnyenglish.core.domain.model

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
