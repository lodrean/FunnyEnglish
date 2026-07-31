package com.funnyenglish.repository

import com.funnyenglish.entity.AuthProvider
import com.funnyenglish.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun findByAuthProviderAndProviderId(provider: AuthProvider, providerId: String): User?
    fun existsByEmail(email: String): Boolean
    fun findAllByOrderByCreatedAtDesc(): List<User>

    @Query("SELECT u FROM User u ORDER BY u.totalPoints DESC LIMIT :limit")
    fun findTopByTotalPoints(limit: Int): List<User>

    @Query("""
        SELECT u FROM User u
        WHERE u.totalPoints > (SELECT u2.totalPoints FROM User u2 WHERE u2.id = :userId)
        ORDER BY u.totalPoints ASC
        LIMIT 1
    """)
    fun findUserAbove(userId: UUID): User?

    @Query("""
        SELECT u FROM User u
        WHERE u.totalPoints < (SELECT u2.totalPoints FROM User u2 WHERE u2.id = :userId)
        ORDER BY u.totalPoints DESC
        LIMIT 1
    """)
    fun findUserBelow(userId: UUID): User?

    @Query(
        value = """
            SELECT 
                CAST(created_at AS DATE) as date,
                COUNT(*) as count
            FROM users
            WHERE created_at >= :startDate
            GROUP BY CAST(created_at AS DATE)
            ORDER BY CAST(created_at AS DATE)
        """,
        nativeQuery = true
    )
    fun countNewUsersByDay(startDate: Instant): List<DateCountProjection>

    @Query("""
        SELECT u.level as level, COUNT(u) as count
        FROM User u
        GROUP BY u.level
        ORDER BY u.level
    """)
    fun countUsersByLevel(): List<LevelCountProjection>

    @Query(
        value = """
            SELECT
                activity.type as type,
                activity."userName" as "userName",
                activity.details as details,
                activity."timestamp" as "timestamp"
            FROM (
                SELECT
                    'NEW_USER' AS type,
                    u.display_name AS "userName",
                    CAST(NULL AS text) AS details,
                    u.created_at AS "timestamp"
                FROM users u
                UNION ALL
                SELECT
                    'TEST_COMPLETED' AS type,
                    u.display_name AS "userName",
                    t.title AS details,
                    p.last_attempt_at AS "timestamp"
                FROM progress p
                JOIN users u ON u.id = p.user_id
                JOIN tests t ON t.id = p.test_id
                UNION ALL
                SELECT
                    'ACHIEVEMENT' AS type,
                    u.display_name AS "userName",
                    a.name AS details,
                    ua.earned_at AS "timestamp"
                FROM user_achievements ua
                JOIN users u ON u.id = ua.user_id
                JOIN achievements a ON a.id = ua.achievement_id
            ) AS activity
            ORDER BY activity."timestamp" DESC
            LIMIT :limit
        """,
        nativeQuery = true
    )
    fun findRecentActivity(limit: Int): List<RecentActivityProjection>
}
