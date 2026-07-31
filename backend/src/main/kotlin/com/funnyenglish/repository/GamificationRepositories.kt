package com.funnyenglish.repository

import com.funnyenglish.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

// ==================== User Achievement Repository ====================

@Repository
interface UserAchievementRepository : JpaRepository<UserAchievementEntity, UUID> {
    
    fun findByUserId(userId: UUID): List<UserAchievementEntity>
    
    fun findByUserIdAndAchievementId(userId: UUID, achievementId: String): UserAchievementEntity?
    
    @Query("SELECT COUNT(ua) FROM UserAchievementEntity ua WHERE ua.userId = :userId AND ua.isEarned = true")
    fun countEarnedByUserId(@Param("userId") userId: UUID): Long
    
    @Query("""
        SELECT COUNT(l) FROM AdaptiveLesson l 
        WHERE l.userId = :userId 
        AND l.status = 'COMPLETED'
    """)
    fun countLessonsCompleted(@Param("userId") userId: UUID): Int
    
    @Query("""
        SELECT DISTINCT q.type FROM Question q 
        JOIN LessonQuestionHistory h ON h.questionId = q.id 
        WHERE h.lesson.userId = :userId
    """)
    fun getTriedExerciseTypes(@Param("userId") userId: UUID): List<String>
}

// ==================== Quest Repository ====================

@Repository
interface QuestRepository : JpaRepository<Quest, UUID> {
    
    @Query(value = """
        SELECT * FROM quests q 
        WHERE q.user_id = :userId 
        AND DATE(q.created_at) = CURRENT_DATE
    """, nativeQuery = true)
    fun findDailyQuestsForToday(@Param("userId") userId: UUID): List<Quest>
    
    @Query("""
        SELECT q FROM Quest q 
        WHERE q.userId = :userId 
        AND q.createdAt >= :weekStart
        AND q.createdAt < :weekEnd
    """)
    fun findWeeklyQuestsForCurrentWeek(
        @Param("userId") userId: UUID,
        @Param("weekStart") weekStart: Instant,
        @Param("weekEnd") weekEnd: Instant
    ): List<Quest>
    
    @Query("""
        SELECT q FROM Quest q 
        WHERE q.userId = :userId 
        AND q.questType = :type
        AND q.isCompleted = false
        AND q.expiresAt > CURRENT_TIMESTAMP
    """)
    fun findActiveQuestsByType(
        @Param("userId") userId: UUID,
        @Param("type") type: com.funnyenglish.shared.model.QuestType
    ): List<Quest>
}

// ==================== XP History Repository ====================

@Repository
interface XpHistoryRepository : JpaRepository<XpHistory, UUID> {
    
    @Query("SELECT xh FROM XpHistory xh WHERE xh.userId = :userId ORDER BY xh.createdAt DESC")
    fun findRecentByUserId(@Param("userId") userId: UUID, limit: Int): List<XpHistory>
    
    @Query("""
        SELECT xh.userId, SUM(xh.amount) as total 
        FROM XpHistory xh 
        GROUP BY xh.userId 
        ORDER BY total DESC
    """)
    fun findTopUsers(limit: Int): List<Array<Any>>
    
    @Query(value = """
        SELECT u.id, u.display_name, u.avatar_url, u.total_points 
        FROM users u 
        ORDER BY u.total_points DESC 
        LIMIT :limit
    """, nativeQuery = true)
    fun findTopUsersNative(@Param("limit") limit: Int): List<Array<Any>>
    
    @Query("""
        SELECT COUNT(DISTINCT x.userId) + 1 
        FROM XpHistory x 
        JOIN User u ON x.userId = u.id 
        WHERE u.totalPoints > (
            SELECT u2.totalPoints FROM User u2 WHERE u2.id = :userId
        )
    """)
    fun getGlobalRank(@Param("userId") userId: UUID): Int?
    
    @Query("SELECT SUM(x.amount) FROM XpHistory x WHERE x.userId = :userId AND x.createdAt >= :since")
    fun getXpSince(@Param("userId") userId: UUID, @Param("since") since: Instant): Int?
}

// ==================== User Streak Repository ====================

@Repository
interface UserStreakRepository : JpaRepository<UserStreak, UUID> {
    
    fun findByUserId(userId: UUID): UserStreak?
    
    @Query(value = """
        SELECT * FROM user_streaks us 
        WHERE us.last_activity_date < CURRENT_DATE - INTERVAL '1 day'
        AND us.current_streak > 0
    """, nativeQuery = true)
    fun findBrokenStreaks(): List<UserStreak>
}
