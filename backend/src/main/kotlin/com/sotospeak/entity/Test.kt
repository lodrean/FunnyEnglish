package com.sotospeak.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tests")
class Test(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @Column(nullable = false)
    var title: String,

    var description: String? = null,

    @Column(name = "thumbnail_url")
    var thumbnailUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var difficulty: Difficulty = Difficulty.EASY,

    @Column(name = "points_reward", nullable = false)
    var pointsReward: Int = 10,

    @Column(name = "time_limit_seconds")
    var timeLimitSeconds: Int? = null,

    @Column(name = "is_published", nullable = false)
    var isPublished: Boolean = false,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "test", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    val questions: MutableList<Question> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Test) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

enum class Difficulty {
    EASY, MEDIUM, HARD
}
