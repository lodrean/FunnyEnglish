# Plan: Bugfix Sprint 001

## Status
APPROVED

## Overview
Исправление 4 критических багов в FunnyEnglish приложении.

## Bugs Summary

| Bug | Priority | Status | Estimated Effort |
|-----|----------|--------|------------------|
| BUG-004: MinIO URL | Critical | ✅ Already Fixed | 0h |
| BUG-003: Submit test | Critical | ✅ Already Fixed | 0h |
| BUG-002: Answer visual | Major | 🔧 Needs Fix | 1h |
| BUG-001: Question text | Critical | 🔧 Needs Fix | 2h |

## Architecture Decisions

### Decision 1: Границы ответственности
- **Backend**: Гарантировать наличие текста вопроса (валидация)
- **Mobile**: Иметь fallback для gracefully handle null значения

### Decision 2: UI Consistency
- Использовать `FunnyColors.Primary` для всех selected состояний
- `FunnyColors.AccentPurple` - только для акцентных элементов (кнопки)

## Implementation Steps

### Phase 1: BUG-002 Fix (Answer Visual Feedback)
1. Заменить `AccentPurple` на `Primary` в `ImageAnswerOptions`
2. Заменить `AccentPurple` на `Primary` в тексте ответов
3. Проверить консистентность стилей

### Phase 2: BUG-001 Fix (Question Text)
1. Добавить fallback текст в `QuestionContent`
2. Проверить backend валидацию
3. Обновить локализацию

### Phase 3: Testing
1. Сборка проекта
2. Ручное тестирование
3. Code review

### Phase 4: Merge
1. Коммит с conventional commits
2. Merge в develop

## Risks

| Risk | Mitigation |
|------|------------|
| Регрессия UI | Тестирование всех типов вопросов |
| Сломана сборка | CI проверка перед merge |

## Dependencies
- Доступ к MinIO для тестирования BUG-004
- Android Studio для тестирования

## Environment Variables
```bash
# For BUG-004 verification
export S3_PUBLIC_URL=http://YOUR_LOCAL_IP:9000
```
