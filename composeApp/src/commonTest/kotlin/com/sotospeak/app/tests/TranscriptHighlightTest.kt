package com.sotospeak.app.tests

import androidx.compose.ui.graphics.Color
import com.sotospeak.app.subtitles.SubtitleCue
import com.sotospeak.app.subtitles.SubtitleWord
import com.sotospeak.app.subtitles.buildTranscriptText
import com.sotospeak.app.subtitles.wordColor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Юнит-тесты логики подсветки транскрипта (без UI):
 * границы цвета слова, плавный lerp текущего слова, reduce-motion, fallback без слов.
 */
class TranscriptHighlightTest {

    private val spoken = Color(0xFF1A2F5E)
    private val unspoken = Color(0xFF58609A)
    private val word = SubtitleWord("hello", startMs = 1000, endMs = 2000)

    @Test
    fun wordBeforeStartIsUnspoken() {
        assertEquals(unspoken, wordColor(word, 0, spoken, unspoken, reduceMotion = false))
        assertEquals(unspoken, wordColor(word, 999, spoken, unspoken, reduceMotion = false))
    }

    @Test
    fun wordAfterEndIsSpoken() {
        assertEquals(spoken, wordColor(word, 2000, spoken, unspoken, reduceMotion = false))
        assertEquals(spoken, wordColor(word, 5000, spoken, unspoken, reduceMotion = false))
    }

    @Test
    fun currentWordLerpsByFraction() {
        val mid = wordColor(word, 1500, spoken, unspoken, reduceMotion = false)
        // середина — примерно половина пути между цветами
        assertEquals(
            (spoken.red + unspoken.red) / 2f, mid.red, 0.01f
        )
        assertEquals(
            (spoken.blue + unspoken.blue) / 2f, mid.blue, 0.01f
        )
    }

    @Test
    fun reduceMotionSwitchesInstantly() {
        assertEquals(spoken, wordColor(word, 1500, spoken, unspoken, reduceMotion = true))
    }

    @Test
    fun zeroLengthWordDoesNotDivideByZero() {
        val zero = SubtitleWord("x", startMs = 1000, endMs = 1000)
        // positionMs == startMs == endMs → считается произнесённым (>= endMs)
        assertEquals(spoken, wordColor(zero, 1000, spoken, unspoken, reduceMotion = false))
        assertEquals(unspoken, wordColor(zero, 999, spoken, unspoken, reduceMotion = false))
    }

    @Test
    fun cueWithoutWordsFallsBackToWholeCueColoring() {
        val cue = SubtitleCue(startMs = 0, endMs = 1000, text = "no words")
        val before = buildTranscriptText(cue, 0, spoken, unspoken, reduceMotion = false)
        val after = buildTranscriptText(cue, 1000, spoken, unspoken, reduceMotion = false)
        assertEquals("no words", before.text)
        assertEquals(unspoken, before.spanStyles.single().item.color)
        assertEquals(spoken, after.spanStyles.single().item.color)
    }

    @Test
    fun wordsAreJoinedWithSpaces() {
        val cue = SubtitleCue(
            startMs = 0, endMs = 2000, text = "one two",
            words = listOf(SubtitleWord("one", 0, 1000), SubtitleWord("two", 1000, 2000))
        )
        assertEquals("one two", buildTranscriptText(cue, 0, spoken, unspoken, false).text)
    }
}
