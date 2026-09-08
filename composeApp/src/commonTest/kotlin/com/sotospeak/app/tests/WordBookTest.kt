package com.sotospeak.app.tests

import com.sotospeak.app.storage.RecordingFileStorage
import com.sotospeak.app.storage.WordBook
import com.sotospeak.shared.platform.Settings
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Юнит-тесты личного словаря слов (bd FunnyEnglish-h3l.13): нормализация,
 * добавление/удаление, персистентность между инстансами с одним Settings.
 */
class WordBookTest {

    private val settingsName = "test_wordbook_${Clock.System.now().toEpochMilliseconds()}"

    private fun newBook() = WordBook(Settings(settingsName))

    @Test
    fun addNormalizesAndDeduplicates() {
        val book = newBook()
        assertTrue(book.add("Hello,", "Hello, world!", 1))
        assertFalse(book.add("hello", "again", 2), "то же слово (нормализовано) не добавляется дважды")
        assertEquals(1, book.list().size)
        assertEquals("hello", book.list().first().word)
    }

    @Test
    fun containsIgnoresPunctuationAndCase() {
        val book = newBook()
        book.add("World", "Hello, World!", 1)
        assertTrue(book.contains("world"))
        assertTrue(book.contains("world."))
        assertTrue(book.contains("\"World\""))
        assertFalse(book.contains("hello"))
    }

    @Test
    fun removeDeletes() {
        val book = newBook()
        book.add("word", "ctx", 1)
        book.remove("word")
        assertFalse(book.contains("word"))
        assertEquals(0, book.list().size)
    }

    @Test
    fun persistsAcrossInstances() {
        val book = newBook()
        book.add("persist", "kept context", 42)
        val reopened = WordBook(Settings(settingsName))
        assertTrue(reopened.contains("persist"))
        assertEquals("kept context", reopened.list().first().context)
    }

    @Test
    fun emptyWordIgnored() {
        val book = newBook()
        assertFalse(book.add("...", "ctx", 1))
        assertFalse(book.add("", "ctx", 1))
        assertEquals(0, book.list().size)
    }
}
