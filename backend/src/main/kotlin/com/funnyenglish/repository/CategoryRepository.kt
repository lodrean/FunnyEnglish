package com.funnyenglish.repository

import com.funnyenglish.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CategoryRepository : JpaRepository<Category, UUID> {
    fun findByIsActiveTrueOrderByDisplayOrder(): List<Category>

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.tests t WHERE c.isActive = true AND t.isPublished = true ORDER BY c.displayOrder")
    fun findAllWithPublishedTests(): List<Category>
}
