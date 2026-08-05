# Design: add-client-logging

## Context

Мотивация — см. proposal.md («Why»). Текущее состояние: два дублирующих `object Logger`
(println) в `shared/.../util/Logger.kt` и `composeApp/.../app/util/Logger.kt`; Napier только
на android (logcat) и desktop (файл+консоль); backend пишет в stdout без ротации; endpoint'а
приёма логов нет. Готовые паттерны: guest-events (backend batch-endpoint V16) и
`GuestProgressRepository`+`GuestAnalytics` (клиентская очередь + best-effort flush).

## Goals / Non-Goals

Goals/Non-goals — см. proposal.md. Дополнительно: минимальная интрузия в существующий код
(сигнатуры `Logger.d/i/w/e` не меняются), переиспользование проверенных паттернов вместо новых
абстракций.

## Key Decisions

### D1. Не возвращаем Napier в commonMain — расширяем свой Logger

Napier недоступен на WASM (memory.md №4), поэтому единая точка — собственный `shared Logger`:
в `log()` при `level >= WARN` запись складывается в `ClientLogQueue`. Дубликат в composeApp
заменяется typealias на shared-логгер. Консольные sink'и (println, Napier logcat,
FileAndConsoleLogger) остаются как есть — remote-sink добавляется рядом, а не вместо.

**Лимиты очереди**: 200 записей И 7 КБ сериализованного JSON (срабатывает первый) —
java.util.prefs (desktop Settings) бросает IllegalArgumentException при значении > 8192
символов (поймано тестами). Поля обрезаются при enqueue (message 1000 / stackTrace 2000).
Отправка батчами ≤ 50 с прогрессивным removeFirst — частичная неудача сохраняет остаток.

**Рекурсия**: `SoToSpeakApi.safeCall` пишет сетевые ошибки в Logger (ERROR) → они попадут
в очередь, включая ошибки самого `sendLogs`. `LogUploader.flush()` ошибки отправки проглатывает
молча (println, НЕ Logger) — цикл разорван. Повторная отправка тех же записей безопасна
(сервер хранит как новые строки — дубли приемлемы, дедупликацию не делаем).

### D2. Wire-формат времени — ISO Instant с Z

`Clock.System.now().toString()` (memory.md №52: LocalDateTime без зоны → 500 на бэке).
На сервере `clientTimestamp: Instant`, `createdAt: Instant = Instant.now()`.

### D3. Без сервис-слоя на backend

Как `GuestEventController`: контроллер → repository напрямую. Валидация — `mapNotNull`
(невалидные uuid/level/timestamp отбрасываются), обрезка message (4k) / stackTrace (16k).
`@Size(1..50)` на batch. SecurityConfig не меняется (`/public/**` уже permitAll).
Rate limit — дефолтный bucket 100/мин/IP достаточен (WARN+ERROR — редкий трафик).

### D4. Файловый лог backend — средствами Spring Boot, без logback-spring.xml

`application.yml`: `logging.file.name: ${LOG_FILE:logs/backend.log}` +
`logging.logback.rollingpolicy.{max-file-size: 10MB, max-history: 7, total-size-cap: 200MB}`.
В test-профилях файл отключать не нужно (пишется рядом с cwd — безвредно), но volume мапим
только в dev/staging compose.

### D5. Access-log — свой OncePerRequestFilter

`CommonsRequestLoggingFilter` пишет ДО обработки и без статуса/длительности — не подходит.
Свой `RequestLoggingFilter` (order после RateLimitingFilter): `METHOD path → status (N ms)`,
skip `/actuator/health` (шум healthcheck'ов docker). Тела не логируем.

### D6. Admin-чтение — Spring Data pagination

`ClientLogRepository` + `Page<ClientLog>` с derived/`@Query` фильтрами (level?, platform?,
from/to?, q LIKE). Ответ — обёртка `{ content, totalElements, totalPages, number, size }`
(стандартный Spring Page JSON уже используется в admin submissions).

### D7. admin-web logger — отдельный fetch, не axios-instance

Чтобы не ловить рекурсию с response-interceptor'ом (который сам пишет в logger), отправка —
голым `fetch('/api/public/logs')` с Authorization из localStorage вручную (есть — ок, нет —
permitAll примет). Буфер in-memory (cap 100), flush по таймеру 10с или по достижении 10 записей
+ `beforeunload` (navigator.sendBeacon не обязателен — best-effort).

### D8. platform/appVersion на клиенте

`platform` — expect/actual `platformName` (уже есть в shared Platform.*). `appVersion` —
новое поле `AppConfig.appVersion` (android: `BuildConfig.VERSION_NAME`; desktop:
`packageVersion`/«dev»; wasm: «web»; ios: «—»). `anonymousId` — `GuestProgressRepository
.getAnonymousId()` (guestId), без PII.

## Risks / Trade-offs

- **Рост таблицы client_logs** — приёмлемо для MVP; retention — пост-MVP (non-goal).
- **Публичный эндпоинт = приём мусора** — смягчено: batch ≤50, лимиты длины, rate limit,
  отброс невалидных. Аутентификация не требуется осознанно (логи нужны и от гостей/сломанного логина).
- **Дубли записей при повторных отправках** — приемлемо, accepted-ответ не используется клиентом для сверки.

## Migration

Только `V23__create_client_logs.sql` (`CREATE TABLE IF NOT EXISTS` + индексы — memory.md №62).
Откат: дроп таблицы, код-фича независима от существующих флоу.
