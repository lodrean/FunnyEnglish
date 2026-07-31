package com.funnyenglish.design.components.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard as MaterialOutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.funnyenglish.design.theme.FunnyEnglishTheme

@Composable
fun OutlinedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    MaterialOutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Preview
@Composable
private fun OutlinedCardLightPreview() {
    FunnyEnglishTheme(darkTheme = false) {
        OutlinedCard {
            Text(
                text = "Daily Challenge",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Complete 5 exercises today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun OutlinedCardDarkPreview() {
    FunnyEnglishTheme(darkTheme = true) {
        OutlinedCard {
            Text(
                text = "Daily Challenge",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Complete 5 exercises today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
