package com.funnyenglish.repository.audio

import com.funnyenglish.entity.audio.AudioTestQuestion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AudioTestQuestionRepository : JpaRepository<AudioTestQuestion, UUID> {

    @Query("""
        SELECT q FROM AudioTestQuestion q
        LEFT JOIN FETCH q.answers
        WHERE q.audioTest.id = :audioTestId
        ORDER BY q.displayOrder ASC, q.startTimeSeconds ASC
    """)
    fun findByAudioTestIdOrderByTime(@Param("audioTestId") audioTestId: UUID): List<AudioTestQuestion>

    @Query("""
        SELECT q FROM AudioTestQuestion q
        LEFT JOIN FETCH q.answers
        WHERE q.audioTest.id = :audioTestId
        AND :timeSeconds BETWEEN q.startTimeSeconds AND q.endTimeSeconds
        ORDER BY q.displayOrder ASC
    """)
    fun findActiveAtTime(
        @Param("audioTestId") audioTestId: UUID,
        @Param("timeSeconds") timeSeconds: Int
    ): List<AudioTestQuestion>

    fun countByAudioTestId(audioTestId: UUID): Long

    @Query("""
        SELECT COALESCE(MAX(q.displayOrder), 0) FROM AudioTestQuestion q
        WHERE q.audioTest.id = :audioTestId
    """)
    fun findMaxDisplayOrder(@Param("audioTestId") audioTestId: UUID): Int

    @Query("""
        SELECT q FROM AudioTestQuestion q
        WHERE q.audioTest.id = :audioTestId
        AND (
            (:startTime BETWEEN q.startTimeSeconds AND q.endTimeSeconds)
            OR (:endTime BETWEEN q.startTimeSeconds AND q.endTimeSeconds)
            OR (q.startTimeSeconds BETWEEN :startTime AND :endTime)
        )
    """)
    fun findOverlappingTimeRanges(
        @Param("audioTestId") audioTestId: UUID,
        @Param("startTime") startTime: Int,
        @Param("endTime") endTime: Int
    ): List<AudioTestQuestion>
}
