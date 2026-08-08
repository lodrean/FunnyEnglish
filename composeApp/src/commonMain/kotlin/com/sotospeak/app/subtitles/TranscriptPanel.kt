package com.sotospeak.app.subtitles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.designsystem.accessibility.LocalReduceMotion
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingTextStyles

/**
 * Панель полного транскрипта видео (заменяет построчный SubtitlePanel):
 * весь текст виден сразу и скроллится; произнесённые слова подсвечиваются
 * из приглушённого цвета в основной, текущее слово заливается плавно
 * (lerp по доле прогресса внутри слова). При reduce-motion — мгновенное
 * переключение без плавной заливки.
 */
@Composable
fun TranscriptPanel(
    cues: List<SubtitleCue>,
    positionMs: Long,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val speaking = LocalSpeakingColors.current
    val reduceMotion = LocalReduceMotion.current

    // Автоскролл к активному cue (только при смене cue, не на каждый тик позиции)
    val currentCueIndex = cues.indexOfFirst { positionMs in it.startMs until it.endMs }
    LaunchedEffect(currentCueIndex) {
        if (currentCueIndex >= 0) {
            listState.animateScrollToItem((currentCueIndex - 1).coerceAtLeast(0))
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth().testTag("transcript_panel"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(cues, key = { _, cue -> cue.startMs }) { cueIndex, cue ->
            Text(
                text = buildTranscriptText(
                    cue = cue,
                    positionMs = positionMs,
                    spokenColor = speaking.text,
                    unspokenColor = speaking.textMuted,
                    reduceMotion = reduceMotion
                ),
                style = SpeakingTextStyles.SubtitleText,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transcript_cue_$cueIndex")
            )
        }
    }
}

/**
 * AnnotatedString cue: слова через пробел, цвет — по таймингу слова относительно positionMs.
 * Текущее слово — плавный lerp muted→text + полужирный (точка отслеживания).
 */
internal fun buildTranscriptText(
    cue: SubtitleCue,
    positionMs: Long,
    spokenColor: Color,
    unspokenColor: Color,
    reduceMotion: Boolean
): AnnotatedString = buildAnnotatedString {
    // Fallback для cue без слов — заливка целого cue по его окну
    if (cue.words.isEmpty()) {
        val color = when {
            positionMs < cue.startMs -> unspokenColor
            positionMs >= cue.endMs -> spokenColor
            reduceMotion -> spokenColor
            else -> {
                val span = (cue.endMs - cue.startMs).coerceAtLeast(1)
                val fraction = ((positionMs - cue.startMs).toFloat() / span).coerceIn(0f, 1f)
                lerp(unspokenColor, spokenColor, fraction)
            }
        }
        append(AnnotatedString(cue.text, SpanStyle(color = color)))
        return@buildAnnotatedString
    }
    cue.words.forEachIndexed { index, word ->
        val isCurrent = positionMs in word.startMs until word.endMs
        val color = wordColor(word, positionMs, spokenColor, unspokenColor, reduceMotion)
        pushStyle(SpanStyle(color = color, fontWeight = if (isCurrent) FontWeight.Bold else null))
        append(word.text)
        pop()
        if (index != cue.words.lastIndex) append(' ')
    }
}

/** Цвет слова: произнесённое / текущее (плавная заливка) / ещё не произнесённое. */
internal fun wordColor(
    word: SubtitleWord,
    positionMs: Long,
    spokenColor: Color,
    unspokenColor: Color,
    reduceMotion: Boolean
): Color = when {
    positionMs >= word.endMs -> spokenColor
    positionMs < word.startMs -> unspokenColor
    reduceMotion -> spokenColor
    else -> {
        val span = (word.endMs - word.startMs).coerceAtLeast(1)
        val fraction = ((positionMs - word.startMs).toFloat() / span).coerceIn(0f, 1f)
        lerp(unspokenColor, spokenColor, fraction)
    }
}
