# Research: Accessibility Improvements

## Ticket
ACCESSIBILITY-001

## Objective
Исследование и планирование внедрения accessibility поддержки в Compose Multiplatform приложение согласно WCAG 2.1 AA.

## Research Findings

### WCAG 2.1 AA Requirements

#### 1. Perceivable
- **Text Alternatives**: Все non-text контент имеет текстовую альтернативу
- **Time-based Media**: Альтернативы для видео/аудио (не применимо)
- **Adaptable**: Контент можно представить по-разному без потери информации
- **Distinguishable**: Пользователь может видеть и слышать контент

#### 2. Operable
- **Keyboard Accessible**: Вся функциональность доступна с клавиатуры
- **Enough Time**: Пользователи имеют достаточно времени (не применимо)
- **Seizures**: Нет flashing content (не применимо)
- **Navigable**: Пользователи могут ориентироваться

#### 3. Understandable
- **Readable**: Текст читаемый и понятный
- **Predictable**: Интерфейс предсказуемый
- **Input Assistance**: Помощь при вводе данных

#### 4. Robust
- **Compatible**: Совместимость с assistive technologies

### Compose Multiplatform Accessibility

#### Key APIs
```kotlin
// Content description
Modifier.semantics {
    contentDescription = "Navigate to profile"
}

// Heading
Modifier.semantics {
    heading()
}

// State description
Modifier.semantics {
    stateDescription = if (isExpanded) "Expanded" else "Collapsed"
}

// Live region (announce changes)
Modifier.semantics {
    liveRegion = LiveRegionMode.Assertive
}
```

#### Screen Reader Support
- **Android**: TalkBack
- **iOS**: VoiceOver
- **Desktop**: Нативные screen readers через Swing/AWT

### Color Contrast Requirements

| Element | Ratio | Example |
|---------|-------|---------|
| Normal text | 4.5:1 | Body text |
| Large text (18pt+) | 3:1 | Headlines |
| UI Components | 3:1 | Buttons, icons |
| Graphical objects | 3:1 | Charts, diagrams |

### Touch Target Size
- **Minimum**: 44x44dp (Apple HIG)
- **Recommended**: 48x48dp (Material Design)
- **WCAG**: 44x44 CSS pixels

### Current State Analysis

#### HomeScreen
- [ ] Категории без content descriptions
- [ ] Статистика не группирована
- [ ] Кнопки без labels

#### TestPlayScreen
- [ ] Варианты ответов без описаний
- [ ] Текст вопроса не как heading
- [ ] Кнопка submit без состояния

#### ProfileScreen
- [ ] Avatar без описания
- [ ] Stats не структурированы
- [ ] Achievements без labels

#### Auth Screens
- [ ] Input fields без связанных labels
- [ ] Password toggle без описания
- [ ] Error messages не announced

## Implementation Strategy

### Phase 1: Content Descriptions
Добавить content descriptions для всех интерактивных элементов

### Phase 2: Color Contrast
Аудит и исправление цветов для WCAG AA

### Phase 3: Touch Targets
Увеличить размер touch target до 48dp

### Phase 4: Screen Reader Testing
Проверка с TalkBack/VoiceOver

## Tools

### Analysis
- Android Accessibility Scanner
- Color Contrast Analyzer
- Compose UI Tests с semantics

### Testing
- TalkBack (Android)
- VoiceOver (iOS)
- Keyboard navigation

## Affected Areas

```
composeApp/src/commonMain/kotlin/
├── screens/
│   ├── HomeScreen.kt
│   ├── TestPlayScreen.kt
│   ├── ProfileScreen.kt
│   ├── LoginScreen.kt
│   └── RegisterScreen.kt
├── components/
│   ├── AnswerOptions.kt
│   ├── CategoryCard.kt
│   └── Common.kt
└── theme/
    └── FunnyColors.kt
```

## Open Questions
- [ ] Нужны ли custom actions?
- [ ] Как группировать сложные элементы?
- [ ] Нужен ли focus management?

## Recommendation
1. Начать с content descriptions
2. Затем color contrast
3. Touch targets
4. Screen reader testing
