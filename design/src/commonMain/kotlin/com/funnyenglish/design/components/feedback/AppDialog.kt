package com.funnyenglish.design.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.funnyenglish.design.components.buttons.PrimaryButton
import com.funnyenglish.design.components.buttons.SecondaryButton
import com.funnyenglish.design.theme.FunnyEnglishTheme

@Composable
fun AppDialog(
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null,
    icon: ImageVector? = null
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        modifier = modifier,
        icon = icon?.let {
            { Icon(imageVector = it, contentDescription = null) }
        },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            PrimaryButton(
                text = confirmButtonText,
                onClick = onConfirm
            )
        },
        dismissButton = dismissButtonText?.let {
            {
                TextButton(onClick = { onDismiss?.invoke() }) {
                    Text(it)
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmButtonText: String,
    dismissButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isDestructive: Boolean = false
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isDestructive) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = dismissButtonText,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = confirmButtonText,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AppDialogLightPreview() {
    FunnyEnglishTheme(darkTheme = false) {
        AppDialog(
            title = "Unlock Premium?",
            text = "Get access to all lessons and features with Premium.",
            confirmButtonText = "Subscribe",
            dismissButtonText = "Maybe Later",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun ConfirmDialogDarkPreview() {
    FunnyEnglishTheme(darkTheme = true) {
        ConfirmDialog(
            title = "Delete Account?",
            text = "This action cannot be undone. All your progress will be lost.",
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel",
            onConfirm = {},
            onDismiss = {},
            icon = Icons.Default.Warning,
            isDestructive = true
        )
    }
}

@Preview
@Composable
private fun AppDialogWithIconPreview() {
    FunnyEnglishTheme {
        AppDialog(
            title = "Daily Goal Reached!",
            text = "Congratulations! You've completed your daily goal of 15 minutes.",
            confirmButtonText = "Awesome!",
            onConfirm = {},
            icon = Icons.Default.Warning
        )
    }
}
