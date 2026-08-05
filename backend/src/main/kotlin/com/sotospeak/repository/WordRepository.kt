package com.sotospeak.repository

import com.sotospeak.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WordRepository : JpaRepository<Word, UUID> {

    /**
     * Поиск по слову (case insensitive, partial match)
     */
    @Query("""
        SELECT w FROM Word w 
        WHERE LOWER(w.word) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(w.translation) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    fun searchByWordOrTranslation(
        @Param("query") query: String,
        pageable: Pageable
    ): Page<Word>

    /**
     * Поиск с использованием PostgreSQL FTS (будет работать если настроен индекс)
     */
    @Query(value = """
        SELECT * FROM words 
        WHERE to_tsvector('english', word || ' ' || COALESCE(translation, '')) 
        @@ plainto_tsquery('english', :query)
    """, nativeQuery = true)
    fun fullTextSearch(@Param("query") query: String, pageable: Pageable): Page<Word>

    /**
     * Найти слова по сложности
     */
    fun findByDifficulty(difficulty: WordDifficulty, pageable: Pageable): Page<Word>

    /**
     * Найти слова по категории
     */
    fun findByCategoryIgnoreCase(category: String, pageable: Pageable): Page<Word>

    /**
     * Найти слова по части речи
     */
    fun findByPartOfSpeech(partOfSpeech: PartOfSpeech, pageable: Pageable): Page<Word>

    /**
     * Найти слова по сложности и категории
     */
    fun findByDifficultyAndCategory(difficulty: WordDifficulty, category: String, pageable: Pageable): Page<Word>

    /**
     * Поиск слов для начинающих (простые слова)
     */
    fun findTop50ByDifficultyOrderByWordAsc(difficulty: WordDifficulty): List<Word>

    /**
     * Случайные слова для практики
     */
    @Query(value = "SELECT * FROM words ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    fun findRandomWords(@Param("limit") limit: Int): List<Word>

    /**
     * Случайные слова по сложности
     */
    @Query(value = """
        SELECT * FROM words 
        WHERE difficulty = :difficulty 
        ORDER BY RANDOM() LIMIT :limit
    """, nativeQuery = true)
    fun findRandomWordsByDifficulty(
        @Param("difficulty") difficulty: String,
        @Param("limit") limit: Int
    ): List<Word>

    /**
     * Проверить существование слова
     */
    fun existsByWordIgnoreCase(word: String): Boolean

    /**
     * Найти слово (точное совпадение)
     */
    fun findByWordIgnoreCase(word: String): Word?
}

@Repository
interface UserWordRepository : JpaRepository<UserWord, UUID> {

    /**
     * Найти слова пользователя по статусу
     */
    fun findByUserIdAndStatus(userId: UUID, status: UserWordStatus, pageable: Pageable): Page<UserWord>

    /**
     * Найти все слова пользователя
     */
    fun findByUserId(userId: UUID, pageable: Pageable): Page<UserWord>

    /**
     * Найти конкретное слово пользователя
     */
    fun findByUserIdAndWordId(userId: UUID, wordId: UUID): UserWord?

    /**
     * Проверить, есть ли слово у пользователя
     */
    fun existsByUserIdAndWordId(userId: UUID, wordId: UUID): Boolean

    /**
     * Посчитать слова по статусу
     */
    fun countByUserIdAndStatus(userId: UUID, status: UserWordStatus): Long

    /**
     * Найти слова для повторения (изученные, но давно не повторявшиеся)
     */
    @Query("""
        SELECT uw FROM UserWord uw 
        WHERE uw.user.id = :userId 
        AND (uw.status = 'LEARNED' OR uw.status = 'HARD')
        ORDER BY uw.lastReviewedAt ASC NULLS FIRST
    """)
    fun findWordsForReview(@Param("userId") userId: UUID, pageable: Pageable): Page<UserWord>

    /**
     * Найти сложные слова пользователя
     */
    fun findByUserIdAndStatusOrderByAddedAtDesc(userId: UUID, status: UserWordStatus): List<UserWord>

    /**
     * Посчитать общее количество слов пользователя
     */
    fun countByUserId(userId: UUID): Long

    /**
     * Найти слова с прогрессом меньше указанного
     */
    fun findByUserIdAndProgressLessThan(userId: UUID, progress: Int, pageable: Pageable): Page<UserWord>
}
