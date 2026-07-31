# E2E Тесты: Drag-and-Drop

Эта директория содержит E2E тесты для функционала drag-and-drop в FunnyEnglish Admin Panel.

## Содержимое

### Тестовые файлы

| Файл | Описание | Тест-кейсы |
|------|----------|------------|
| `image-word-match.spec.ts` | Тесты создания IMAGE_WORD_MATCH вопросов | TC-IWM-001 - TC-IWM-010 |

### Page Objects

| Файл | Описание |
|------|----------|
| `../../pages/ImageWordMatchEditorPage.ts` | Page Object для многошагового редактора |

## Типы Drag-and-Drop тестов

### 1. Image-Word-Match (IMAGE_WORD_MATCH)

Ученики перетаскивают слова к областям на изображении.

**Редактор (Admin Panel):**
- Step 1: Загрузка изображения
- Step 2: Добавление слов
- Step 3: Создание hotspot областей
- Step 4: Preview и сохранение

**Мобильное приложение:**
- Перетаскивание слов на изображение
- Анимации успеха/ошибки
- Подсчет очков

### 2. Drag-Drop-Match (DRAG_DROP_MATCH)

Сопоставление пар элементов перетаскиванием.

## Запуск тестов

### Все drag-and-drop тесты

```bash
cd admin-web
npx playwright test tests/dragdrop/
```

### Конкретный тест

```bash
npx playwright test tests/dragdrop/image-word-match.spec.ts
```

### С UI режимом

```bash
npx playwright test tests/dragdrop/ --ui
```

### Headed режим (видимый браузер)

```bash
npx playwright test tests/dragdrop/ --headed
```

## Структура тестов

### image-word-match.spec.ts

```
Image-Word-Match Editor
├── Step 1: Image Upload
│   ├── должен загружать изображение (TC-IWM-002)
│   └── должен показывать ошибку для файла > 5MB (TC-IWM-002-N1)
├── Step 2: Add Words
│   ├── должен добавлять слова с переводом (TC-IWM-003)
│   ├── должен ограничивать максимум 8 слов (TC-IWM-003-N2)
│   └── должен удалять слова (TC-IWM-009)
├── Step 3: Hotspots
│   ├── должен создавать прямоугольные hotspot (TC-IWM-004)
│   ├── должен переключать инструменты (TC-IWM-005)
│   ├── должен работать zoom (TC-IWM-005)
│   └── должен удалять hotspot (TC-IWM-009)
├── Step 4: Preview & Save
│   ├── должен показывать preview (TC-IWM-006)
│   └── должен сохранять вопрос (TC-IWM-006)
└── Full Workflow
    ├── полный флоу создания IMAGE_WORD_MATCH вопроса (TC-IWM-007)
    └── навигация между шагами (TC-IWM-010)
```

## Требования к окружению

### Тестовые изображения

Файлы должны находиться в `e2e/fixtures/`:
- `test-image.jpg` - обычное изображение для тестов
- `large-image.jpg` - изображение > 5MB для негативных тестов

Создать тестовое изображение:
```bash
# Linux/Mac
convert -size 400x300 xc:lightblue -pointsize 30 -fill black -gravity center -annotate +0+0 "Test Image" e2e/fixtures/test-image.jpg

# Windows (PowerShell with ImageMagick)
magick convert -size 400x300 xc:lightblue -pointsize 30 -fill black -gravity center -annotate +0+0 "Test Image" e2e/fixtures/test-image.jpg
```

### Переменные окружения

```env
# .env.local
ADMIN_URL=http://localhost:5173
TEST_ADMIN_EMAIL=admin@funnyenglish.com
TEST_ADMIN_PASSWORD=admin123
```

## Отладка

### Trace Mode

```bash
npx playwright test tests/dragdrop/ --trace on
npx playwright show-trace playwright-report/trace.zip
```

### Screenshots на каждом шаге

Добавить в тест:
```typescript
await page.screenshot({ path: `screenshots/step-${Date.now()}.png` });
```

### Console Logs

```typescript
page.on('console', msg => console.log(msg.text()));
```

## Maestro тесты (Mobile)

Мобильные E2E тесты находятся в `maestro/flows/`:

| Файл | Описание |
|------|----------|
| `image_word_match_admin.yaml` | Создание вопроса через админку |
| `image_word_match_play.yaml` | Прохождение теста в приложении |
| `dragdrop_comprehensive.yaml` | Комплексные drag-and-drop тесты |

### Запуск Maestro тестов

```bash
# Admin creation flow
maestro test maestro/flows/image_word_match_admin.yaml

# Play flow
maestro test maestro/flows/image_word_match_play.yaml

# Comprehensive tests
maestro test maestro/flows/dragdrop_comprehensive.yaml
```

## Связь с тест-кейсами

Полный список тест-кейсов: `qa/test-cases/IMAGE_WORD_MATCH_TEST_CASES.md`

Соответствие:
- E2E Playwright → TC-IWM-001 - TC-IWM-010
- Maestro Mobile → TC-IWM-MOB-001 - TC-IWM-MOB-006
