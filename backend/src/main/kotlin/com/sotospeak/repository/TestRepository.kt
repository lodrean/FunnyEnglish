package com.sotospeak.repository

import com.sotospeak.entity.Difficulty
import com.sotospeak.entity.Test
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TestRepository : JpaRepository<Test, UUID> {
    // EntityGraph: toListResponse читает questions.size и category — без fetch был N+1
    @EntityGraph(attributePaths = ["category", "questions"])
    fun findByCategoryIdAndIsPublishedTrueOrderByDisplayOrder(categoryId: UUID): List<Test>

    @EntityGraph(attributePaths = ["category", "questions"])
    fun findByIsPublishedTrueOrderByDisplayOrder(): List<Test>
    fun findByDifficultyAndIsPublishedTrueOrderByDisplayOrder(difficulty: Difficulty): List<Test>
    fun countByIsPublishedTrue(): Long

    @Query("""
        SELECT t FROM Test t
        WHERE t.isPublished = true
        AND t.id NOT IN (SELECT p.test.id FROM Progress p WHERE p.user.id = :userId)
        ORDER BY t.displayOrder
    """)
    fun findUncompletedByUser(userId: UUID): List<Test>

    @Query("SELECT t FROM Test t LEFT JOIN FETCH t.questions WHERE t.id = :id")
    fun findByIdWithQuestions(id: UUID): Test?

    @EntityGraph(attributePaths = ["questions"])
    @Query("SELECT t FROM Test t")
    fun findAllWithQuestions(): List<Test>

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM Test t WHERE t.id = :id")
    fun deleteByIdWithoutLoading(id: UUID): Int
}
