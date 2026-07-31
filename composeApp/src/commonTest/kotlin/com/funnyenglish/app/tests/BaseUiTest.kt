package com.funnyenglish.app.tests

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
            // Моки API клиента
            // single<FunnyEnglishApi> { MockFunnyEnglishApi() }
            
            // Моки репозиториев
            // single<TestRepository> { MockTestRepository() }
            // single<UserRepository> { MockUserRepository() }
            // single<AchievementRepository> { MockAchievementRepository() }
        }
        
        /**
         * Модуль с моками для ViewModel
         */
        val viewModelTestModule = module {
            // ViewModel будут использовать моки из testModule
            // factory { LoginViewModel(get()) }
            // factory { TestCatalogViewModel(get()) }
            // factory { TestPlayViewModel(get()) }
            // factory { AchievementsViewModel(get()) }
        }
    }
}

/**
 * Тестовые данные для UI тестов
 */
object TestData {
    const val TEST_USER_EMAIL = "test@example.com"
    const val TEST_USER_PASSWORD = "password123"
    const val TEST_USER_NAME = "Test User"
    
    const val INVALID_EMAIL = "invalid-email"
    const val INVALID_PASSWORD = "short"
    
    val TEST_CATEGORIES = listOf(
        "Grammar" to "grammar-id",
        "Vocabulary" to "vocab-id",
        "Listening" to "listening-id"
    )
    
    val TEST_TESTS = listOf(
        "Present Simple" to "test-1",
        "Past Tense" to "test-2",
        "Food Vocabulary" to "test-3"
    )
    
    val SAMPLE_ACHIEVEMENTS = listOf(
        Triple("First Steps", "Complete your first test", "COMMON"),
        Triple("Streak Master", "7 days streak", "RARE"),
        Triple("Word Wizard", "Learn 100 words", "EPIC")
    )
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
