package com.funnyenglish.repository

import com.funnyenglish.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ImageWordMatchQuestionRepository : JpaRepository<ImageWordMatchQuestionEntity, UUID> {
    fun findByQuestionId(questionId: UUID): ImageWordMatchQuestionEntity?
    fun findByTestId(testId: UUID): List<ImageWordMatchQuestionEntity>
    fun deleteByQuestionId(questionId: UUID)
}

@Repository
interface ImageWordMatchWordRepository : JpaRepository<ImageWordMatchWordEntity, UUID> {
    fun findByQuestionId(questionId: UUID): List<ImageWordMatchWordEntity>
    fun deleteByQuestionId(questionId: UUID)
}

@Repository
interface ImageWordMatchHotspotRepository : JpaRepository<ImageWordMatchHotspotEntity, UUID> {
    fun findByQuestionId(questionId: UUID): List<ImageWordMatchHotspotEntity>
    fun deleteByQuestionId(questionId: UUID)
}
