package com.sotospeak.controller

import com.sotospeak.dto.AdminUserDetailResponse
import com.sotospeak.dto.AdminUserSummaryResponse
import com.sotospeak.dto.toResponse
import com.sotospeak.service.AchievementService
import com.sotospeak.service.ProgressService
import com.sotospeak.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/users")
class AdminUserController(
    private val userService: UserService,
    private val progressService: ProgressService,
    private val achievementService: AchievementService
) {
    private companion object {
        const val DEFAULT_PAGE = 0
        const val MIN_PAGE_SIZE = 1
        const val MAX_PAGE_SIZE = 100
    }

    @GetMapping
    fun getUsers(
        @RequestParam(name = "q", required = false) query: String?,
        @RequestParam(name = "role", required = false) role: String?,
        @RequestParam(name = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(name = "size", required = false, defaultValue = "50") size: Int,
    ): ResponseEntity<Page<AdminUserSummaryResponse>> {
        // wy7.6: серверная пагинация (Page-контракт; сортировка createdAt DESC)
        val safePage = page.coerceAtLeast(DEFAULT_PAGE)
        val safeSize = size.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE)
        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        return ResponseEntity.ok(userService.getAdminUserSummaries(query, role, pageable))
    }

    @GetMapping("/{userId}")
    fun getUserDetail(@PathVariable userId: String): ResponseEntity<AdminUserDetailResponse> {
        val user = userService.getUserById(userId)
        val stats = userService.getUserStats(user)
        val achievements = achievementService.getUserAchievements(userId)
        val progressSummary = progressService.getUserProgressSummary(userId)
        val progress = progressService.getUserProgress(userId)

        return ResponseEntity.ok(
            AdminUserDetailResponse(
                user = user.toResponse(),
                stats = stats,
                achievements = achievements,
                progressSummary = progressSummary,
                progress = progress
            )
        )
    }
}
