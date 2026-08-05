package com.sotospeak.design.components.gamification

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.design.theme.SoToSpeakTheme

@Composable
fun StreakFlame(
    streakCount: Int,
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame_animation")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animated) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = "Streak flame",
            modifier = Modifier
                .size(32.dp)
                .scale(if (animated) scale else 1f),
            tint = SoToSpeakTheme.colors.flame
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = streakCount.toString(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = SoToSpeakTheme.colors.flame
        )
    }
}

@Preview
@Composable
private fun StreakFlameLightPreview() {
    SoToSpeakTheme(darkTheme = false) {
        StreakFlame(streakCount = 7)
    }
}

@Preview
@Composable
private fun StreakFlameDarkPreview() {
    SoToSpeakTheme(darkTheme = true) {
        StreakFlame(streakCount = 30)
    }
}

@Preview
@Composable
private fun StreakFlameStaticPreview() {
    SoToSpeakTheme {
        StreakFlame(streakCount = 100, animated = false)
    }
}

@Preview
@Composable
private fun StreakFlameLongPreview() {
    SoToSpeakTheme {
        StreakFlame(streakCount = 365)
    }
}
