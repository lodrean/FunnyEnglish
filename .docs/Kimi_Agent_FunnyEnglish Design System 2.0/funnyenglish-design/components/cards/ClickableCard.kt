package com.funnyenglish.design.components.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.funnyenglish.design.theme.FunnyEnglishTheme

@Composable
fun ClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                enabled = enabled,
                onClick = onClick,
                role = Role.Button
            )
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 0.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClickableCardLightPreview() {
    FunnyEnglishTheme(darkTheme = false) {
        ClickableCard(onClick = {}) {
            Text(
                text = "Start Lesson",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Tap to begin learning",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClickableCardDarkPreview() {
    FunnyEnglishTheme(darkTheme = true) {
        ClickableCard(onClick = {}) {
            Text(
                text = "Start Lesson",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Tap to begin learning",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClickableCardDisabledPreview() {
    FunnyEnglishTheme {
        ClickableCard(onClick = {}, enabled = false) {
            Text(
                text = "Locked Lesson",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Complete previous lesson first",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
