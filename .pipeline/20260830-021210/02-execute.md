# 02-execute — bd FunnyEnglish-wy7.4: BE Testcontainers-Postgres + security-контракты

Дата: 2026-08-30. Исполнитель: Kimi Code CLI.

## Что сделано

1. **Testcontainers-Postgres для интеграционных тестов** (замена слепой зоны H2 + create-drop, грабли №31/81):
   - Зависимости `org.testcontainers:junit-jupiter` + `org.testcontainers:postgresql` добавлены в `gradle/libs.versions.toml` (БЕЗ version.ref — версия из Spring Boot BOM, как jackson-module-kotlin) и подключены в `backend/build.gradle.kts` (testImplementation).
   - Новый базовый класс `PostgresContainerTest`: `@Testcontainers(disabledWithoutDocker = true)` (без Docker тесты SKIPPED, не FAILED), `@ActiveProfiles("integration-test")` (Flyway V1–V26 + `ddl-auto: validate` — паритет со staging/prod), companion с `@Container @JvmStatic` PostgreSQLContainer(`postgres:16-alpine` — тот же образ, что docker-compose), `@DynamicPropertySource` на JDBC-параметры контейнера.
   - **Баг-фикс по ходу**: дефолтный JWT-секрет в `application-integration-test.yml` был 31 байт (`integration-test-jwt-secret-key`) — падение fail-fast `JwtService` (требуется ≥32 байт) при любом запуске профиля. Исправлено на 42-байтовый дефолт.

2. **Тесты AdminController/аналитика** — `AdminAnalyticsPostgresIT` (9 тестов, на живом Postgres):
   - все `/admin/analytics/*` (counters, daily-activity 7 дней + alias `/activity`, levels, popular-tests, recent-activity, guests) — упражняют native CAST AS DATE / Timestamp-проекции / JPQL nullable-параметры (грабли №21/81);
   - `/admin/tests` (путь JSONB-workaround TestService, грабля №3), `/admin/settings`.

3. **Security-контракты** — `SecurityContractPostgresIT` (8 тестов):
   - аноним на защищённом → 401 `UNAUTHORIZED`; истёкший access → 401 `TOKEN_EXPIRED`; истёкший токен НЕ ломает публичные эндпоинты (200);
   - аутентифицированный USER на `/admin/**` → 403; claim `role` в токене игнорируется — роль из БД (nj2.7): токен с claim ADMIN + роль USER в БД → 403;
   - refresh-токен как Bearer → 401; аноним на `/admin/**` → 401 (не 403);
   - **rate-limit e2e**: ёмкость из env `RATE_LIMIT_LOGIN_CAPACITY` (дефолт 5) попыток логина → 401 `INVALID_CREDENTIALS` + заголовки `X-RateLimit-*`, следующая → 429 + `Retry-After` + JSON-тело «Too Many Requests»/retryAfter.

4. **memory.md** — запись решения (Testcontainers-инфраструктура, правила для PG-тестов, фикс 31-байтового секрета).

## Изменённые/созданные файлы

- `gradle/libs.versions.toml` — +2 алиаса testcontainers (без версии, BOM).
- `backend/build.gradle.kts` — +2 testImplementation (testcontainers).
- `backend/src/main/resources/application-integration-test.yml` — фикс JWT-секрета (31→42 байта).
- `backend/src/test/kotlin/com/sotospeak/support/PostgresContainerTest.kt` — НОВЫЙ (базовый класс).
- `backend/src/test/kotlin/com/sotospeak/controller/AdminAnalyticsPostgresIT.kt` — НОВЫЙ (9 тестов).
- `backend/src/test/kotlin/com/sotospeak/controller/SecurityContractPostgresIT.kt` — НОВЫЙ (8 тестов).
- `memory.md` — запись решения.

Спеки/PRD (docs/, openspec/) не тронуты. Код продакшена (src/main/kotlin) не изменён — только тестовая инфраструктура и test-профиль.

## Как проверить

```bash
# С запущенным Docker Desktop (поднимется postgres:16-alpine контейнер):
.\gradlew.bat :backend:test --tests "*PostgresIT"
# Полный гейт (H2-тесты + PG-тесты; без Docker PG-тесты будут skipped):
.\gradlew.bat :backend:test
```

Ожидание: 17 новых тестов зелёные; существующие H2-тесты не затронуты (отдельный Spring-контекст, rate-limit bucket'ы не пересекаются).

## Замечания для драйвера

- Первый запуск PG-тестов тянет образ `postgres:16-alpine` (~80 МБ) — время старта больше обычного.
- Сборки/тесты сам не запускал (гейт — за драйвером, по ТЗ).
