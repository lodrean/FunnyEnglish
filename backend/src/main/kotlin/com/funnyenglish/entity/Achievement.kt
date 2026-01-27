package com.funnyenglish.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "achievements")
data class Achievement(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    val code: String,  // e.g., FIRST_TEST, PERFECT_SCORE, STREAK_7

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val description: String,

    @Column(name = "icon_url")
    val iconUrl: String? = null,

    @Column(name = "points_reward", nullable = false)
    val pointsReward: Int = 0,

    @Column(name = "is_hidden", nullable = false)
    val isHidden: Boolean = false  // Secret achievements
)
