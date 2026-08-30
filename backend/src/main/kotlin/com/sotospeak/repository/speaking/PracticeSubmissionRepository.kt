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

/** Проекция: момент отправки и момент оценки (reviewed). */
interface ReviewedTimestamps {
    fun getSubmittedAt(): Instant
    fun getReviewedAt(): Instant
}

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

    /** Отправок за период (для метрики PRD «practice-отправок/ученик/неделю») */
    @Query("SELECT COUNT(s) FROM PracticeSubmission s WHERE s.createdAt >= :since")
    fun countCreatedSince(@Param("since") since: Instant): Long

    /** Уникальных учеников с отправками за период */
    @Query("SELECT COUNT(DISTINCT s.user.id) FROM PracticeSubmission s WHERE s.createdAt >= :since")
    fun countDistinctSubmittersSince(@Param("since") since: Instant): Long

    /** Таймстемпы оценённых отправок (для метрики PRD «доля REVIEWED за 48ч») */
    @Query("SELECT s.createdAt AS submittedAt, g.createdAt AS reviewedAt FROM PracticeSubmission s JOIN s.grade g")
    fun findReviewedTimestamps(): List<ReviewedTimestamps>

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
