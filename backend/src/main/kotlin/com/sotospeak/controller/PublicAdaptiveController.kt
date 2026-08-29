package com.sotospeak.controller

import com.sotospeak.service.PublicAdaptiveService
import com.sotospeak.shared.model.AdaptiveLessonState
import com.sotospeak.shared.model.FeedbackResponse
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Public endpoints for guest adaptive lessons.
 * No authentication required - returns read-only lesson content.
 */
@Profile("legacy")
@RestController
@RequestMapping("/public/adaptive")
class PublicAdaptiveController(
    private val publicAdaptiveService: PublicAdaptiveService
) {

    @GetMapping("/random-lesson")
    fun getRandomLessonContent(
        @RequestParam(required = false) categoryId: String?,
        @RequestParam(required = false, defaultValue = "5") duration: Int
    ): ResponseEntity<AdaptiveLessonState> {
        return ResponseEntity.ok(
            publicAdaptiveService.createRandomLesson(categoryId, duration)
        )
    }

    @PostMapping("/validate-answer")
    fun validateAnswer(
        @RequestBody request: ValidateAnswerRequest
    ): ResponseEntity<FeedbackResponse> {
        return ResponseEntity.ok(
            publicAdaptiveService.validateAnswer(request.questionId, request.answerId)
        )
    }
}

data class ValidateAnswerRequest(
    val questionId: String,
    val answerId: String
)
