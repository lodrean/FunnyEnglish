package com.sotospeak.controller

import com.sotospeak.dto.*
import com.sotospeak.service.AdaptiveLessonService
import com.sotospeak.service.DifficultyEngine
import com.sotospeak.shared.model.DifficultyLevel
import com.sotospeak.shared.model.SkillType
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST API для адаптивных уроков
 */
@RestController
@RequestMapping("/api/v1/adaptive-lessons")
class AdaptiveLessonController(
    private val adaptiveLessonService: AdaptiveLessonService,
    private val difficultyEngine: DifficultyEngine
) {

    /**
     * Начать новый адаптивный урок
     */
    @PostMapping("/start")
    fun startLesson(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: StartAdaptiveLessonRequest
    ): ResponseEntity<StartAdaptiveLessonResponse> {
        val userId = UUID.fromString(userDetails.username)
        val response = adaptiveLessonService.startLesson(
            userId = userId,
            categoryId = request.categoryId,
            skillType = request.skillType,
            targetDurationMinutes = request.duration
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Получить следующий вопрос с адаптивной сложностью
     */
    @GetMapping("/{lessonId}/next")
    fun getNextQuestion(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable lessonId: UUID
    ): ResponseEntity<NextQuestionResponse> {
        val userId = UUID.fromString(userDetails.username)
        val response = adaptiveLessonService.getNextQuestion(userId, lessonId)
        return ResponseEntity.ok(response)
    }

    /**
     * Отправить ответ и получить feedback
     */
    @PostMapping("/{lessonId}/answer")
    fun submitAnswer(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable lessonId: UUID,
        @Valid @RequestBody request: SubmitAdaptiveAnswerRequest
    ): ResponseEntity<SubmitAnswerResponse> {
        val userId = UUID.fromString(userDetails.username)
        val response = adaptiveLessonService.submitAnswer(
            userId = userId,
            lessonId = lessonId,
            questionId = request.questionId,
            answerId = request.answerId,
            timeSpentSeconds = request.timeSpent
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Запросить перерыв (после 10 минут)
     */
    @PostMapping("/{lessonId}/break")
    fun requestBreak(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable lessonId: UUID
    ): ResponseEntity<BreakResponse> {
        val userId = UUID.fromString(userDetails.username)
        val response = adaptiveLessonService.requestBreak(userId, lessonId)
        return ResponseEntity.ok(response)
    }

    /**
     * Продолжить урок после перерыва
     */
    @PostMapping("/{lessonId}/resume")
    fun resumeLesson(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable lessonId: UUID
    ): ResponseEntity<ResumeLessonResponse> {
        val userId = UUID.fromString(userDetails.username)
        val response = adaptiveLessonService.resumeLesson(userId, lessonId)
        return ResponseEntity.ok(response)
    }

    /**
     * Завершить урок
     */
    @PostMapping("/{lessonId}/complete")
    fun completeLesson(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable lessonId: UUID
    ): ResponseEntity<CompleteLessonResponse> {
        val userId = UUID.fromString(userDetails.username)
        val response = adaptiveLessonService.completeLesson(userId, lessonId)
        return ResponseEntity.ok(response)
    }

    /**
     * Получить текущее состояние урока
     */
    @GetMapping("/{lessonId}/state")
    fun getLessonState(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable lessonId: UUID
    ): ResponseEntity<LessonStateResponse> {
        val userId = UUID.fromString(userDetails.username)
        val response = adaptiveLessonService.getLessonState(userId, lessonId)
        return ResponseEntity.ok(response)
    }

    /**
     * Получить историю слабых навыков пользователя
     */
    @GetMapping("/weak-areas")
    fun getWeakAreas(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<WeakAreasResponse> {
        val userId = UUID.fromString(userDetails.username)
        val weakAreas = adaptiveLessonService.getWeakAreas(userId)
        return ResponseEntity.ok(WeakAreasResponse(weakAreas))
    }

    /**
     * Получить рекомендацию следующего урока
     */
    @GetMapping("/recommendation")
    fun getRecommendation(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<LessonRecommendationResponse> {
        val userId = UUID.fromString(userDetails.username)
        val recommendation = adaptiveLessonService.getRecommendation(userId)
        return ResponseEntity.ok(LessonRecommendationResponse(recommendation))
    }
}
