# 🚩 Feature Flags - Руководство

## Что сделано

Для подготовки к production релизу были отключены нестабильные фичи через систему Feature Flags.

---

## 📋 Отключённые фичи

| Фича | Флаг | Статус в Release | Причина |
|------|------|------------------|---------|
| **DRAG_DROP_IMAGE** | `ENABLE_DRAG_DROP_QUESTIONS` | ❌ Отключено | Требуется отладка drag-and-drop интерфейса |
| **IMAGE_WORD_MATCH** | `ENABLE_IMAGE_WORD_MATCH` | ❌ Отключено | Требуется отладка позиционирования hotspot'ов |
| **Network Logs** | `ENABLE_NETWORK_LOGGING` | ❌ Отключено | Соображения безопасности |
| **Debug Tools** | `ENABLE_DEBUG_TOOLS` | ❌ Отключено | Только для разработки |

---

## 🔧 Как это работает

### 1. FeatureFlags объект (shared модуль)

```kotlin
// shared/src/commonMain/kotlin/com/sotospeak/shared/config/FeatureFlags.kt
object FeatureFlags {
    var ENABLE_DRAG_DROP_QUESTIONS: Boolean = false
    var ENABLE_IMAGE_WORD_MATCH: Boolean = false
    // ...
    
    fun isQuestionTypeEnabled(questionType: String): Boolean {
        return when (questionType) {
            "DRAG_DROP_IMAGE" -> ENABLE_DRAG_DROP_QUESTIONS
            "IMAGE_WORD_MATCH" -> ENABLE_IMAGE_WORD_MATCH
            else -> true
        }
    }
}
```

### 2. Фильтрация вопросов в TestViewModel

```kotlin
// feature-tests/src/commonMain/.../TestViewModel.kt
fun loadTest(testId: String) {
    api.getTestById(testId).onSuccess { test ->
        val filteredQuestions = test.questions.filter { question ->
            when (question.type) {
                QuestionType.DRAG_DROP_IMAGE -> FeatureFlags.ENABLE_DRAG_DROP_QUESTIONS
                QuestionType.IMAGE_WORD_MATCH -> FeatureFlags.ENABLE_IMAGE_WORD_MATCH
                else -> true
            }
        }
        val filteredTest = test.copy(questions = filteredQuestions)
        // ...
    }
}
```

### 3. BuildConfig для Android

```kotlin
// composeApp/build.gradle.kts
buildTypes {
    getByName("debug") {
        buildConfigField("boolean", "ENABLE_DRAG_DROP_QUESTIONS", "true")
        buildConfigField("boolean", "ENABLE_IMAGE_WORD_MATCH", "true")
    }
    getByName("release") {
        buildConfigField("boolean", "ENABLE_DRAG_DROP_QUESTIONS", "false")
        buildConfigField("boolean", "ENABLE_IMAGE_WORD_MATCH", "false")
    }
}
```

---

## 🚀 Включение фич обратно

### Android (BuildConfig)

```kotlin
// composeApp/build.gradle.kts
getByName("release") {
    buildConfigField("boolean", "ENABLE_DRAG_DROP_QUESTIONS", "true")  // Включить
    buildConfigField("boolean", "ENABLE_IMAGE_WORD_MATCH", "true")     // Включить
}
```

### Desktop (переменные окружения)

```bash
export SOTOSPEAK_ENABLE_DRAG_DROP=true
export SOTOSPEAK_ENABLE_IMAGE_WORD_MATCH=true
./gradlew :composeApp:run
```

### iOS (в коде)

```kotlin
// AppConfig.ios.kt
FeatureFlags.init(
    enableDragDrop = true,      // Включить
    enableImageWordMatch = true // Включить
)
```

### Web/WASM

По умолчанию включено только на localhost. Для production:
```javascript
// Добавить в URL: ?enableExperimental=true
// Или изменить AppConfig.wasmJs.kt
```

---

## 📁 Изменённые файлы

1. **shared/src/commonMain/kotlin/com/sotospeak/shared/config/FeatureFlags.kt** (создан)
2. **composeApp/build.gradle.kts** (обновлён)
3. **composeApp/src/androidMain/kotlin/com/sotospeak/app/di/AppConfig.android.kt** (обновлён)
4. **composeApp/src/desktopMain/kotlin/com/sotospeak/app/di/AppConfig.desktop.kt** (обновлён)
5. **composeApp/src/wasmJsMain/kotlin/com/sotospeak/app/di/AppConfig.wasmJs.kt** (обновлён)
6. **composeApp/src/iosMain/kotlin/com/sotospeak/app/di/AppConfig.ios.kt** (обновлён)
7. **feature-tests/src/commonMain/kotlin/com/sotospeak/tests/presentation/TestViewModel.kt** (обновлён)

---

## 🎯 Что будет в Release билде

```
✅ TEXT_SELECT      - Работает
✅ AUDIO_SELECT     - Работает
✅ IMAGE_SELECT     - Работает
✅ FILL_BLANK       - Работает
❌ DRAG_DROP_IMAGE  - Отключено
❌ IMAGE_WORD_MATCH - Отключено
```

Все тесты с неподдерживаемыми типами вопросов будут автоматически фильтроваться.

---

## ⚠️ Примечания

- Фильтрация происходит на клиенте при загрузке теста
- Backend всё ещё может отдавать все типы вопросов
- Пользователь не увидит отключённые типы вопросов
- При включении фичи обратно, всё заработает без изменений на backend
