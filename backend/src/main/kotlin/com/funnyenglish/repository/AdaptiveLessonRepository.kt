package com.funnyenglish.repository

import com.funnyenglish.entity.AdaptiveLesson
import com.funnyenglish.entity.LessonSegment
import com.funnyenglish.entity.LessonQuestionHistory
import com.funnyenglish.entity.UserSkill
import com.funnyenglish.shared.model.SkillType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Repository
interface AdaptiveLessonRepository : JpaRepository<AdaptiveLesson, UUID> {
    
    fun findByIdAndUserId(id: UUID, userId: UUID): AdaptiveLesson?
    
    fun findByUserIdAndStatus(userId: UUID, status: com.funnyenglish.shared.model.LessonStatus): List<AdaptiveLesson>
    
    @Query("SELECT al FROM AdaptiveLesson al WHERE al.userId = :userId AND al.status = 'IN_PROGRESS'")
    fun findActiveLessons(@Param("userId") userId: UUID): List<AdaptiveLesson>
    
    @Query("""
        SELECT al FROM AdaptiveLesson al 
        WHERE al.userId = :userId 
        AND al.status = 'COMPLETED' 
        ORDER BY al.completedAt DESC
    """)
    fun findRecentCompletedLessons(
        @Param("userId") userId: UUID,
        limit: Int = 10
    ): List<AdaptiveLesson>
    
    @Query("""
        SELECT COUNT(al) FROM AdaptiveLesson al 
        WHERE al.userId = :userId 
        AND al.status = 'COMPLETED'
    """)
    fun countCompletedLessons(@Param("userId") userId: UUID): Long
    
    @Query("""
        SELECT AVG(al.correctAnswers * 1.0 / al.questionsAnswered) 
        FROM AdaptiveLesson al 
        WHERE al.userId = :userId 
        AND al.status = 'COMPLETED'
        AND al.questionsAnswered > 0
    """)
    fun getAverageAccuracy(@Param("userId") userId: UUID): Double?
}

@Repository
interface LessonSegmentRepository : JpaRepository<LessonSegment, UUID> {
    fun findByLessonId(lessonId: UUID): List<LessonSegment>
}

@Repository
interface LessonQuestionHistoryRepository : JpaRepository<LessonQuestionHistory, UUID> {
    fun findByLessonId(lessonId: UUID): List<LessonQuestionHistory>
    
    @Query("""
        SELECT h FROM LessonQuestionHistory h 
        WHERE h.lesson.userId = :userId 
        ORDER BY h.answeredAt DESC
    """)
    fun findRecentHistoryByUserId(
        @Param("userId") userId: UUID,
        limit: Int = 50
    ): List<LessonQuestionHistory>
}

@Repository
interface UserSkillRepository : JpaRepository<UserSkill, UUID> {
    
    fun findByUserId(userId: UUID): List<UserSkill>
    
    fun findByUserIdAndSkillType(
        userId: UUID, 
        skillType: com.funnyenglish.shared.model.SkillType
    ): UserSkill?
    
    @Query("""
        SELECT us FROM UserSkill us 
        WHERE us.userId = :userId 
        AND us.masteryLevel < 0.7
        ORDER BY us.masteryLevel ASC
    """)
    fun findWeakAreas(@Param("userId") userId: UUID): List<UserSkill>
    
    @Query("""
        SELECT AVG(us.masteryLevel) FROM UserSkill us 
        WHERE us.userId = :userId
    """)
    fun getAverageMastery(@Param("userId") userId: UUID): Double?
    
}
