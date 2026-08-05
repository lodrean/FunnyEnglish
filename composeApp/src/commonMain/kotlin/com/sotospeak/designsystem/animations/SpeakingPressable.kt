package com.sotospeak.designsystem.animations

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.sotospeak.designsystem.accessibility.LocalReduceMotion
import com.sotospeak.designsystem.theme.SpeakingMotion

/**
 * Press-анимация мокапов (`transition: transform 150ms ease-standard`,
 * `:active { transform: scale(.97–.98) }`): лёгкое сжатие при нажатии.
 *
 * Применять к карточкам/кнопкам/чипам вместе с clickable/Card(onClick):
 * передавать тот же [interactionSource], что и в clickable, чтобы press
 * отслеживался корректно. При Reduce motion — без анимации.
 */
fun Modifier.speakingPressable(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f
): Modifier = composed {
    val reduceMotion = LocalReduceMotion.current
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = if (reduceMotion) snap() else SpeakingMotion.tweenFast(),
        label = "speaking_press_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Упрощённый вариант без внешнего interactionSource (для Surface/Card onClick). */
fun Modifier.speakingPressable(pressedScale: Float = 0.97f): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.speakingPressable(interactionSource, pressedScale)
}
