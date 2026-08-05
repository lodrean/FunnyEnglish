# CMP (Compose Multiplatform) E2E Tests

E2E тесты для So to Speak CMP приложения с использованием Playwright.

## 🎯 Цель

Тестирование WASM (WebAssembly) версии CMP приложения в браузере через Playwright.

## 📁 Структура

```
e2e-cmp/
├── playwright.config.ts    # Конфигурация Playwright
├── package.json            # Зависимости
├── README.md               # Этот файл
└── tests/
    ├── helpers.ts          # Координаты/хелперы speaking-флоу (canvas-only)
    ├── smoke.spec.ts       # Базовые тесты загрузки + онбординг
    ├── auth.spec.ts        # Онбординг → Register/Login/guest → Library
    ├── navigation.spec.ts  # Speaking-флоу: Library→Topics→Questions→Training
    ├── config.spec.ts      # Проверка конфигурации сьюта
    └── performance.spec.ts # Тесты производительности
```

> После пивота продукта (SPEAKING-TRAINER, bd `8tg.5.5`) legacy-спеки
> `image-word-match.spec.ts` и `gamification.spec.ts` удалены.

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
- ✅ Онбординг: 2 слайда → экран выбора режима
- ✅ Register ↔ Login навигация
- ✅ Успешный логин (admin dev-стека) → Library
- ✅ Гостевая сессия → Library, переживает reload

### Navigation Tests (`navigation.spec.ts`)
- ✅ Speaking-флоу гостя: Library → Topics → шит субтитров → Questions → Training
- ✅ Practice-гейтинг гостя (CTA на логин)
- ✅ «Мои записи» гостя — заглушка с CTA регистрации
- ✅ Back navigation (Topics→Library, Training→Questions)
- ✅ Mobile viewport

### Config Tests (`config.spec.ts`)
- ✅ Валидность playwright.config.ts, структура сьюта

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

CMP 1.7.1 (wasmJs) рендерит в HTML5 Canvas, семантика/testTag'и в DOM НЕ экспонируются:
- Нельзя использовать CSS/text-селекторы для контента приложения
- Взаимодействие через координаты (калиброваны под 1280x720, см. `tests/helpers.ts`)
- Assertion'ы: смена пикселей clipped-региона + отсутствие console.error/HTTP 5xx
- Координатные тесты скипаются на мобильном проекте (`skipOnMobile`)

### Требования к окружению

- Backend на `:8080` с seed-контентом («Разговорный английский»): `docker compose up -d`
- Креды для логина: `E2E_USER_EMAIL` / `E2E_USER_PASSWORD` (дефолт — admin dev-стека)

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
