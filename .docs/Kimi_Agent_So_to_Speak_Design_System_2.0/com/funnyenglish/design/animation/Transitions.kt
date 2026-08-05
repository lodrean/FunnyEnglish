package com.funnyenglish.design.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private const val TRANSITION_DURATION = 300
private const val FADE_DURATION = 200

@Stable
@Composable
fun SharedElementTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(FADE_DURATION)) +
                scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                ),
        exit = fadeOut(animationSpec = tween(FADE_DURATION)) +
                scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutLinearInEasing)
                ),
        content = content
    )
}

@Stable
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FadeThrough(
    targetState: Any,
    modifier: Modifier = Modifier,
    content: @Composable (Any) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(FADE_DURATION, easing = LinearOutSlowInEasing)
            ) togetherWith
            fadeOut(
                animationSpec = tween(FADE_DURATION, easing = FastOutLinearInEasing)
            )
        },
        content = content
    )
}

@Stable
@Composable
fun SlideUpFadeIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
        ) + fadeIn(
            animationSpec = tween(FADE_DURATION, delayMillis = 50)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(TRANSITION_DURATION, easing = FastOutLinearInEasing)
        ) + fadeOut(
            animationSpec = tween(FADE_DURATION)
        )
    ) {
        content()
    }
}

@Stable
@Composable
fun ScaleInOut(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
        ) + fadeIn(
            animationSpec = tween(FADE_DURATION)
        ),
        exit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(TRANSITION_DURATION, easing = FastOutLinearInEasing)
        ) + fadeOut(
            animationSpec = tween(FADE_DURATION)
        )
    ) {
        content()
    }
}

@Stable
@Composable
fun SlideInFromRight(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(FADE_DURATION)),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth / 3 },
            animationSpec = tween(TRANSITION_DURATION, easing = FastOutLinearInEasing)
        ) + fadeOut(animationSpec = tween(FADE_DURATION)),
        content = content
    )
}

@Stable
@Composable
fun CrossfadeTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.animation.Crossfade(
        targetState = visible,
        modifier = modifier,
        animationSpec = tween(TRANSITION_DURATION),
        label = "crossfade"
    ) { isVisible ->
        if (isVisible) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SharedElementTransitionPreview() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SharedElementTransition(visible = true) {
            Card {
                Text(
                    text = "Shared Element",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FadeThroughPreview() {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    FadeThrough(targetState = selectedTab) { tab ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Tab $tab")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SlideUpFadeInPreview() {
    SlideUpFadeIn(visible = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Bottom Sheet Content")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScaleInOutPreview() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ScaleInOut(visible = true) {
            Card {
                Text(
                    text = "Dialog Content",
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}
