# Playwright MCP для So to Speak Admin

Этот проект настроен для использования Playwright MCP сервера, который позволяет Claude напрямую взаимодействовать с браузером для тестирования и анализа админ-панели.

## 🚀 Быстрый старт

### 1. Установка (уже выполнено)

```bash
cd admin-web
npm install -D @playwright/test
npx playwright install chromium
```

### 2. Запуск тестов

```bash
# Все тесты
npm run test:e2e

# С UI интерфейсом
npm run test:e2e:ui

# В headed режиме (видимый браузер)
npm run test:e2e:headed

# Debug режим
npm run test:e2e:debug
```

## 💡 Как использовать с Claude

### Пример 1: Анализ страницы логина

```
Перейди на http://localhost:5173/login и проанализируй:
1. Какие поля формы доступны?
2. Есть ли accessibility атрибуты?
3. Какие цвета используются?
4. Есть ли клиентская валидация?
```

### Пример 2: Тестирование авторизации

```
Открой http://localhost:5173 и протестируй логин:
1. Введи email: admin@sotospeak.com, password: admin123
2. Нажми кнопку входа
3. Проверь редирект на дашборд
4. Сделай скриншот
```

### Пример 3: Проверка responsive design

```
Проверь адаптивность:
1. Открой дашборд при 1920x1080
2. Сделай скриншот
3. Измени размер на 375x667 (iPhone)
4. Сделай скриншот
5. Опиши различия
```

## 📁 Структура тестов

```
admin-web/
├── e2e/
│   ├── tests/
│   │   ├── auth.spec.ts         # Тесты авторизации
│   │   ├── dashboard.spec.ts    # Тесты дашборда
│   │   └── navigation.spec.ts   # Тесты навигации
│   ├── pages/
│   │   ├── LoginPage.ts         # Page Object для логина
│   │   └── DashboardPage.ts     # Page Object для дашборда
│   ├── fixtures/
│   │   └── auth.fixture.ts      # Фикстуры аутентификации
│   ├── README.md                # Документация по тестам
│   └── MCP_EXAMPLES.md          # Примеры MCP запросов
└── playwright.config.ts         # Конфигурация Playwright
```

## 🛠️ Команды

| Команда | Описание |
|---------|----------|
| `npm run test:e2e` | Запуск всех E2E тестов |
| `npm run test:e2e:ui` | Запуск с UI интерфейсом |
| `npm run test:e2e:headed` | Запуск с видимым браузером |
| `npm run test:e2e:debug` | Debug режим с пошаговым выполнением |
| `npm run test:e2e:report` | Просмотр HTML отчета |

## 📊 Отчеты

Playwright автоматически генерирует:
- **HTML отчеты** - `playwright-report/`
- **Скриншоты** - при ошибках
- **Видео** - `on-first-retry`
- **Trace** - для детальной отладки

## 🎯 Use Cases для MCP

### 1. Визуальное тестирование
Claude может:
- Открывать страницы и делать скриншоты
- Сравнивать состояния до/после
- Проверять responsive design

### 2. Анализ компонентов
- Определять используемые библиотеки (Material UI)
- Проверять доступность (accessibility)
- Анализировать структуру DOM

### 3. Интерактивное тестирование
- Заполнять формы
- Кликать по элементам
- Проверять навигацию
- Тестировать валидацию

### 4. Генерация тестов
Claude может записывать действия и генерировать Playwright тесты на их основе.

## 🔧 Настройка окружения

Создайте `.env.local` в `admin-web/`:

```env
# URL для тестов
ADMIN_URL=http://localhost:5173

# Тестовые учетные данные
TEST_ADMIN_EMAIL=admin@sotospeak.com
TEST_ADMIN_PASSWORD=admin123

# Пропустить автозапуск сервера
SKIP_WEB_SERVER=true
```

## 📝 Примеры запросов к Claude

### Полный аудит страницы
```
Выполни полный аудит страницы http://localhost:5173/login:
1. Структура и семантика HTML
2. CSS стили и дизайн
3. Интерактивные элементы
4. Accessibility
5. Performance метрики
6. Console errors
```

### Тестирование формы
```
Протестируй форму создания пользователя:
1. Перейди на страницу Users
2. Нажми Add User
3. Попробуй сохранить пустую форму
4. Проверь ошибки валидации
5. Заполни валидными данными
6. Сохрани и проверь результат
```

### Responsive testing
```
Проверь адаптивность всех основных страниц:
- Login
- Dashboard
- Users
- Settings

Используй breakpoints: 1920, 1366, 768, 375
```

## 🔍 Отладка

### С trace файлом
```bash
npx playwright test --trace on
npx playwright show-trace trace.zip
```

### Скриншоты на каждом шаге
```typescript
await page.screenshot({ path: 'step1.png' });
```

### Console logs
```typescript
page.on('console', msg => console.log(msg.text()));
```

## 📚 Ресурсы

- [Playwright Docs](https://playwright.dev/docs/intro)
- [Playwright API](https://playwright.dev/docs/api/class-page)
- [MCP Examples](./admin-web/e2e/MCP_EXAMPLES.md)

## 🤝 Интеграция с CI

```yaml
# .github/workflows/e2e.yml
- name: Run E2E Tests
  run: |
    cd admin-web
    npm ci
    npx playwright install --with-deps
    npm run test:e2e
  
- name: Upload Report
  if: failure()
  uses: actions/upload-artifact@v3
  with:
    name: playwright-report
    path: admin-web/playwright-report/
```
