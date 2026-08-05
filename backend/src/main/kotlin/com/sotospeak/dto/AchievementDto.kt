package com.sotospeak.dto

data class AchievementResponse(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val pointsReward: Int,
    val earned: Boolean = false
)
