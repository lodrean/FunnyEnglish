# Chromatic + Storybook Integration

## Обзор

Chromatic интегрирован с Storybook для визуального регрессионного тестирования компонентов.

## Что было настроено

- ✅ Storybook конфигурация (`.storybook/main.ts`)
- ✅ Preview с MUI темой (`.storybook/preview.tsx`)
- ✅ Chromatic GitHub Actions workflow
- ✅ Пример stories для Button компонента

## Локальный запуск

### Сборка Storybook

```bash
cd admin-web
npm run build-storybook
```

### Запуск Storybook в dev режиме

```bash
cd admin-web
npm run storybook
```

### Публикация в Chromatic

```bash
cd admin-web
npm run build-storybook
npx chromatic --project-token=chpt_8823bd24ed03ea4 --storybook-build-dir=storybook-static
```

## GitHub Actions

Workflow автоматически:
1. Собирает Storybook
2. Публикует в Chromatic
3. Сообщает о визуальных изменениях

Файл: `.github/workflows/chromatic.yml`

## Добавление новых stories

```typescript
// ComponentName.stories.tsx
import type { Meta, StoryObj } from '@storybook/react';
import { ComponentName } from './ComponentName';

const meta: Meta<typeof ComponentName> = {
  title: 'Components/ComponentName',
  component: ComponentName,
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    // props
  },
};
```

## Review изменений

1. Откройте ссылку из CLI вывода
2. Просмотрите visual diff
3. Accept или Deny изменения
4. Изменения автоматически становятся baseline

## Полезные ссылки

- [Chromatic Docs](https://www.chromatic.com/docs/)
- [Storybook Docs](https://storybook.js.org/docs/react/get-started/introduction)
