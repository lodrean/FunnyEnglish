package com.funnyenglish.controller

import com.funnyenglish.dto.SubmitTestRequest
import com.funnyenglish.dto.SubmitTestResponse
import com.funnyenglish.service.TestValidationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Public endpoints for guest users to validate test answers without authentication.
 * No data is persisted - this is read-only validation only.
 */
@RestController
@RequestMapping("/public/tests")
class PublicTestController(
    private val testValidationService: TestValidationService
) {
    @PostMapping("/{testId}/validate")
    fun validateTest(
        @PathVariable testId: String,
        @Valid @RequestBody request: SubmitTestRequest
    ): ResponseEntity<SubmitTestResponse> {
        val validation = testValidationService.validateTest(
            UUID.fromString(testId),
            request.answers
        )
        return ResponseEntity.ok(
            SubmitTestResponse(
                score = validation.score,
                maxScore = validation.maxScore,
                percentage = validation.percentage,
                stars = validation.stars,
                pointsEarned = 0,
                isNewBestScore = false,
                newAchievements = emptyList(),
                levelUp = null
            )
        )
    }
}
