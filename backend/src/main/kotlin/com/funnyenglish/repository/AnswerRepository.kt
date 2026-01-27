package com.funnyenglish.repository

import com.funnyenglish.entity.Answer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AnswerRepository : JpaRepository<Answer, UUID> {
    fun findByQuestionId(questionId: UUID): List<Answer>
    fun deleteByQuestionId(questionId: UUID)
}
