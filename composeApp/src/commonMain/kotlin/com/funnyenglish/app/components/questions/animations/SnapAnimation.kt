package com.funnyenglish.app.components.questions.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

/**
 * Snap анимация при успешном попадании слова в hotspot
 * Spring с bounce эффектом
 */
@Composable
fun SnapAnimation(
    modifier: Modifier = Modifier,
    targetScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val scale: Float = animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "snapScale"
    ).value
    
    Box(modifier = modifier.scale(scale)) {
        content()
    }
}

/**
 * Success анимация - масштабирование с bounce
 */
@Composable
fun SuccessAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale: Float = animateFloatAsState(
        targetValue = 1f,
        animationSpec = keyframes {
            durationMillis = 400
            0.0f at 0
            1.2f at 200
            1.0f at 400
        },
        label = "successScale"
    ).value
    
    Box(modifier = modifier.scale(scale)) {
        content()
    }
}
