# Design: add-speaking-backend

## Context

См. proposal.md → Why. Текущее состояние: backend Spring Boot 3.4.1 (Kotlin, Java 21), PostgreSQL 16 + Flyway (последняя миграция V16), JPA с `ddl-auto: validate`, JWT (principal `UserPrincipal(userId, role)`), `server.servlet.context-path: /api`, MinIO через `StorageService` (whitelist image/audio), публичные URL через `app.s3.public-url` (BUG-004 закрыт). Blueprint-фича — Audio Tests (`entity/audio/*`, `AudioTestService`, `AudioTestController`, `AudioTestIntegrationTest`); все новые компоненты повторяют её паттерны. Детальные DDL/DTO/API/сценарии — `docs/SPEAKING_TRAINER_SPEC_PART1.md` v1.0 (источник истины).

## Goals / Non-Goals

**Goals:**
- Контентная и practice/grading модель + REST API строго по Part 1, в стиле audio-tests.
- Ноль изменений в существующих API, SecurityConfig и legacy-коде (кроме `StorageService` whitelist и лимитов).
- Покрытие: unit (mockk) + интеграционный тест полного флоу, регрессия 34 существующих backend-тестов.

**Non-Goals:** см. proposal.md → Non-goals (клиент, admin-web UI, дизайн-токены, rate-limit, presigned URL).

## Decisions

1. **Blueprint = audio-tests.** Сущности — `class` с `var`, `@CreationTimestamp/@UpdateTimestamp`, LAZY-связи, кастомные equals/hashCode по id; репозитории — JPQL `JOIN FETCH` (не native query — грабля №21: projection + Timestamp → 500); контроллеры — `@PreAuthorize("hasRole('ADMIN')")` на admin-классе (defense-in-depth к SecurityConfig-матчеру `/admin/**`). Альтернатива (новый стиль, Specification API по умолчанию) отклонена: консистентность с кодовой базой важнее; Specification — только fallback, если JPQL с nullable timestamp-фильтрами будет капризничать.
2. **Маппинг контроллеров БЕЗ `/api`-префикса** (`/public/speaking`, `/speaking`, `/admin/speaking`) — context-path добавляет `/api` сам. Квирк `AudioTestController` (`/api/audio-tests` → фактически `/api/api/...`) НЕ копировать. Альтернатива (явный `/api` в маппинге) создаёт двойной префикс.
3. **`grades.total` — generated column в PostgreSQL** (`GENERATED ALWAYS AS ((g+v+p+f)/4.0) STORED`), в JPA `insertable=false, updatable=false`. Единый источник правды на уровне БД, невозможно рассинхронизировать. Альтернатива (расчёт в сервисе/маппере) — fallback, если тестовая БД (H2) не переварит DDL; решение фиксируется в memory.md.
4. **Soft delete топиков (`deleted_at`) + `ON DELETE RESTRICT` на submissions.topic_id.** Записи учеников не теряются никогда; физическое удаление library с submissions ловим через `DataIntegrityViolationException` → 400 с подсказкой. Альтернатива (каскадное удаление записей) отклонена PRD.
5. **Upload-порядок: validate → MinIO → INSERT.** Валидация (duration 1..60, ≤ 5 МБ) до загрузки; при падении INSERT файл-сирота допустим (зачистка out of scope). Ключ: `speaking/submissions/u_<userId>/<uuid>.m4a` — неугадуемый, presigned URL не нужен (принятый риск).
6. **Замена видео — upsert с best-effort удалением старых файлов** (`StorageService.deleteFile` в try/catch, ошибка удаления не откатывает транзакцию).
7. **Лимиты 200MB/200m** (Spring multipart + nginx) — под видео топиков; practice-аудио дополнительно ограничено 5 МБ в коде сервиса. Caddy в prod тело не лимитит — проверка при деплое (Фаза 5).
8. **JSON-контракт с `is`-префиксом** (`isPublished`): jackson-module-kotlin уже подключён; контракт закрепляется jsonPath-тестом `$.isPublished` (грабля №18). Shared-модели (kotlinx.serialization) именуются 1:1.
9. **Публичные URL на чтении** прогоняются через `MediaUrlService.normalize()` — страховка от legacy-записей с внутренним endpoint.

## Risks / Trade-offs

- [H2/Testcontainers не поддерживает generated column `total`] → проверить первым интеграционным тестом; fallback — расчёт total в маппере + отказ от generated column в test profile (фиксируется в memory.md и спеке).
- [JPQL с nullable timestamp-параметрами (dateFrom/dateTo) капризничает в Hibernate 6] → `CAST(:dateFrom AS timestamp)` в запросе; fallback — `JpaSpecificationExecutor`.
- [Большие видео через nginx → 413] → `client_max_body_size 200m` + рекомендация сжатия ≤ 100 МБ/720p (документируется в Part 3).
- [Расхождение контрактов с Part 2/Part 3] → сверка проведена 2026-07-30 (`docs/plan/SPEAKING-TRAINER-001.md` §«Сверка контрактов спек»); любое отклонение — сначала bump спеки, потом код (SDD).
- [`POST /speaking/submissions` без rate-limit — тяжёлый endpoint] → принятый риск MVP; при росте — bucket-лимит в существующий `RateLimitingFilter`.

## Migration Plan

1. Flyway V17/V18 применяются автоматически при старте backend; `ddl-auto: validate` гарантирует соответствие сущностей.
2. Откат: миграции только добавляют таблицы — откат = `DROP TABLE` вручную (данных до релиза нет); код-изменения обратимо совместимы.
3. nginx/spring лимиты — конфигурация, применяется рестартом; откат тривиален.
