package com.funnyenglish.controller

import com.funnyenglish.dto.*
import com.funnyenglish.security.UserPrincipal
import com.funnyenglish.service.AchievementService
import com.funnyenglish.service.ProgressService
import com.funnyenglish.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val progressService: ProgressService,
    private val achievementService: AchievementService
) {
    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<UserResponse> {
        val user = userService.getUserById(principal.userId)
        return ResponseEntity.ok(user.toResponse())
    }

    @GetMapping("/me/profile")
    fun getCurrentUserProfile(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<UserProfileResponse> {
        return ResponseEntity.ok(userService.getUserProfile(principal.userId))
    }

    @GetMapping("/me/progress")
    fun getCurrentUserProgress(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<ProgressResponse>> {
        return ResponseEntity.ok(progressService.getUserProgress(principal.userId))
    }

    @GetMapping("/me/progress/summary")
    fun getCurrentUserProgressSummary(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<UserProgressSummary> {
        return ResponseEntity.ok(progressService.getUserProgressSummary(principal.userId))
    }

    @GetMapping("/me/achievements")
    fun getCurrentUserAchievements(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<AchievementResponse>> {
        return ResponseEntity.ok(achievementService.getUserAchievements(principal.userId))
    }
}
