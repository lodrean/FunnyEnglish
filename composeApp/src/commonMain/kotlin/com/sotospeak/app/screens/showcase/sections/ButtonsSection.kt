package com.sotospeak.app.screens.showcase.sections

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.designsystem.animations.speakingPressable
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Демо «Кнопки»: стоковые M3-кнопки (брендинг даёт тема: primary=primaryStrong,
 * shapes.medium 16dp, labelLarge) + press-эффект speakingPressable на карточке.
 */
@Composable
fun ButtonsSection(onBack: () -> Unit) {
    val speaking = LocalSpeakingColors.current

    ShowcaseScaffold(section = ShowcaseSection.Buttons, onBack = onBack) {
        DemoCard(title = "M3-кнопки") {
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Button · Primary")
            }
            FilledTonalButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("FilledTonalButton")
            }
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("OutlinedButton")
            }
            ElevatedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("ElevatedButton")
            }
            TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("TextButton")
            }
        }
        DemoCard(title = "Disabled") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) {
                    Text("Button")
                }
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) {
                    Text("OutlinedButton")
                }
            }
        }
        DemoCard(title = "speakingPressable (scale .97 при нажатии)") {
            val interaction = remember { MutableInteractionSource() }
            Card(
                onClick = {},
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = speaking.primaryContainer),
                interactionSource = interaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .speakingPressable(interaction)
            ) {
                Text(
                    text = "Нажми меня",
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.onPrimaryContainer,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        DemoCard(title = "Кнопка на record-подложке") {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = speaking.record,
                    contentColor = speaking.onRecord
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("record / onRecord (WCAG AA)")
            }
        }
    }
}
