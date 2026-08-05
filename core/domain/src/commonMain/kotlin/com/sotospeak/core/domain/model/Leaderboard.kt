package com.sotospeak.core.domain.model

import kotlinx.serialization.Serializable

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
