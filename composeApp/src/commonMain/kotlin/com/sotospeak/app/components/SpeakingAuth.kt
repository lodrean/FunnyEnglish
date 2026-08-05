package com.sotospeak.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes

/**
 * Компоненты auth/onboarding/profile по мокапам Playful Coach v1.1
 * (.docs/design-system/mockups.html, секция AUTH & ONBOARDING).
 *
 * Размеры — tokens.json font.scale: labelSmall 12 / bodySmall 14 /
 * bodyMedium 16 / titleMedium 20 / headlineSmall 31.
 */

/**
 * Поле ввода .field/.input: label над полем (labelSmall, extrabold),
 * surface bg, 2dp border outline, radius-button 16, min-height touch 48.
 * Ошибка: border error + текст [error] цвета errorText (#B3261E, WCAG AA).
 */
@Composable
fun SpeakingField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    isPassword: Boolean = false,
    inputModifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.48.sp, // .04em от 12sp
            color = speaking.text
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = inputModifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                color = speaking.text
            ),
            placeholder = placeholder?.let {
                { Text(it, color = speaking.textMuted) }
            },
            isError = error != null,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            singleLine = true,
            shape = SpeakingShapes.Button,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = speaking.primary,
                unfocusedBorderColor = speaking.outline,
                errorBorderColor = speaking.error,
                disabledBorderColor = speaking.outline.copy(alpha = 0.5f),
                focusedContainerColor = speaking.surface,
                unfocusedContainerColor = speaking.surface,
                errorContainerColor = speaking.surface,
                disabledContainerColor = speaking.surface,
                cursorColor = speaking.primary,
                errorCursorColor = speaking.error
            )
        )
        if (error != null) {
            Text(
                text = error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = speaking.errorText // #B3261E — WCAG AA для мелкого текста
            )
        }
    }
}

/** Primary .btn.btn-primary.btn-wide: radius-button 16, min-height 48. */
@Composable
fun SpeakingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val speaking = LocalSpeakingColors.current

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        enabled = enabled,
        shape = SpeakingShapes.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = speaking.primaryStrong,
            contentColor = speaking.onPrimary
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/** Ghost .btn.btn-ghost.btn-wide: transparent bg, текст primary. */
@Composable
fun SpeakingGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        shape = SpeakingShapes.Button,
        colors = ButtonDefaults.textButtonColors(contentColor = speaking.primary)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/** Danger-ghost .btn.btn-danger-ghost.btn-wide: transparent bg, текст #B3261E. */
@Composable
fun SpeakingDangerGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        shape = SpeakingShapes.Button,
        colors = ButtonDefaults.textButtonColors(contentColor = speaking.errorText)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * Текст-ссылка .text-link: «Вопрос? <b>Акцент</b>» — muted текст + primary bold часть.
 * Вся строка кликабельна (min-height touch 48).
 */
@Composable
fun SpeakingTextLink(
    text: String,
    accent: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = SpeakingShapes.Button,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
    ) {
        Row {
            Text(
                text = "$text ",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = speaking.textMuted
            )
            Text(
                text = accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = speaking.primary
            )
        }
    }
}

/**
 * Гейт .gate (locked/empty state): круг 120dp на secondaryContainer,
 * заголовок titleMedium extrabold, текст bodySmall muted.
 * Иконка [icon] (например SpeakingIcons.Lock) ИЛИ emoji [emoji].
 */
@Composable
fun SpeakingGate(
    title: String?,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emoji: String? = null,
    actions: @Composable ColumnScope.() -> Unit
) {
    val speaking = LocalSpeakingColors.current

    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(speaking.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = speaking.secondary,
                    modifier = Modifier.size(48.dp)
                )
            } else if (emoji != null) {
                Text(text = emoji, fontSize = 56.sp)
            }
        }
        if (title != null) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = speaking.text,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = speaking.textMuted,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            actions()
        }
    }
}
