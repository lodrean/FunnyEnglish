package com.funnyenglish.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.core.ui.theme.FunnyColors

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = FunnyColors.Primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Упс!",
            style = MaterialTheme.typography.headlineMedium,
            color = FunnyColors.Error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            FunnyButton(
                text = "Попробовать снова",
                onClick = onRetry
            )
        }
    }
}

@Composable
fun StarsDisplay(
    stars: Int,
    maxStars: Int = 3,
    modifier: Modifier = Modifier,
    size: Int = 24
) {
    Row(modifier = modifier) {
        repeat(maxStars) { index ->
            val isFilled = index < stars

            val animatedScale by animateFloatAsState(
                targetValue = if (isFilled) 1.2f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )

            Icon(
                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (isFilled) FunnyColors.StarFilled else FunnyColors.StarEmpty,
                modifier = Modifier
                    .size(size.dp)
                    .scale(animatedScale)
            )
        }
    }
}

@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = FunnyColors.Primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(color, color.copy(alpha = 0.7f))
                    )
                )
        )
    }
}
