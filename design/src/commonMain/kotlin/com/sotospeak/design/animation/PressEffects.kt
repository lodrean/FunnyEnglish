package com.sotospeak.design.animation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private const val BUTTON_PRESS_SCALE = 0.95f
private const val CARD_PRESS_SCALE = 0.98f
private const val PRESS_ANIMATION_STIFFNESS = 400f

@Stable
@Composable
fun ButtonPressEffect(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) BUTTON_PRESS_SCALE else 1f,
        animationSpec = spring(stiffness = PRESS_ANIMATION_STIFFNESS),
        label = "button_scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        content()
    }
}

@Stable
@Composable
fun CardPressEffect(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    defaultElevation: androidx.compose.ui.unit.Dp = 2.dp,
    pressedElevation: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) CARD_PRESS_SCALE else 1f,
        animationSpec = spring(stiffness = PRESS_ANIMATION_STIFFNESS),
        label = "card_scale"
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (isPressed && enabled) 
            pressedElevation.value else defaultElevation.value,
        animationSpec = spring(stiffness = PRESS_ANIMATION_STIFFNESS),
        label = "card_elevation"
    )
    
    ElevatedCard(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.scale(scale),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = elevation.dp,
            pressedElevation = pressedElevation
        )
    ) {
        content()
    }
}

@Stable
@Composable
fun ListItemPressEffect(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    defaultBackgroundColor: Color = Color.Transparent,
    pressedBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isPressed && enabled) pressedBackgroundColor else defaultBackgroundColor,
        animationSpec = androidx.compose.animation.core.tween(100),
        label = "list_item_background"
    )
    
    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                enabled = enabled,
                onClick = onClick
            )
    ) {
        content()
    }
}

// Extension function for easier usage
@Stable
fun Modifier.pressEffect(
    scale: Float = BUTTON_PRESS_SCALE,
    stiffness: Float = PRESS_ANIMATION_STIFFNESS
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) scale else 1f,
        animationSpec = spring(stiffness = stiffness),
        label = "press_effect_scale"
    )
    
    this
        .scale(animatedScale)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = { }
        )
}

@Stable
fun Modifier.bounceClick(
    onClick: () -> Unit,
    enabled: Boolean = true
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = 500f
        ),
        label = "bounce_click"
    )
    
    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Button,
            onClick = onClick
        )
}

@Preview
@Composable
private fun ButtonPressEffectPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ButtonPressEffect(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Press Me",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Preview
@Composable
private fun CardPressEffectPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        CardPressEffect(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Card Content",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun ListItemPressEffectPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ListItemPressEffect(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "List Item",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
