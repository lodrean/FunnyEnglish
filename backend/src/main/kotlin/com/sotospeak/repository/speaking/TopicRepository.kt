package com.sotospeak.repository.speaking

import com.sotospeak.entity.speaking.Topic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface TopicRepository : JpaRepository<Topic, UUID> {

    @Query("""
        SELECT t FROM Topic t
        LEFT JOIN FETCH t.video
        LEFT JOIN FETCH t.questions
        WHERE t.id = :id AND t.isPublished = true AND t.deletedAt IS NULL
        AND t.library.isPublished = true
    """)
    fun findPublishedActiveByIdWithDetails(@Param("id") id: UUID): Optional<Topic>

    @Query("""
        SELECT t FROM Topic t
        LEFT JOIN FETCH t.video
        WHERE t.library.id = :libraryId AND t.isPublished = true AND t.deletedAt IS NULL
        AND t.library.isPublished = true
        ORDER BY t.displayOrder ASC
    """)
    fun findPublishedActiveByLibraryIdWithVideo(@Param("libraryId") libraryId: UUID): List<Topic>

    fun findByIdAndIsPublishedTrueAndDeletedAtIsNull(id: UUID): Optional<Topic>

    @Query("""
        SELECT t FROM Topic t
        LEFT JOIN FETCH t.video
        LEFT JOIN FETCH t.questions
        WHERE t.id = :id
    """)
    fun findByIdWithDetails(@Param("id") id: UUID): Optional<Topic>

    /** Все топики темы, включая soft-deleted (admin видит архив) */
    fun findByLibraryIdOrderByDisplayOrderAsc(libraryId: UUID): List<Topic>

    /** Количество вопросов по топикам: [topicId, count] */
    @Query("""
        SELECT q.topic.id, COUNT(q) FROM SpeakingQuestion q
        WHERE q.topic.id IN :topicIds
        GROUP BY q.topic.id
    """)
    fun countQuestionsByTopicIds(@Param("topicIds") topicIds: List<UUID>): List<Array<Any>>
}
