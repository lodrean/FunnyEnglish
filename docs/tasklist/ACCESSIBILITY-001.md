# Tasklist: Accessibility Improvements

## Ticket
ACCESSIBILITY-001

## Status
🔄 READY FOR IMPLEMENTATION

## Overview
Внедрение accessibility поддержки в Compose Multiplatform приложение согласно WCAG 2.1 AA.

## Acceptance Criteria
- [ ] Все интерактивные элементы имеют content descriptions
- [ ] Color contrast соответствует WCAG AA (4.5:1 для текста)
- [ ] Touch targets минимум 48dp
- [ ] Screen readers корректно озвучивают UI
- [ ] Accessibility scanner не находит критических проблем

## Tasks

### 1. Content Descriptions
- [ ] **Task 1.1**: Add semantics to HomeScreen
  - AC: Category cards имеют описания
  - AC: Navigation buttons имеют labels
  - AC: Progress indicators доступны
  
- [ ] **Task 1.2**: Add semantics to TestPlayScreen
  - AC: Answer options имеют descriptions
  - AC: Question text доступен
  - AC: Submit button labeled
  
- [ ] **Task 1.3**: Add semantics to ProfileScreen
  - AC: Avatar имеет description
  - AC: Stats readable
  - AC: Edit buttons labeled
  
- [ ] **Task 1.4**: Add semantics to Auth screens
  - AC: Input fields имеют labels
  - AC: Password toggle accessible
  - AC: Error messages announced

### 2. Color Contrast
- [ ] **Task 2.1**: Audit current colors
  - AC: Проверить все цветовые комбинации
  - AC: Использовать Color Contrast Analyzer
  - AC: Документировать несоответствия
  
- [ ] **Task 2.2**: Fix low contrast colors
  - AC: Обновить disabled states
  - AC: Обновить hint текст
  - AC: Обновить secondary текст
  - AC: Все изменения в FunnyColors.kt
  
- [ ] **Task 2.3**: Test with color blindness simulator
  - AC: Проверить protanopia
  - AC: Проверить deuteranopia
  - AC: Проверить tritanopia

### 3. Touch Targets
- [ ] **Task 3.1**: Measure current touch targets
  - AC: Проверить все кнопки
  - AC: Проверить иконки
  - AC: Проверить chips
  
- [ ] **Task 3.2**: Increase small touch targets
  - AC: Минимум 48x48dp
  - AC: Padding где нужно
  - AC: Ripple effects корректны

### 4. Screen Reader Support
- [ ] **Task 4.1**: Test with TalkBack (Android)
  - AC: Навигация по элементам
  - AC: Жесты работают
  - AC: Focus order логичен
  
- [ ] **Task 4.2**: Fix screen reader issues
  - AC: Hidden decorative elements
  - AC: Group related elements
  - AC: Live regions для alerts

### 5. Testing
- [ ] **Task 5.1**: Setup accessibility scanner
  - AC: Android Accessibility Scanner
  - AC: Compose UI tests с semantics
  
- [ ] **Task 5.2**: Create accessibility test checklist
  - AC: Тестовые сценарии
  - AC: Критерии прохождения

## Implementation Guidelines

### Content Description Pattern
```kotlin
// Good
IconButton(
    onClick = { /* ... */ },
    modifier = Modifier.semantics { 
        contentDescription = "Navigate to profile"
    }
) {
    Icon(Icons.Default.Person, contentDescription = null)
}

// Even better - use string resources
IconButton(
    onClick = { /* ... */ },
    modifier = Modifier.semantics { 
        contentDescription = Strings.navigateToProfile
    }
) {
    Icon(
        Icons.Default.Person, 
        contentDescription = null // Parent has description
    )
}
```

### Color Contrast
```kotlin
// FunnyColors.kt
object FunnyColors {
    // Ensure 4.5:1 contrast ratio
    val TextPrimary = Color(0xFF212121)      // on White: 15.3:1
    val TextSecondary = Color(0xFF616161)    // on White: 6.3:1
    val Disabled = Color(0xFF9E9E9E)         // on White: 2.7:1 - needs fix
    
    // Fix: Darker disabled
    val DisabledFixed = Color(0xFF757575)    // on White: 4.6:1 ✓
}
```

### Touch Target
```kotlin
// Minimum 48dp touch target
IconButton(
    onClick = { /* ... */ },
    modifier = Modifier.size(48.dp) // Not just 24dp
) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close",
        modifier = Modifier.size(24.dp) // Icon can be smaller
    )
}
```

## Files to Modify

```
composeApp/src/commonMain/kotlin/
├── com/funnyenglish/app/screens/
│   ├── HomeScreen.kt
│   ├── TestPlayScreen.kt
│   ├── ProfileScreen.kt
│   ├── LoginScreen.kt
│   └── RegisterScreen.kt
├── com/funnyenglish/app/components/
│   ├── AnswerOptions.kt
│   ├── CategoryCard.kt
│   └── Common.kt
└── com/funnyenglish/designsystem/tokens/
    └── FunnyColors.kt
```

## Testing Checklist

- [ ] Activate TalkBack
- [ ] Navigate through all screens
- [ ] Verify all interactive elements announced
- [ ] Verify logical focus order
- [ ] Test with high contrast mode
- [ ] Test with large text (200%)
- [ ] Run Accessibility Scanner
- [ ] Color contrast analyzer passed

## Related

- WCAG 2.1 Guidelines: https://www.w3.org/WAI/WCAG21/quickref/
- Compose Accessibility: https://developer.android.com/jetpack/compose/accessibility
- Parent Tasklist: `docs/tasklist/IMPROVEMENTS-2025-001.md`
