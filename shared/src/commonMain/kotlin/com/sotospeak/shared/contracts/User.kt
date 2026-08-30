package com.sotospeak.shared.contracts

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
    val user: User
)

/** Используется backend'ом (GamificationController/XpService, leaderboard).
 *  Удаление сломало :backend:compileKotlin — возвращено в Фазе 5 (8tg.5.1). */
@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val level: Int,
    val totalPoints: Int
)
