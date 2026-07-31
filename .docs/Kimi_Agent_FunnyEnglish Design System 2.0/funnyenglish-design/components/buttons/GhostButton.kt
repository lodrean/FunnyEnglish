package com.funnyenglish.design.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.funnyenglish.design.theme.FunnyEnglishTheme

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
                indication = rememberRipple(bounded = false),
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

@Preview(showBackground = true)
@Composable
private fun GhostButtonLightPreview() {
    FunnyEnglishTheme(darkTheme = false) {
        GhostButton(
            text = "Forgot password?",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GhostButtonDarkPreview() {
    FunnyEnglishTheme(darkTheme = true) {
        GhostButton(
            text = "Forgot password?",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GhostButtonDisabledPreview() {
    FunnyEnglishTheme {
        GhostButton(
            text = "Forgot password?",
            onClick = {},
            enabled = false
        )
    }
}
