package com.funnyenglish.dto

import com.funnyenglish.entity.Achievement

data class AchievementResponse(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val pointsReward: Int,
    val earned: Boolean = false
)

fun Achievement.toResponse(earned: Boolean = false) = AchievementResponse(
    id = id.toString(),
    code = code,
    name = name,
    description = description,
    iconUrl = iconUrl,
    pointsReward = pointsReward,
    earned = earned
)
