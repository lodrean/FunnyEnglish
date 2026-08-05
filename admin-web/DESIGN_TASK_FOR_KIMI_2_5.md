# Техническое Задание для Kimi 2.5: Дизайн и Макеты Admin Panel So to Speak

## 1. Общая Информация о Проекте

### 1.1 Описание проекта
**So to Speak Admin Panel** — это административная панель для управления образовательным приложением по изучению английского языка. Приложение включает систему тестов, категорий, пользователей, геймификацию (достижения, streaks, лидерборд).

### 1.2 Технологический стек
- **Framework**: React 18 + Vite 5
- **Language**: TypeScript 5.x
- **UI Library**: Material UI (MUI) v6.1.6
- **State Management**: Zustand 5.x
- **HTTP Client**: Axios
- **Charts**: Recharts
- **Forms**: React Hook Form + Zod
- **Date**: date-fns
- **Rich Text**: TipTap
- **Drag & Drop**: @dnd-kit

### 1.3 Текущая структура проекта
```
admin-web/src/
├── components/
│   ├── layout/          # Header, Sidebar, AdminLayout
│   ├── content/         # CategoryTree, QuestionBuilder, TestList
│   ├── questions/       # Question editors (5 types)
│   ├── forms/           # Form components
│   ├── data/            # DataTable, StatsCard, Pagination
│   ├── feedback/        # Toast, ConfirmDialog, ErrorBoundary
│   ├── charts/          # BarChart, LineChart, PieChart
│   └── users/           # User management components
├── screens/
│   ├── Dashboard.tsx    # Main dashboard with analytics
│   ├── Tests.tsx        # Test list
│   ├── TestEditor.tsx   # Test creation/editing
│   ├── Categories.tsx   # Category management
│   ├── Users.tsx        # User management
│   ├── Analytics.tsx    # Analytics dashboard
│   ├── Settings.tsx     # App settings
│   └── Login.tsx        # Auth screen
├── theme/
│   ├── Theme.ts         # MUI theme configuration
│   ├── ThemeProvider.tsx
│   └── GlobalStyles.ts
└── api/                 # API clients
```

---

## 2. Текущее Состояние Темы

### 2.1 Реализовано ✅
- [x] Базовая система темы (светлая/тёмная)
- [x] ThemeProvider с localStorage persistence
- [x] Переключатель темы в Header
- [x] Основные цвета в палитре MUI

### 2.2 Цветовая палитра (Design System)
```typescript
const colors = {
  primary: '#4A90D9',      // Основной синий
  success: '#43A047',      // Зелёный успех
  error: '#E53935',        // Красная ошибка
  warning: '#FB8C00',      // Оранжевое предупреждение
  info: '#2196F3',         // Информационный синий
  background: '#F5F5F5',   // Фон светлой темы
  card: '#FFFFFF',         // Карточки светлой темы
  textPrimary: '#212121',  // Основной текст
  textSecondary: '#757575',// Вторичный текст
  sidebar: '#1a237e',      // Цвет сайдбара
};
```

### 2.3 Известные проблемы с темной темой
- [ ] Hardcoded цвета в Dashboard.tsx (COLORS константа)
- [ ] Графики Recharts не адаптируются к темной теме
- [ ] Некоторые компоненты используют inline styles без учета темы
- [ ] Нет полной палитры для тёмной темы (только базовые цвета)
- [ ] Отсутствуют специфичные для темы токены (тени, границы, фоны)

---

## 3. Задачи для Kimi 2.5

### 3.1 Цель
Создать полноценный дизайн-система и макеты для Admin Panel с полной поддержкой светлой и тёмной темы, улучшенным UX/UI, адаптивностью.

### 3.2 Основные направления работы

#### 🔴 Высокий приоритет

##### 1. Полная цветовая палитра для обеих тем
Создать расширенную палитру цветов:

**Светлая тема:**
```
Background: #F5F5F5
Surface: #FFFFFF
Surface Elevated: #FFFFFF
Primary: #4A90D9
Primary Light: #7AB8E8
Primary Dark: #2E5A8C
Secondary: #1a237e
Success: #43A047
Warning: #FB8C00
Error: #E53935
Info: #2196F3
Text Primary: #212121
Text Secondary: #757575
Text Disabled: #9E9E9E
Border: #E0E0E0
Divider: rgba(0,0,0,0.12)
```

**Тёмная тема:**
```
Background: #0A1929 (глубокий синий-чёрный)
Surface: #1E293B (тёмно-синий)
Surface Elevated: #334155
Primary: #60A5FA (светлее для контраста)
Primary Light: #93C5FD
Primary Dark: #3B82F6
Secondary: #4F46E5
Success: #4ADE80
Warning: #FBBF24
Error: #F87171
Info: #60A5FA
Text Primary: #F8FAFC
Text Secondary: #94A3B8
Text Disabled: #64748B
Border: #334155
Divider: rgba(255,255,255,0.12)
```

