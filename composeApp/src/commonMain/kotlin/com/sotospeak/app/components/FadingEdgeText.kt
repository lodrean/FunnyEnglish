package com.sotospeak.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Текст с ограничением строк и плавным затуханием вместо многоточия (мокап/ДС):
 * перенос строго по словам (softWrap, без разрыва слов), максимум [maxLines] строк,
 * при переполнении — нижняя кромка затухает градиентом в цвет фона [fadeColor].
 */
@Composable
fun FadingEdgeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalSpeakingColors.current.text,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = 3,
    fadeColor: Color = LocalSpeakingColors.current.background
) {
    var overflowed by remember(text, maxLines) { mutableStateOf(false) }

    Box(modifier = modifier) {
        Text(
            text = text,
            color = color,
            style = style,
            maxLines = maxLines,
            softWrap = true,
            overflow = TextOverflow.Clip,
            onTextLayout = { overflowed = it.hasVisualOverflow },
            modifier = Modifier.fillMaxWidth()
        )
        if (overflowed) {
            // Градиентная «вуаль» поверх последней строки (~1.2 строки высотой)
            val fadeHeight = with(androidx.compose.ui.platform.LocalDensity.current) {
                // lineHeight в типографии задан в Sp → toDp() на Sp крашится
                // («Only Sp can convert to Px») — конвертируем через toPx().toDp().
                val lh = style.lineHeight
                // TextUnit бывает только Sp/Em/Unspecified (Dp — не TextUnit)
                val lineDp = if (lh.type == androidx.compose.ui.unit.TextUnitType.Sp) {
                    lh.toPx().toDp()
                } else {
                    18.dp // Em/Unspecified — fallback
                }
                (lineDp * 1.2f).coerceAtLeast(18.dp)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(fadeHeight)
                    .background(
                        Brush.verticalGradient(
                            listOf(fadeColor.copy(alpha = 0f), fadeColor)
                        )
                    )
            )
        }
    }
}
