# Tasks: add-client-logging

> bd-эпик: `So to Speak-i01`. Порядок: backend → shared/composeApp → admin-web → гейты.

## 1. Backend: приём и хранение (зависимостей нет)

- [x] 1.1 Миграция `V23__create_client_logs.sql` (IF NOT EXISTS, индексы created_at/level/anonymous_id)
- [x] 1.2 `entity/ClientLog.kt` + `repository/ClientLogRepository.kt` (JpaRepository + Page-запрос с фильтрами)
- [x] 1.3 `controller/ClientLogController.kt`: `POST /public/logs` — batch DTO `@Size(1..50)`, mapNotNull-валидация, обрезка 4k/16k, `{accepted}`
- [x] 1.4 `GET /admin/logs` (AdminController или AdminLogController): level/platform/from/to/q/page/size → Page JSON
- [x] 1.5 `application.yml`: logging.file.name + rollingpolicy (10MB/7/200MB); docker-compose dev+staging volume `./logs:/app/logs`
- [x] 1.6 `config/RequestLoggingFilter.kt`: `METHOD path → status (N ms)`, skip `/actuator/health`
- [x] 1.7 Тесты MockMvc (без /api-префикса — memory.md №65): 200 batch, отброс невалидных, 400 при 0/51+, admin GET 403 для не-админа, фильтры

## 2. Клиент: shared + composeApp (зависит от 1.3 — контракт)

- [x] 2.1 `shared/model/ClientLog.kt`: ClientLogDto/ClientLogsBatchRequest/ClientLogsBatchResponse (время — Instant.toString с Z, memory.md №52)
- [x] 2.2 `shared/util/ClientLogQueue.kt`: Settings-ключ `client_pending_logs`, cap 200, FIFO-вытеснение, fault-tolerant decode (паттерн GuestProgressRepository)
- [x] 2.3 `shared/util/Logger.kt`: WARN+ → enqueue в ClientLogQueue; composeApp `app/util/Logger.kt` → делегат на shared (убрать дубль)
- [x] 2.4 `SoToSpeakApi.sendLogs()` (паттерн submitGuestEvents)
- [x] 2.5 `AppConfig` + `appVersion` (android BuildConfig.VERSION_NAME, desktop/wasm/ios — разумные дефолты)
- [x] 2.6 composeApp `LogUploader` (паттерн GuestAnalytics; ошибки — println, НЕ Logger — анти-рекурсия) + DI + вызов на старте App.kt
- [x] 2.7 commonTest: очередь (cap/FIFO/битый JSON), uploader (clear только при success, фейк api)

## 3. admin-web (зависит от 1.4)

- [x] 3.1 `src/utils/logger.ts`: console + буфер (cap 100), flush 10с/10 записей, fetch напрямую (не axios), подключение в ErrorBoundary + axios response-interceptor
- [x] 3.2 `client.ts`: `getLogs(params)` → `GET /admin/logs`; типы LogEntry/LogsPage
- [x] 3.3 Страница `ClientLogs` (фильтры level/platform/даты/поиск, MUI-таблица, TanStack Query пагинация), роут `/logs` + пункт Sidebar
- [x] 3.4 vitest: logger-буфер, рендер страницы (с app ThemeProvider — memory.md №47)

## 4. Гейты

- [x] 4.1 backend-тесты зелёные (по exit code)
- [x] 4.2 `:composeApp:desktopTest` зелёные
- [x] 4.3 admin `npx vitest run` (НЕ watch — memory.md №69) + build
- [x] 4.4 (2026-08-05: curl batch → accepted:2, невалидный level отброшен accepted:0; GET /admin/logs все фильтры 200 на Postgres; UI /logs — строки видны, фильтр Level=ERROR → 1–1 of 1; скриншот .playwright-mcp/gate-4.4-client-logs-ui.png) Живой прогон: ошибка в приложении → строка в `client_logs` → видна на странице «Логи»
- [x] 4.5 (2026-08-05: validate ✅, archive выполнен, specs/client-logging создан; memory.md №81 + решение, API.md раздел Client Logs API) `openspec validate add-client-logging` + archive после реализации; обновить memory.md, docs/API.md
