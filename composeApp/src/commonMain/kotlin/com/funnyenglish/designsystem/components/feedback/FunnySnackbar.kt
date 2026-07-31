package com.funnyenglish.designsystem.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.tokens.CardShape
import com.funnyenglish.designsystem.tokens.SpaceMd
import com.funnyenglish.designsystem.tokens.SpaceSm

/**
 * FunnyEnglish Snackbar Component
 * 
 * Four severity levels: INFO, SUCCESS, WARNING, ERROR
 * Duration: 4-10 seconds based on complexity
 * Position: Bottom-center (desktop)
 * Action button support with inverse color
 */

enum class FunnySnackbarType {
    INFO,     // Primary blue
    SUCCESS,  // Success green
    WARNING,  // Warning yellow
    ERROR     // Error red
}

/**
 * Custom Snackbar Visuals for extended configuration
 */
class FunnySnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = true,
    val type: FunnySnackbarType = FunnySnackbarType.INFO
) : SnackbarVisuals

/**
 * FunnyEnglish Snackbar with type support
 */
@Composable
fun FunnySnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    type: FunnySnackbarType = FunnySnackbarType.INFO
) {
    val (icon, containerColor, contentColor) = when (type) {
        FunnySnackbarType.INFO -> Triple(
            Icons.Default.Info,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
        FunnySnackbarType.SUCCESS -> Triple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.primary,  // Use primary for consistency
            MaterialTheme.colorScheme.onPrimary
        )
        FunnySnackbarType.WARNING -> Triple(
            Icons.Default.Warning,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary
        )
        FunnySnackbarType.ERROR -> Triple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError
        )
    }
    
    Snackbar(
        modifier = modifier.padding(SpaceMd),
        action = {
            snackbarData.visuals.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = { snackbarData.performAction() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = contentColor
                    )
                ) {
                    Text(actionLabel)
                }
            }
        },
        dismissAction = {
            if (snackbarData.visuals.withDismissAction) {
                IconButton(
                    onClick = { snackbarData.dismiss() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = contentColor
                    )
                }
            }
        },
        shape = CardShape,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpaceSm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            Text(
                text = snackbarData.visuals.message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Snackbar Host with FunnyEnglish styling
 */
@Composable
fun FunnySnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    defaultType: FunnySnackbarType = FunnySnackbarType.INFO
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            // Try to extract type from custom visuals
            val type = if (snackbarData.visuals is FunnySnackbarVisuals) {
                (snackbarData.visuals as FunnySnackbarVisuals).type
            } else {
                defaultType
            }
            
            FunnySnackbar(
                snackbarData = snackbarData,
                type = type
            )
        }
    )
}

/**
 * Helper functions to show different snackbar types
 */
suspend fun SnackbarHostState.showInfoSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    return showSnackbar(
        FunnySnackbarVisuals(
            message = message,
            actionLabel = actionLabel,
            duration = duration,
            type = FunnySnackbarType.INFO
        )
    )
}

suspend fun SnackbarHostState.showSuccessSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    return showSnackbar(
        FunnySnackbarVisuals(
            message = message,
            actionLabel = actionLabel,
            duration = duration,
            type = FunnySnackbarType.SUCCESS
        )
    )
}

suspend fun SnackbarHostState.showWarningSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Long
): SnackbarResult {
    return showSnackbar(
        FunnySnackbarVisuals(
            message = message,
            actionLabel = actionLabel,
            duration = duration,
            type = FunnySnackbarType.WARNING
        )
    )
}

suspend fun SnackbarHostState.showErrorSnackbar(
    message: String,
    actionLabel: String? = "Retry",
    duration: SnackbarDuration = SnackbarDuration.Long
): SnackbarResult {
    return showSnackbar(
        FunnySnackbarVisuals(
            message = message,
            actionLabel = actionLabel,
            duration = duration,
            type = FunnySnackbarType.ERROR
        )
    )
}
