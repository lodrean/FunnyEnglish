package com.funnyenglish.design.components.gamification

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funnyenglish.design.theme.FunnyEnglishTheme

@Composable
fun StarRating(
    rating: Int,
    modifier: Modifier = Modifier,
    maxStars: Int = 3,
    starSize: Dp = 48.dp
) {
    val animatedScales = remember(maxStars) {
        List(maxStars) { Animatable(0f) }
    }

    LaunchedEffect(rating) {
        animatedScales.forEachIndexed { index, animatable ->
            if (index < rating) {
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 150,
                        easing = FastOutSlowInEasing
                    )
                )
            } else {
                animatable.snapTo(0f)
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(maxStars) { index ->
            val isFilled = index < rating
            val scale = animatedScales[index].value

            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = if (isFilled) "Star earned" else "Star not earned",
                modifier = Modifier
                    .size(starSize)
                    .scale(scale.coerceAtLeast(0.1f)),
                tint = if (isFilled) {
                    MaterialTheme.colorScheme.gold
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StarRatingLightPreview() {
    FunnyEnglishTheme(darkTheme = false) {
        StarRating(rating = 2)
    }
}

@Preview(showBackground = true)
@Composable
private fun StarRatingDarkPreview() {
    FunnyEnglishTheme(darkTheme = true) {
        StarRating(rating = 3)
    }
}

@Preview(showBackground = true)
@Composable
private fun StarRatingZeroPreview() {
    FunnyEnglishTheme {
        StarRating(rating = 0)
    }
}

@Preview(showBackground = true)
@Composable
private fun StarRatingOnePreview() {
    FunnyEnglishTheme {
        StarRating(rating = 1, starSize = 32.dp)
    }
}
