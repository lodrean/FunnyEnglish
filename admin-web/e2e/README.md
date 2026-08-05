# E2E Тестирование с Playwright

Эта директория содержит E2E тесты для So to Speak Admin Panel с использованием Playwright.

## Быстрый старт

```bash
# Установка зависимостей
npm install

# Установка браузеров Playwright
npx playwright install

# Запуск всех тестов
npm run test:e2e

# Запуск тестов с UI
npm run test:e2e:ui

# Запуск в headed режиме (видимый браузер)
npx playwright test --headed

# Запуск конкретного теста
npx playwright test auth.spec.ts
```

## Структура

```
e2e/
├── fixtures/          # Фикстуры для тестов (auth, data)
├── pages/             # Page Object Model
│   ├── LoginPage.ts
│   └── DashboardPage.ts
├── tests/             # Тестовые спеки
│   ├── auth.spec.ts
│   ├── dashboard.spec.ts
│   └── navigation.spec.ts
├── screenshots/       # Скриншоты (gitignored)
└── README.md          # Этот файл
```

## Переменные окружения

Создайте `.env.local` файл:

```env
# URL админ-панели
ADMIN_URL=http://localhost:5173

# Тестовые учетные данные
TEST_ADMIN_EMAIL=admin@sotospeak.com
TEST_ADMIN_PASSWORD=admin123

# Пропустить автозапуск dev сервера
SKIP_WEB_SERVER=true
```

## Использование с Claude MCP

Playwright MCP сервер позволяет Claude взаимодействовать с браузером напрямую:

### Пример: Анализ компонентов

```
Перейди на http://localhost:5173, сделай логин с admin@sotospeak.com / admin123,
проанализируй компоненты на дашборде и проверь соответствие дизайн-системе.
```

### Пример: Тестирование форм

```
Открой страницу создания пользователя, заполни форму тестовыми данными,
проверь валидацию полей и сделай скриншот результата.
```

### Пример: Визуальное сравнение

```
1. Открой дашборд
2. Сделай скриншот
3. Измени размер окна до мобильного
4. Сделай скриншот
5. Сравни адаптивность интерфейса
```

## Page Object Model

### LoginPage

```typescript
const loginPage = new LoginPage(page);
await loginPage.goto();
await loginPage.login('admin@example.com', 'password');
await loginPage.expectSuccessfulLogin();
```

### DashboardPage

```typescript
const dashboardPage = new DashboardPage(page);
await dashboardPage.goto();
await dashboardPage.navigateTo('Users');
await dashboardPage.logout();
```

## Написание тестов

### Базовый тест

```typescript
import { test, expect } from '@playwright/test';

test('должен отображать дашборд', async ({ page }) => {
  await page.goto('/dashboard');
  await expect(page.locator('h1')).toContainText('Dashboard');
});
```

### Тест с аутентификацией

```typescript
import { test, expect } from '../fixtures/auth.fixture';

test('должен показывать данные пользователя', async ({ page, authenticatedPage }) => {
  await authenticatedPage.login('admin@example.com', 'password');
  await page.goto('/profile');
  await expect(page.locator('.user-name')).toBeVisible();
});
```

## Отладка

```bash
# Запуск в debug режиме
npx playwright test --debug

# Запись trace (включается автоматически при ошибке)
npx playwright test --trace on

# Просмотр отчета
npx playwright show-report
```

## CI/CD Интеграция

```yaml
# Пример GitHub Actions
- name: Run Playwright tests
  run: |
    cd admin-web
    npx playwright test
```

## Полезные команды

| Команда | Описание |
|---------|----------|
| `npx playwright test` | Запуск всех тестов |
| `npx playwright test --headed` | Запуск с видимым браузером |
| `npx playwright test --ui` | Запуск с UI режимом |
| `npx playwright codegen` | Генерация тестов из действий |
| `npx playwright show-report` | Просмотр HTML отчета |

## Ресурсы

- [Playwright Docs](https://playwright.dev/docs/intro)
- [Best Practices](https://playwright.dev/docs/best-practices)
- [Selectors](https://playwright.dev/docs/selectors)
- [Assertions](https://playwright.dev/docs/test-assertions)
