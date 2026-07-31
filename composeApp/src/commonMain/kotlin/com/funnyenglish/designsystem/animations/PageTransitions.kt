package com.funnyenglish.designsystem.animations

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion

/**
 * FunnyEnglish Page Transitions
 * 
 * Priority 5 (Must-have)
 * Duration: 300ms (NORMAL)
 * Purpose: Orientation maintenance, spatial memory
 */

enum class PageTransitionDirection {
    LEFT,    // Enter from left
    RIGHT,   // Enter from right
    UP,      // Enter from bottom (upward)
    DOWN,    // Enter from top (downward)
    FADE,    // Fade only
    NONE     // No animation
}

/**
 * Animated content with page transition
 */
@Composable
fun <T> PageTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    direction: PageTransitionDirection = PageTransitionDirection.RIGHT,
    duration: Int = AnimationDurations.NORMAL,
    content: @Composable (T) -> Unit
) {
    val reduceMotion = LocalReduceMotion.current
    
    // If reduce motion, use fade only or no animation
    val actualDirection = if (reduceMotion) {
        if (direction == PageTransitionDirection.NONE) PageTransitionDirection.NONE 
        else PageTransitionDirection.FADE
    } else direction
    
    val enterTransition = getEnterTransition(actualDirection, duration)
    val exitTransition = getExitTransition(actualDirection, duration)
    
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            enterTransition togetherWith exitTransition
        },
        label = "page_transition"
    ) { state ->
        content(state)
    }
}

/**
 * Horizontal slide transition (for navigation)
 */
@Composable
fun HorizontalSlideTransition(
    targetState: Int,
    modifier: Modifier = Modifier,
    slideRight: Boolean = true,
    content: @Composable (Int) -> Unit
) {
    val reduceMotion = LocalReduceMotion.current
    
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            if (reduceMotion) {
                fadeIn(tween(150)) togetherWith fadeOut(tween(150))
            } else {
                val direction = if (slideRight) 1 else -1
                (slideInHorizontally(
                    animationSpec = tween(AnimationDurations.NORMAL, easing = EaseOut),
                    initialOffsetX = { fullWidth -> direction * fullWidth }
                ) + fadeIn(tween(AnimationDurations.NORMAL))) togetherWith
                (slideOutHorizontally(
                    animationSpec = tween(AnimationDurations.NORMAL, easing = EaseIn),
                    targetOffsetX = { fullWidth -> -direction * fullWidth }
                ) + fadeOut(tween(AnimationDurations.NORMAL)))
            }
        },
        label = "horizontal_slide"
    ) { state ->
        content(state)
    }
}

/**
 * Vertical slide transition (for modals/bottom sheets)
 */
@Composable
fun VerticalSlideTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    slideUp: Boolean = true,
    content: @Composable () -> Unit
) {
    val reduceMotion = LocalReduceMotion.current
    
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (reduceMotion) {
            fadeIn(tween(AnimationDurations.FAST))
        } else {
            slideInVertically(
                animationSpec = tween(AnimationDurations.NORMAL, easing = EaseOut),
                initialOffsetY = { fullHeight -> if (slideUp) fullHeight else -fullHeight }
            ) + fadeIn(tween(AnimationDurations.NORMAL))
        },
        exit = if (reduceMotion) {
            fadeOut(tween(AnimationDurations.FAST))
        } else {
            slideOutVertically(
                animationSpec = tween(AnimationDurations.NORMAL, easing = EaseIn),
                targetOffsetY = { fullHeight -> if (slideUp) fullHeight else -fullHeight }
            ) + fadeOut(tween(AnimationDurations.NORMAL))
        }
    ) {
        content()
    }
}

/**
 * Fade transition (default, accessibility-friendly)
 */
@Composable
fun FadeTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    duration: Int = AnimationDurations.NORMAL,
    content: @Composable () -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(duration, easing = EaseInOut)),
        exit = fadeOut(tween(duration, easing = EaseInOut))
    ) {
        content()
    }
}

