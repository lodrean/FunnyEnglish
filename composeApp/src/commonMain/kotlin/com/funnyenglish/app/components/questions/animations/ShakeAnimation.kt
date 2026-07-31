package com.funnyenglish.app.components.questions.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Shake анимация при ошибке (неправильное сопоставление)
 * Горизонтальное дрожание ±10px
 */
@Composable
fun ShakeAnimation(
    modifier: Modifier = Modifier,
    trigger: Boolean,
    content: @Composable () -> Unit
) {
    val shake: Float = animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 300
            -10f at 50
            10f at 100
            -10f at 150
            10f at 200
            0f at 300
        },
        label = "shake"
    ).value
    
    Box(
        modifier = modifier.graphicsLayer {
            translationX = shake * 5f
        }
    ) {
        content()
    }
}

/**
 * Return анимация - возврат слова на место при промахе
 */
@Composable
fun ReturnAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Анимация возврата управляется через offset в основном компоненте
    // Этот компонент может быть использован для дополнительных эффектов
    Box(modifier = modifier) {
        content()
    }
}
