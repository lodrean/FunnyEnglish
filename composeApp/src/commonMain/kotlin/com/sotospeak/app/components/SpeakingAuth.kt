package com.sotospeak.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Auth/onboarding/profile — M3-компоненты с фирменной темой (спека DS v3.0 §4, DSM-5 §4).
 *
 * Кнопки и ссылки (бывшие C1–C4) заменены стоковыми M3 в точках вызова —
 * брендинг даёт схема FunnyTheme (primary=primaryStrong #3B6FD4, shape medium 16,
 * labelLarge 16/800). Здесь остались композиции: SpeakingField (C5) и SpeakingGate (C6).
 */

/**
 * Поле ввода — M3 OutlinedTextField (C5): label в бордере, radius 16 (shapes.medium),
 * focused border 2dp primary, ошибка — supportingText цвета errorText (#B3261E, WCAG AA).
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

    // Обёртка сохраняет modifier (testTag …_field), поле — inputModifier (testTag …_input):
    // оба тега на одном узле перезаписывают друг друга — тесты держатся за оба (грабля №16).
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = inputModifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            enabled = enabled,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            isError = error != null,
            supportingText = error?.let { err ->
                {
                    Text(
                        text = err,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = speaking.errorText // #B3261E — WCAG AA для мелкого текста
                    )
                }
            },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = speaking.error,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                errorContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                errorCursorColor = speaking.error,
                focusedLabelColor = speaking.primary,
                unfocusedLabelColor = speaking.textMuted,
                disabledLabelColor = speaking.textMuted,
                focusedPlaceholderColor = speaking.textMuted,
                unfocusedPlaceholderColor = speaking.textMuted,
                disabledPlaceholderColor = speaking.textMuted
            )
        )
    }
}

/**
 * Гейт (locked/empty state) — C6: M3 Filled Card (surfaceContainerHigh, shape large 22)
 * с кругом 120dp на secondaryContainer, заголовком titleMedium и действиями.
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
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
}
