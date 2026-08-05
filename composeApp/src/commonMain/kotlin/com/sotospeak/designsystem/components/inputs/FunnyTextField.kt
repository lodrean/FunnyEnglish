package com.sotospeak.designsystem.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sotospeak.designsystem.tokens.InputBorderWidth
import com.sotospeak.designsystem.tokens.InputBorderWidthFocused
import com.sotospeak.designsystem.tokens.InputHeight
import com.sotospeak.designsystem.tokens.InputRadius
import com.sotospeak.designsystem.tokens.InputFieldShape
import com.sotospeak.designsystem.tokens.SpaceSm

/**
 * So to Speak Text Field Component
 * 
 * Height: 56dp
 * Radius: 12dp
 * Border: 1dp rest, 2dp focused
 * States: DEFAULT, FOCUSED, ERROR, SUCCESS, DISABLED
 * 
 * Features: Leading/trailing icons, clear button, error text
 */

enum class FunnyTextFieldState {
    DEFAULT,
    ERROR,
    SUCCESS,
    DISABLED
}

@Composable
fun FunnyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    state: FunnyTextFieldState = FunnyTextFieldState.DEFAULT,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    singleLine: Boolean = true,
    maxLines: Int = 1,
    enabled: Boolean = state != FunnyTextFieldState.DISABLED,
    readOnly: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = InputFieldShape
) {
    val isError = state == FunnyTextFieldState.ERROR
    val isSuccess = state == FunnyTextFieldState.SUCCESS
    
    // Colors based on state
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isSuccess -> MaterialTheme.colorScheme.primary  // Use primary for success
        else -> MaterialTheme.colorScheme.outline
    }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = InputHeight),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = MaterialTheme.typography.bodyLarge,
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            leadingIcon = leadingIcon?.let {
                { 
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            trailingIcon = {
                when {
                    // Clear button when text is not empty
                    value.isNotEmpty() && enabled -> {
                        IconButton(
                            onClick = { onValueChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Custom trailing icon
                    trailingIcon != null -> {
                        IconButton(
                            onClick = onTrailingIconClick ?: {},
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = trailingIcon,
                                contentDescription = null,
                                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            isError = isError,
            visualTransformation = if (isPassword && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            singleLine = singleLine,
            maxLines = maxLines,
            interactionSource = interactionSource,
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor.copy(alpha = 0.5f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                cursorColor = MaterialTheme.colorScheme.primary,
                errorCursorColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = borderColor,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorLabelColor = MaterialTheme.colorScheme.error
            )
        )
        
        // Helper text or error message
        helperText?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = SpaceSm),
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    isSuccess -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * Password field variant with visibility toggle
 */
@Composable
fun FunnyPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    placeholder: String = "Enter password",
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityToggle: () -> Unit,
    state: FunnyTextFieldState = FunnyTextFieldState.DEFAULT,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {}
) {
    FunnyTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        state = state,
        isPassword = true,
        isPasswordVisible = isPasswordVisible,
        onPasswordVisibilityToggle = onPasswordVisibilityToggle,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        onImeAction = onImeAction
    )
}

/**
 * Search field variant with search icon and clear button
 */
@Composable
fun FunnySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearch: () -> Unit = {}
) {
    FunnyTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        imeAction = ImeAction.Search,
        onImeAction = onSearch
    )
}
