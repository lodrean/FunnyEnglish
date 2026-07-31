# E2E UI Тесты для Image Word Match

## Обзор

Этот пакет содержит E2E UI тесты для фичи **Image Word Match** - интерактивного вопроса, где пользователи перетаскивают слова к соответствующим областям на изображении.

## Структура тестов

### 1. Page Object

#### `ImageWordMatchPage.kt`
Page Object для взаимодействия с компонентом ImageWordMatch:

**Основные методы:**
- `assertComponentDisplayed()` - проверка отображения компонента
- `assertWordDisplayed(index)` - проверка отображения слова
- `assertHotspotDisplayed(index)` - проверка отображения hotspot'а
- `assertProgress(current, total)` - проверка прогресса
- `getWordCount()` / `getHotspotCount()` - получение количества элементов
- `verifyInitialState()` / `verifyCompletedState()` - проверка состояний

**Test Tags:**
- `iwm_instruction` - Текст инструкции
- `iwm_progress_bar` - Прогресс-бар
- `iwm_progress_text` - Текст прогресса
- `iwm_image` - Изображение вопроса
- `iwm_word_bank` - Банк слов
- `iwm_word_bank_label` - Метка банка слов
- `iwm_word_{index}` - Слово по индексу
- `iwm_hotspot_{index}` - Hotspot по индексу

### 2. UI Тесты

#### `ImageWordMatchFlowTest.kt`
Базовые UI тесты компонента:

| Тест | Описание |
|------|----------|
| `componentDisplaysCorrectly()` | Проверка отображения всех элементов |
| `matchWordToHotspotUpdatesProgress()` | Проверка callback при сопоставлении |
| `correctNumberOfElementsDisplayed()` | Проверка количества слов и hotspot'ов |
| `progressUpdatesWithMatches()` | Проверка обновления прогресса |
| `allWordsMatchedShowsCompleteProgress()` | Проверка полного сопоставления |
| `fallbackDisplayedOnImageError()` | Проверка fallback при ошибке загрузки |

### 3. Интеграционные тесты

#### `ImageWordMatchIntegrationTest.kt`
Тесты интеграции с TestPlayScreen:

| Тест | Описание |
|------|----------|
| `userCanOpenImageWordMatchTest()` | Открытие теста с IMAGE_WORD_MATCH |
| `userSeesMatchProgress()` | Отображение прогресса сопоставления |
| `userCanCompleteImageWordMatchTest()` | Полное прохождение вопроса |
| `submitButtonDisabledUntilAllMatched()` | Логика кнопки завершения |
| `instructionIsDisplayed()` | Отображение инструкции |
| `imageWordMatchQuestionHasCorrectStructure()` | Проверка структуры вопроса |

## Запуск тестов

### Запуск всех тестов модуля
```bash
./gradlew :composeApp:test
```

### Запуск конкретного тестового класса
```bash
./gradlew :composeApp:test --tests "com.funnyenglish.app.tests.ImageWordMatchFlowTest"
```

### Запуск конкретного теста
```bash
./gradlew :composeApp:test --tests "com.funnyenglish.app.tests.ImageWordMatchFlowTest.componentDisplaysCorrectly"
```

### Запуск на конкретной платформе
```bash
# Android
./gradlew :composeApp:testDebugUnitTest

# Desktop (JVM)
./gradlew :composeApp:jvmTest

# iOS (требуется Xcode)
./gradlew :composeApp:iosSimulatorArm64Test
```

## Особенности тестирования

### Drag-and-drop
Полное drag-and-drop тестирование требует платформенных инструментов (Maestro, Espresso, XCUITest). В Compose UI тестах доступны:
- Проверка отображения элементов
- Проверка callback'ов
- Симуляция состояний

### Test Tags
Все интерактивные элементы имеют test tags для надежного поиска в тестах:

```kotlin
// Добавление testTag в компонент
Text(
    text = content.instruction,
    modifier = Modifier.testTag("iwm_instruction")
)
```

### Мок-данные
Используются мок-данные из `TestMocks.kt`:
- `mockImageWordMatchQuestions` - вопросы IMAGE_WORD_MATCH
- Функция `createImageWordMatchTestDetail()` - создание TestDetail

## Расширение тестов

### Добавление нового теста

1. Добавьте testTag в компонент (если нужно)
2. Добавьте метод в `ImageWordMatchPage`
3. Создайте тест в `ImageWordMatchFlowTest` или `ImageWordMatchIntegrationTest`

Пример:
```kotlin
@Test
fun myNewTest() = runTest(
    content = { ImageWordMatchScreenForTest() }
) {
    val iwmPage = ImageWordMatchPage(this)
    
    // Given/When/Then
    iwmPage.assertComponentDisplayed()
    // ...
}
```

## Известные ограничения

1. **Drag-and-drop**: Полное тестирование drag-and-drop требует Maestro или платформенных тестов
2. **Загрузка изображений**: Используются placeholder изображения для стабильности тестов
3. **Анимации**: Некоторые анимации могут влиять на тайминги тестов

## Связанные файлы

- `ImageWordMatchQuestion.kt` - Основной компонент
- `TestPlayScreen.kt` - Экран прохождения теста
- `TestMocks.kt` - Мок-данные для тестов
- `BasePage.kt` / `BaseUiTest.kt` - Базовые классы
