package com.sotospeak.app.screens.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Общие демо-примитивы секций шоукейса: карточка-обёртка [DemoCard],
 * сетка цветовых свотчей [SwatchGrid] и hex-конвертер [colorToHex].
 */

/** Карточка-секция на surface с подписью — единая рамка всех демо. */
@Composable
fun DemoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val speaking = LocalSpeakingColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = speaking.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = speaking.textMuted
            )
            content()
        }
    }
}

/** Сетка свотчей в 2 колонки: имя токена + hex. */
@Composable
fun SwatchGrid(swatches: List<Pair<String, Color>>) {
    swatches.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { (name, color) ->
                ColorSwatch(name = name, color = color, modifier = Modifier.weight(1f))
            }
            if (row.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color, modifier: Modifier = Modifier) {
    val speaking = LocalSpeakingColors.current
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(shape)
                .background(color)
                .border(1.dp, speaking.outline, shape)
        )
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = speaking.text
            )
            Text(
                text = colorToHex(color),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = speaking.textMuted
            )
        }
    }
}

/**
 * Hex цвета без String.format — он недоступен на Kotlin/WASM (грабля №38).
 */
fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    val digits = "0123456789ABCDEF"
    fun hexByte(value: Int): String =
        "${digits[(value shr 4) and 0xF]}${digits[value and 0xF]}"
    return "#${hexByte((argb shr 16) and 0xFF)}${hexByte((argb shr 8) and 0xFF)}${hexByte(argb and 0xFF)}"
}
