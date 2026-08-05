# Proposal: add-client-logging

> bd-задача: `So to Speak-i01`

## Why

Сейчас диагностика проблем пользователей почти слепая: клиентские логи живут только в консоли/logcat
(Android), файле `~/.sotospeak/logs/` (desktop) или браузерной консоли (WASM), а до разработчика
не доходят. Backend пишет только в stdout docker-контейнера (без ротации), access-log отсутствует.
Для MVP-тестирования на реальных устройствах (QA по Wi-Fi, bd `So to Speak-ggl`) нужен канал
доставки логов WARN/ERROR с устройств на backend и UI просмотра в админке.

## What Changes

- **Backend**: новый публичный эндпоинт `POST /api/public/logs` (batch ≤ 50 записей, по паттерну
  guest-events) → таблица `client_logs` (миграция V23). Админ-эндпоинт `GET /api/admin/logs`
  с фильтрами (level/platform/from/to/q) и пагинацией. Rolling file appender
  (`logging.file.name` + rollingpolicy, без logback-spring.xml) и `RequestLoggingFilter`
  (метод/путь/статус/длительность, без тел запросов).
- **Клиент (shared/composeApp)**: единый логгер с remote-sink — записи уровня WARN+ складываются
  в локальную очередь в Settings (cap 200, FIFO), `LogUploader` best-effort отправляет батчами
  (паттерн `GuestAnalytics`). Дедупликация двух `object Logger` (composeApp → делегат shared).
- **admin-web**: `utils/logger.ts` (console + буфер WARN/ERROR → `POST /api/public/logs`,
  platform `admin-web`), подключение в ErrorBoundary и axios-interceptor. Страница «Логи»
  (`/logs`) с фильтрами и пагинацией поверх `GET /admin/logs`.

## Capabilities

### New Capabilities
- `client-logging`: приём, хранение и просмотр клиентских логов (backend API + хранилище),
  клиентская очередь и отправка WARN+ERROR, UI просмотра в админке.

### Modified Capabilities

(пусто — требования speaking-* спек не меняются)

## Impact

- **API**: `POST /api/public/logs` (permitAll, дефолтный rate-limit 100/мин/IP), `GET /api/admin/logs` (ROLE_ADMIN).
- **БД**: миграция `V23__create_client_logs.sql` (IF NOT EXISTS — грабля №62 memory.md).
- **Код**: backend (`controller/ClientLogController`, `entity/ClientLog`, `repository/ClientLogRepository`,
  `config/RequestLoggingFilter`, application.yml), shared (`util/Logger`, `model/ClientLog`,
  `api/SoToSpeakApi.sendLogs`, очередь), composeApp (`LogUploader`, DI, AppConfig + appVersion),
  admin-web (`utils/logger.ts`, страница ClientLogs, client.ts).
- **Приватность**: не отправляем тела запросов/токены; anonymousId = guestId (обезличенный),
  уровень отправки — только WARN+ERROR.
- **Операции**: лог-файл `logs/backend.log` (10MB × 7 дней), volume в docker-compose (dev/staging).

## Non-goals

- НЕ возвращаем Napier в commonMain (WASM-грабля №4 memory.md).
- НЕ отправляем DEBUG/INFO с клиента на backend (шум/трафик).
- НЕ делаем real-time стриминг логов, алерты, retention-политику БД (пост-MVP).
- НЕ логируем тела запросов в RequestLoggingFilter (пароли/токены).
- iOS-логирование (нет iOS-таргета приложения).
