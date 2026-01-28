package com.funnyenglish.controller

import com.funnyenglish.dto.AdminUserDetailResponse
import com.funnyenglish.dto.AdminUserSummaryResponse
import com.funnyenglish.dto.toResponse
import com.funnyenglish.service.AchievementService
import com.funnyenglish.service.ProgressService
import com.funnyenglish.service.UserService
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
        val users = userService.getAllUsers()
        val filtered = users.filter { user ->
            val matchesQuery = query.isNullOrBlank() ||
                user.email.contains(query, ignoreCase = true) ||
                user.displayName.contains(query, ignoreCase = true)
            val matchesRole = role.isNullOrBlank() || user.role.equals(role, ignoreCase = true)
            matchesQuery && matchesRole
        }
        val response = filtered.map { user ->
            AdminUserSummaryResponse(
                id = user.id.toString(),
                email = user.email,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
                role = user.role,
                level = user.level,
                totalPoints = user.totalPoints,
                currentStreak = user.currentStreak,
                createdAt = user.createdAt,
                stats = userService.getUserStats(user)
            )
        }
        return ResponseEntity.ok(response)
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
