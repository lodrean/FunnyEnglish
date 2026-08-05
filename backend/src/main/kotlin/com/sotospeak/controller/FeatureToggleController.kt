package com.sotospeak.controller

import com.sotospeak.security.UserPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Controller for feature toggle management
 */
@RestController
@RequestMapping("/api/features")
class FeatureToggleController {

    /**
     * Get feature toggles for the current user
     * This allows A/B testing and gradual rollouts
     */
    @GetMapping("/toggles")
    fun getFeatureToggles(
        @AuthenticationPrincipal principal: UserPrincipal?
    ): ResponseEntity<Map<String, Boolean>> {
        val userId = principal?.userId ?: "anonymous"
        
        // Determine toggles based on user
        val toggles = determineTogglesForUser(userId)
        
        return ResponseEntity.ok(toggles)
    }

    /**
     * Get all available features with their descriptions
     */
    @GetMapping("/list")
    fun getAvailableFeatures(): ResponseEntity<List<FeatureInfo>> {
        val features = listOf(
            FeatureInfo(
                key = "learning.adaptive",
                name = "Adaptive Learning",
                description = "ML-based adaptive learning algorithm",
                defaultValue = false,
                category = "Learning"
            ),
            FeatureInfo(
                key = "learning.micro",
                name = "Micro Lessons",
                description = "5-7 minute micro-lessons",
                defaultValue = true,
                category = "Learning"
            ),
            FeatureInfo(
                key = "gamification.streaks",
                name = "Daily Streaks",
                description = "Daily streak tracking",
                defaultValue = true,
                category = "Gamification"
            ),
            FeatureInfo(
                key = "gamification.achievements",
                name = "Achievements",
                description = "Achievement system",
                defaultValue = true,
                category = "Gamification"
            ),
            FeatureInfo(
                key = "gamification.daily_quests",
                name = "Daily Quests",
                description = "Daily quest system",
                defaultValue = false,
                category = "Gamification"
            ),
            FeatureInfo(
                key = "social.groups",
                name = "Student Groups",
                description = "Student groups and classes",
                defaultValue = true,
                category = "Social"
            ),
            FeatureInfo(
                key = "social.friends",
                name = "Friends",
                description = "Friends system",
                defaultValue = false,
                category = "Social"
            ),
            FeatureInfo(
                key = "social.chat",
                name = "Chat",
                description = "In-app chat",
                defaultValue = false,
                category = "Social"
            ),
            FeatureInfo(
                key = "content.video",
                name = "Video Lessons",
                description = "Video lesson content",
                defaultValue = false,
                category = "Content"
            ),
            FeatureInfo(
                key = "content.audio",
                name = "Audio Lessons",
                description = "Audio lesson content",
                defaultValue = false,
                category = "Content"
            ),
            FeatureInfo(
                key = "ui.dark_mode",
                name = "Dark Mode",
                description = "Dark theme support",
                defaultValue = true,
                category = "UI"
            ),
            FeatureInfo(
                key = "ui.animations",
                name = "Animations",
                description = "UI animations and micro-interactions",
                defaultValue = true,
                category = "UI"
            ),
            FeatureInfo(
                key = "admin.beta",
                name = "Beta Features",
                description = "Experimental beta features",
                defaultValue = false,
                category = "Admin"
            )
        )
        
        return ResponseEntity.ok(features)
    }

    /**
     * Admin endpoint: Override feature toggle for specific user
     */
    @PostMapping("/admin/override")
    fun setUserFeatureOverride(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: FeatureOverrideRequest
    ): ResponseEntity<Void> {
        // Verify admin role
        // In real implementation, store override in database
        return ResponseEntity.ok().build()
    }

    /**
     * Admin endpoint: Get feature toggle statistics
     */
    @GetMapping("/admin/stats")
    fun getFeatureStats(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Map<String, Any>> {
        val stats = mapOf(
            "totalUsers" to 1000,
            "features" to mapOf(
                "learning.adaptive" to mapOf("enabled" to 100, "disabled" to 900),
                "gamification.daily_quests" to mapOf("enabled" to 500, "disabled" to 500),
                "social.groups" to mapOf("enabled" to 1000, "disabled" to 0)
            )
        )
        
        return ResponseEntity.ok(stats)
    }

    /**
     * Determine which features to enable for a specific user
     * This is where A/B testing logic lives
     */
    private fun determineTogglesForUser(userId: String): Map<String, Boolean> {
        val toggles = mutableMapOf<String, Boolean>()
        
        // Core features - always enabled
        toggles["learning.micro"] = true
        toggles["gamification.streaks"] = true
        toggles["gamification.achievements"] = true
        toggles["gamification.levels"] = true
        toggles["social.groups"] = true
        toggles["ui.dark_mode"] = true
        toggles["ui.animations"] = true
        toggles["ui.haptics"] = true
        
        // A/B Testing: Adaptive Learning
        // Enable for 20% of users
        toggles["learning.adaptive"] = isInTestGroup(userId, "adaptive_learning", 20)
        
        // A/B Testing: Daily Quests
        // Enable for 50% of users
        toggles["gamification.daily_quests"] = isInTestGroup(userId, "daily_quests", 50)
        
        // Beta features - only for specific users
        toggles["social.friends"] = isBetaUser(userId)
        toggles["social.chat"] = isBetaUser(userId)
        toggles["content.video"] = isBetaUser(userId)
        toggles["content.audio"] = isBetaUser(userId)
        
        // Admin only
        toggles["admin.beta"] = false
        toggles["admin.debug_menu"] = false
        
        return toggles
    }

    /**
     * Check if user is in a specific test group
     * Consistent hashing ensures same user always gets same result
     */
    private fun isInTestGroup(userId: String, testName: String, percentage: Int): Boolean {
        val hash = (userId + testName).hashCode()
        val bucket = Math.abs(hash) % 100
        return bucket < percentage
    }

    /**
     * Check if user is a beta tester
     */
    private fun isBetaUser(userId: String): Boolean {
        // In real implementation, check database for beta flag
        // For now, use consistent hashing
        val hash = userId.hashCode()
        return Math.abs(hash) % 100 < 10 // 10% beta users
    }
}

/**
 * Feature information
 */
data class FeatureInfo(
    val key: String,
    val name: String,
    val description: String,
    val defaultValue: Boolean,
    val category: String
)

/**
 * Request to override feature toggle
 */
data class FeatureOverrideRequest(
    val userId: String,
    val featureKey: String,
    val enabled: Boolean
)
