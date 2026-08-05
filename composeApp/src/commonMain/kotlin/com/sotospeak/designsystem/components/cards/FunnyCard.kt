package com.sotospeak.designsystem.components.cards

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.sotospeak.designsystem.accessibility.LocalReduceMotion
import com.sotospeak.designsystem.tokens.CardPadding
import com.sotospeak.designsystem.tokens.CardRadius
import com.sotospeak.designsystem.tokens.CardShape
import com.sotospeak.designsystem.tokens.ElevationLarge
import com.sotospeak.designsystem.tokens.ElevationMedium

/**
 * So to Speak Card Component
 *
 * Three types: ELEVATED (MD3 level 1/3), FILLED, OUTLINED
 * Corner radius: 16dp (MD3 large, intentional playful deviation from strict 12dp)
 * Padding: 16dp
 *
 * Hover/press animations for desktop
 */

enum class FunnyCardType {
    ELEVATED,  // MD3 level 1 default, level 3 featured
    FILLED,    // Surface variant background
    OUTLINED   // 1dp outline border
}

enum class FunnyCardElevation {
    DEFAULT,  // MD3 level 1 (1dp)
    FEATURED  // MD3 level 3 (6dp)
}

@Composable
fun FunnyCard(
    modifier: Modifier = Modifier,
    type: FunnyCardType = FunnyCardType.ELEVATED,
    elevation: FunnyCardElevation = FunnyCardElevation.DEFAULT,
    shape: Shape = CardShape,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current

    // Elevation animation
    val targetElevation = when {
        !reduceMotion && isPressed -> ElevationMedium
        !reduceMotion && isHovered -> ElevationLarge
        else -> when (elevation) {
            FunnyCardElevation.DEFAULT -> ElevationMedium
            FunnyCardElevation.FEATURED -> ElevationLarge
        }
    }

    val animatedElevation by animateDpAsState(
        targetValue = targetElevation,
        label = "card_elevation"
    )

    when (type) {
        FunnyCardType.ELEVATED -> {
            if (onClick != null) {
                ElevatedCard(
                    onClick = onClick,
                    modifier = modifier.fillMaxWidth(),
                    shape = shape,
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = if (reduceMotion) targetElevation else animatedElevation,
                        pressedElevation = ElevationMedium,
                        hoveredElevation = ElevationLarge
                    ),
                    interactionSource = interactionSource
                ) {
                    CardContent(content)
                }
            } else {
                ElevatedCard(
                    modifier = modifier.fillMaxWidth(),
                    shape = shape,
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = if (reduceMotion) targetElevation else animatedElevation
                    )
                ) {
                    CardContent(content)
                }
            }
        }

        FunnyCardType.FILLED -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                CardContent(content)
            }
        }

        FunnyCardType.OUTLINED -> {
            OutlinedCard(
                modifier = modifier.fillMaxWidth(),
                shape = shape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp)
            ) {
                CardContent(content)
            }
        }
    }
}

@Composable
private fun CardContent(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(CardPadding)
    ) {
        content()
    }
}

/**
 * Featured card with higher elevation and accent border
 */
@Composable
fun FunnyFeaturedCard(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit) = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = ElevationMedium,
        shadowElevation = ElevationLarge,
        border = BorderStroke(2.dp, accentColor),
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier.padding(CardPadding)
        ) {
            content()
        }
    }
}
