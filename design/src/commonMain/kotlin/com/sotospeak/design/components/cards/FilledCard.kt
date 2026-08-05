package com.sotospeak.design.components.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sotospeak.design.theme.SoToSpeakTheme

@Composable
fun FilledCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Preview
@Composable
private fun FilledCardLightPreview() {
    SoToSpeakTheme(darkTheme = false) {
        FilledCard {
            Text(
                text = "Grammar Tip",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Use 'a' before consonants and 'an' before vowels",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun FilledCardDarkPreview() {
    SoToSpeakTheme(darkTheme = true) {
        FilledCard {
            Text(
                text = "Grammar Tip",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Use 'a' before consonants and 'an' before vowels",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
