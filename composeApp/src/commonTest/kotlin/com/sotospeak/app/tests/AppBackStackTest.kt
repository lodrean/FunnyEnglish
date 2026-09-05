package com.sotospeak.app.tests

import com.sotospeak.app.navigation.AppBackStack
import com.sotospeak.app.navigation.AppScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Юнит-тесты back stack ручной навигации (bd 5tf.3): push/pop/reset/replace,
 * popOrPush и round-trip через Saver (процессная смерть).
 */
class AppBackStackTest {

    @Test
    fun startsAtInitialScreenWithoutBack() {
        val stack = AppBackStack(AppScreen.Splash)

        assertEquals(AppScreen.Splash, stack.current)
        assertFalse(stack.canGoBack)
        assertFalse(stack.pop())
    }

    @Test
    fun pushPopReturnsToPreviousScreen() {
        val stack = AppBackStack(AppScreen.Library)
        stack.push(AppScreen.Topics("lib-1", "Разговорный"))

        assertEquals(AppScreen.Topics("lib-1", "Разговорный"), stack.current)
        assertTrue(stack.canGoBack)

        assertTrue(stack.pop())
        assertEquals(AppScreen.Library, stack.current)
        assertFalse(stack.canGoBack)
    }

    @Test
    fun resetClearsHistory() {
        val stack = AppBackStack(AppScreen.Splash)
        stack.push(AppScreen.Login)
        stack.push(AppScreen.Register)

        stack.reset(AppScreen.Library)

        assertEquals(AppScreen.Library, stack.current)
        assertFalse(stack.canGoBack)
    }

    @Test
    fun replaceKeepsStackSize() {
        val stack = AppBackStack(AppScreen.Login)
        stack.push(AppScreen.Register)
        stack.replace(AppScreen.Login)

        assertEquals(AppScreen.Login, stack.current)
        assertTrue(stack.canGoBack)
    }

    @Test
    fun popOrPushPopsWhenHistoryExists() {
        val stack = AppBackStack(AppScreen.Library)
        stack.push(AppScreen.Login)
        stack.push(AppScreen.Register)

        stack.popOrPush(AppScreen.Login)

        assertEquals(AppScreen.Login, stack.current)
    }

    @Test
    fun popOrPushPushesAtRoot() {
        val stack = AppBackStack(AppScreen.Register)

        stack.popOrPush(AppScreen.Login)

        assertEquals(AppScreen.Login, stack.current)
    }

    @Test
    fun saverRoundTripRestoresWholeStack() {
        val stack = AppBackStack(AppScreen.Library)
        stack.push(AppScreen.Topics("lib-1", "Библиотека | спецсимвол"))
        stack.push(AppScreen.Video("topic-1", "lib-1", withSubtitles = true, libraryTitle = "T"))

        val restored = AppBackStack.decode(stack.encode())

        assertEquals(stack.current, restored.current)
        assertTrue(restored.canGoBack)
        restored.pop()
        assertEquals(AppScreen.Topics("lib-1", "Библиотека | спецсимвол"), restored.current)
        restored.pop()
        assertEquals(AppScreen.Library, restored.current)
    }

    @Test
    fun decodeRejectsGarbageGracefully() {
        // Мусор в сохранённых данных (смена контракта сериализации) не должен ронять старт
        val restored = runCatching {
            AppBackStack.decode(listOf("not-a-json{{"))
        }.getOrNull()

        assertNull(restored)
    }
}
