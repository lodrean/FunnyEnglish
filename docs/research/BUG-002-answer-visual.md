# Research: BUG-002 - Визуальная обратная связь ответов

## Ticket
BUG-002

## Objective
Исправить визуальное отображение выбранных ответов - фиолетовая рамка создаёт впечатление "правильного" ответа.

## Current Code Analysis

### TestPlayScreen.kt - AnswerOptions (строки 551-617)
```kotlin
border = if (isSelected)
    CardDefaults.outlinedCardBorder().copy(
        width = 2.dp,
        brush = Brush.linearGradient(listOf(FunnyColors.Primary, FunnyColors.Primary))
    )
```
**Статус**: ✅ Уже используется `FunnyColors.Primary` (синий) для border

### TestPlayScreen.kt - ImageAnswerOptions (строки 619-697)
```kotlin
border = if (isSelected)
    CardDefaults.outlinedCardBorder().copy(
        width = 3.dp,
        brush = Brush.linearGradient(listOf(FunnyColors.AccentPurple, FunnyColors.AccentPurple))
    )
```
**Статус**: ❌ Используется `FunnyColors.AccentPurple` (фиолетовый) - нужно исправить

### Цвет текста в AnswerOptions (строка 611)
```kotlin
color = if (isSelected) FunnyColors.AccentPurple else FunnyColors.OnBackground
```
**Статус**: ❌ Текст выбранного ответа фиолетовый - нужно исправить

## Affected Areas
- `composeApp/.../screens/TestPlayScreen.kt:611` - Цвет текста AnswerOptions
- `composeApp/.../screens/TestPlayScreen.kt:656-660` - Border ImageAnswerOptions
- `composeApp/.../screens/TestPlayScreen.kt:689` - Цвет текста ImageAnswerOptions

## Root Cause Analysis
Несогласованность стилей между разными типами вопросов:
- TEXT_SELECT/AUDIO_SELECT/FILL_BLANK - уже используют Primary
- IMAGE_SELECT - всё ещё использует AccentPurple

## Recommended Fix
Заменить все использования `FunnyColors.AccentPurple` на `FunnyColors.Primary` для состояния `isSelected`.

## Complexity Assessment
- **Estimated scope**: Very Low
- **Risk areas**: Нет, чисто UI изменение

## Open Questions
- [x] Проверить все места с AccentPurple (Status: RESOLVED - найдены 3 места)

## Recommendation
Заменить `AccentPurple` на `Primary` в:
1. `ImageAnswerOptions` border (line 659)
2. `ImageAnswerOptions` text color (line 689)
3. `AnswerOptions` text color (line 611)
