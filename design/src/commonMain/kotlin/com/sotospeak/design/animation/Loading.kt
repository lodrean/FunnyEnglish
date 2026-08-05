package com.sotospeak.design.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// So to Speak brand colors
private val Primary = Color(0xFF6200EE)
private val SurfaceVariant = Color(0xFFE7E0EC)
private val ShimmerBase = Color(0xFFE0E0E0)
private val ShimmerHighlight = Color(0xFFF5F5F5)

@Stable
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val shimmerColors = listOf(
        ShimmerBase,
        ShimmerHighlight,
        ShimmerBase
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Stable
@Composable
fun SkeletonItem(
    modifier: Modifier = Modifier,
    lines: Int = 3
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title line
        SkeletonLoader(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(20.dp),
            shape = RoundedCornerShape(4.dp)
        )
        
        // Content lines
        repeat(lines) { index ->
            SkeletonLoader(
                modifier = Modifier
                    .fillMaxWidth(if (index == lines - 1) 0.7f else 1f)
                    .height(14.dp),
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}

@Stable
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Image placeholder
        SkeletonLoader(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Title
        SkeletonLoader(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(18.dp),
            shape = RoundedCornerShape(4.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Description
        SkeletonLoader(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@Stable
@Composable
fun AppCircularProgress(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp,
    color: Color = Primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular_progress")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress_rotation"
    )
    
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress_sweep"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawCircle(
                color = SurfaceVariant,
                radius = (size.toPx() - strokeWidth.toPx()) / 2,
                style = Stroke(width = strokeWidth.toPx())
            )
            
            drawArc(
                color = color,
                startAngle = rotation - 90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Stable
@Composable
fun DotsTypingIndicator(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotColor: Color = Primary,
    spacing: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    val delays = listOf(0, 150, 300)
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        delays.forEach { delay ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale_$delay"
            )
            
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(scale)
                    .background(dotColor, CircleShape)
            )
        }
    }
}

@Stable
@Composable
fun AppLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Primary,
    trackColor: Color = SurfaceVariant,
    height: Dp = 4.dp,
    animated: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (animated) {
            tween(300, easing = FastOutSlowInEasing)
        } else {
            androidx.compose.animation.core.snap()
        },
        label = "linear_progress"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(height)
                .background(color, RoundedCornerShape(height / 2))
        )
    }
}

@Stable
@Composable
fun IndeterminateLinearProgress(
    modifier: Modifier = Modifier,
    color: Color = Primary,
    trackColor: Color = SurfaceVariant,
    height: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "indeterminate")
    
    val firstLineHead by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "line_head"
    )
    
    val firstLineTail by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "line_tail"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        val headOffset = firstLineHead * 1000f
        val tailOffset = firstLineTail * 1000f - 200f
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .drawBehind {
                    val barWidth = size.width
                    val headX = (headOffset % 1000f) / 1000f * barWidth
                    val tailX = ((tailOffset % 1000f) / 1000f * barWidth).coerceAtLeast(0f)
                    
                    if (headX > tailX) {
                        drawRect(
                            color = color,
                            topLeft = Offset(tailX, 0f),
                            size = androidx.compose.ui.geometry.Size(headX - tailX, size.height)
                        )
                    }
                }
        )
    }
}

@Stable
@Composable
fun PulsingLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = Primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_loader")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * scale)
                .background(color.copy(alpha = alpha), CircleShape)
        )
        
        Box(
            modifier = Modifier
                .size(size * 0.5f)
                .background(color, CircleShape)
        )
    }
}

@Preview
@Composable
private fun SkeletonLoaderPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        SkeletonLoader(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
    }
}

@Preview
@Composable
private fun SkeletonItemPreview() {
    SkeletonItem(lines = 3)
}

@Preview
@Composable
private fun SkeletonCardPreview() {
    SkeletonCard()
}

@Preview
@Composable
private fun AppCircularProgressPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppCircularProgress(size = 32.dp, strokeWidth = 3.dp)
        AppCircularProgress(size = 48.dp, strokeWidth = 4.dp)
        AppCircularProgress(size = 64.dp, strokeWidth = 6.dp)
    }
}

@Preview
@Composable
private fun DotsTypingIndicatorPreview() {
    DotsTypingIndicator()
}

@Preview
@Composable
private fun AppLinearProgressPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppLinearProgress(progress = 0.25f)
        AppLinearProgress(progress = 0.5f)
        AppLinearProgress(progress = 0.75f)
        AppLinearProgress(progress = 1f)
    }
}

@Preview
@Composable
private fun IndeterminateLinearProgressPreview() {
    IndeterminateLinearProgress()
}

@Preview
@Composable
private fun PulsingLoaderPreview() {
    PulsingLoader()
}

@Preview
@Composable
private fun LoadingScreenPreview() {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonCard()
            SkeletonCard()
            SkeletonCard()
        }
    }
}
