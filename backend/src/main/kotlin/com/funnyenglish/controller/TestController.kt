package com.funnyenglish.controller

import com.funnyenglish.dto.*
import com.funnyenglish.security.UserPrincipal
import com.funnyenglish.service.ProgressService
import com.funnyenglish.service.TestService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tests")
class TestController(
    private val testService: TestService,
    private val progressService: ProgressService
) {
    @GetMapping
    fun getAllTests(
        @AuthenticationPrincipal principal: UserPrincipal?
    ): ResponseEntity<List<TestListResponse>> {
        return ResponseEntity.ok(testService.getAllTests(principal?.userId))
    }

    @GetMapping("/{testId}")
    fun getTestById(@PathVariable testId: String): ResponseEntity<TestDetailResponse> {
        return ResponseEntity.ok(testService.getTestById(testId))
    }

    @PostMapping("/{testId}/submit")
    fun submitTest(
        @PathVariable testId: String,
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: SubmitTestRequest
    ): ResponseEntity<SubmitTestResponse> {
        val submitRequest = request.copy(testId = testId)
        return ResponseEntity.ok(progressService.submitTest(principal.userId, submitRequest))
    }
}
