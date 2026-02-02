# Research: BUG-001 - Текст вопроса не отображается

## Ticket
BUG-001

## Objective
Исследовать причину отсутствия текста вопроса в мобильном приложении при прохождении тестов.

## Current Code Analysis

### TestPlayScreen.kt (строки 440-449)
```kotlin
question.text?.let { text ->
    Text(
        text = text,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        lineHeight = 30.sp
    )
}
```
**Проблема**: Используется `?.let` - если `text` равен null, блок не выполняется и текст не отображается.

### Backend Entity (Question.kt)
Нужно проверить:
- Объявлено ли поле как nullable `@Column(nullable = true)`
- Корректно ли работает валидация при создании вопроса

### Shared Model (Test.kt)
Нужно проверить:
- Объявление поля `text: String?`
- Десериализация из JSON

## Affected Areas
- `composeApp/.../screens/TestPlayScreen.kt:440-449` - UI отображение
- `backend/.../entity/Question.kt` - Entity определение
- `shared/.../model/Test.kt` - Shared модель
- `backend/.../dto/TestDto.kt` - DTO маппинг

## Root Cause Analysis

### Возможные причины:
1. **Backend**: Поле `text` в БД nullable, допускается создание вопросов без текста
2. **DTO**: Неправильный маппинг при конвертации Entity → DTO
3. **API**: Текст не передаётся в JSON ответе
4. **Mobile**: Корректная обработка null, но требуется fallback

## Recommended Fix

### Option 1 (Backend Validation) - RECOMMENDED
Сделать поле обязательным на уровне БД:
```kotlin
@Column(nullable = false)
val text: String,
```

### Option 2 (UI Fallback)
Добавить fallback текст:
```kotlin
text = question.text ?: "Вопрос ${index + 1}"
```

## Complexity Assessment
- **Estimated scope**: Low
- **Risk areas**: Миграция БД если меняем nullable

## Open Questions
- [x] Проверить текущую схему БД (Status: RESOLVED - поле nullable)
- [x] Проверить маппинг DTO (Status: RESOLVED - корректен)

## Recommendation
Реализовать **Option 2** (UI Fallback) как быстрый фикс + **Option 1** как долгосрочное решение.
