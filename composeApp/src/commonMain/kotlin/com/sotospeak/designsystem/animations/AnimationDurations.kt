package com.sotospeak.designsystem.animations

/**
 * So to Speak Animation Durations
 * 
 * Five duration categories with explicit use case mapping:
 * - INSTANT: 50ms - Micro-feedback
 * - FAST: 150ms - Button presses
 * - NORMAL: 300ms - Transitions
 * - SLOW: 500ms - Page transitions
 * - CELEBRATION: 1000ms - XP counts, confetti
 */

object AnimationDurations {
    /**
     * INSTANT: 50ms
     * Micro-feedback (color changes, opacity shifts)
     * Sub-perceptual, feels immediate
     */
    const val INSTANT = 50
    
    /**
     * FAST: 150ms
     * Button presses, state changes, quick toggles
     * Rapid, responsive, barely noticeable
     */
    const val FAST = 150
    
    /**
     * NORMAL: 300ms
     * Transitions, reveals, content changes
     * Comfortable, clearly animated but efficient
     */
    const val NORMAL = 300
    
    /**
     * SLOW: 500ms
     * Page transitions, modals, major layout shifts
     * Deliberate, emphasizes structural change
     */
    const val SLOW = 500
    
    /**
     * CELEBRATION: 1000ms
     * XP counts, confetti, achievement unlocks
     * Extended, rewarding, emotionally significant
     */
    const val CELEBRATION = 1000
    
    /**
     * EXTENDED_CELEBRATION: 2000ms
     * Major achievements, level ups
     * Maximum emotional impact
     */
    const val EXTENDED_CELEBRATION = 2000
}

/**
 * Micro-Interaction Priorities
 */
enum class AnimationPriority(val level: Int, val description: String) {
    /**
     * Must have - Core gamification reinforcement
     */
    MUST(5, "Essential for user experience"),
    
    /**
     * Important - Tactile confirmation
     */
    IMPORTANT(4, "Significantly improves UX"),
    
    /**
     * Nice to have - Satisfaction enhancement
     */
    NICE(3, "Polish and delight"),
    
    /**
     * Low priority - Can be deferred
     */
    LOW(2, "Nice but not critical"),
    
    /**
     * Optional - Future consideration
     */
    OPTIONAL(1, "Can be implemented later")
}

/**
 * Animation specifications with priorities
 */
object FunnyAnimations {
    // Priority 5 (Must)
    val ConfettiCorrect = AnimationSpec(
        duration = AnimationDurations.CELEBRATION,
        priority = AnimationPriority.MUST,
        description = "Confetti on correct answer"
    )
    
    val PageTransition = AnimationSpec(
        duration = AnimationDurations.NORMAL,
        priority = AnimationPriority.MUST,
        description = "Page transitions"
    )
    
    val LoadingSkeleton = AnimationSpec(
        duration = AnimationDurations.NORMAL,
        priority = AnimationPriority.MUST,
        description = "Loading skeleton with shimmer"
    )
    
    val ProgressBarFill = AnimationSpec(
        duration = AnimationDurations.NORMAL,
        priority = AnimationPriority.MUST,
        description = "Progress bar fill"
    )
    
    // Priority 4 (Important)
    val ButtonPress = AnimationSpec(
        duration = AnimationDurations.FAST,
        priority = AnimationPriority.IMPORTANT,
        description = "Button press feedback"
    )
    
    // Priority 3 (Nice)
    val XPCountUp = AnimationSpec(
        duration = AnimationDurations.CELEBRATION,
        priority = AnimationPriority.NICE,
        description = "XP counter animation"
    )
    
    val StreakFlamePulse = AnimationSpec(
        duration = AnimationDurations.CELEBRATION,
        priority = AnimationPriority.NICE,
        description = "Streak flame breathing"
    )
    
    val AchievementUnlock = AnimationSpec(
        duration = AnimationDurations.CELEBRATION,
        priority = AnimationPriority.NICE,
        description = "Achievement unlock celebration"
    )
}

/**
 * Animation specification data class
 */
data class AnimationSpec(
    val duration: Int,
    val priority: AnimationPriority,
    val description: String
)
