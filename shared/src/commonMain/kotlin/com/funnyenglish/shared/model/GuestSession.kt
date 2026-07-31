package com.funnyenglish.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class GuestSession(
    val guestId: String,
    val createdAt: String, // ISO-8601
    val testProgress: List<GuestTestProgress> = emptyList(),
    val totalXpEarned: Int = 0
)

@Serializable
data class GuestTestProgress(
    val testId: String,
    val score: Int,
    val maxScore: Int,
    val stars: Int,
    val timeSpentSeconds: Int? = null,
    val completedAt: String // ISO-8601
)

@Serializable
data class MergeGuestProgressRequest(
    val testProgress: List<GuestTestProgress>,
    /** Анонимный ID гостя (guestId сессии) — для метрики конверсии */
    val anonymousId: String? = null
)

@Serializable
data class MergeGuestProgressResponse(
    val mergedTests: Int,
    val totalXpAdded: Int,
    val newAchievements: List<Achievement>,
    val levelUp: LevelUpInfo? = null
)
