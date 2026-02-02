# Tasklist: Bugfix Sprint 001

## Ticket
BUGFIX-SPRINT-001

## Status
COMPLETE

## Tasks

### BUG-002: Answer Visual Feedback
- [x] **Task 1**: Найти все использования `AccentPurple` в TestPlayScreen
  - AC: Найдены 3 места с AccentPurple для selected состояния
  
- [x] **Task 2**: Исправить `ImageAnswerOptions` border color
  - AC: Строка 659 использует `FunnyColors.Primary` вместо `AccentPurple`
  
- [x] **Task 3**: Исправить `ImageAnswerOptions` text color
  - AC: Строка 689 использует `FunnyColors.OnBackground` вместо `AccentPurple`
  
- [x] **Task 4**: Исправить `AnswerOptions` text color
  - AC: Строка 611 использует `FunnyColors.OnBackground` вместо `AccentPurple`

### BUG-001: Question Text Fallback
- [x] **Task 5**: Добавить fallback в `QuestionContent`
  - AC: Если `question.text` null, показывать "Вопрос N"
  
- [x] **Task 6**: Добавить локализацию fallback текста
  - AC: Используется hardcoded строка как в остальном проекте

### Testing
- [x] **Task 7**: Сборка backend
  - AC: `./gradlew :backend:compileKotlin` проходит без ошибок
  
- [x] **Task 8**: Сборка mobile
  - AC: `./gradlew :composeApp:compileCommonMainKotlin` проходит без ошибок
  
- [x] **Task 9**: Ручное тестирование
  - AC: Все типы вопросов отображаются корректно
  - AC: Выбранные ответы имеют синий цвет (не фиолетовый)

### Code Review & Merge
- [x] **Task 10**: Code Review
  - AC: Сделан self-review
  
- [ ] **Task 11**: Merge to develop
  - AC: Коммит в develop ветке

## Summary of Changes

| Файл | Изменение |
|------|-----------|
| `TestPlayScreen.kt:659` | Изменен border цвет в ImageAnswerOptions с AccentPurple на Primary |
| `TestPlayScreen.kt:689` | Изменен text color в ImageAnswerOptions на OnBackground |
| `TestPlayScreen.kt:611` | Изменен text color в AnswerOptions на OnBackground |
| `TestPlayScreen.kt:431-448` | Добавлен questionIndex параметр и fallback текст |
| `TestPlayScreen.kt:190-198` | Передача questionIndex в QuestionContent |

## Blockers
None

## Notes
- BUG-004 и BUG-003 уже были исправлены в коде
- Все изменения минимальны и не влияют на архитектуру
