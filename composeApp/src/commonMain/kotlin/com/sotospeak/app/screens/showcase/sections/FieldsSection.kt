package com.sotospeak.app.screens.showcase.sections

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.sotospeak.app.components.SpeakingField
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Демо «Поля ввода»: SpeakingField (M3 OutlinedTextField, radius 16) в
 * обычном / password / error / disabled состояниях.
 */
@Composable
fun FieldsSection(onBack: () -> Unit) {
    val speaking = LocalSpeakingColors.current
    var email by remember { mutableStateOf("learner@sotospeak.com") }
    var password by remember { mutableStateOf("") }
    var invalid by remember { mutableStateOf("learner@") }

    ShowcaseScaffold(section = ShowcaseSection.Fields, onBack = onBack) {
        DemoCard(title = "SpeakingField · обычное") {
            SpeakingField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "you@example.com",
                keyboardType = KeyboardType.Email
            )
        }
        DemoCard(title = "SpeakingField · пароль") {
            SpeakingField(
                value = password,
                onValueChange = { password = it },
                label = "Пароль",
                isPassword = true,
                imeAction = ImeAction.Done
            )
        }
        DemoCard(title = "SpeakingField · ошибка") {
            SpeakingField(
                value = invalid,
                onValueChange = { invalid = it },
                label = "Email",
                error = "Введите корректный email"
            )
            Text(
                text = "supportingText — errorText #B3261E (WCAG AA для мелкого текста)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = speaking.textMuted
            )
        }
        DemoCard(title = "SpeakingField · disabled") {
            SpeakingField(
                value = "Недоступно",
                onValueChange = {},
                label = "Имя пользователя",
                enabled = false
            )
        }
    }
}
