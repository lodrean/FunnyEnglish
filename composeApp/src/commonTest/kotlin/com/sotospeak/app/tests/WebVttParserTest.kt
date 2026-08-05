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
}