##### 2. Дизайн-токены (Design Tokens)
Создать систему токенов для:
- **Colors**: Все цвета с вариантами (50, 100, 200...900)
- **Typography**: Размеры шрифтов, веса, межстрочные
- **Spacing**: 4px базовая сетка (4, 8, 12, 16, 24, 32, 48, 64)
- **Shadows**: Тени для светлой и тёмной темы
- **Border Radius**: Скругления (4, 8, 12, 16, 24, 50%)
- **Transitions**: Длительность анимаций
- **Z-Index**: Уровни наложения

##### 3. Компоненты для Storybook
Для каждого компонента создать:
- Визуальные макеты в Figma-подобном формате
- Описание props и вариантов
- Состояния: default, hover, active, disabled, loading
- Для каждой темы (светлая/тёмная)

**Список компонентов:**
1. **Buttons**
   - PrimaryButton (contained, outlined, text)
   - SecondaryButton
   - DangerButton
   - IconButton
   - LoadingButton

2. **Inputs**
   - TextField (with label, error, helper text)
   - Select/Dropdown
   - Checkbox
   - RadioButton
   - Switch (Toggle)
   - DatePicker
   - FileUploader

3. **Data Display**
   - Card (elevation levels)
   - DataTable (header, row, cell, pagination)
   - StatsCard (with trend indicator)
   - Badge/Tag
   - Avatar
   - ProgressBar
   - Skeleton

4. **Feedback**
   - Toast/Notification (success, error, warning, info)
   - Modal/Dialog
   - ConfirmDialog
   - Loading Spinner
   - EmptyState
   - ErrorBoundary

5. **Navigation**
   - Sidebar (expanded/collapsed)
   - Breadcrumbs
   - Tabs
   - Pagination
   - Menu/Dropdown

6. **Question Editor Components** (специфичные)
   - QuestionCard
   - AnswerOption
   - DragDropArea
   - MediaUploader
   - RichTextEditor

##### 4. Макеты страниц (Page Layouts)

**Dashboard:**
- Stats cards grid (4 cards)
- Charts section (LineChart + BarChart)
- Recent Activity list
- Quick actions

**Tests List:**
- Search + Filters bar
- DataTable with columns: Name, Category, Questions, Status, Actions
- Pagination
- Bulk actions toolbar

**Test Editor:**
- Two-panel layout (left: settings, right: questions)
- Question builder with 5 types:
  - TEXT_SELECT (multiple choice text)
  - IMAGE_SELECT (multiple choice images)
  - AUDIO_SELECT (listening)
  - DRAG_DROP_MATCH (matching)
  - FILL_BLANK (fill blanks)
- Drag & drop reordering
- Preview mode

**Categories:**
- Tree view / Hierarchy
- Category cards grid
- Edit/Delete actions

**Users:**
- User cards grid
- Filters (role, status, date)
- User detail sidebar

**Analytics:**
- Date range picker
- Multiple chart types
- Export buttons

**Settings:**
- Form sections
- Toggle switches
- Color pickers

##### 5. Тёмная тема - полная реализация

**Требования к тёмной теме:**
- [ ] Все фоны с глубиной (слоистая структура)
- [ ] Цвета с повышенной контрастностью
- [ ] Тени заменены на glow-эффекты
- [ ] Границы с низкой непрозрачностью
- [ ] Графики с темной палитрой
- [ ] Scrollbar стилизован
- [ ] Selection цвета
- [ ] Focus rings видимы

**Примеры компонентов в тёмной теме:**
```
Card Dark:
- Background: #1E293B
- Border: 1px solid #334155
- Shadow: 0 4px 6px rgba(0,0,0,0.3)
- Hover: border-color #475569

Button Primary Dark:
- Background: #3B82F6
- Hover: #60A5FA
- Text: #FFFFFF
```

---

#### 🟡 Средний приоритет

##### 6. Адаптивный дизайн (Responsive)

**Breakpoints:**
- Mobile: < 600px
- Tablet: 600px - 960px
- Desktop: 960px - 1440px
- Large: > 1440px

**Адаптации для компонентов:**
- Sidebar → Bottom Navigation (mobile)
- DataTable → Cards list (mobile)
- Charts → Simplified/Stacked (mobile)
- Forms → Full width, stacked fields (mobile)
- Header → Hamburger menu, collapsed search

##### 7. Микро-взаимодействия (Micro-interactions)

