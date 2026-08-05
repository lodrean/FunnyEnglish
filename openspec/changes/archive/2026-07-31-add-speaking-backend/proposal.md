# Proposal: add-speaking-backend

## Why

So to Speak пивотируется в speaking-тренажёр (эпик bd `So to Speak-8tg`, PRD `docs/prd/SPEAKING-TRAINER-001.prd.md`). Новое ядро продукта — Library → Topic → видео (WebVTT) → вопросы → Training/Practice — требует backend-фундамента: контентная модель, публичная выдача для гостей, приём голосовых practice-записей и grading учителем. Без этого backend-слоя невозможны Фаза 2 (клиент Android) и Фаза 3 (admin-web).

Источник истины по требованиям — утверждённая спека `docs/SPEAKING_TRAINER_SPEC_PART1.md` v1.0 (дельта ниже не дублирует её, а фиксирует контракты в каноническом виде OpenSpec).

## What Changes

- **Новая контентная модель** (Flyway V17): `libraries`, `topics` (soft delete через `deleted_at`), `videos` (1:1 с топиком, `video_url`/`subtitle_url`), `speaking_questions`.
- **Новая модель practice/grading** (Flyway V18): `practice_submissions` (статус-машина `NEW → REVIEWED`), `grades` (рубрика grammar/vocabulary/pronunciation/fluency 1–10, `total` — generated column в БД).
- **Public API** (`/api/public/speaking/**`, гость): список тем, топиков темы, детали топика (видео + вопросы). Видны только `is_published` и не удалённые; пустые темы скрыты.
- **User API** (`/api/speaking/**`, авторизованный): multipart-загрузка practice-записи (аудио ≤ 5 МБ, duration 1..60с) в MinIO, список своих отправок с оценками.
- **Admin API** (`/api/admin/speaking/**`, ROLE_ADMIN): CRUD libraries/topics/video/questions (soft delete топиков), grading inbox с фильтрами/пагинацией, POST/PUT grade.
- **StorageService**: расширение whitelist — видео (mp4/webm/mov/m4v) и субтитры (vtt); лимиты multipart подняты до 200MB (Spring) / 200m (nginx).
- **shared (KMP)**: методы `SoToSpeakApi` + модели для public content, multipart submit, my submissions.
- Breaking changes: нет (существующие API не меняются; legacy-фичи остаются до Фазы 5).

## Capabilities

### New Capabilities
- `speaking-content`: контентная модель и публичная выдача (libraries → topics → videos/questions), правила видимости (published, soft delete, пустые темы скрыты), admin CRUD контента.
- `speaking-practice`: приём голосовых practice-записей ученика (multipart upload в S3/MinIO, валидация), список «мои отправки».
- `speaking-grading`: grading inbox учителя (фильтры, пагинация), оценка по рубрике с авто-усреднённым `total`, статус-машина `NEW → REVIEWED`, редактирование оценки.

### Modified Capabilities
(пусто — существующие capability-спеки в `openspec/specs/` отсутствуют, поведение legacy-API не меняется)

## Non-goals

- Клиент Android (экраны, VoiceRecorder, VideoPlayer, WebVTT-парсинг) — Фаза 2, спека Part 2.
- Admin-web UI (Speaking Content CRUD, Grading inbox) — Фаза 3, спека Part 3.
- Дизайн-система (токены в `:design` и MUI) — Фаза 4.
- Пивот навигации и скрытие legacy-экранов — Фаза 5.
- Rate limiting на `POST /speaking/submissions` и HTTP-кэш public GET — зафиксированы как риски, в MVP не вводятся.
- Presigned URL для аудио submissions — принятый риск MVP (публично читаемые неугадуемые URL).
- Периодическая зачистка файлов-сирот в MinIO — out of scope.

## Impact

- **bd**: эпик `So to Speak-8tg`, задачи `So to Speak-8tg.1` (BE-1…BE-13 = `8tg.1.1`–`8tg.1.13`).
- **Backend** (`backend/`): новые пакеты `entity/speaking`, `repository/speaking`, `service/speaking`, `controller/speaking`, `dto/SpeakingDtos.kt`; миграции V17/V18; изменения в `StorageService.kt`, `application.yml`.
- **Infra**: `docker/nginx.conf` (`client_max_body_size 200m`); prod Caddy — без лимита тела (проверить при деплое).
- **Shared** (`shared/`): новые методы и модели API.
- **Тесты**: unit (mockk) + интеграционный `SpeakingFlowIntegrationTest`; обновление `api-tests/sotospeak-api-collection.json`.
- **Зависимые фазы**: разблокирует Фазу 2 (Android-клиент) и Фазу 3 (admin-web).
