package com.funnyenglish.repository

import com.funnyenglish.entity.AchievementEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface AchievementRepository : JpaRepository<AchievementEntity, String> {
    fun findByCode(code: String): AchievementEntity?

    fun findByCategory(category: String): List<AchievementEntity>

    fun findByIsHidden(isHidden: Boolean): List<AchievementEntity>

    @Query("SELECT a FROM AchievementEntity a WHERE a.isHidden = false ORDER BY a.pointsReward")
    fun findVisibleAchievements(): List<AchievementEntity>

    @Query("""
        SELECT a.* FROM achievements a
        INNER JOIN user_achievements ua ON a.id = ua.achievement_id
        WHERE ua.user_id = :userId
    """, nativeQuery = true)
    fun findByUserId(userId: UUID): List<AchievementEntity>

    @Query(
        value = """
            SELECT 
                CAST(earned_at AS DATE) as date,
                COUNT(*) as count
            FROM user_achievements
            WHERE earned_at >= :startDate
            GROUP BY CAST(earned_at AS DATE)
            ORDER BY CAST(earned_at AS DATE)
        """,
        nativeQuery = true
    )
    fun countAchievementsEarnedByDay(startDate: Instant): List<DateCountProjection>
}
