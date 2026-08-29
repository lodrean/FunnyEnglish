# 02-execute — FunnyEnglish-nj2.6: SEC: UNIQUE (user_id, topic_id) на practice_submissions

## Что сделано

1. **Миграция V25** — `ALTER TABLE practice_submissions ADD CONSTRAINT uq_practice_submissions_user_topic UNIQUE (user_id, topic_id)`.
   Идемпотентность через `DO $$ ... pg_constraint ... $$` (PostgreSQL не поддерживает `ADD CONSTRAINT IF NOT EXISTS`, дисциплина Flyway из memory №62 соблюдена).
   В комментарии миграции — SQL для ручной зачистки дублей, если бы они вдруг существовали (409-гейт работает с 2026-08-03, дублей быть не должно).
2. **Entity `PracticeSubmission`** — добавлен `UniqueConstraint` в `@Table`, чтобы H2 (test profile, `ddl-auto: create-drop`) создавал тот же constraint и схема не дрейфовала от Flyway.
3. **Fallback race → 409** в `PracticeSubmissionService.createSubmission`: `save` заменён на `saveAndFlush` внутри `try/catch (DataIntegrityViolationException)` → бросается тот же `DuplicateSubmissionException`, который `GlobalExceptionHandler` уже маппит в 409 `DUPLICATE_SUBMISSION`. Без flush исключение всплыло бы на коммите транзакции вне try.
4. **Тесты** (`PracticeSubmissionServiceTest`):
   - success-тест обновлён на `saveAndFlush`;
   - новый тест 1b: `saveAndFlush` бросает `DataIntegrityViolationException` → `DuplicateSubmissionException`.

Изменение контракта API отсутствует (тот же 409 `DUPLICATE_SUBMISSION`), правок спек не требуется — это устранение race уже задокументированного гейта (memory №79, Part 2 §2.6).

## Изменённые/созданные файлы

- `backend/src/main/resources/db/migration/V25__unique_practice_submission_user_topic.sql` — **создан**
- `backend/src/main/kotlin/com/sotospeak/entity/speaking/PracticeSubmission.kt` — uniqueConstraints в `@Table`
- `backend/src/main/kotlin/com/sotospeak/service/speaking/PracticeSubmissionService.kt` — import `DuplicateSubmissionException`/`DataIntegrityViolationException`, saveAndFlush + fallback
- `backend/src/test/kotlin/com/sotospeak/service/speaking/PracticeSubmissionServiceTest.kt` — обновлён stub success-теста + новый тест fallback

## Как проверить

- `.\gradlew.bat :backend:test` (H2 test profile; гейт прогоняет драйвер) — должны пройти обновлённый success-тест, новый тест 1b и интеграционный `user cannot submit practice twice for same topic` (409).
- На живом Postgres: `docker compose up -d backend` → Flyway применит V25; `SELECT conname FROM pg_constraint WHERE conname = 'uq_practice_submissions_user_topic';` — 1 строка.
- Race-проверка (опционально, вручную): два параллельных `POST /api/speaking/submissions` одним пользователем по одному topicId → один 201, второй 409 `DUPLICATE_SUBMISSION`.

## Замечания

- Запуск сборок/тестов не выполнялся (гейты прогоняет драйвер).
- Git-коммиты не делались.
