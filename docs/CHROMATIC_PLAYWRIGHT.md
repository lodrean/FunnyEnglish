# Chromatic + Playwright Integration

## Обзор

Интеграция Chromatic с Playwright позволяет:
- Автоматически создавать визуальные снапшоты при выполнении E2E тестов
- Обнаруживать визуальные регрессии
- Просматривать изменения UI в Chromatic UI

## Установка

```bash
cd admin-web
npm install --save-dev @chromatic-com/playwright chromatic
```

## Как это работает

Chromatic запускает Playwright тесты и автоматически:
1. Создает архивы со скриншотами
2. Загружает их в Chromatic
3. Сравнивает с baseline
4. Показывает visual diff в UI

## Обновление тестов

### Импорты

Замените импорты во всех тестовых файлах:

```typescript
// ➖ Remove this line
import { test, expect } from '@playwright/test';

// ➕ Add this line
import { test, expect } from '@chromatic-com/playwright';
```

### Использование

Используйте `expect(page).toHaveScreenshot()` для создания визуальных снапшотов:

```typescript
import { test, expect } from '@chromatic-com/playwright';
import { LoginPage } from '../pages/LoginPage';

test('Login page visual regression', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await loginPage.goto();
  
  // Создает визуальный снапшот
  await expect(page).toHaveScreenshot('login-page.png');
});

test('Dashboard after login', async ({ page }) => {
  // ... login logic
  
  // Снапшот конкретного элемента
  await expect(page.locator('[data-testid="stats-cards"]')).toHaveScreenshot('stats.png');
});
```

## Конфигурация

### Основная конфигурация (playwright.config.ts)

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  use: {
    // Обязательно для Chromatic
    screenshot: 'on',
    
    // Задержка перед скриншотом для стабильности
    delay: 500,
  },
});
```

### Конфигурация для Chromatic (playwright.chromatic.config.ts)

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  outputDir: 'test-results',
  
  use: {
    screenshot: 'on',
    delay: 500,
  },
  
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list']
  ],
});
```

## Запуск тестов

### Локально (с Chromatic CLI)

```bash
cd admin-web

# Запуск через Chromatic (рекомендуется)
npm run test:e2e:visual

# Или напрямую с токеном
npx chromatic --playwright --project-token=YOUR_TOKEN
```

### Без Chromatic (только Playwright)

```bash
# Запуск только Playwright тестов
npm run test:e2e:visual:local
```

### Обновление снапшотов

```bash
# Обновить все снапшоты
npx playwright test --config=playwright.chromatic.config.ts --update-snapshots

# Обновить конкретный тест
npx playwright test tests/visual/visual-regression.spec.ts --update-snapshots
```

## NPM скрипты

```json
{
  "test:e2e:visual": "npx chromatic --playwright --project-token=$CHROMATIC_TOKEN",
  "test:e2e:visual:local": "playwright test --config=playwright.chromatic.config.ts",
  "chromatic": "chromatic --playwright"
}
```

## GitHub Actions

```yaml
name: Chromatic Visual Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  chromatic-visual-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: admin-web/package-lock.json

      - name: Install dependencies
        run: |
          cd admin-web
          npm ci

      - name: Install Playwright
        run: |
          cd admin-web
          npx playwright install chromium

      # ... setup backend and start servers ...

      - name: Run Playwright Tests and Upload to Chromatic
        uses: chromaui/action@latest
        with:
          projectToken: ${{ secrets.CHROMATIC_TOKEN }}
          workingDir: admin-web
          playwright: true
          exitOnceUploaded: true
```

## Работа с визуальными изменениями

### Игнорирование элементов

```typescript
// Игнорировать динамические элементы (время, анимации)
await expect(page).toHaveScreenshot('dashboard.png', {
  mask: [page.locator('[data-testid="current-time"]')],
  animations: 'disabled',
});
```

### Пороговые значения

```typescript
// Разрешить небольшие различия (например, для шрифтов)
await expect(page).toHaveScreenshot('page.png', {
  threshold: 0.2,
  maxDiffPixels: 100,
});
```

## Лучшие практики

1. **Стабильные селекторы**: Используйте `data-testid` вместо CSS классов
2. **Фиксированные данные**: Используйте моки для динамических данных
3. **Очистка состояния**: Сбрасывайте состояние между тестами
4. **Консистентность**: Запускайте тесты в одном и том же окружении
5. **Задержки**: Добавляйте `delay` перед скриншотами для стабильности

## Отладка

```bash
# Запуск в headed режиме для отладки
npx playwright test --headed

# Запуск с trace
npx playwright test --trace on

# Просмотр отчета
npx playwright show-report
```

## Troubleshooting

### Ошибка: "No baseline found"

**Причина:** Первый запуск или baseline был удален.

**Решение:** Принять новые снапшоты в Chromatic UI как baseline.

### Ошибка: "Project token is required"

**Причина:** Не указан токен Chromatic.

**Решение:**
```bash
npx chromatic --playwright --project-token=YOUR_TOKEN
```

### CI: публикация пропускается с notice "CHROMATIC_TOKEN secret is not set"

**Причина:** В репозитории не задан секрет `CHROMATIC_TOKEN` — workflow `.github/workflows/chromatic.yml`
пропускает шаг публикации (job при этом зелёный), чтобы отсутствие секрета не роняло CI.

**Решение (для включения реальной публикации):** владелец добавляет секрет
`Settings → Secrets and variables → Actions → CHROMATIC_TOKEN` (токен со страницы Manage проекта
на chromatic.com). После этого шаг `Publish to Chromatic` начнёт выполняться автоматически.

## Полезные ссылки

- [Chromatic Playwright Docs](https://www.chromatic.com/docs/playwright/)
- [Playwright Visual Comparisons](https://playwright.dev/docs/test-snapshots)
- [Chromatic CLI Options](https://www.chromatic.com/docs/cli/)
