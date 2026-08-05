# Руководство по тестированию Drag-and-Drop

## Обзор

Этот документ описывает полный набор тест-кейсов и E2E тестов для функционала drag-and-drop в So to Speak.

## Что было создано

### 1. Тест-кейсы (`qa/test-cases/IMAGE_WORD_MATCH_TEST_CASES.md`)

Содержит **20+ тест-кейсов** для Image-Word-Match функционала:

#### Admin Panel тест-кейсы:
- **TC-IWM-001**: Создание теста с типом IMAGE_WORD_MATCH
- **TC-IWM-002**: Загрузка изображения
- **TC-IWM-003**: Добавление слов
- **TC-IWM-004**: Создание hotspot областей
- **TC-IWM-005**: Работа с инструментами canvas
- **TC-IWM-006**: Предпросмотр и сохранение
- **TC-IWM-007**: Полный флоу создания теста
- **TC-IWM-008**: Редактирование вопроса
- **TC-IWM-009**: Удаление слова и hotspot
- **TC-IWM-010**: Навигация между шагами

#### Mobile App тест-кейсы:
- **TC-IWM-MOB-001**: Отображение drag-and-drop теста
- **TC-IWM-MOB-002**: Успешное перетаскивание слова
- **TC-IWM-MOB-003**: Неправильное сопоставление
- **TC-IWM-MOB-004**: Завершение теста
- **TC-IWM-MOB-005**: Отмена перетаскивания

#### Performance и Accessibility:
- **TC-IWM-PERF-001**: Большое изображение
- **TC-IWM-A11Y-001**: Keyboard navigation

---

### 2. Playwright E2E тесты (`admin-web/e2e/tests/dragdrop/`)

#### Page Object (`ImageWordMatchEditorPage.ts`)

Полноценный Page Object с методами для:
- **Step 1 (Image)**: `uploadImage()`, `expectImageUploaded()`
- **Step 2 (Words)**: `addWord()`, `removeWord()`, `getWordCount()`
- **Step 3 (Hotspots)**: `selectTool()`, `drawRectangleHotspot()`, `linkHotspotToWord()`, zoom операции
- **Step 4 (Preview)**: `expectPreviewDisplayed()`, `saveQuestion()`
- **Full Workflow**: `createFullQuestion()` для полного флоу

#### Тестовый файл (`image-word-match.spec.ts`)

**12 тестов** покрывающих:

```
✓ Загрузка изображения
✓ Ошибка для файла > 5MB
✓ Добавление слов с переводом
✓ Ограничение в 8 слов
✓ Удаление слов
✓ Создание hotspot
✓ Переключение инструментов
✓ Zoom операции
✓ Удаление hotspot
✓ Preview отображение
✓ Сохранение вопроса
✓ Полный флоу
✓ Навигация между шагами
```

---

### 3. Maestro E2E тесты (`maestro/flows/`)

#### Существующие тесты:
- `image_word_match_admin.yaml` - Создание вопроса через админку
- `image_word_match_play.yaml` - Прохождение теста в приложении

#### Новый тест:
- `dragdrop_comprehensive.yaml` - Комплексные тесты включая:
  - Отмену перетаскивания
  - Неправильное сопоставление
  - Частичное заполнение
  - Performance тест

---

## Быстрый старт

### Запуск Playwright тестов

```bash
cd admin-web

# Установка (если не установлено)
npm install
npx playwright install chromium

# Все drag-and-drop тесты
npx playwright test tests/dragdrop/

# С UI режимом
npx playwright test tests/dragdrop/ --ui

# Headed режим
npx playwright test tests/dragdrop/ --headed
```

### Запуск через batch скрипт

```bash
run-dragdrop-tests.bat
```

### Запуск Maestro тестов

```bash
# Создание вопроса
maestro test maestro/flows/image_word_match_admin.yaml

# Прохождение теста
maestro test maestro/flows/image_word_match_play.yaml

# Комплексные тесты
maestro test maestro/flows/dragdrop_comprehensive.yaml
```

---

## Структура файлов

```
So to Speak/
├── qa/
│   └── test-cases/
│       ├── IMAGE_WORD_MATCH_TEST_CASES.md      # Тест-кейсы
│       └── DRAG_DROP_TESTING_GUIDE.md          # Этот файл
│
├── admin-web/
│   └── e2e/
│       ├── fixtures/
│       │   └── test-image.jpg                  # Тестовое изображение
│       ├── pages/
│       │   ├── TestsPage.ts                    # Существующий
│       │   └── ImageWordMatchEditorPage.ts     # Новый Page Object
│       └── tests/
│           └── dragdrop/
│               ├── image-word-match.spec.ts    # E2E тесты
│               └── README.md                   # Документация
│
├── maestro/
│   └── flows/
│       ├── image_word_match_admin.yaml         # Создание вопроса
│       ├── image_word_match_play.yaml          # Прохождение теста
│       └── dragdrop_comprehensive.yaml         # Комплексные тесты
│
└── run-dragdrop-tests.bat                      # Скрипт запуска
```

---

## Чек-лист перед релизом

### Admin Panel (Playwright)

- [ ] TC-IWM-001: Создание теста с IMAGE_WORD_MATCH
- [ ] TC-IWM-002: Загрузка изображения (< 5MB)
- [ ] TC-IWM-002-N1: Ошибка для файла > 5MB
- [ ] TC-IWM-003: Добавление слов (2-8)
- [ ] TC-IWM-004: Создание hotspot областей
- [ ] TC-IWM-005: Инструменты canvas (select, rectangle, circle)
- [ ] TC-IWM-006: Preview и сохранение
- [ ] TC-IWM-007: Полный флоу создания
- [ ] TC-IWM-009: Удаление слов и hotspot

### Mobile App (Maestro)

- [ ] TC-IWM-MOB-001: Отображение теста
- [ ] TC-IWM-MOB-002: Успешное сопоставление
- [ ] TC-IWM-MOB-003: Неправильное сопоставление (возврат слова)
- [ ] TC-IWM-MOB-004: Завершение и результаты
- [ ] TC-IWM-MOB-005: Отмена перетаскивания

### Performance

- [ ] Загрузка изображения < 3 сек
- [ ] Canvas 60 FPS
- [ ] Нет утечек памяти

---

## Troubleshooting

### Playwright

**Проблема**: Тест не находит элементы  
**Решение**: Проверьте `data-testid` атрибуты в компонентах

**Проблема**: Изображение не загружается  
**Решение**: Убедитесь что файл `test-image.jpg` существует в `e2e/fixtures/`

**Проблема**: Тест падает на canvas операциях  
**Решение**: Запустите с `--headed` для визуальной отладки

### Maestro

**Проблема**: Drag-and-drop не работает  
**Решение**: Используйте `swipe` вместо `dragAndDrop` для сложных сценариев

**Проблема**: Элементы не находятся  
**Решение**: Добавьте `optional: true` и проверьте accessibility labels

---

## Дополнительная документация

- [Playwright Docs](https://playwright.dev/docs/intro)
- [Maestro Docs](https://maestro.mobile.dev/)
- [IMAGE_WORD_MATCH_DESIGN_AUDIT.md](../../IMAGE_WORD_MATCH_DESIGN_AUDIT.md)
- [DRAG_DROP_FIXES.md](../../DRAG_DROP_FIXES.md)

---

## Контакты

При возникновении проблем с тестами:
1. Проверьте этот гайд
2. Посмотрите README в `admin-web/e2e/tests/dragdrop/`
3. Проверьте тест-кейсы в `qa/test-cases/IMAGE_WORD_MATCH_TEST_CASES.md`