**Анимации:**
- Page transitions (fade + slide)
- Card hover effects (lift + shadow)
- Button press (scale 0.98)
- Loading states (skeleton shimmer)
- Toast enter/exit (slide + fade)
- Modal open/close (scale + fade)
- Drag & drop (ghost + drop indicator)

**Характеристики:**
- Duration: 150-300ms
- Easing: ease-out, cubic-bezier(0.4, 0, 0.2, 1)
- Performance: use transform, opacity

##### 8. Accessibility (A11y)

**Требования:**
- Контрастность текста WCAG AA (4.5:1)
- Focus indicators visible
- Keyboard navigation
- ARIA labels
- Screen reader support
- Reduced motion support
- Цвет не единственный индикатор состояния

---

## 4. Формат Доставки

### 4.1 Дизайн-макеты
- **Формат**: Figma-совместимый или изображения PNG/SVG
- **Структура**: По страницам и компонентам
- **Темы**: Отдельные версии для Light/Dark

### 4.2 Документация
```
docs/
├── design-system/
│   ├── colors.md          # Полная палитра
│   ├── typography.md      # Шрифты
│   ├── spacing.md         # Отступы
│   ├── shadows.md         # Тени
│   └── components/        # Документация компонентов
├── layouts/
│   ├── dashboard.md
│   ├── tests.md
│   ├── test-editor.md
│   ├── categories.md
│   ├── users.md
│   └── analytics.md
└── assets/
    ├── icons/             # SVG иконки
    └── images/            # Примеры изображений
```

### 4.3 CSS/CSS-in-JS стили
- CSS variables для тем
- MUI theme overrides
- Component variants
- Animation keyframes

---

## 5. Критерии Приёмки

### 5.1 Обязательно
- [ ] Полная палитра для Light и Dark тем
- [ ] Все компоненты с обеими темами
- [ ] Адаптивность для mobile/tablet/desktop
- [ ] Accessibility compliance (WCAG AA)
- [ ] Документация для разработчиков

### 5.2 Желательно
- [ ] Интерактивные прототипы
- [ ] Анимационные спецификации
- [ ] Иконографика (SVG set)
- [ ] Гайдлайны по использованию

---

## 6. Ограничения и Учёт Текущего Кода

### 6.1 НЕ изменять структуру
- Сохранить существующую файловую структуру
- НЕ менять названия компонентов
- НЕ менять роутинг

### 6.2 Совместимость с MUI v6
- Использовать MUI system (sx prop)
- Поддержка emotion/styled
- Override MUI компонентов через theme.components

### 6.3 Цвета должны быть вынесены
```typescript
// Правильно:
const theme = useTheme();
<Box sx={{ bgcolor: theme.palette.background.paper }} />

// Неправильно:
<Box sx={{ bgcolor: '#FFFFFF' }} />
```

---

## 7. Примеры для Вдохновения

### 7.1 Референсы
- **Vercel Dashboard**: Тёмная тема, минимализм
- **GitHub**: Система цветов, контрастность
- **Linear**: Интеракции, анимации
- **Notion**: Редактор контента
- **Stripe**: Документация, формы

### 7.2 Образовательные платформы
- Duolingo for Business (админка)
- Coursera Admin
- Khan Academy

---

## 8. Контакты и Доступ

### 8.1 Для вопросов
- Текущий код админки: `admin-web/src/`
- Тема: `admin-web/src/theme/`
- Компоненты: `admin-web/src/components/`

### 8.2 Тестовые данные
- Для просмотра реальных форм запустить:
```bash
cd admin-web
npm install
npm run dev
```
- Доступ: http://localhost:5173
- Логин: admin@sotospeak.com / admin123

---

## 9. Временные Рамки

- **Анализ текущего кода**: 2-4 часа
- **Создание палитры и токенов**: 4-6 часов
- **Компоненты (Storybook style)**: 12-16 часов
- **Макеты страниц**: 8-12 часов
- **Тёмная тема детали**: 4-6 часов
- **Документация**: 4 часа

**Итого**: 34-48 часов

---

## 10. Финальный Результат

По завершению ожидается:
1. ✅ Полный набор макетов в Figma/PNG
2. ✅ Design tokens (colors, typography, spacing)
3. ✅ Component specifications
4. ✅ Theme implementation guide
5. ✅ Assets (icons, illustrations)
6. ✅ Responsive breakpoints spec
7. ✅ Animation specifications

---

**Примечание**: Это ТЗ для агента kimi 2.5 по созданию ДИЗАЙНА и МАКЕТОВ, не кода. Реализация кода будет выполнена отдельно на основе предоставленных макетов.
