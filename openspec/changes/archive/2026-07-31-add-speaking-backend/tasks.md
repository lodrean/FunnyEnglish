# Tasks: add-speaking-backend

> Источник: Part 1 §9. Каждая задача уже заведена в bd (указан id) — claim через `bd update <id> --claim`, закрытие `bd close <id>` после проверки. Критический путь: BE-1 → BE-3 → BE-6 → BE-8 → BE-9 → BE-11.

## 1. Миграции и модель данных

- [x] 1.1 BE-1 (bd `8tg.1.1`): Flyway `V17__create_speaking_content_tables.sql` — libraries, topics (deleted_at), videos, speaking_questions + индексы (Part 1 §3.1). Зависимости: —
- [x] 1.2 BE-2 (bd `8tg.1.2`): Flyway `V18__create_speaking_submissions_tables.sql` — practice_submissions, grades (generated column total) (§3.2). Зависимости: 1.1
- [x] 1.3 BE-3 (bd `8tg.1.3`): JPA-сущности `entity/speaking/*` (6 шт.) + репозитории `repository/speaking/*` с JPQL join-fetch (§4.1, §6.5). Зависимости: 1.1, 1.2
- [x] 1.4 BE-4 (bd `8tg.1.4`): `dto/SpeakingDtos.kt` — все request/response DTO + мапперы (§4.2). Зависимости: 1.3
- [x] 1.5 Проверка точки 1: `./gradlew :backend:compileKotlin` + bootRun против PostgreSQL — Flyway применил V17/V18, `ddl-auto: validate` зелёный. Зависимости: 1.1–1.4

## 2. Инфраструктура загрузки

- [x] 2.1 BE-5 (bd `8tg.1.5`): `StorageService` — whitelist video (mp4/webm/mov/m4v) + vtt, cross-check content-type (§6.1); `application.yml` multipart → 200MB; `docker/nginx.conf` → `client_max_body_size 200m`. Зависимости: — (параллельно с группой 1)

## 3. Public API и Admin CRUD

- [x] 3.1 BE-6 (bd `8tg.1.6`): `SpeakingContentService` (публичная выдача §6.4) + `SpeakingPublicController` — 3 GET `/public/speaking/**` (§5.2). Зависимости: 1.3, 1.4
- [x] 3.2 BE-7 (bd `8tg.1.7`): Admin CRUD контента в `SpeakingAdminController` (`/admin/speaking/**`): libraries/topics/video/questions, soft delete, upsert video с deleteFile, DELETE library → 400 при submissions (§5.4). Зависимости: 3.1

## 4. Practice и Grading

- [x] 4.1 BE-8 (bd `8tg.1.8`): `PracticeSubmissionService` + `SpeakingSubmissionController` — multipart `POST /speaking/submissions` (validate → upload → INSERT, §6.2) + `GET /speaking/submissions/my`. Зависимости: 2.1, 3.1
- [x] 4.2 BE-9 (bd `8tg.1.9`): Grading — inbox `GET /admin/speaking/submissions` (фильтры/пагинация, size ≤ 100) + POST/PUT grade + статус-машина NEW→REVIEWED (§5.5, §6.3). Зависимости: 4.1

## 5. Тесты

- [x] 5.1 BE-10 (bd `8tg.1.10`): Unit-тесты mockk — `PracticeSubmissionServiceTest` (8 сценариев §8.1), `SpeakingContentServiceTest` (3 сценария §8.2). Зависимости: 4.1, 4.2
- [x] 5.2 BE-11 (bd `8tg.1.11`): `SpeakingFlowIntegrationTest` — сценарии §8.3 включая контрактный jsonPath `$.isPublished`; проверка generated column на тестовой БД. Зависимости: 4.2
- [x] 5.3 Проверка точки 5: `./gradlew :backend:test` — новые + 34 существующих теста зелёные (exit code, не tail). Зависимости: 5.1, 5.2

## 6. Shared и api-коллекция

- [x] 6.1 BE-12 (bd `8tg.1.12`): shared — методы `SoToSpeakApi` (public content, multipart submit через `submitFormWithBinaryData`, my submissions) + модели (kotlinx.serialization, имена = JSON-контракту, пути с `/api`). Зависимости: 3.1, 4.1
- [x] 6.2 BE-13 (bd `8tg.1.13`): регрессия backend-тестов + новые эндпоинты в `api-tests/sotospeak-api-collection.json`. Зависимости: 5.2

## 7. Завершение

- [x] 7.1 Полная проверка: `./gradlew :backend:build`, `:shared` тесты; опционально Newman против docker-стека. Зависимости: 6.1, 6.2
- [x] 7.2 `openspec archive` change → канонические спеки в `openspec/specs/`. Зависимости: 7.1
- [x] 7.3 `memory.md`: решения (generated column, soft delete, isPublished-контракт, лимиты 200MB) + новые грабли. Зависимости: 7.1
- [x] 7.4 bd: закрыть `8tg.1.1`–`8tg.1.13` и `8tg.1`. Зависимости: 7.1
