package com.funnyenglish.designsystem.accessibility

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

/**
 * FunnyEnglish Accessibility - Reduce Motion Support
 * 
 * Provides CompositionLocal for motion preference and utilities for
 * conditionally applying animations based on user preference.
 * 
 * Target: ADHD, autism, vestibular disorders, motion sensitivity
 */

/**
 * CompositionLocal for reduce motion preference
 * Default: false (animations enabled)
 */
val LocalReduceMotion = compositionLocalOf { false }

/**
 * Check if reduce motion is enabled
 */
@Composable
fun isReduceMotionEnabled(): Boolean {
    return LocalReduceMotion.current
}

/**
 * Optional animation modifier
 * 
 * Applies the animation modifier only if reduce motion is NOT enabled.
 * Otherwise applies the reduced motion alternative (default: no modifier).
 * 
 * Usage:
 * ```
 * Modifier.optionalAnimation(
 *     animation = Modifier.scale(scale),
 *     reducedMotionAlternative = Modifier.alpha(0.8f)
 * )
 * ```
 */
@Composable
fun Modifier.optionalAnimation(
    animation: Modifier,
    reducedMotionAlternative: Modifier = Modifier
): Modifier {
    val reduceMotion = LocalReduceMotion.current
    return if (reduceMotion) {
        this.then(reducedMotionAlternative)
    } else {
        this.then(animation)
    }
}

/**
 * Get appropriate animation duration based on reduce motion setting
 * 
 * @param normalDuration Duration when animations are enabled
 * @param reducedDuration Duration when reduce motion is enabled (default: 0)
 * @return Appropriate duration
 */
@Composable
fun getAnimationDuration(normalDuration: Int, reducedDuration: Int = 0): Int {
    return if (LocalReduceMotion.current) reducedDuration else normalDuration
}

/**
 * Get appropriate animation spec based on reduce motion setting
 * 
 * @param normalSpec Animation spec when animations are enabled
 * @param reducedSpec Animation spec when reduce motion is enabled (default: instant)
 */
@Composable
fun <T> getAnimationSpec(
    normalSpec: AnimationSpec<T>,
    reducedSpec: AnimationSpec<T> = tween(durationMillis = 0)
): AnimationSpec<T> {
    return if (LocalReduceMotion.current) reducedSpec else normalSpec
}

/**
 * Standard animation specs respecting reduce motion
 */
object FunnyAnimationSpecs {
    /**
     * Instant: 50ms - Micro-feedback
     */
    @Composable
    fun <T> instant(): AnimationSpec<T> = getAnimationSpec(
        normalSpec = tween(durationMillis = 50, easing = EaseInOutCubic)
    )
    
    /**
     * Fast: 150ms - Button presses, state changes
     */
    @Composable
    fun <T> fast(): AnimationSpec<T> = getAnimationSpec(
        normalSpec = tween(durationMillis = 150, easing = EaseInOutCubic)
    )
    
    /**
     * Normal: 300ms - Transitions, reveals
     */
    @Composable
    fun <T> normal(): AnimationSpec<T> = getAnimationSpec(
        normalSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    
    /**
     * Slow: 500ms - Page transitions, modals
     */
    @Composable
    fun <T> slow(): AnimationSpec<T> = getAnimationSpec(
        normalSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )
    
    /**
     * Celebration: 1000ms - XP counts, confetti, achievements
     * Note: Celebrations may still play with reduce motion, but faster
     */
    @Composable
    fun <T> celebration(): AnimationSpec<T> = getAnimationSpec(
        normalSpec = tween(durationMillis = 1000, easing = EaseInOutCubic),
        reducedSpec = tween(durationMillis = 300, easing = EaseInOutCubic)
    )
}

/**
 * Easing curves for animations
 */
object FunnyEasings {
    /**
     * Standard: EaseInOutCubic - General transitions
     */
    val Standard: Easing = EaseInOutCubic
    
    /**
     * Enter: EaseOutCubic - Elements appearing
     */
    val Enter: Easing = androidx.compose.animation.core.EaseOutCubic
    
    /**
     * Exit: EaseInCubic - Elements disappearing
     */
    val Exit: Easing = androidx.compose.animation.core.EaseInCubic
    
    /**
     * Bounce: EaseOutBounce - Celebrations, rewards
     */
    val Bounce: Easing = androidx.compose.animation.core.EaseOutBounce
    
    /**
     * Elastic: EaseOutElastic - Special celebrations
     */
    val Elastic: Easing = androidx.compose.animation.core.EaseOutElastic
}
