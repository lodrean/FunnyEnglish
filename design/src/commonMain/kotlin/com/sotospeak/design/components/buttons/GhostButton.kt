package com.sotospeak.design.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sotospeak.design.theme.SoToSpeakTheme

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "button_scale"
    )

    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        },
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
                role = Role.Button
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics {
                contentDescription = text
            }
    )
}

@Preview
@Composable
private fun GhostButtonLightPreview() {
    SoToSpeakTheme(darkTheme = false) {
        GhostButton(
            text = "Forgot password?",
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun GhostButtonDarkPreview() {
    SoToSpeakTheme(darkTheme = true) {
        GhostButton(
            text = "Forgot password?",
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun GhostButtonDisabledPreview() {
    SoToSpeakTheme {
        GhostButton(
            text = "Forgot password?",
            onClick = {},
            enabled = false
        )
    }
}
