package com.sotospeak.repository.speaking

import com.sotospeak.entity.speaking.GradeAudit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface GradeAuditRepository : JpaRepository<GradeAudit, UUID> {

    @Query(
        """
        SELECT a FROM GradeAudit a
        LEFT JOIN FETCH a.reviewer
        WHERE a.submission.id = :submissionId
        ORDER BY a.createdAt DESC
        """
    )
    fun findBySubmissionIdOrderByCreatedAtDesc(@Param("submissionId") submissionId: UUID): List<GradeAudit>
}
