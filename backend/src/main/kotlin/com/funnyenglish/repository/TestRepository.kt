package com.funnyenglish.repository

import com.funnyenglish.entity.Difficulty
import com.funnyenglish.entity.Test
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TestRepository : JpaRepository<Test, UUID> {
    fun findByCategoryIdAndIsPublishedTrueOrderByDisplayOrder(categoryId: UUID): List<Test>
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
}
