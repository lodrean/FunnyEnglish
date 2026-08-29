# Отчёт: bd FunnyEnglish-qbq.3 — INF: README под текущий продукт

## Что сделано

`README.md` полностью переписан под текущий продукт — Speaking-тренажёр (источник: §5 memory.md, пивот 2026-07-30; сверено с `docs/prd/SPEAKING-TRAINER-001.prd.md`).

Изменения по разделам:

- **Заголовок/описание** — «приложение с геймификацией» → тренажёр устной речи (видео → вопросы → голосовые ответы → оценка учителем).
- **Новый раздел «Как это работает»** — флоу ученика (Library → Topic → Video → Training/Practice) и флоу учителя (контент + Grading по рубрике).
- **«Возможности»** — удалены legacy-пункты (тесты, очки/звёзды, достижения, лидерборд, streak); добавлены guest-first, WebVTT, голосовые ответы, рубричное оценивание, Playful Coach.
- **«Скриншоты»** — раздел удалён: ссылался на несуществующий каталог `docs/screenshots/` (битые изображения).
- **«Структура проекта»** — добавлены `app/` (Android-обёртка) и уточнены роли модулей (context-path `/api`, разделы админки).
- **«Быстрый старт»** — Backend API URL исправлен на `http://localhost:8080/api`; Android-сборка — `:app:assembleDebug` (не `:composeApp`).
- **«Конфигурация»** — default `ADMIN_EMAIL` исправлен на `admin@sotospeak.com` (как в application.yml).
- **«API»** — legacy-эндпоинты (/categories, /tests, /leaderboard) заменены на speaking-эндпоинты (`/public/speaking/*`, `/speaking/submissions`, `/admin/speaking/*`); добавлено про базовый путь `/api`.
- **«Документация»** — добавлены ссылки на PRD, USER_GUIDE, TESTING.
- **«Тесты/Сборка»** — команды актуализированы по memory.md §3 (`:backend:test`, `:composeApp:desktopTest`, `npx vitest run`, maestro, newman с актуальным файлом коллекции `api-tests/sotospeak-api-collection.json`). Удалён раздел про AI QA Agent (устаревший инструмент вне текущего стека).
- **«Roadmap»** — переписан: отмечены выполненные этапы пивота; убраны legacy-пункты (достижения/лидерборд/multiplayer); OAuth помечен как отключённый до верификации токенов (решение 2026-08-29); добавлен iOS.

Спеки/PRD (`docs/`, `openspec/`) не изменялись — README не является спекой, ADR-007 не требуется.

## Изменённые файлы

- `README.md` (переписан целиком)

## Как проверить

1. `cat README.md` — визуальный осмотр: 0 упоминаний legacy-фич (тесты/очки/достижения/лидерборд/streak/qa-agent).
2. Проверка ссылок: `CREDENTIALS.md`, `CONTRIBUTING.md`, `LICENSE`, `docs/API.md`, `docs/ARCHITECTURE.md`, `docs/USER_GUIDE.md`, `docs/TESTING.md`, `docs/prd/SPEAKING-TRAINER-001.prd.md` — все существуют (проверено `ls`).
3. `grep -iE "leaderboard|достижен|qa-agent|categories" README.md` → пусто (кроме roadmap-пометки OAuth, это норма).
