package com.funnyenglish.repository.speaking

import com.funnyenglish.entity.speaking.Grade
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface GradeRepository : JpaRepository<Grade, UUID> {

    @Query("""
        SELECT g FROM Grade g
        LEFT JOIN FETCH g.reviewer
        WHERE g.submission.id = :submissionId
    """)
    fun findBySubmissionId(@Param("submissionId") submissionId: UUID): Optional<Grade>
}
