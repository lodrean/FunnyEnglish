package com.funnyenglish.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val iconUrl: String? = null,
    val pointsReward: Int,
    val earned: Boolean = false
)
