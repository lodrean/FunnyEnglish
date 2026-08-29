package com.sotospeak.repository.speaking

import com.sotospeak.entity.speaking.PracticeSubmission
import com.sotospeak.entity.speaking.SubmissionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

@Repository
interface PracticeSubmissionRepository : JpaRepository<PracticeSubmission, UUID> {

    @Query("""
        SELECT s FROM PracticeSubmission s
        LEFT JOIN FETCH s.user
        LEFT JOIN FETCH s.topic
        LEFT JOIN FETCH s.grade
        WHERE (:status IS NULL OR s.status = :status)
        AND (:userId IS NULL OR s.user.id = :userId)
        AND (:topicId IS NULL OR s.topic.id = :topicId)
        AND (CAST(:dateFrom AS timestamp) IS NULL OR s.createdAt >= :dateFrom)
        AND (CAST(:dateTo AS timestamp) IS NULL OR s.createdAt < :dateTo)
        ORDER BY s.createdAt DESC
    """)
    fun search(
        @Param("status") status: SubmissionStatus?,
        @Param("userId") userId: UUID?,
        @Param("topicId") topicId: UUID?,
        @Param("dateFrom") dateFrom: Instant?,
        @Param("dateTo") dateTo: Instant?,
        pageable: Pageable
    ): Page<PracticeSubmission>

    @Query("""
        SELECT s FROM PracticeSubmission s
        LEFT JOIN FETCH s.grade g
        LEFT JOIN FETCH g.reviewer
        LEFT JOIN FETCH s.topic
        WHERE s.user.id = :userId
        ORDER BY s.createdAt DESC
    """)
    fun findByUserIdWithGrade(@Param("userId") userId: UUID): List<PracticeSubmission>

    fun findFirstByUserIdAndTopicId(userId: UUID, topicId: UUID): PracticeSubmission?

    fun countByStatus(status: SubmissionStatus): Long

    @Query("""
        SELECT s FROM PracticeSubmission s
        LEFT JOIN FETCH s.user
        LEFT JOIN FETCH s.topic
        LEFT JOIN FETCH s.grade g
        LEFT JOIN FETCH g.reviewer
        WHERE s.id = :id
    """)
    fun findByIdWithDetails(@Param("id") id: UUID): Optional<PracticeSubmission>
}
