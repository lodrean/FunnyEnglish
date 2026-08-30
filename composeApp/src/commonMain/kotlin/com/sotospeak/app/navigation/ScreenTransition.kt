package com.sotospeak.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.sotospeak.designsystem.accessibility.LocalReduceMotion
import com.sotospeak.designsystem.theme.SpeakingMotion

/**
 * M3 Emphasized-переходы между экранами (спека §2 motion);
 * при Reduce motion — мгновенная смена без анимации.
 * Единый transition для внешнего (App) и внутреннего (MainNavHost) AnimatedContent.
 */
@Composable
fun rememberScreenTransition(): AnimatedContentTransitionScope<AppScreen>.() -> ContentTransform {
    val reduceMotion = LocalReduceMotion.current
    return remember(reduceMotion) {
        {
            if (reduceMotion) {
                ContentTransform(
                    EnterTransition.None,
                    ExitTransition.None
                )
            } else {
                fadeIn(
                    tween(300, easing = SpeakingMotion.EasingM3Emphasized)
                ) + scaleIn(
                    initialScale = 0.96f,
                    animationSpec = tween(300, easing = SpeakingMotion.EasingM3Emphasized)
                ) togetherWith fadeOut(
                    tween(200, easing = SpeakingMotion.EasingM3Standard)
                )
            }
        }
    }
}
