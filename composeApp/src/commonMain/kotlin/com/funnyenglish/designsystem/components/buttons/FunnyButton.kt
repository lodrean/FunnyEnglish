package com.funnyenglish.designsystem.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.accessibility.FunnyAnimationSpecs
import com.funnyenglish.designsystem.accessibility.LocalReduceMotion
import com.funnyenglish.designsystem.tokens.ButtonHeightLarge
import com.funnyenglish.designsystem.tokens.ButtonHeightMedium
import com.funnyenglish.designsystem.tokens.ButtonHeightSmall
import com.funnyenglish.designsystem.tokens.ButtonPaddingHorizontalLarge
import com.funnyenglish.designsystem.tokens.ButtonPaddingHorizontalMedium
import com.funnyenglish.designsystem.tokens.ButtonPaddingHorizontalSmall
import com.funnyenglish.designsystem.tokens.ButtonShape
import com.funnyenglish.designsystem.tokens.IconSizeMedium
import com.funnyenglish.designsystem.tokens.SpaceSm

/**
 * FunnyEnglish Button Component
 * 
 * Five semantic types: PRIMARY, SECONDARY, TERTIARY, GHOST, DESTRUCTIVE
 * Three sizes: SMALL, MEDIUM, LARGE
 * States: Rest, Hover, Pressed, Loading, Disabled
 * 
 * Animation: Scale 0.95 on press (150ms, FAST duration)
 */

enum class FunnyButtonType {
    PRIMARY,      // Filled container, main action
    SECONDARY,    // Outlined container, alternative action
    TERTIARY,     // Text only, low priority
    GHOST,        // Subtle background, toolbar actions
    DESTRUCTIVE   // Error color, irreversible actions
}

enum class FunnyButtonSize {
    SMALL,   // 36dp height
    MEDIUM,  // 48dp height (default)
    LARGE    // 56dp height, CTA
}

@Composable
fun FunnyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    type: FunnyButtonType = FunnyButtonType.PRIMARY,
    size: FunnyButtonSize = FunnyButtonSize.MEDIUM,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    shape: Shape = ButtonShape,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit = {}
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    
    // Scale animation on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) 0.95f else 1f,
        animationSpec = if (reduceMotion) {
            androidx.compose.animation.core.snap()
        } else {
            FunnyAnimationSpecs.fast<Float>().let { it as androidx.compose.animation.core.AnimationSpec<Float> }
        },
        label = "button_scale"
    )
    
    val colors = when (type) {
        FunnyButtonType.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        FunnyButtonType.SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        FunnyButtonType.TERTIARY -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        FunnyButtonType.GHOST -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        FunnyButtonType.DESTRUCTIVE -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
    
    val border = when (type) {
        FunnyButtonType.SECONDARY -> BorderStroke(
            width = 1.dp,
            color = if (enabled) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
        else -> null
    }
    
    val height = when (size) {
        FunnyButtonSize.SMALL -> ButtonHeightSmall
        FunnyButtonSize.MEDIUM -> ButtonHeightMedium
        FunnyButtonSize.LARGE -> ButtonHeightLarge
    }
    
    val horizontalPadding = when (size) {
        FunnyButtonSize.SMALL -> ButtonPaddingHorizontalSmall
        FunnyButtonSize.MEDIUM -> ButtonPaddingHorizontalMedium
        FunnyButtonSize.LARGE -> ButtonPaddingHorizontalLarge
    }
    
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .scale(scale),
        enabled = enabled && !loading,
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = SpaceSm),
        interactionSource = interactionSource
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(IconSizeMedium),
                color = if (type == FunnyButtonType.PRIMARY || type == FunnyButtonType.DESTRUCTIVE) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(IconSizeMedium)
                    )
                }
                iconPainter?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier.size(IconSizeMedium)
                    )
                }
                text?.let {
                    Text(
                        text = it,
                        style = when (size) {
                            FunnyButtonSize.SMALL -> MaterialTheme.typography.labelMedium
                            else -> MaterialTheme.typography.labelLarge
                        }
                    )
                }
                content()
            }
        }
    }
}

/**
 * Icon-only button variant
 */
@Composable
fun FunnyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    type: FunnyButtonType = FunnyButtonType.GHOST,
    size: FunnyButtonSize = FunnyButtonSize.MEDIUM,
    enabled: Boolean = true,
    shape: Shape = ButtonShape
) {
    FunnyButton(
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        type = type,
        size = size,
        enabled = enabled,
        shape = shape
    ) {
        // Hidden text for accessibility
        Text(
            text = contentDescription,
            modifier = Modifier.size(0.dp)
        )
    }
}
