package com.sotospeak.app.subtitles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingTextStyles

/**
 * Панель субтитров ПОД плеером (дизайн-система v1.0: не оверлей поверх видео).
 * Тёмная подложка scrimSubtitle (70%), текст 17sp (SpeakingTextStyles.SubtitleText).
 */
@Composable
fun SubtitlePanel(
    cues: List<SubtitleCue>,
    positionMs: Long,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    val current = cues.firstOrNull { positionMs in it.startMs until it.endMs }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(speaking.scrimSubtitle)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("subtitle_text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = current?.text ?: "…",
            style = SpeakingTextStyles.SubtitleText,
            color = androidx.compose.ui.graphics.Color.White,
            textAlign = TextAlign.Center
        )
    }
}
