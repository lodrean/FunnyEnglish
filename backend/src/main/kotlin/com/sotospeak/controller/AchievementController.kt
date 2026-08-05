package com.sotospeak.controller

import com.sotospeak.dto.AchievementResponse
import com.sotospeak.security.UserPrincipal
import com.sotospeak.service.AchievementService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/achievements")
class AchievementController(
    private val achievementService: AchievementService
) {
    @GetMapping
    fun getAllAchievements(
        @AuthenticationPrincipal principal: UserPrincipal?
    ): ResponseEntity<List<AchievementResponse>> {
        return ResponseEntity.ok(achievementService.getAllAchievements(principal?.userId))
    }
}
