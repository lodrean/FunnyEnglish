package com.sotospeak.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(name = "password_hash")
    val passwordHash: String? = null,

    @Column(name = "display_name", nullable = false)
    val displayName: String,

    @Column(name = "avatar_url")
    val avatarUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    val authProvider: AuthProvider = AuthProvider.EMAIL,

    @Column(name = "provider_id")
    val providerId: String? = null,

    @Column(nullable = false)
    var level: Int = 1,

    @Column(name = "total_points", nullable = false)
    var totalPoints: Int = 0,

    @Column(name = "current_streak", nullable = false)
    var currentStreak: Int = 0,

    @Column(name = "longest_streak", nullable = false)
    var longestStreak: Int = 0,

    @Column(name = "last_activity_date")
    var lastActivityDate: Instant? = null,

    @Column(name = "previous_streak_before_break")
    var previousStreakBeforeBreak: Int? = null,

    @Column(nullable = false)
    val role: String = "USER",

    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val progress: MutableList<Progress> = mutableListOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_achievements",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "achievement_id")]
    )
    val achievements: MutableSet<AchievementEntity> = mutableSetOf()
)

enum class AuthProvider {
    EMAIL, GOOGLE, VK, TELEGRAM
}
