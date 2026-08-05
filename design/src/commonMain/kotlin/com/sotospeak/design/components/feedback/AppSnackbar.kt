package com.sotospeak.design.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sotospeak.design.theme.SoToSpeakTheme

enum class SnackbarType {
    SUCCESS, ERROR, INFO
}

@Composable
fun AppSnackbar(
    message: String,
    type: SnackbarType,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val (icon, containerColor, contentColor) = when (type) {
        SnackbarType.SUCCESS -> Triple(
            Icons.Default.CheckCircle,
            SoToSpeakTheme.colors.successContainer,
            SoToSpeakTheme.colors.onSuccessContainer
        )
        SnackbarType.ERROR -> Triple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        SnackbarType.INFO -> Triple(
            Icons.Default.Info,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )

            actionLabel?.let {
                TextButton(
                    onClick = { onAction?.invoke() }
                ) {
                    Text(
                        text = it,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        // Default to INFO type if using standard SnackbarData
        Snackbar(
            snackbarData = data,
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionColor = MaterialTheme.colorScheme.inversePrimary
        )
    }
}

@Preview
@Composable
private fun AppSnackbarSuccessPreview() {
    SoToSpeakTheme {
        AppSnackbar(
            message = "Lesson completed successfully!",
            type = SnackbarType.SUCCESS,
            actionLabel = "View",
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun AppSnackbarErrorPreview() {
    SoToSpeakTheme {
        AppSnackbar(
            message = "Failed to save progress",
            type = SnackbarType.ERROR,
            actionLabel = "Retry",
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun AppSnackbarInfoPreview() {
    SoToSpeakTheme {
        AppSnackbar(
            message = "New lesson available",
            type = SnackbarType.INFO
        )
    }
}
