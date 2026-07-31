# FunnyEnglish Design System

## Обзор

Полная дизайн-система для Admin Panel FunnyEnglish с поддержкой светлой и тёмной тем.

**Демо:** https://nfnir7jsjkdnu.ok.kimi.link

---

## Содержание

1. [Цветовая палитра](#цветовая-палитра)
2. [Типографика](#типографика)
3. [Отступы и сетка](#отступы-и-сетка)
4. [Компоненты](#компоненты)
5. [Макеты страниц](#макеты-страниц)
6. [Темы](#темы)
7. [Адаптивность](#адаптивность)

---

## Цветовая палитра

### Primary Colors

| Shade | Light Theme | Dark Theme | CSS Variable |
|-------|-------------|------------|--------------|
| 50 | `#E3F2FD` | `#0F172A` | `--color-primary-50` |
| 100 | `#BBDEFB` | `#1E293B` | `--color-primary-100` |
| 200 | `#90CAF9` | `#334155` | `--color-primary-200` |
| 300 | `#64B5F6` | `#475569` | `--color-primary-300` |
| 400 | `#42A5F5` | `#64748B` | `--color-primary-400` |
| 500 | `#4A90D9` | `#60A5FA` | `--color-primary-500` |
| 600 | `#3B82F6` | `#93C5FD` | `--color-primary-600` |
| 700 | `#2E5A8C` | `#BFDBFE` | `--color-primary-700` |
| 800 | `#1E3A5F` | `#DBEAFE` | `--color-primary-800` |
| 900 | `#0F1F33` | `#EFF6FF` | `--color-primary-900` |

### Semantic Colors

#### Light Theme
```
Background: #F5F5F5 (--bg-primary)
Surface: #FFFFFF (--bg-surface)
Surface Elevated: #FFFFFF (--bg-surface-elevated)
Text Primary: #212121 (--text-primary)
Text Secondary: #757575 (--text-secondary)
Border: #E0E0E0 (--border-primary)
```

#### Dark Theme
```
Background: #0A1929 (--bg-primary)
Surface: #1E293B (--bg-surface)
Surface Elevated: #334155 (--bg-surface-elevated)
Text Primary: #F8FAFC (--text-primary)
Text Secondary: #94A3B8 (--text-secondary)
Border: #334155 (--border-primary)
```

### Status Colors

| Status | Light | Dark |
|--------|-------|------|
| Success | `#43A047` | `#4ADE80` |
| Warning | `#FB8C00` | `#FBBF24` |
| Error | `#E53935` | `#F87171` |
| Info | `#2196F3` | `#60A5FA` |

---

## Типографика

### Font Family
- **Primary:** Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto
- **Mono:** JetBrains Mono, 'Fira Code', 'SF Mono', Consolas

### Type Scale

| Style | Size | Weight | Line Height |
|-------|------|--------|-------------|
| H1 | 36px | 700 | 1.25 |
| H2 | 30px | 700 | 1.25 |
| H3 | 24px | 600 | 1.375 |
| H4 | 20px | 600 | 1.375 |
| H5 | 18px | 500 | 1.375 |
| H6 | 16px | 500 | 1.5 |
| Body | 16px | 400 | 1.5 |
| Body Small | 14px | 400 | 1.5 |
| Caption | 12px | 500 | 1.5 |
| Button | 14px | 600 | 1 |

---

## Отступы и сетка

### Base Unit: 4px

| Token | Value |
|-------|-------|
| space-1 | 4px |
| space-2 | 8px |
| space-3 | 12px |
| space-4 | 16px |
| space-6 | 24px |
| space-8 | 32px |
| space-10 | 40px |
| space-12 | 48px |
| space-16 | 64px |

### Border Radius

| Token | Value |
|-------|-------|
| radius-sm | 4px |
| radius-md | 8px |
| radius-lg | 12px |
| radius-xl | 16px |
| radius-full | 9999px |

---

## Компоненты

### Buttons

#### Variants
- `btn-primary` - Основная кнопка
- `btn-secondary` - Вторичная кнопка
- `btn-outlined` - Контурная кнопка
- `btn-ghost` - Прозрачная кнопка
- `btn-danger` - Кнопка удаления

#### Sizes
- `btn-sm` - Маленькая
- Default - Стандартная
- `btn-lg` - Большая

#### States
- Default
- Hover (lift + shadow)
- Active (scale 0.98)
- Disabled (opacity 0.5)
- Loading (spinner)

### Form Inputs

#### Text Input
```html
<div class="form-field">
  <label class="form-label">Label</label>
  <input type="text" class="form-input" placeholder="Placeholder..." />
  <span class="form-helper">Helper text</span>
</div>
```

#### States
- Default
- Focus (border-color + shadow)
- Error (red border)
- Disabled

### Cards

#### Variants
- Default (border)
- `card-elevated` (shadow)
- `card-bordered` (thick border)
- `card-hover` (hover effect)

### Badges

#### Variants
- `badge-default`
- `badge-primary`
- `badge-success`
- `badge-warning`
- `badge-error`
- `badge-info`
- `badge-outline`
- `badge-soft`

### Data Table

```html
<div class="data-table-wrapper">
  <table class="data-table">
    <thead>
      <tr>
        <th>Column</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>Data</td>
      </tr>
    </tbody>
  </table>
</div>
```

### Toast Notifications

#### Types
- `toast-success`
- `toast-error`
- `toast-warning`
- `toast-info`

---

## Макеты страниц

### Dashboard
- Stats cards grid (4 cards)
- Charts section (AreaChart + BarChart)
- Recent Activity list
- Quick actions

### Tests List
- Search + Filters bar
- DataTable with columns
- Pagination
- Bulk actions toolbar

### Test Editor
- Two-panel layout
- Question builder with 5 types
- Drag & drop reordering
- Preview mode

### Categories
- Grid/List view toggle
- Category cards
- Detail sidebar

### Users
- User cards grid
- Filters (role, status)
- User detail sidebar

---

## Темы

### Переключение темы

```typescript
import { useTheme } from './design-system/components/ThemeProvider';

function MyComponent() {
  const { theme, toggleTheme, setTheme } = useTheme();
  
  return (
    <button onClick={toggleTheme}>
      Switch to {theme === 'light' ? 'dark' : 'light'} theme
    </button>
  );
}
```

### CSS переменные

Все цвета автоматически меняются при изменении атрибута `data-theme`:

```css
/* Light theme (default) */
:root {
  --bg-primary: #F5F5F5;
  --text-primary: #212121;
}

/* Dark theme */
[data-theme="dark"] {
  --bg-primary: #0A1929;
  --text-primary: #F8FAFC;
}
```

### Glow эффекты (только Dark)

```css
[data-theme="dark"] {
  --glow-primary: 0 0 20px rgba(96, 165, 250, 0.3);
  --glow-success: 0 0 20px rgba(74, 222, 128, 0.3);
  --glow-warning: 0 0 20px rgba(251, 191, 36, 0.3);
  --glow-error: 0 0 20px rgba(248, 113, 113, 0.3);
}
```

---

## Адаптивность

### Breakpoints

| Breakpoint | Width |
|------------|-------|
| Mobile | < 600px |
| Tablet | 600px - 960px |
| Desktop | 960px - 1440px |
| Large | > 1440px |

### Адаптации

- **Sidebar** → Bottom Navigation (mobile)
- **DataTable** → Cards list (mobile)
- **Charts** → Simplified/Stacked (mobile)
- **Forms** → Full width, stacked fields (mobile)
- **Header** → Hamburger menu, collapsed search

---

## Микро-взаимодействия

### Длительность
- Fast: 100ms
- Normal: 150ms
- Medium: 200ms
- Slow: 300ms

### Easing
- `ease-out` - для hover
- `cubic-bezier(0.4, 0, 0.2, 1)` - для transitions
- `cubic-bezier(0.68, -0.55, 0.265, 1.55)` - для bounce

### Анимации
- `fadeIn` / `fadeOut`
- `fadeInUp` / `fadeInDown`
- `scaleIn` / `scaleOut`
- `slideInRight` / `slideOutRight`
- `spin` - для loaders
- `shimmer` - для skeleton

---

## Accessibility

- Контрастность текста WCAG AA (4.5:1)
- Focus indicators visible
- Keyboard navigation
- ARIA labels
- Screen reader support
- `prefers-reduced-motion` support

---

## Файловая структура

```
src/
├── design-system/
│   ├── tokens/
│   │   ├── colors.css
│   │   ├── typography.css
│   │   ├── spacing.css
│   │   ├── animations.css
│   │   └── index.css
│   └── components/
│       └── ThemeProvider.tsx
├── sections/
│   ├── colors/
│   │   └── ColorPalette.tsx
│   ├── typography/
│   │   └── TypographyScale.tsx
│   ├── components/
│   │   ├── ButtonShowcase.tsx
│   │   ├── FormShowcase.tsx
│   │   ├── DataDisplayShowcase.tsx
│   │   └── FeedbackShowcase.tsx
│   └── pages/
│       ├── DashboardLayout.tsx
│       ├── TestsListLayout.tsx
│       ├── TestEditorLayout.tsx
│       ├── CategoriesLayout.tsx
│       └── UsersLayout.tsx
├── App.tsx
└── App.css
```

---

## Рекомендации по коду

### Правильно:
```typescript
const theme = useTheme();
<Box sx={{ bgcolor: theme.palette.background.paper }} />
```

### Неправильно:
```typescript
<Box sx={{ bgcolor: '#FFFFFF' }} />
```

### Использование CSS переменных:
```css
.my-component {
  background-color: var(--bg-surface);
  color: var(--text-primary);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
}
```

---

## Референсы

- **Vercel Dashboard** - Тёмная тема, минимализм
- **GitHub** - Система цветов, контрастность
- **Linear** - Интеракции, анимации
- **Notion** - Редактор контента
- **Stripe** - Документация, формы
