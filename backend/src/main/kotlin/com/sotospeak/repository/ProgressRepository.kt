package com.sotospeak.repository

import com.sotospeak.entity.Progress
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface ProgressRepository : JpaRepository<Progress, UUID> {
    fun findByUserIdAndTestId(userId: UUID, testId: UUID): Progress?
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
}
