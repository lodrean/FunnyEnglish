package com.funnyenglish.repository

import com.funnyenglish.entity.Lesson
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LessonRepository : JpaRepository<Lesson, UUID> {

    /**
     * Найти уроки по категории
     */
    fun findByCategoryIdAndIsPublishedTrue(categoryId: UUID): List<Lesson>

    /**
     * Найти все опубликованные уроки
     */
    fun findAllByIsPublishedTrueOrderByDisplayOrderAsc(): List<Lesson>

    /**
     * Найти уроки по ID категории
     */
    @Query("""
        SELECT l FROM Lesson l 
        WHERE l.category.id = :categoryId 
        AND l.isPublished = true 
        ORDER BY l.displayOrder ASC
    """)
    fun findPublishedByCategoryId(@Param("categoryId") categoryId: UUID): List<Lesson>

    /**
     * Найти уроки с displayOrder больше указанного
     */
    fun findByDisplayOrderGreaterThanOrderByDisplayOrderAsc(displayOrder: Int): List<Lesson>

    /**
     * Поиск уроков по названию
     */
    @Query("""
        SELECT l FROM Lesson l 
        WHERE LOWER(l.title) LIKE LOWER(CONCAT('%', :query, '%')) 
        AND l.isPublished = true
    """)
    fun searchByTitle(@Param("query") query: String): List<Lesson>
}

@Repository
interface LearningPathRepository : JpaRepository<com.funnyenglish.entity.LearningPath, UUID> {

    /**
     * Найти активный путь обучения
     */
    fun findByIsActiveTrue(): List<com.funnyenglish.entity.LearningPath>

    /**
     * Найти первый активный путь (для использования по умолчанию)
     */
    fun findFirstByIsActiveTrue(): com.funnyenglish.entity.LearningPath?
}

@Repository
interface PathLessonRepository : JpaRepository<com.funnyenglish.entity.PathLesson, UUID> {

    /**
     * Найти уроки в пути по ID пути
     */
    fun findByLearningPathIdOrderByDisplayOrderAsc(pathId: UUID): List<com.funnyenglish.entity.PathLesson>

    /**
     * Найти следующий урок в пути
     */
    fun findByLearningPathIdAndDisplayOrder(learningPathId: UUID, displayOrder: Int): com.funnyenglish.entity.PathLesson?
}

@Repository
interface UserPathProgressRepository : JpaRepository<com.funnyenglish.entity.UserPathProgress, UUID> {

    /**
     * Найти прогресс пользователя по пути
     */
    fun findByUserIdAndLearningPathId(userId: UUID, pathId: UUID): com.funnyenglish.entity.UserPathProgress?

    /**
     * Найти все прогрессы пользователя
     */
    fun findByUserId(userId: UUID): List<com.funnyenglish.entity.UserPathProgress>

    /**
     * Найти прогресс пользователя по активному пути
     */
    @Query("""
        SELECT upp FROM UserPathProgress upp 
        JOIN upp.learningPath lp 
        WHERE upp.user.id = :userId AND lp.isActive = true
    """)
    fun findActiveByUserId(@Param("userId") userId: UUID): com.funnyenglish.entity.UserPathProgress?
}

@Repository
interface CompletedLessonRepository : JpaRepository<com.funnyenglish.entity.CompletedLesson, UUID> {

    /**
     * Найти завершенные уроки пользователя
     */
    fun findByUserId(userId: UUID): List<com.funnyenglish.entity.CompletedLesson>

    /**
     * Проверить, завершил ли пользователь урок
     */
    fun existsByUserIdAndLessonId(userId: UUID, lessonId: UUID): Boolean

    /**
     * Найти завершенный урок
     */
    fun findByUserIdAndLessonId(userId: UUID, lessonId: UUID): com.funnyenglish.entity.CompletedLesson?

    /**
     * Посчитать количество завершенных уроков пользователем
     */
    fun countByUserId(userId: UUID): Long
}
