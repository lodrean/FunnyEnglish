package com.sotospeak.controller

import com.sotospeak.dto.*
import com.sotospeak.security.UserPrincipal
import com.sotospeak.service.ProgressService
import com.sotospeak.service.TestService
import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Profile("legacy")
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
    
    @GetMapping("/{testId}/details")
    fun getTestWithDetails(@PathVariable testId: String): ResponseEntity<TestDetailResponse> {
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
