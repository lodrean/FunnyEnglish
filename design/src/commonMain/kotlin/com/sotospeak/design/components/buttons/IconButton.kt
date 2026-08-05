package com.sotospeak.design.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import com.sotospeak.design.theme.SoToSpeakTheme

@Composable
fun IconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "icon_button_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .size(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
                role = Role.Button
            )
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = if (enabled) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        }
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview
@Composable
private fun IconButtonLightPreview() {
    SoToSpeakTheme(darkTheme = false) {
        IconButton(
            icon = Icons.Default.Close,
            onClick = {},
            contentDescription = "Close"
        )
    }
}

@Preview
@Composable
private fun IconButtonDarkPreview() {
    SoToSpeakTheme(darkTheme = true) {
        IconButton(
            icon = Icons.Default.ArrowBack,
            onClick = {},
            contentDescription = "Go back"
        )
    }
}

@Preview
@Composable
private fun IconButtonDisabledPreview() {
    SoToSpeakTheme {
        IconButton(
            icon = Icons.Default.Close,
            onClick = {},
            enabled = false,
            contentDescription = "Close"
        )
    }
}
