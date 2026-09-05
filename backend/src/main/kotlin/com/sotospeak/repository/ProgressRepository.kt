package com.sotospeak.repository

import com.sotospeak.entity.Progress
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface ProgressRepository : JpaRepository<Progress, UUID> {
    fun findByUserIdAndTestId(userId: UUID, testId: UUID): Progress?

    // EntityGraph: ProgressService читает test.title и test.category — без fetch был N+1
    @EntityGraph(attributePaths = ["test", "test.category"])
    fun findByUserId(userId: UUID): List<Progress>
    fun countByUserId(userId: UUID): Long

    @Query("SELECT SUM(p.stars) FROM Progress p WHERE p.user.id = :userId")
    fun sumStarsByUserId(userId: UUID): Int?

    @Query("SELECT COUNT(p) FROM Progress p WHERE p.user.id = :userId AND p.stars = 3")
    fun countPerfectScoresByUserId(userId: UUID): Long

    @Query("SELECT p FROM Progress p WHERE p.test.category.id = :categoryId AND p.user.id = :userId")
    fun findByUserIdAndCategoryId(userId: UUID, categoryId: UUID): List<Progress>

    @Query(
        value = """
            SELECT 
                CAST(completed_at AS DATE) as date,
                COUNT(*) as count
            FROM progress
            WHERE completed_at >= :startDate
            GROUP BY CAST(completed_at AS DATE)
            ORDER BY CAST(completed_at AS DATE)
        """,
        nativeQuery = true
    )
    fun countCompletionsByDay(startDate: Instant): List<DateCountProjection>

    @Query("""
        SELECT c.id as categoryId, c.name as categoryName, COUNT(p) as completions
        FROM Progress p
        JOIN p.test t
        JOIN t.category c
        GROUP BY c.id, c.name
        ORDER BY COUNT(p) DESC
    """)
    fun findCategoryCompletions(pageable: Pageable): List<CategoryCompletionProjection>

    @Query("""
        SELECT t.id as id, t.title as name, COUNT(p) as completions, c.name as category
        FROM Progress p
        JOIN p.test t
        JOIN t.category c
        GROUP BY t.id, t.title, c.name
        ORDER BY COUNT(p) DESC
    """)
    fun findPopularTests(): List<PopularTestProjection>

    // wy7.3: все агрегаты статистики списка пользователей одним запросом
    // (было 3 запроса × N пользователей в AdminUserController.getUsers)
    @Query("""
        SELECT p.user.id as userId, COUNT(p) as completed,
               COALESCE(SUM(p.stars), 0) as stars,
               COALESCE(SUM(CASE WHEN p.stars = 3 THEN 1 ELSE 0 END), 0) as perfect
        FROM Progress p
        WHERE p.user.id IN :userIds
        GROUP BY p.user.id
    """)
    fun aggregateStatsByUserIds(userIds: Collection<UUID>): List<UserStatsProjection>

    // wy7.3: 8 COUNT-ов дашборда одним round-trip (scalar subselects, H2+PG)
    @Query(
        value = """
            SELECT
                (SELECT COUNT(*) FROM users) AS total_users,
                (SELECT COUNT(*) FROM tests) AS total_tests,
                (SELECT COUNT(*) FROM tests WHERE is_published = TRUE) AS published_tests,
                (SELECT COUNT(*) FROM questions) AS total_questions,
                (SELECT COUNT(*) FROM answers) AS total_answers,
                (SELECT COUNT(*) FROM progress) AS total_completions,
                (SELECT COUNT(*) FROM categories) AS total_categories,
                (SELECT COUNT(*) FROM achievements) AS total_achievements
        """,
        nativeQuery = true
    )
    fun countAdminTotals(): AdminTotalsProjection
}

interface UserStatsProjection {
    fun getUserId(): UUID
    fun getCompleted(): Long
    fun getStars(): Long
    fun getPerfect(): Long
}

interface AdminTotalsProjection {
    fun getTotalUsers(): Long
    fun getTotalTests(): Long
    fun getPublishedTests(): Long
    fun getTotalQuestions(): Long
    fun getTotalAnswers(): Long
    fun getTotalCompletions(): Long
    fun getTotalCategories(): Long
    fun getTotalAchievements(): Long
}