/**
 * Scale transition (for dialogs/popups)
 */
@Composable
fun ScaleTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val reduceMotion = LocalReduceMotion.current
    
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (reduceMotion) {
            fadeIn(tween(AnimationDurations.FAST))
        } else {
            androidx.compose.animation.scaleIn(
                animationSpec = tween(AnimationDurations.NORMAL, easing = EaseOut),
                initialScale = 0.8f
            ) + fadeIn(tween(AnimationDurations.NORMAL))
        },
        exit = if (reduceMotion) {
            fadeOut(tween(AnimationDurations.FAST))
        } else {
            androidx.compose.animation.scaleOut(
                animationSpec = tween(AnimationDurations.NORMAL, easing = EaseIn),
                targetScale = 0.8f
            ) + fadeOut(tween(AnimationDurations.NORMAL))
        }
    ) {
        content()
    }
}

// ==================== Private Helpers ====================

private fun getEnterTransition(
    direction: PageTransitionDirection,
    duration: Int
): EnterTransition {
    return when (direction) {
        PageTransitionDirection.LEFT -> slideInHorizontally(
            animationSpec = tween(duration, easing = EaseOut),
            initialOffsetX = { -it }
        ) + fadeIn(tween(duration))
        
        PageTransitionDirection.RIGHT -> slideInHorizontally(
            animationSpec = tween(duration, easing = EaseOut),
            initialOffsetX = { it }
        ) + fadeIn(tween(duration))
        
        PageTransitionDirection.UP -> slideInVertically(
            animationSpec = tween(duration, easing = EaseOut),
            initialOffsetY = { it }
        ) + fadeIn(tween(duration))
        
        PageTransitionDirection.DOWN -> slideInVertically(
            animationSpec = tween(duration, easing = EaseOut),
            initialOffsetY = { -it }
        ) + fadeIn(tween(duration))
        
        PageTransitionDirection.FADE -> fadeIn(tween(duration, easing = EaseInOut))
        
        PageTransitionDirection.NONE -> fadeIn(tween(0))
    }
}

private fun getExitTransition(
    direction: PageTransitionDirection,
    duration: Int
): ExitTransition {
    return when (direction) {
        PageTransitionDirection.LEFT -> slideOutHorizontally(
            animationSpec = tween(duration, easing = EaseIn),
            targetOffsetX = { it }
        ) + fadeOut(tween(duration))
        
        PageTransitionDirection.RIGHT -> slideOutHorizontally(
            animationSpec = tween(duration, easing = EaseIn),
            targetOffsetX = { -it }
        ) + fadeOut(tween(duration))
        
        PageTransitionDirection.UP -> slideOutVertically(
            animationSpec = tween(duration, easing = EaseIn),
            targetOffsetY = { -it }
        ) + fadeOut(tween(duration))
        
        PageTransitionDirection.DOWN -> slideOutVertically(
            animationSpec = tween(duration, easing = EaseIn),
            targetOffsetY = { it }
        ) + fadeOut(tween(duration))
        
        PageTransitionDirection.FADE -> fadeOut(tween(duration, easing = EaseInOut))
        
        PageTransitionDirection.NONE -> fadeOut(tween(0))
    }
}

/**
 * Navigation transition spec for NavHost
 */
fun navigationEnterTransition(direction: PageTransitionDirection = PageTransitionDirection.RIGHT): EnterTransition {
    return getEnterTransition(direction, AnimationDurations.NORMAL)
}

fun navigationExitTransition(direction: PageTransitionDirection = PageTransitionDirection.RIGHT): ExitTransition {
    return getExitTransition(direction, AnimationDurations.NORMAL)
}

fun navigationPopEnterTransition(direction: PageTransitionDirection = PageTransitionDirection.LEFT): EnterTransition {
    return getEnterTransition(direction, AnimationDurations.NORMAL)
}

fun navigationPopExitTransition(direction: PageTransitionDirection = PageTransitionDirection.LEFT): ExitTransition {
    return getExitTransition(direction, AnimationDurations.NORMAL)
}
