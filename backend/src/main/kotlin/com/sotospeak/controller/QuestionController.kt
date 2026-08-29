package com.sotospeak.controller

import com.sotospeak.dto.*
import com.sotospeak.entity.QuestionType
import com.sotospeak.service.QuestionService
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * ВРЕМЕННО УПРОЩЁННАЯ ВЕРСИЯ
 * 
 * NOTE: Некоторые endpoints отключены из-за JSONB миграции.
 * Будут восстановлены после полной миграции данных.
 */
@Profile("legacy")
@RestController
@RequestMapping("/questions")
class QuestionController(
    private val questionService: QuestionService
) {

    // ============ Admin Endpoints ============

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createQuestion(@RequestBody request: QuestionCreateRequest): ResponseEntity<QuestionResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(questionService.createQuestion(request))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateQuestion(
        @PathVariable id: UUID,
        @RequestBody request: QuestionUpdateRequest
    ): ResponseEntity<QuestionResponse> {
        return ResponseEntity.ok(questionService.updateQuestion(id, request))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteQuestion(@PathVariable id: UUID): ResponseEntity<Void> {
        questionService.deleteQuestion(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun getQuestionForAdmin(@PathVariable id: UUID): ResponseEntity<QuestionResponse> {
        return ResponseEntity.ok(questionService.getQuestionById(id))
    }

    @GetMapping("/test/{testId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getQuestionsByTest(@PathVariable testId: UUID): ResponseEntity<List<QuestionResponse>> {
        return ResponseEntity.ok(questionService.getQuestionsByTest(testId))
    }
    
    @GetMapping("/test/{testId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    fun getQuestionsByTestWithDetails(@PathVariable testId: UUID): ResponseEntity<List<QuestionDetailResponse>> {
        return ResponseEntity.ok(questionService.getQuestionsByTestWithDetails(testId))
    }

    @PostMapping("/reorder/{testId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun reorderQuestions(
        @PathVariable testId: UUID,
        @RequestBody request: ReorderQuestionsRequest
    ): ResponseEntity<Void> {
        questionService.reorderQuestions(testId, request.questionIds)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasRole('ADMIN')")
    fun duplicateQuestion(@PathVariable id: UUID): ResponseEntity<QuestionResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(questionService.duplicateQuestion(id))
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN')")
    fun validateContent(
        @RequestParam type: QuestionType,
        @RequestBody content: com.fasterxml.jackson.databind.JsonNode
    ): ResponseEntity<Map<String, Boolean>> {
        val isValid = questionService.validateContent(type, content)
        return ResponseEntity.ok(mapOf("valid" to isValid))
    }

    // ============ Public/User Endpoints ============

    @GetMapping("/{id}")
    fun getQuestionForUser(@PathVariable id: UUID): ResponseEntity<QuestionPublicResponse> {
        return ResponseEntity.ok(questionService.getQuestionForUser(id))
    }

    @GetMapping("/test/{testId}/published")
    fun getPublishedQuestionsByTest(@PathVariable testId: UUID): ResponseEntity<List<QuestionPublicResponse>> {
        return ResponseEntity.ok(questionService.getPublishedQuestionsByTest(testId))
    }
    
    // ============ IMAGE_WORD_MATCH Endpoints ============
    
    @PostMapping("/image-word-match")
    @PreAuthorize("hasRole('ADMIN')")
    fun createImageWordMatchQuestion(
        @RequestBody request: CreateImageWordMatchRequest
    ): ResponseEntity<ImageWordMatchQuestionResponse> {
        val question = questionService.createImageWordMatchQuestion(request)
        // Возвращаем заглушку пока JSONB отключен
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ImageWordMatchQuestionResponse(
                id = question.id.toString(),
                type = QuestionType.IMAGE_WORD_MATCH,
                instruction = question.title,
                points = question.points,
                imageUrl = question.imageUrl ?: "",
                words = emptyList(),
                hotspots = emptyList()
            )
        )
    }
    
    @PutMapping("/image-word-match/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateImageWordMatchQuestion(
        @PathVariable id: UUID,
        @RequestBody request: CreateImageWordMatchRequest
    ): ResponseEntity<ImageWordMatchQuestionResponse> {
        val question = questionService.updateImageWordMatchQuestion(id, request)
        return ResponseEntity.ok(
            ImageWordMatchQuestionResponse(
                id = question.id.toString(),
                type = QuestionType.IMAGE_WORD_MATCH,
                instruction = question.title,
                points = question.points,
                imageUrl = question.imageUrl ?: "",
                words = emptyList(),
                hotspots = emptyList()
            )
        )
    }
    
    @GetMapping("/image-word-match/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getImageWordMatchQuestion(@PathVariable id: UUID): ResponseEntity<ImageWordMatchQuestionResponse> {
        return ResponseEntity.ok(questionService.getImageWordMatchQuestionForAdmin(id))
    }
    
    @PostMapping("/image-word-match/{id}/validate")
    fun validateImageWordMatchAnswer(
        @PathVariable id: UUID,
        @RequestBody request: SubmitImageWordMatchAnswerRequest
    ): ResponseEntity<ImageWordMatchResultResponse> {
        val result = questionService.validateImageWordMatchAnswer(id, request.matches)
        return ResponseEntity.ok(result)
    }
}
