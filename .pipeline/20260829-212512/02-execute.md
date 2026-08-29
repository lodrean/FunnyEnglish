# 02-execute — bd FunnyEnglish-nj2.1: SEC: миграция удаления demo-юзера из V1

## Что сделано

1. **Создана миграция `backend/src/main/resources/db/migration/V24__delete_demo_user.sql`**:
   - `DELETE FROM users WHERE email = 'demo@sotospeak.app'` — удаляет demo-юзера,
     вставленного миграцией V1, на всех существующих БД (dev/staging/prod) при ближайшем
     деплое. На чистой БД demo вставляется V1 и тут же удаляется V24 в рамках того же
     прогона `migrate` (приложение стартует после миграций — окно доступа отсутствует).
   - Зависимые строки (practice_submissions, messages, guest-progress и пр.) удаляются
     каскадом — все FK на `users` имеют `ON DELETE CASCADE` (проверено grep по всем миграциям).
   - Перед DELETE обнуляется `media_files.uploaded_by` — единственная некаскадная
     nullable-ссылка на `users` (V7, FK без ON DELETE), чтобы миграция не упала,
     если demo где-то оказался uploader'ом.
2. **`AdminUserInitializer.kt` не менялся** — создание demo-юзера уже выполняется только
   под флагом `app.demo-user.enabled` (`DEMO_USER_ENABLED`, дефолт false; строки 68–72),
   включая синхронизацию пароля/emailVerified. Требование «создание только в
   AdminUserInitializer под DEMO_USER_ENABLED» уже выполнено существующим кодом.

## Осознанное отклонение от буквальной формулировки задачи

Сам INSERT в `V1__initial_schema.sql:142-151` **НЕ удалён**: V1 уже применена на
существующих БД, а Flyway по умолчанию валидирует checksums применённых миграций
(`validate-on-migrate=true`, в `application.yml` не отключено) — любая правка V1
сломает старт backend на dev/staging/prod с «Migration checksum mismatch».
Безопасный и идемпотентный эквивалент — отдельная миграция V24 (стандартная практика
Flyway: applied-миграции не редактируются). Если владелец всё же хочет убрать INSERT
из V1 (косметика для чистых БД), потребуется решение владельца + `flyway repair`
на всех окружениях — это отдельная задача.

## Известный остаточный риск (зафиксирован, не блокер)

- `grades.reviewer_id` имеет `ON DELETE RESTRICT` (V18): если demo-юзер где-то
  оказался reviewer'ом оценки, DELETE упадёт. По логике невозможно (роль USER,
  grading требует ADMIN), при реальном падении — разбирать данные вручную.
- В dev (`DEMO_USER_ENABLED=true`) V24 удалит demo с его данными (submissions и пр.
  каскадом), после чего `AdminUserInitializer` пересоздаст demo с новым UUID.
  Для dev-окружения приемлемо.

## Изменённые/созданные файлы

- `backend/src/main/resources/db/migration/V24__delete_demo_user.sql` — **создан**.

## Как проверить

1. `.\gradlew.bat :backend:test` (гейт драйвера; Flyway в test-профиле отключён — H2,
   миграция на тесты не влияет, падений быть не должно).
2. Живой прогон против PostgreSQL (dev/staging): `docker compose up -d --build backend`
   → в логах Flyway `Migrating schema ... to version "24 - delete demo user"` →
   `Successfully applied 1 migration`; в БД `SELECT * FROM users WHERE email='demo@sotospeak.app'`
   → 0 строк (при `DEMO_USER_ENABLED=false`).
3. Dev-регрессия: с `DEMO_USER_ENABLED=true` после деплоя demo-логин
   `demo@sotospeak.app/demo123` продолжает работать (пересоздан initializer'ом).

Правок спек/PRD не требуется (ADR-007): поведенческий контракт «demo-юзер только в dev»
уже зафиксирован в memory.md (решение 2026-07-20).
