package com.funnyenglish.controller

import com.funnyenglish.dto.CategoryResponse
import com.funnyenglish.dto.TestListResponse
import com.funnyenglish.security.UserPrincipal
import com.funnyenglish.service.TestService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(
    private val testService: TestService
) {
    @GetMapping
    fun getAllCategories(
        @AuthenticationPrincipal principal: UserPrincipal?
    ): ResponseEntity<List<CategoryResponse>> {
        return ResponseEntity.ok(testService.getCategories(principal?.userId))
    }

    @GetMapping("/{categoryId}/tests")
    fun getTestsByCategory(
        @PathVariable categoryId: String,
        @AuthenticationPrincipal principal: UserPrincipal?
    ): ResponseEntity<List<TestListResponse>> {
        return ResponseEntity.ok(testService.getTestsByCategory(categoryId, principal?.userId))
    }
}
