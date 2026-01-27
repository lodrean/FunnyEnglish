package com.funnyenglish.repository

import com.funnyenglish.entity.Progress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
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
}
