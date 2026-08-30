# 02-execute — bd FunnyEnglish-h3l.3 «PR: дашборд метрик PRD»

## Что сделано

Реализованы метрики PRD (Speaking Trainer §Metrics) на реальных данных:

**Backend** — агрегирующий эндпоинт `GET /admin/analytics/prd-metrics` (ROLE_ADMIN, как весь `/admin/**`):
- `practicePerStudentPerWeek` — отправки за 7д / уникальные отправители за 7д (+ сырые счётчики);
- `reviewedWithin48hShare` — доля оценённых отправок, где grade.createdAt − submission.createdAt ≤ 48ч
  (JPQL-проекция `ReviewedTimestamps` + сравнение `Duration` в коде — переносимо H2/Postgres, без native-запросов);
- `guestConversionRate` — гости с `converted_user_id` / все гости (те же запросы, что `/admin/analytics/guests`).

**Admin-web** — секция «PRD Metrics (Speaking Trainer)» на странице Analytics: 3 карточки
(Practice/Student/Week, REVIEWED within 48h, Guest → Registration) с сырыми счётчиками в подписях, `data-testid="prd-metrics-section"`.

**Тест** — кейс `prd metrics returns 200 with real aggregates` в `AdminAnalyticsPostgresIT`
(Testcontainers-Postgres: сид submission+grade → точные значения метрик).

## Изменённые файлы

- `backend/src/main/kotlin/com/sotospeak/dto/AdminDto.kt` — DTO `PrdMetricsResponse`
- `backend/src/main/kotlin/com/sotospeak/repository/speaking/PracticeSubmissionRepository.kt` — проекция `ReviewedTimestamps` + 3 запроса (`countCreatedSince`, `countDistinctSubmittersSince`, `findReviewedTimestamps`)
- `backend/src/main/kotlin/com/sotospeak/service/AdminService.kt` — `getPrdMetrics()` + инжекция `PracticeSubmissionRepository`
- `backend/src/main/kotlin/com/sotospeak/controller/AdminController.kt` — эндпоинт `/analytics/prd-metrics`
- `backend/src/test/kotlin/com/sotospeak/controller/AdminAnalyticsPostgresIT.kt` — новый тест-кейс
- `admin-web/src/api/client.ts` — интерфейс `PrdMetrics` + `getPrdMetrics()`
- `admin-web/src/screens/Analytics.tsx` — секция «PRD Metrics» (query + 3 MetricCard)
- `memory.md` — запись в «Решения и договорённости»

## Как проверить

- Backend: `./gradlew :backend:test --tests "*AdminAnalyticsPostgresIT*"` (нужен Docker для Testcontainers);
  ручная проверка: `curl -H "Authorization: Bearer <admin>" http://localhost:8080/api/admin/analytics/prd-metrics`.
- Admin-web: `cd admin-web && npm run typecheck` (прогнан в сессии: exit 0) и `npx vitest run`;
  визуально — страница Analytics, секция «PRD Metrics (Speaking Trainer)» над блоком Guest Users.
- Gradle-гейты драйвера: `:composeApp:desktopTest`, `compileDebugKotlinAndroid`, `compileKotlinWasmJs` — клиент composeApp не затронут.

## Замечания

- Спеки/PRD не правились: метрики уже требуются PRD §Metrics; правки спеки не требуется.
- Остальные метрики PRD (Training usage, crash-free) — вне скоупа тикета (нет источников данных: Training записи локальные, crash-аналитика не собирается).
