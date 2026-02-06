# Plan: Accessibility Improvements

## Ticket
ACCESSIBILITY-001

## Status
DRAFT → APPROVED

## Overview
Внедрение WCAG 2.1 AA accessibility в Compose Multiplatform приложение.

## Approach

Фазовый подход:
1. Content descriptions (семантика)
2. Color contrast (визуал)
3. Touch targets (интерактивность)
4. Screen reader testing

## Architecture Decisions

### ADR-001: Accessibility Implementation Strategy
- **Context**: Нужно сделать приложение accessible
- **Decision**: Следовать WCAG 2.1 AA, начать с high-impact areas
- **Consequences**:
  - + Более широкая аудитория
  - + Лучшее SEO
  - - Время на внедрение

### ADR-002: Color Contrast Target
- **Context**: Какой уровень contrast
- **Decision**: WCAG AA (4.5:1 для текста)
- **Consequences**:
  - + Достаточно для большинства users
  - - Может потребоваться менять дизайн

## Implementation Steps

### Step 1: Content Descriptions (2-3 hours)
- [ ] HomeScreen: Categories, navigation buttons
- [ ] TestPlayScreen: Answer options, question text
- [ ] ProfileScreen: Avatar, stats, achievements
- [ ] Login/Register: Input fields, toggles, errors

### Step 2: Color Contrast (1-2 hours)
- [ ] Audit current colors
- [ ] Update FunnyColors.kt
- [ ] Fix TextSecondary, Disabled, Error colors

### Step 3: Touch Targets (1 hour)
- [ ] Measure current touch targets
- [ ] Increase to 48dp where needed
- [ ] Add padding if necessary

### Step 4: Testing (1-2 hours)
- [ ] TalkBack testing
- [ ] Keyboard navigation
- [ ] Color contrast verification

## Files to Modify

```
composeApp/src/commonMain/kotlin/
├── screens/
│   ├── HomeScreen.kt           # Add semantics
│   ├── TestPlayScreen.kt       # Add semantics
│   ├── ProfileScreen.kt        # Add semantics
│   ├── LoginScreen.kt          # Add semantics
│   └── RegisterScreen.kt       # Add semantics
├── components/
│   ├── AnswerOptions.kt        # Add semantics
│   ├── CategoryCard.kt         # Add semantics
│   └── Common.kt               # Add semantics
└── theme/
    └── FunnyColors.kt          # Fix contrast
```

## Configuration

### New File: AccessibilityUtils.kt
```kotlin
object AccessibilityUtils {
    fun Modifier.contentDescription(desc: String) = semantics {
        contentDescription = desc
    }
    
    fun Modifier.heading() = semantics {
        heading()
    }
}
```

## Testing Checklist

- [ ] All interactive elements announced
- [ ] Logical focus order
- [ ] Color contrast 4.5:1 for text
- [ ] Touch targets 48dp
- [ ] No accessibility scanner warnings

## Success Criteria

- [ ] Accessibility Scanner: 0 warnings
- [ ] TalkBack: All elements announced
- [ ] Color Contrast: 100% AA compliant
- [ ] Touch Targets: All 48dp+

## Dependencies
None - используем встроенные Compose APIs

## Notes
- Использовать string resources для локализации
- Тестировать на реальном устройстве
- Группировать связанные элементы
