package com.funnyenglish.app.components.questions.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale

/**
 * Pulse анимация для подсветки hotspot'ов при drag
 * Масштаб 1.0 -> 1.15, прозрачность 0.6 -> 1.0
 */
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale: Float = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    ).value
    
    val alpha: Float = infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    ).value
    
    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        content()
    }
}
