package com.sotospeak.controller

import com.sotospeak.dto.AdminUserDetailResponse
import com.sotospeak.dto.AdminUserSummaryResponse
import com.sotospeak.dto.toResponse
import com.sotospeak.service.AchievementService
import com.sotospeak.service.ProgressService
import com.sotospeak.service.UserService
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
    @GetMapping
    fun getUsers(
        @RequestParam(name = "q", required = false) query: String?,
        @RequestParam(name = "role", required = false) role: String?
    ): ResponseEntity<List<AdminUserSummaryResponse>> {
        // wy7.3: фильтр в БД + статистика batch-агрегатом (контракт ответа не менялся)
        return ResponseEntity.ok(userService.getAdminUserSummaries(query, role))
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
