# CMP (Compose Multiplatform) E2E Tests

E2E тесты для FunnyEnglish CMP приложения с использованием Playwright.

## 🎯 Цель

Тестирование WASM (WebAssembly) версии CMP приложения в браузере через Playwright.

## 📁 Структура

```
e2e-cmp/
├── playwright.config.ts    # Конфигурация Playwright
├── package.json            # Зависимости
├── README.md               # Этот файл
└── tests/
    ├── smoke.spec.ts       # Базовые тесты загрузки
    ├── auth.spec.ts        # Тесты авторизации
    ├── navigation.spec.ts  # Тесты навигации
    ├── image-word-match.spec.ts  # Тесты IWM
    ├── gamification.spec.ts      # Тесты геймификации
    └── performance.spec.ts       # Тесты производительности
```

## 🚀 Быстрый старт

### 1. Установка зависимостей

```bash
cd e2e-cmp
npm install
npx playwright install
```

### 2. Запуск тестов

```bash
# Запустить все тесты
npm test

# Запустить с UI режимом
npm run test:ui

# Запустить в debug режиме
npm run test:debug

# Показать отчёт
npm run test:report
```

### 3. Запуск с существующим сервером

Если WASM dev server уже запущен:

```bash
SKIP_WEB_SERVER=true npm test
```

Или с другим URL:

```bash
CMP_URL=http://localhost:8082 npm test
```

## 🧪 Тесты

### Smoke Tests (`smoke.spec.ts`)
- ✅ Загрузка WASM приложения
- ✅ Отображение loading state
- ✅ Рендеринг контента
- ✅ Работа с изменением размера окна

### Auth Tests (`auth.spec.ts`)
- ✅ Отображение экрана логина
- ✅ Ввод с клавиатуры
- ✅ Сохранение сессии

### Navigation Tests (`navigation.spec.ts`)
- ✅ Навигация между экранами
- ✅ Back navigation
- ✅ Mobile viewport

### Image Word Match Tests (`image-word-match.spec.ts`)
- ✅ Загрузка теста
- ✅ Отображение изображения и hotspots
- ✅ Drag & drop взаимодействие
- ✅ Обратная связь о завершении

### Gamification Tests (`gamification.spec.ts`)
- ✅ Отображение статистики
- ✅ Достижения
- ✅ Таблица лидеров
- ✅ Streak

### Performance Tests (`performance.spec.ts`)
- ⏱️ Время загрузки
- 💾 Использование памяти
- 🎮 Отзывчивость
- 📊 FPS

## ⚙️ Конфигурация

### Web Server

Playwright автоматически запускает WASM dev server:

```typescript
webServer: {
  command: 'cd ../composeApp && ../gradlew wasmJsBrowserDevelopmentRun',
  url: 'http://localhost:8081',
  timeout: 600 * 1000, // 10 минут на компиляцию
}
```

### Проекты

- `chromium` - Desktop Chrome
- `Mobile Chrome` - Pixel 5 emulation

## 🎨 Особенности тестирования CMP

### Canvas-based рендеринг

CMP рендерит в HTML5 Canvas, поэтому:
- Нельзя использовать CSS селекторы
- Взаимодействие через координаты
- Screenshot comparison для визуальной проверки

### Пример клика по координатам

```typescript
const canvas = page.locator('canvas');
const box = await canvas.boundingBox();

await canvas.click({
  position: { x: box.width / 2, y: box.height / 2 }
});
```

### Console Logs

```typescript
page.on('console', msg => {
  console.log(`[${msg.type()}] ${msg.text()}`);
});
```

## 📊 Отчёты

После запуска тестов:

```bash
npx playwright show-report
```

Скриншоты сохраняются в `test-results/`.

## 🔧 Troubleshooting

### WASM не компилируется

```bash
# Увеличить память в gradle.properties
kotlin.daemon.jvmargs=-Xmx4096m
```

### Таймаут при запуске сервера

```bash
# Запустить сервер отдельно
cd composeApp
./gradlew wasmJsBrowserDevelopmentRun

# В другом терминале
SKIP_WEB_SERVER=true npm test
```

### Canvas не найден

```bash
# Увеличить таймаут
npx playwright test --timeout 120000
```

## 📝 Добавление новых тестов

```typescript
import { test, expect } from '@playwright/test';

test('my test', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('canvas')).toBeVisible({ timeout: 30000 });
  
  // Your test code here
  await page.screenshot({ path: 'test-results/my-test.png' });
});
```

## ✅ CI/CD

Для запуска в CI:

```yaml
- name: Run CMP E2E Tests
  run: |
    cd e2e-cmp
    npm ci
    npx playwright install --with-deps
    npm test
```

## 📚 Документация

- [Playwright Docs](https://playwright.dev/)
- [Kotlin/WASM](https://kotlinlang.org/docs/wasm-overview.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-mpp/)
