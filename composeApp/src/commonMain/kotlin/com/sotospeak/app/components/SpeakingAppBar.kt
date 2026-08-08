package com.sotospeak.app.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Шапка экрана по мокапу (.appbar): заголовок + подзаголовок-цепочка
 * («Travel & Holidays · At the airport · 5 вопросов»), БЕЗ стрелки «назад» —
 * назад через системную кнопку/жест и bottom nav (поведение мокапов авторитетно).
 */
@Composable
fun SpeakingAppBar(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            .testTag("app_bar")
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = speaking.text
        )
        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = speaking.textMuted,
                modifier = Modifier.testTag("app_bar_subtitle")
            )
        }
    }
}

/** Плюрализация: «1 вопрос», «3 вопроса», «5 вопросов». */
fun questionsCountText(count: Int): String {
    val mod100 = count % 100
    val mod10 = count % 10
    val word = when {
        mod100 in 11..14 -> "вопросов"
        mod10 == 1 -> "вопрос"
        mod10 in 2..4 -> "вопроса"
        else -> "вопросов"
    }
    return "$count $word"
}
