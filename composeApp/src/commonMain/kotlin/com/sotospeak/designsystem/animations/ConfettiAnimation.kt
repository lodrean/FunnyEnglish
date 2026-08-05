package com.sotospeak.designsystem.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.sotospeak.designsystem.accessibility.LocalReduceMotion
import com.sotospeak.designsystem.tokens.AchievementPurple
import com.sotospeak.designsystem.tokens.PrimaryLight
import com.sotospeak.designsystem.tokens.SecondaryLight
import com.sotospeak.designsystem.tokens.TertiaryLight
import com.sotospeak.designsystem.tokens.XPGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * So to Speak Confetti Animation
 * 
 * Priority 5 (Must-have)
 * Trigger: Correct answer, level up, achievement unlock
 * Duration: 1-2 seconds
 * Colors: Primary, Secondary, Tertiary, XP Gold
 * 
 * Physics: Gravity, slight wind, fade out
 */

enum class ConfettiShape {
    CIRCLE,
    SQUARE,
    RECTANGLE
}

enum class ConfettiDirection {
    UP,           // Explosion upward
    DOWN,         // Falling down
    EXPLODE,      // Center explosion
    SIDES         // From sides
}

data class ConfettiParticle(
    val id: Int,
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val shape: ConfettiShape,
    val rotation: Float,
    val rotationSpeed: Float,
    val velocityX: Float,
    val velocityY: Float,
    val gravity: Float = 0.5f,
    val drag: Float = 0.98f
)

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    particleCount: Int = 50,
    duration: Int = 2000,
    direction: ConfettiDirection = ConfettiDirection.EXPLODE,
    colors: List<Color> = listOf(
        PrimaryLight,
        SecondaryLight,
        TertiaryLight,
        XPGold,
        AchievementPurple
    ),
    onAnimationEnd: () -> Unit = {}
) {
    val reduceMotion = LocalReduceMotion.current
    val density = LocalDensity.current
    val particles = remember { mutableStateListOf<ConfettiParticle>() }
    val animatables = remember { mutableStateListOf<Animatable<Float, *>>() }
    
    // Skip animation if reduce motion is enabled
    if (reduceMotion) {
        if (isActive) {
            LaunchedEffect(Unit) {
                delay(300)
                onAnimationEnd()
            }
        }
        return
    }
    
    LaunchedEffect(isActive) {
        if (isActive) {
            // Initialize particles
            particles.clear()
            animatables.clear()
            
            repeat(particleCount) { index ->
                val particle = createParticle(
                    id = index,
                    direction = direction,
                    colors = colors
                )
                particles.add(particle)
                animatables.add(Animatable(1f))
            }
            
            // Animate all particles
            animatables.forEachIndexed { index, animatable ->
                launch {
                    delay(Random.nextLong(0, 200))
                    animatable.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = duration,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            }
            
            delay(duration.toLong())
            particles.clear()
            onAnimationEnd()
        }
    }
    
    if (particles.isNotEmpty()) {
        Box(modifier = modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerX = width / 2
                val centerY = height / 2
                
                particles.forEachIndexed { index, particle ->
                    val progress = animatables.getOrNull(index)?.value ?: 0f
                    
                    // Update position based on physics
                    val time = (1f - progress) * (duration / 16f) // Approximate frames
                    val newX = when (direction) {
                        ConfettiDirection.EXPLODE -> centerX + particle.velocityX * time
                        ConfettiDirection.UP -> particle.x + particle.velocityX * time
                        ConfettiDirection.DOWN -> particle.x + particle.velocityX * time
                        ConfettiDirection.SIDES -> particle.x + particle.velocityX * time
                    }
                    
                    val newY = when (direction) {
                        ConfettiDirection.EXPLODE -> 
                            centerY + particle.velocityY * time + 0.5f * particle.gravity * time * time
                        ConfettiDirection.UP -> 
                            height - (particle.velocityY * time - 0.5f * particle.gravity * time * time)
                        ConfettiDirection.DOWN -> 
                            particle.y + particle.velocityY * time + 0.5f * particle.gravity * time * time
                        ConfettiDirection.SIDES -> 
                            centerY + particle.velocityY * time + 0.5f * particle.gravity * time * time
                    }
                    
                    // Update rotation
                    val currentRotation = particle.rotation + particle.rotationSpeed * time
                    
                    // Draw particle
                    translate(left = newX, top = newY) {
                        rotate(degrees = currentRotation) {
                            scale(scale = progress) {
                                when (particle.shape) {
                                    ConfettiShape.CIRCLE -> {
                                        drawCircle(
                                            color = particle.color,
                                            radius = particle.size,
                                            alpha = progress
                                        )
                                    }
                                    ConfettiShape.SQUARE -> {
                                        drawRect(
                                            color = particle.color,
                                            size = androidx.compose.ui.geometry.Size(
                                                particle.size * 2,
                                                particle.size * 2
                                            ),
                                            alpha = progress
                                        )
                                    }
                                    ConfettiShape.RECTANGLE -> {
                                        drawRect(
                                            color = particle.color,
                                            size = androidx.compose.ui.geometry.Size(
                                                particle.size * 2,
                                                particle.size * 1.2f
                                            ),
                                            alpha = progress
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun createParticle(
    id: Int,
    direction: ConfettiDirection,
    colors: List<Color>
): ConfettiParticle {
    val random = Random(id)
    
    val (velocityX, velocityY) = when (direction) {
        ConfettiDirection.EXPLODE -> {
            val angle = random.nextFloat() * 2 * PI.toFloat()
            val speed = random.nextFloat() * 15 + 5
            Pair(cos(angle) * speed, sin(angle) * speed)
        }
        ConfettiDirection.UP -> {
            Pair(
                (random.nextFloat() - 0.5f) * 10,
                -random.nextFloat() * 20 - 10
            )
        }
        ConfettiDirection.DOWN -> {
            Pair(
                (random.nextFloat() - 0.5f) * 10,
                random.nextFloat() * 5
            )
        }
        ConfettiDirection.SIDES -> {
            val fromLeft = random.nextBoolean()
            Pair(
                if (fromLeft) random.nextFloat() * 10 + 5 else -random.nextFloat() * 10 - 5,
                (random.nextFloat() - 0.5f) * 10
            )
        }
    }
    
    return ConfettiParticle(
        id = id,
        x = random.nextFloat() * 1000,
        y = if (direction == ConfettiDirection.UP) 1000f else random.nextFloat() * 500f,
        size = random.nextFloat() * 8 + 4,
        color = colors.random(),
        shape = ConfettiShape.entries.random(),
        rotation = random.nextFloat() * 360,
        rotationSpeed = (random.nextFloat() - 0.5f) * 10,
        velocityX = velocityX,
        velocityY = velocityY,
        gravity = random.nextFloat() * 0.5f + 0.3f,
        drag = random.nextFloat() * 0.02f + 0.97f
    )
}

/**
 * Success celebration with confetti
 */
@Composable
fun SuccessCelebration(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    ConfettiAnimation(
        isActive = isVisible,
        particleCount = 80,
        duration = 2000,
        direction = ConfettiDirection.EXPLODE,
        onAnimationEnd = onComplete,
        modifier = modifier
    )
}

/**
 * XP gain celebration
 */
@Composable
fun XPGainCelebration(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    ConfettiAnimation(
        isActive = isVisible,
        particleCount = 40,
        duration = 1500,
        direction = ConfettiDirection.UP,
        colors = listOf(XPGold, AchievementPurple),
        onAnimationEnd = onComplete,
        modifier = modifier
    )
}

/**
 * Level up celebration (extended)
 */
@Composable
fun LevelUpCelebration(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    ConfettiAnimation(
        isActive = isVisible,
        particleCount = 100,
        duration = 3000,
        direction = ConfettiDirection.EXPLODE,
        onAnimationEnd = onComplete,
        modifier = modifier
    )
}
