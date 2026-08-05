package com.sotospeak.repository.speaking

import com.sotospeak.entity.speaking.Library
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LibraryRepository : JpaRepository<Library, UUID> {

    fun findAllByIsPublishedTrueOrderByDisplayOrderAsc(): List<Library>

    fun findAllByOrderByDisplayOrderAsc(): List<Library>

    /** Количество опубликованных и не удалённых топиков по темам: [libraryId, count] */
    @Query("""
        SELECT t.library.id, COUNT(t) FROM Topic t
        WHERE t.isPublished = true AND t.deletedAt IS NULL
        GROUP BY t.library.id
    """)
    fun countPublishedActiveTopicsByLibrary(): List<Array<Any>>
}
