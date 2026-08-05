package com.sotospeak.app.tests

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

/**
 * Базовый класс для UI тестов с Compose и Koin.
 * 
 * Предоставляет:
 * - Инициализацию Compose Test Framework
 * - Настройку Koin DI с моками
 * - Вспомогательные методы для тестов
 */
@OptIn(ExperimentalTestApi::class)
abstract class BaseUiTest : KoinTest {
    
    /**
     * Запустить UI тест с Compose
     */
    fun runTest(
        content: @Composable () -> Unit,
        testBlock: ComposeUiTest.() -> Unit
    ) = runComposeUiTest {
        // Настраиваем Koin перед тестом
        setupKoin()
        
        try {
            // Устанавливаем UI
            setContent(content)
            
            // Выполняем тест
            testBlock()
        } finally {
            // Очищаем Koin после теста
            tearDownKoin()
        }
    }
    
    /**
     * Запустить UI тест с навигацией
     */
    fun runTestWithNavigation(
        content: @Composable () -> Unit,
        testBlock: ComposeUiTest.() -> Unit
    ) = runTest(content, testBlock)
    
    /**
     * Настройка Koin DI с моками
     */
    private fun setupKoin() {
        stopKoin()
        startKoin {
            modules(
                testModule,
                viewModelTestModule
            )
        }
    }
    
    /**
     * Очистка Koin
     */
    private fun tearDownKoin() {
        stopKoin()
    }
    
    companion object {
        /**
         * Модуль с моками для API и репозиториев
         */
        val testModule = module {
            // Моки подключаются здесь при необходимости
        }

        /**
         * Модуль с моками для ViewModel
         */
        val viewModelTestModule = module {
            // ViewModel будут использовать моки из testModule
        }
    }
}

/**
 * Тестовые данные для UI тестов
 */
object TestData {
    const val TEST_USER_EMAIL = "test@example.com"
    const val TEST_USER_PASSWORD = "password123"
}

/**
 * Ожидания для UI элементов
 */
object Timeouts {
    const val SHORT = 1000L
    const val DEFAULT = 5000L
    const val LONG = 10000L
    const val NETWORK = 15000L
}
