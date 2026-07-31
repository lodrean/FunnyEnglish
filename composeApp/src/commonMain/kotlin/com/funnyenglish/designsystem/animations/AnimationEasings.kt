package com.funnyenglish.designsystem.animations

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing

/**
 * FunnyEnglish Animation Easings
 * 
 * Curves for natural and playful motion:
 * - Standard: EaseInOutCubic - General transitions
 * - Enter: EaseOutCubic - Elements appearing
 * - Exit: EaseInCubic - Elements disappearing
 * - Bounce: EaseOutBounce - Celebrations
 * - Elastic: EaseOutElastic - Special celebrations
 */

object FunnyEasings {
    /**
     * Standard: EaseInOutCubic
     * Natural acceleration and deceleration
     * Application: General transitions, balanced motion
     */
    val Standard: Easing = androidx.compose.animation.core.EaseInOutCubic
    
    /**
     * Enter: EaseOutCubic
     * Quick start, gentle settle
     * Application: Elements appearing, content reveals
     */
    val Enter: Easing = androidx.compose.animation.core.EaseOutCubic
    
    /**
     * Exit: EaseInCubic
     * Gentle start, quick completion
     * Application: Elements disappearing, content removal
     */
    val Exit: Easing = androidx.compose.animation.core.EaseInCubic
    
    /**
     * Bounce: EaseOutBounce
     * Energetic overshoot, satisfying settle
     * Application: Celebrations, rewards, playful feedback
     */
    val Bounce: Easing = androidx.compose.animation.core.EaseOutBounce
    
    /**
     * Elastic: EaseOutElastic
     * Extended oscillation, whimsical
     * Application: Special celebrations, maximum playfulness
     */
    val Elastic: Easing = androidx.compose.animation.core.EaseOutElastic
    
    /**
     * Linear: LinearEasing
     * Constant speed
     * Application: Progress indicators, infinite rotations
     */
    val Linear: Easing = LinearEasing
    
    /**
     * FastOutSlowIn: FastOutSlowInEasing
     * Material Design standard
     * Application: Large transitions, page changes
     */
    val FastOutSlowIn: Easing = FastOutSlowInEasing
}

/**
 * Easing selection helper based on animation type
 */
fun selectEasing(
    isEntering: Boolean = false,
    isExiting: Boolean = false,
    isCelebration: Boolean = false,
    isSpecial: Boolean = false
): Easing {
    return when {
        isSpecial -> FunnyEasings.Elastic
        isCelebration -> FunnyEasings.Bounce
        isEntering -> FunnyEasings.Enter
        isExiting -> FunnyEasings.Exit
        else -> FunnyEasings.Standard
    }
}
