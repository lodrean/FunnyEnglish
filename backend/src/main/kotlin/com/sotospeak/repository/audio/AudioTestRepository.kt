package com.sotospeak.repository.audio

import com.sotospeak.entity.audio.AudioTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface AudioTestRepository : JpaRepository<AudioTest, UUID> {

    @Query("""
        SELECT at FROM AudioTest at
        LEFT JOIN FETCH at.category
        WHERE at.isPublished = true
        AND (:categoryId IS NULL OR at.category.id = :categoryId)
        AND (:difficulty IS NULL OR at.difficulty = :difficulty)
        ORDER BY at.createdAt DESC
    """)
    fun findPublishedAudioTests(
        @Param("categoryId") categoryId: UUID?,
        @Param("difficulty") difficulty: Int?,
        pageable: Pageable
    ): Page<AudioTest>

    @Query("""
        SELECT at FROM AudioTest at
        LEFT JOIN FETCH at.category
        LEFT JOIN FETCH at.questions q
        LEFT JOIN FETCH q.answers
        WHERE at.id = :id
    """)
    fun findByIdWithDetails(@Param("id") id: UUID): Optional<AudioTest>

    @Query("""
        SELECT at FROM AudioTest at
        LEFT JOIN FETCH at.category
        LEFT JOIN FETCH at.questions q
        LEFT JOIN FETCH q.answers
        WHERE at.id = :id AND at.isPublished = true
    """)
    fun findPublishedByIdWithDetails(@Param("id") id: UUID): Optional<AudioTest>

    @Query("""
        SELECT at FROM AudioTest at
        LEFT JOIN FETCH at.category
        WHERE at.isPublished = true
        ORDER BY at.difficulty ASC, at.createdAt DESC
    """)
    fun findAllPublishedWithCategory(): List<AudioTest>

    @Query("""
        SELECT COUNT(at) FROM AudioTest at
        WHERE at.isPublished = true
        AND (:categoryId IS NULL OR at.category.id = :categoryId)
    """)
    fun countPublishedByCategory(@Param("categoryId") categoryId: UUID?): Long

    fun existsByTitleIgnoreCase(title: String): Boolean

    // EntityGraph: admin-список маппит category в AudioTestResponse — без fetch был N+1
    // (коллекции questions/transcripts намеренно не фетчатся: Page + join-fetch коллекции =
    // in-memory пагинация; их догружает hibernate.default_batch_fetch_size)
    @EntityGraph(attributePaths = ["category"])
    override fun findAll(pageable: Pageable): Page<AudioTest>
}
