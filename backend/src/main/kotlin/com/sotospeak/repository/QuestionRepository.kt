package com.sotospeak.repository

import com.sotospeak.entity.Question
import com.sotospeak.entity.QuestionType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface QuestionRepository : JpaRepository<Question, UUID> {

    /**
     * Найти вопросы по тесту с загрузкой answers (для совместимости с legacy кодом)
     */
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers WHERE q.test.id = :testId ORDER BY q.displayOrder ASC")
    fun findByTestIdWithAnswers(@Param("testId") testId: UUID): List<Question>

    /**
     * Найти вопросы по тесту (с пагинацией)
     */
    fun findByTestIdOrderByDisplayOrderAsc(testId: UUID, pageable: Pageable): Page<Question>

    /**
     * Найти вопросы по тесту (все)
     */
    fun findByTestIdOrderByDisplayOrderAsc(testId: UUID): List<Question>

    /**
     * Найти опубликованные вопросы по тесту
     */
    fun findByTestIdAndIsPublishedTrueOrderByDisplayOrderAsc(testId: UUID): List<Question>

    /**
     * Посчитать количество вопросов в тесте
     */
    fun countByTestId(testId: UUID): Long

    /**
     * Найти максимальный displayOrder для теста
     */
    @Query("SELECT MAX(q.displayOrder) FROM Question q WHERE q.test.id = :testId")
    fun findMaxDisplayOrderByTestId(@Param("testId") testId: UUID): Int?

    /**
     * Поиск вопросов по тексту (внутри JSON content)
     * Требует PostgreSQL JSONB операторов
     */
    @Query(value = """
        SELECT q.* FROM questions q 
        WHERE q.content->>'text' ILIKE CONCAT('%', :query, '%')
        OR q.title ILIKE CONCAT('%', :query, '%')
    """, nativeQuery = true)
    fun searchByText(@Param("query") query: String, pageable: Pageable): Page<Question>

    /**
     * Найти вопросы по типу
     */
    fun findByTypeAndTestId(type: QuestionType, testId: UUID): List<Question>

    /**
     * Найти следующий вопрос (по displayOrder)
     */
    @Query("""
        SELECT q FROM Question q 
        WHERE q.test.id = :testId 
        AND q.displayOrder > :currentOrder 
        AND q.isPublished = true
        ORDER BY q.displayOrder ASC 
        LIMIT 1
    """)
    fun findNextInTest(
        @Param("testId") testId: UUID,
        @Param("currentOrder") currentOrder: Int
    ): Question?

    /**
     * Найти предыдущий вопрос (по displayOrder)
     */
    @Query("""
        SELECT q FROM Question q 
        WHERE q.test.id = :testId 
        AND q.displayOrder < :currentOrder 
        AND q.isPublished = true
        ORDER BY q.displayOrder DESC 
        LIMIT 1
    """)
    fun findPreviousInTest(
        @Param("testId") testId: UUID,
        @Param("currentOrder") currentOrder: Int
    ): Question?

    /**
     * Удалить все вопросы теста (для обновления)
     */
    @Modifying
    @Query("DELETE FROM Question q WHERE q.test.id = :testId")
    fun deleteByTestId(@Param("testId") testId: UUID)
}
