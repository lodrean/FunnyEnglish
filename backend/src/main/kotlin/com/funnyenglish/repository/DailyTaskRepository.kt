package com.funnyenglish.repository

import com.funnyenglish.entity.DailyTask
import com.funnyenglish.entity.DailyTaskTemplate
import com.funnyenglish.entity.TaskType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface DailyTaskRepository : JpaRepository<DailyTask, UUID> {

    /**
     * Найти задания пользователя на конкретную дату
     */
    fun findByUserIdAndTaskDate(userId: UUID, taskDate: LocalDate): List<DailyTask>

    /**
     * Найти невыполненные задания пользователя на дату
     */
    fun findByUserIdAndTaskDateAndIsCompletedFalse(userId: UUID, taskDate: LocalDate): List<DailyTask>

    /**
     * Найти выполненные задания пользователя на дату
     */
    fun findByUserIdAndTaskDateAndIsCompletedTrue(userId: UUID, taskDate: LocalDate): List<DailyTask>

    /**
     * Проверить, есть ли у пользователя задания на дату
     */
    fun existsByUserIdAndTaskDate(userId: UUID, taskDate: LocalDate): Boolean

    /**
     * Найти задания по типу и дате
     */
    fun findByTaskTypeAndTaskDate(taskType: TaskType, taskDate: LocalDate): List<DailyTask>

    /**
     * Найти все задания пользователя за период
     */
    @Query("""
        SELECT dt FROM DailyTask dt 
        WHERE dt.user.id = :userId 
        AND dt.taskDate BETWEEN :startDate AND :endDate
        ORDER BY dt.taskDate DESC
    """)
    fun findByUserIdAndDateRange(
        @Param("userId") userId: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<DailyTask>

    /**
     * Посчитать выполненные задания пользователя
     */
    fun countByUserIdAndIsCompletedTrue(userId: UUID): Long

    /**
     * Найти задания для обновления (невыполненные на сегодня)
     */
    @Query("""
        SELECT dt FROM DailyTask dt 
        WHERE dt.user.id = :userId 
        AND dt.taskDate = :taskDate 
        AND dt.isCompleted = false
    """)
    fun findIncompleteByUserAndDate(
        @Param("userId") userId: UUID,
        @Param("taskDate") taskDate: LocalDate
    ): List<DailyTask>
}

@Repository
interface DailyTaskTemplateRepository : JpaRepository<DailyTaskTemplate, UUID> {

    /**
     * Найти активные шаблоны
     */
    fun findByIsActiveTrueOrderByPriorityAsc(): List<DailyTaskTemplate>

    /**
     * Найти шаблоны по типу
     */
    fun findByTaskTypeAndIsActiveTrue(taskType: TaskType): List<DailyTaskTemplate>
}
