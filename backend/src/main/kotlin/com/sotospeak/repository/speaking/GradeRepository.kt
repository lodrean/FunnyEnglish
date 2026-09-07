package com.sotospeak.repository.speaking

import com.sotospeak.entity.speaking.Grade
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface GradeRepository : JpaRepository<Grade, UUID> {

    @Query("""
        SELECT g FROM Grade g
        LEFT JOIN FETCH g.reviewer
        WHERE g.submission.id = :submissionId
    """)
    fun findBySubmissionId(@Param("submissionId") submissionId: UUID): Optional<Grade>

    /** Средние по всем критериям рубрики (null, если оценок нет) — bd h3l.4, §4.2.1. */
    @Query("""
        SELECT avg(g.grammar) AS avgGrammar, avg(g.vocabulary) AS avgVocabulary,
               avg(g.pronunciation) AS avgPronunciation, avg(g.fluency) AS avgFluency,
               avg(g.total) AS avgTotal
        FROM Grade g
    """)
    fun aggregateAverages(): GradeAveragesProjection?

    /** Распределение оценок по топикам: количество и средний балл. */
    @Query("""
        SELECT s.topic.id AS topicId, s.topic.title AS topicTitle,
               COUNT(g) AS gradeCount, avg(g.total) AS avgTotal
        FROM Grade g JOIN g.submission s
        GROUP BY s.topic.id, s.topic.title
        ORDER BY COUNT(g) DESC
    """)
    fun aggregateByTopic(): List<TopicGradeDistributionProjection>
}

/** Проекция средних по критериям рубрики (bd FunnyEnglish-h3l.4, §4.2.1). */
interface GradeAveragesProjection {
    fun getAvgGrammar(): Double?
    fun getAvgVocabulary(): Double?
    fun getAvgPronunciation(): Double?
    fun getAvgFluency(): Double?
    fun getAvgTotal(): Double?
}

/** Проекция распределения оценок по топикам (bd FunnyEnglish-h3l.4, §4.2.1). */
interface TopicGradeDistributionProjection {
    fun getTopicId(): UUID
    fun getTopicTitle(): String
    fun getGradeCount(): Long
    fun getAvgTotal(): Double?
}
