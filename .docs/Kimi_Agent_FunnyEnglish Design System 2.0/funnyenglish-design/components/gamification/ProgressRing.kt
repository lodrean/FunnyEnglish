package com.funnyenglish.design.components.gamification

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funnyenglish.design.theme.FunnyEnglishTheme

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showPercentage: Boolean = false
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val diameter = size.minDimension - stroke.width
            val radius = diameter / 2
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = stroke
            )

            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                style = stroke,
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                topLeft = androidx.compose.ui.geometry.Offset(
                    centerX - radius,
                    centerY - radius
                )
            )
        }

        if (showPercentage) {
            Text(
                text = "${(animatedProgress.value * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressRingLightPreview() {
    FunnyEnglishTheme(darkTheme = false) {
        ProgressRing(
            progress = 0.75f,
            modifier = Modifier.size(100.dp),
            showPercentage = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressRingDarkPreview() {
    FunnyEnglishTheme(darkTheme = true) {
        ProgressRing(
            progress = 0.45f,
            modifier = Modifier.size(120.dp),
            strokeWidth = 12.dp,
            color = MaterialTheme.colorScheme.xp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressRingCompletePreview() {
    FunnyEnglishTheme {
        ProgressRing(
            progress = 1f,
            modifier = Modifier.size(80.dp),
            showPercentage = true
        )
    }
}
