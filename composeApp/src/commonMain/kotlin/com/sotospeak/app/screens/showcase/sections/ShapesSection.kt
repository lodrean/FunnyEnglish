package com.sotospeak.app.screens.showcase.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes

/**
 * Демо «Формы»: фирменные радиусы SpeakingShapes (tokens.json radius)
 * и стоковая M3-шкала MaterialTheme.shapes.
 */
@Composable
fun ShapesSection(onBack: () -> Unit) {
    val speaking = LocalSpeakingColors.current

    ShowcaseScaffold(section = ShowcaseSection.Shapes, onBack = onBack) {
        DemoCard(title = "SpeakingShapes (tokens.json radius)") {
            val shapes: List<Pair<String, RoundedCornerShape>> = listOf(
                "Recorder · 22dp" to SpeakingShapes.Recorder,
                "Card · 22dp" to SpeakingShapes.Card,
                "CardLarge · 26dp" to SpeakingShapes.CardLarge,
                "Button · 16dp" to SpeakingShapes.Button,
                "Chip · 12dp" to SpeakingShapes.Chip,
                "Sheet · 28dp" to SpeakingShapes.Sheet,
                "StatusPill" to SpeakingShapes.StatusPill
            )
            shapes.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (label, shape) ->
                        ShapeBox(label, shape, Modifier.weight(1f))
                    }
                    if (row.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        DemoCard(title = "MaterialTheme.shapes") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShapeBox("small · 8dp", MaterialTheme.shapes.small as RoundedCornerShape, Modifier.weight(1f))
                ShapeBox("medium · 12dp", MaterialTheme.shapes.medium as RoundedCornerShape, Modifier.weight(1f))
                ShapeBox("large · 16dp", MaterialTheme.shapes.large as RoundedCornerShape, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ShapeBox(label: String, shape: RoundedCornerShape, modifier: Modifier = Modifier) {
    val speaking = LocalSpeakingColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(shape)
                .background(speaking.primaryContainer)
                .border(1.dp, speaking.outline, shape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = speaking.text
        )
    }
}
