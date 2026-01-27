package com.funnyenglish.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tests")
data class Test(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category,

    @Column(nullable = false)
    val title: String,

    val description: String? = null,

    @Column(name = "thumbnail_url")
    val thumbnailUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val difficulty: Difficulty = Difficulty.EASY,

    @Column(name = "points_reward", nullable = false)
    val pointsReward: Int = 10,

    @Column(name = "time_limit_seconds")
    val timeLimitSeconds: Int? = null,

    @Column(name = "is_published", nullable = false)
    val isPublished: Boolean = false,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "test", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    val questions: MutableList<Question> = mutableListOf()
)

enum class Difficulty {
    EASY, MEDIUM, HARD
}
