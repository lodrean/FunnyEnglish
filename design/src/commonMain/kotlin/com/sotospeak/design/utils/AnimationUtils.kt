package com.sotospeak.design.utils

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.pow

private const val DEFAULT_ANIMATION_DURATION = 300
private const val DEFAULT_FADE_DURATION = 200

@Stable
@Composable
fun rememberAnimatedFloat(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = tween(DEFAULT_ANIMATION_DURATION)
): State<Float> {
    return animateFloatAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = "animated_float"
    )
}

@Stable
@Composable
fun animateColorOnChange(
    targetValue: Color,
    animationSpec: AnimationSpec<Color> = tween(DEFAULT_ANIMATION_DURATION)
): State<Color> {
    return androidx.compose.animation.animateColorAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = "animated_color"
    )
}

@Stable
fun <T> bounceAnimationSpec(): SpringSpec<T> {
    return spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

@Stable
fun <T> gentleBounceSpec(): SpringSpec<T> {
    return spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
}

@Stable
fun <T> stiffSpringSpec(): SpringSpec<T> {
    return spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
}

@Stable
fun fadeInSpec(
    durationMillis: Int = DEFAULT_FADE_DURATION,
    delayMillis: Int = 0
): EnterTransition {
    return fadeIn(
        animationSpec = tween(durationMillis, delayMillis, LinearOutSlowInEasing)
    )
}

@Stable
fun fadeOutSpec(
    durationMillis: Int = DEFAULT_FADE_DURATION
): ExitTransition {
    return fadeOut(
        animationSpec = tween(durationMillis, easing = FastOutLinearInEasing)
    )
}

@Stable
fun slideInFromBottomSpec(
    durationMillis: Int = DEFAULT_ANIMATION_DURATION
): EnterTransition {
    return slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
    ) + fadeInSpec()
}

@Stable
fun slideOutToBottomSpec(
    durationMillis: Int = DEFAULT_ANIMATION_DURATION
): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(durationMillis, easing = FastOutLinearInEasing)
    ) + fadeOutSpec()
}

@Stable
fun slideInFromRightSpec(
    durationMillis: Int = DEFAULT_ANIMATION_DURATION
): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
    ) + fadeInSpec()
}

@Stable
fun slideOutToRightSpec(
    durationMillis: Int = DEFAULT_ANIMATION_DURATION
): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(durationMillis, easing = FastOutLinearInEasing)
    ) + fadeOutSpec()
}

@Stable
fun slideInFromLeftSpec(
    durationMillis: Int = DEFAULT_ANIMATION_DURATION
): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
    ) + fadeInSpec()
}

@Stable
fun slideOutToLeftSpec(
    durationMillis: Int = DEFAULT_ANIMATION_DURATION
): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(durationMillis, easing = FastOutLinearInEasing)
    ) + fadeOutSpec()
}

@Stable
fun scaleInSpec(
    durationMillis: Int = DEFAULT_ANIMATION_DURATION
): EnterTransition {
    return androidx.compose.animation.scaleIn(
        initialScale = 0.8f,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
    ) + fadeInSpec()
}

@Stable
fun scaleOutSpec(
    durationMillis: Int = DEFAULT_ANIMATION_DURATION
): ExitTransition {
    return androidx.compose.animation.scaleOut(
        targetScale = 0.8f,
        animationSpec = tween(durationMillis, easing = FastOutLinearInEasing)
    ) + fadeOutSpec()
}

// Animation duration constants
object AnimationDurations {
    const val INSTANT = 0
    const val FAST = 150
    const val NORMAL = 300
    const val SLOW = 500
    const val VERY_SLOW = 800
}

// Easing presets
object AnimationEasings {
    val standard = FastOutSlowInEasing
    val decelerate = LinearOutSlowInEasing
    val accelerate = FastOutLinearInEasing
    val linear = androidx.compose.animation.core.LinearEasing
}

// Animation specs for common use cases
object AnimationSpecs {
    @Stable
    fun <T> defaultTween() = tween<T>(AnimationDurations.NORMAL)
    
    @Stable
    fun <T> fastTween() = tween<T>(AnimationDurations.FAST)
    
    @Stable
    fun <T> slowTween() = tween<T>(AnimationDurations.SLOW)
    
    @Stable
    fun <T> bounce() = bounceAnimationSpec<T>()
    
    @Stable
    fun <T> gentleBounce() = gentleBounceSpec<T>()
}

// Derived state helpers for performance
@Stable
@Composable
fun <T> rememberDerivedState(
    calculation: () -> T
): State<T> {
    return remember { derivedStateOf(calculation) }
}

// Debounced animation trigger
@Stable
@Composable
fun rememberDebouncedAnimation(
    trigger: Boolean,
    debounceMillis: Long = 100
): Boolean {
    val debouncedState = remember { androidx.compose.runtime.mutableStateOf(trigger) }
    
    androidx.compose.runtime.LaunchedEffect(trigger) {
        if (trigger) {
            kotlinx.coroutines.delay(debounceMillis)
            debouncedState.value = true
        } else {
            debouncedState.value = false
        }
    }
    
    return debouncedState.value
}

// Animation progress calculator
@Stable
fun calculateProgress(
    current: Float,
    total: Float,
    minProgress: Float = 0f,
    maxProgress: Float = 1f
): Float {
    return if (total > 0) {
        ((current / total) * (maxProgress - minProgress) + minProgress).coerceIn(minProgress, maxProgress)
    } else {
        minProgress
    }
}

// Interpolation helpers
@Stable
fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

@Stable
fun lerp(start: Int, stop: Int, fraction: Float): Int {
    return (start + (stop - start) * fraction).toInt()
}

@Stable
fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = lerp(start.red, stop.red, fraction),
        green = lerp(start.green, stop.green, fraction),
        blue = lerp(start.blue, stop.blue, fraction),
        alpha = lerp(start.alpha, stop.alpha, fraction)
    )
}

// Easing function wrapper for custom interpolators
@Stable
fun easeInOutCubic(t: Float): Float {
    return if (t < 0.5f) {
        4 * t * t * t
    } else {
        1 - (-2 * t + 2).toDouble().pow(3.0).toFloat() / 2
    }
}

@Stable
fun easeOutCubic(t: Float): Float {
    return 1 - (1 - t).toDouble().pow(3.0).toFloat()
}

@Stable
fun easeInCubic(t: Float): Float {
    return t * t * t
}

// Animation state machine helper
sealed class AnimationState {
    data object Idle : AnimationState()
    data object Running : AnimationState()
    data object Completed : AnimationState()
    data class Error(val message: String) : AnimationState()
}

// Recomposition-optimized animation values
@Stable
@Composable
fun rememberAnimationValue(
    initialValue: Float,
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = tween(DEFAULT_ANIMATION_DURATION)
): State<Float> {
    val animatable = remember { androidx.compose.animation.core.Animatable(initialValue) }
    
    androidx.compose.runtime.LaunchedEffect(targetValue) {
        animatable.animateTo(targetValue, animationSpec)
    }
    
    return animatable.asState()
}

// Visibility animation helper
@Stable
@Composable
fun AnimatedVisibilityHelper(
    visible: Boolean,
    enter: EnterTransition = fadeInSpec(),
    exit: ExitTransition = fadeOutSpec(),
    content: @Composable () -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = enter,
        exit = exit
    ) {
        content()
    }
}
