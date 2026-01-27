package com.funnyenglish.entity

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
    val level: Int = 1,

    @Column(name = "total_points", nullable = false)
    val totalPoints: Int = 0,

    @Column(name = "current_streak", nullable = false)
    val currentStreak: Int = 0,

    @Column(name = "last_activity_date")
    val lastActivityDate: Instant? = null,

    @Column(nullable = false)
    val role: String = "USER",

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val progress: MutableList<Progress> = mutableListOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_achievements",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "achievement_id")]
    )
    val achievements: MutableSet<Achievement> = mutableSetOf()
)

enum class AuthProvider {
    EMAIL, GOOGLE, VK, TELEGRAM
}
