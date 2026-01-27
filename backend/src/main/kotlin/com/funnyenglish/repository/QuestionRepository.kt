package com.funnyenglish.repository

import com.funnyenglish.entity.Question
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface QuestionRepository : JpaRepository<Question, UUID> {
    fun findByTestIdOrderByDisplayOrder(testId: UUID): List<Question>

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers WHERE q.test.id = :testId ORDER BY q.displayOrder")
    fun findByTestIdWithAnswers(testId: UUID): List<Question>

    fun deleteByTestId(testId: UUID)
}
