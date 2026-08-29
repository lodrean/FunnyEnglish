package com.sotospeak.repository.audio

import com.sotospeak.entity.audio.AudioTestProgress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface AudioTestProgressRepository : JpaRepository<AudioTestProgress, UUID> {

    @Query("""
        SELECT p FROM AudioTestProgress p
        LEFT JOIN FETCH p.audioTest at
        LEFT JOIN FETCH at.category
        WHERE p.user.id = :userId
        ORDER BY p.lastAttemptAt DESC
    """)
    fun findByUserIdWithAudioTest(@Param("userId") userId: UUID): List<AudioTestProgress>

    @Query("""
        SELECT p FROM AudioTestProgress p
        LEFT JOIN FETCH p.audioTest
        WHERE p.user.id = :userId AND p.audioTest.id = :audioTestId
    """)
    fun findByUserIdAndAudioTestId(
        @Param("userId") userId: UUID,
        @Param("audioTestId") audioTestId: UUID
    ): Optional<AudioTestProgress>

    fun existsByUserIdAndAudioTestId(userId: UUID, audioTestId: UUID): Boolean

    @Query("""
        SELECT COALESCE(SUM(p.score), 0) FROM AudioTestProgress p
        WHERE p.user.id = :userId
    """)
    fun getTotalScoreByUserId(@Param("userId") userId: UUID): Long

    @Query("""
        SELECT COALESCE(SUM(p.stars), 0) FROM AudioTestProgress p
        WHERE p.user.id = :userId
    """)
    fun getTotalStarsByUserId(@Param("userId") userId: UUID): Long

    @Query("""
        SELECT COUNT(p) FROM AudioTestProgress p
        WHERE p.user.id = :userId AND p.completedAt IS NOT NULL
    """)
    fun countCompletedByUserId(@Param("userId") userId: UUID): Long

    @Query("""
        SELECT p FROM AudioTestProgress p
        LEFT JOIN FETCH p.audioTest
        WHERE p.user.id = :userId
        AND p.bestScore = (SELECT MAX(p2.bestScore) FROM AudioTestProgress p2 WHERE p2.user.id = :userId)
    """)
    fun findBestScoreProgressByUserId(@Param("userId") userId: UUID): Optional<AudioTestProgress>
}
