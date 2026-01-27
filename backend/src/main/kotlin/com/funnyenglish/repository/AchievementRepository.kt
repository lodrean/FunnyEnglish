package com.funnyenglish.repository

import com.funnyenglish.entity.Achievement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AchievementRepository : JpaRepository<Achievement, UUID> {
    fun findByCode(code: String): Achievement?

    @Query("SELECT a FROM Achievement a WHERE a.isHidden = false ORDER BY a.pointsReward")
    fun findVisibleAchievements(): List<Achievement>

    @Query("""
        SELECT a.* FROM achievements a
        INNER JOIN user_achievements ua ON a.id = ua.achievement_id
        WHERE ua.user_id = :userId
    """, nativeQuery = true)
    fun findByUserId(userId: UUID): List<Achievement>
}
