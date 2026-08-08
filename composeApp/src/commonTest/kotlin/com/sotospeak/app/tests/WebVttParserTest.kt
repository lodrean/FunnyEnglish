package com.sotospeak.app.tests

import com.sotospeak.app.subtitles.WebVttParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Юнит-тесты WebVTT-парсера (спека Part 2 §3.3, §10.1):
 * тайминги обоих форматов, NOTE, многострочный cue, мусорные строки, настройки cue, теги.
 */
class WebVttParserTest {

    @Test
    fun parsesMmSsAndHhMmSsTimings() {
        val vtt = """
            WEBVTT

            00:00.500 --> 00:02.000
            Hello

            00:01:03.250 --> 00:01:05.000
            World
        """.trimIndent()

        val cues = WebVttParser.parse(vtt)
        assertEquals(2, cues.size)
        assertEquals(500L, cues[0].startMs)
        assertEquals(2000L, cues[0].endMs)
        assertEquals("Hello", cues[0].text)
        // 00:01:03.250 = 1 мин 3.25 сек = 63_250 мс (hh:mm:ss.mmm)
        assertEquals(63_250L, cues[1].startMs)
        assertEquals("World", cues[1].text)
    }

    @Test
    fun skipsNoteBlocks() {
        val vtt = """
            WEBVTT

            NOTE this is a comment
            spanning two lines

            00:01.000 --> 00:02.000
            Real cue
        """.trimIndent()

        val cues = WebVttParser.parse(vtt)
        assertEquals(1, cues.size)
        assertEquals("Real cue", cues[0].text)
    }

    @Test
    fun joinsMultilineCueText() {
        val vtt = """
            WEBVTT

            00:01.000 --> 00:03.000
            First line
            second line
        """.trimIndent()

        val cues = WebVttParser.parse(vtt)
        assertEquals(1, cues.size)
        assertEquals("First line\nsecond line", cues[0].text)
    }

    @Test
    fun ignoresCueSettingsAndIdentifiers() {
        val vtt = """
            WEBVTT

            cue-42
            00:01.000 --> 00:02.000 align:start position:10%
            Styled
        """.trimIndent()

        val cues = WebVttParser.parse(vtt)
        assertEquals(1, cues.size)
        assertEquals("Styled", cues[0].text)
    }

    @Test
    fun stripsInlineTags() {
        val vtt = """
            WEBVTT

            00:01.000 --> 00:02.000
            <b>bold</b> and <c.class>colored</c>
        """.trimIndent()

        val cues = WebVttParser.parse(vtt)
        assertEquals("bold and colored", cues[0].text)
    }

    @Test
    fun ignoresGarbageLinesAndEmptyText() {
        val vtt = """
            WEBVTT

            random garbage line
            another one

            00:05.000 --> 00:03.000
            reversed timing

            00:10.000 --> 00:12.000
            Valid
        """.trimIndent()

        val cues = WebVttParser.parse(vtt)
        assertEquals(1, cues.size)
        assertEquals("Valid", cues[0].text)
        assertEquals(10_000L, cues[0].startMs)
    }

    @Test
    fun handlesCrlfAndEmptyInput() {
        assertTrue(WebVttParser.parse("").isEmpty())
        val cues = WebVttParser.parse("WEBVTT\r\n\r\n00:01.000 --> 00:02.000\r\nCRLF\r\n")
        assertEquals(1, cues.size)
        assertEquals("CRLF", cues[0].text)
    }

    // ---------- Пословные тайминги (транскрипт с подсветкой) ----------

    @Test
    fun interpolatesWordTimingsProportionallyToLength() {
        val vtt = """
            WEBVTT

            00:00.000 --> 00:04.000
            I go home now
        """.trimIndent()

        val words = WebVttParser.parse(vtt).single().words
        assertEquals(listOf("I", "go", "home", "now"), words.map { it.text })
        // веса 1+2+4+3=10 → I: 0-400, go: 400-1200, home: 1200-2800, now: 2800-4000
        assertEquals(0L, words[0].startMs)
        assertEquals(400L, words[0].endMs)
        assertEquals(400L, words[1].startMs)
        assertEquals(1200L, words[1].endMs)
        assertEquals(1200L, words[2].startMs)
        assertEquals(2800L, words[2].endMs)
        assertEquals(2800L, words[3].startMs)
        assertEquals(4000L, words[3].endMs)
        // непрерывность и покрытие всего окна cue
        words.zipWithNext().forEach { (a, b) -> assertEquals(a.endMs, b.startMs) }
    }

    @Test
    fun parsesKaraokeTimestamps() {
        val vtt = """
            WEBVTT

            00:00.000 --> 00:03.000
            <00:00.000>Hello <00:01.000>brave <00:02.000>world
        """.trimIndent()

        val cue = WebVttParser.parse(vtt).single()
        assertEquals("Hello brave world", cue.text)
        val words = cue.words
        assertEquals(listOf("Hello", "brave", "world"), words.map { it.text })
        assertEquals(0L to 1000L, words[0].startMs to words[0].endMs)
        assertEquals(1000L to 2000L, words[1].startMs to words[1].endMs)
        assertEquals(2000L to 3000L, words[2].startMs to words[2].endMs)
    }

    @Test
    fun karaokeWordsBeforeFirstTimestampStartAtCueStart() {
        val vtt = """
            WEBVTT

            00:00.500 --> 00:02.500
            Hey <00:01.000>there
        """.trimIndent()

        val words = WebVttParser.parse(vtt).single().words
        assertEquals(listOf("Hey", "there"), words.map { it.text })
        assertEquals(500L, words[0].startMs)
        assertEquals(1000L, words[0].endMs)
        assertEquals(1000L, words[1].startMs)
        assertEquals(2500L, words[1].endMs)
    }

    @Test
    fun multilineCueWordsAreInterpolatedAcrossJoinedText() {
        val vtt = """
            WEBVTT

            00:00.000 --> 00:02.000
            first
            line
        """.trimIndent()

        val cue = WebVttParser.parse(vtt).single()
        assertEquals(listOf("first", "line"), cue.words.map { it.text })
        assertEquals(0L, cue.words.first().startMs)
        assertEquals(2000L, cue.words.last().endMs)
    }
}
