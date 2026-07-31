package com.funnyenglish.design.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// FunnyEnglish brand colors
private val Gold = Color(0xFFFFD700)
private val Primary = Color(0xFF6200EE)
private val Success = Color(0xFF4CAF50)
private val Flame = Color(0xFFFF5722)
private val Error = Color(0xFFE53935)

@Stable
@Composable
fun ConfettiEffect(
    active: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    colors: List<Color> = listOf(Gold, Primary, Success, Flame)
) {
    if (!active) return
    
    val particles = remember {
        List(particleCount) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.5f,
                color = colors.random(),
                size = Random.nextFloat() * 8f + 4f,
                velocityX = (Random.nextFloat() - 0.5f) * 4f,
                velocityY = Random.nextFloat() * 8f + 4f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 20f
            )
        }
    }
    
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(active) {
        if (active) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(2000, easing = FastOutSlowInEasing)
            )
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        particles.forEach { particle ->
            val currentX = particle.x * canvasWidth + particle.velocityX * progress.value * 200f
            val currentY = particle.y * canvasHeight + particle.velocityY * progress.value * canvasHeight
            val currentRotation = particle.rotation + particle.rotationSpeed * progress.value * 10f
            val alpha = 1f - progress.value
            
            if (alpha > 0f) {
                drawConfettiParticle(
                    x = currentX,
                    y = currentY,
                    size = particle.size,
                    color = particle.color.copy(alpha = alpha),
                    rotation = currentRotation
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

private fun DrawScope.drawConfettiParticle(
    x: Float,
    y: Float,
    size: Float,
    color: Color,
    rotation: Float
) {
    val radians = rotation * PI / 180f
    val cos = cos(radians).toFloat()
    val sin = sin(radians).toFloat()
    
    drawRect(
        color = color,
        topLeft = Offset(x - size / 2, y - size / 4),
        size = androidx.compose.ui.geometry.Size(size, size / 2)
    )
}

@Stable
@Composable
fun StarFillAnimation(
    filled: Boolean,
    modifier: Modifier = Modifier,
    starColor: Color = Gold,
    animationDuration: Int = 500
) {
    val scale by animateFloatAsState(
        targetValue = if (filled) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = animationDuration
            0f at 0
            1.3f at (animationDuration * 0.6).toInt()
            1f at animationDuration
        },
        label = "star_fill_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (filled) 1f else 0.3f,
        animationSpec = tween(animationDuration),
        label = "star_fill_alpha"
    )
    
    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = if (filled) "Filled Star" else "Empty Star",
        modifier = modifier
            .scale(scale.coerceAtLeast(0f))
            .size(48.dp),
        tint = starColor.copy(alpha = alpha)
    )
}

@Stable
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    pulseScale: Float = 1.2f,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = pulseScale,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = modifier.scale(scale),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Stable
@Composable
fun ShakeAnimation(
    shake: Boolean,
    modifier: Modifier = Modifier,
    shakeIntensity: Float = 10f,
    content: @Composable () -> Unit
) {
    val shakeOffset = remember { Animatable(0f) }
    
    LaunchedEffect(shake) {
        if (shake) {
            repeat(5) { index ->
                val direction = if (index % 2 == 0) 1f else -1f
                shakeOffset.animateTo(
                    targetValue = shakeIntensity * direction,
                    animationSpec = tween(50)
                )
            }
            shakeOffset.animateTo(0f, animationSpec = tween(100))
        }
    }
    
    Box(
        modifier = modifier.offset(x = shakeOffset.value.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Stable
@Composable
fun BounceAnimation(
    bounce: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (bounce) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce_scale"
    )
    
    Box(
        modifier = modifier.scale(scale.coerceIn(0.5f, 1.5f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Stable
@Composable
fun CountUpAnimation(
    targetValue: Int,
    modifier: Modifier = Modifier,
    durationMillis: Int = 1000,
    suffix: String = ""
) {
    var currentValue by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(targetValue) {
        val startValue = currentValue
        val diff = targetValue - startValue
        val startTime = System.currentTimeMillis()
        
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / durationMillis).coerceIn(0f, 1f)
            
            // Ease out cubic
            val easedProgress = 1f - (1f - progress).pow(3)
            currentValue = (startValue + diff * easedProgress).toInt()
            
            if (progress >= 1f) {
                currentValue = targetValue
                break
            }
            
            delay(16) // ~60fps
        }
    }
    
    val displayValue by remember(currentValue, targetValue) {
        derivedStateOf { currentValue }
    }
    
    Text(
        text = "${displayValue}$suffix",
        modifier = modifier,
        style = MaterialTheme.typography.headlineLarge,
        fontSize = 48.sp
    )
}

private fun Float.pow(n: Int): Float {
    var result = 1f
    repeat(n) { result *= this }
    return result
}

// Streak flame animation
@Stable
@Composable
fun StreakFlameAnimation(
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )
    
    val flameRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_rotation"
    )
    
    Box(
        modifier = modifier
            .scale(flameScale)
            .rotate(flameRotation),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Flame",
                tint = Flame,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "$streakCount",
                color = Flame,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

// Success checkmark animation
@Stable
@Composable
fun SuccessCheckmarkAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 64.dp
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkmark_scale"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else -180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkmark_rotation"
    )
    
    if (scale > 0.01f) {
        Box(
            modifier = modifier
                .size(size)
                .scale(scale)
                .rotate(rotation)
                .background(Success, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = Color.White,
                fontSize = (size.value * 0.6).sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StarFillAnimationPreview() {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StarFillAnimation(filled = false)
        Spacer(modifier = Modifier.size(8.dp))
        StarFillAnimation(filled = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun PulseAnimationPreview() {
    PulseAnimation {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Flame, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🔥", fontSize = 32.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShakeAnimationPreview() {
    var shake by remember { mutableFloatStateOf(false) }
    
    ShakeAnimation(shake = shake) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Error, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✗", color = Color.White, fontSize = 32.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BounceAnimationPreview() {
    BounceAnimation(bounce = true) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Success, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = Color.White, fontSize = 32.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CountUpAnimationPreview() {
    CountUpAnimation(
        targetValue = 1000,
        suffix = " XP"
    )
}

@Preview(showBackground = true)
@Composable
private fun StreakFlameAnimationPreview() {
    StreakFlameAnimation(streakCount = 7)
}

@Preview(showBackground = true)
@Composable
private fun SuccessCheckmarkAnimationPreview() {
    SuccessCheckmarkAnimation(visible = true)
}

@Preview(showBackground = true)
@Composable
private fun ConfettiEffectPreview() {
    ConfettiEffect(active = true)
}
