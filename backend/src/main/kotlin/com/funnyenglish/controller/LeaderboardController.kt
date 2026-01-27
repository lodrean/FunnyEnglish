package com.funnyenglish.controller

import com.funnyenglish.dto.LeaderboardResponse
import com.funnyenglish.security.UserPrincipal
import com.funnyenglish.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/leaderboard")
class LeaderboardController(
    private val userService: UserService
) {
    @GetMapping
    fun getLeaderboard(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<LeaderboardResponse> {
        return ResponseEntity.ok(
            userService.getLeaderboard(principal?.userId, limit.coerceIn(1, 100))
        )
    }
}
