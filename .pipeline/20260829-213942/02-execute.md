# 02-execute — bd FunnyEnglish-nj2.4 (SEC: rate-limit обходится через X-Forwarded-For)

## Что сделано

`RateLimitingFilter.extractClientIp` доверял первому значению `X-Forwarded-For` без проверки —
при прямом доступе клиент мог подменять IP в каждом запросе и обходить rate-limit (§2.1 Б4 ревью).

Исправление:
- Введён **whitelist доверенных прокси** — env `RATE_LIMIT_TRUSTED_PROXIES` (через запятую:
  точный IP или IPv4 CIDR, напр. `10.0.0.1,172.16.0.0/12`). Дефолт пустой → прокси-заголовки
  игнорируются, лимит считается по `remoteAddr` (спуфинг невозможен).
- `X-Forwarded-For` / `X-Real-IP` учитываются **только если `remoteAddr` ∈ whitelist**.
- Из XFF берётся **первый недоверенный IP справа налево** (доверенные прокси инфраструктуры
  отбрасываются; левые элементы цепочки контролируются клиентом и не доверяются).
- Извлечённый IP валидируется (строгий IPv4 / наличие `:` для IPv6); мусор → fallback на `remoteAddr`.
- В compose-стеках выставлено `RATE_LIMIT_TRUSTED_PROXIES=172.16.0.0/12` (docker-сети;
  admin nginx / Caddy → backend), иначе все клиенты схлопнулись бы в один бакет по IP прокси.
  В prod порт backend наружу не проброшен (вход только через Caddy) — доверие docker-диапазону безопасно.
- Конструктор фильтра получил параметр `trustedProxiesConfig` с дефолтом из env — для тестов
  (kotlin-reflect в classpath, дефолтные параметры со Spring работают).
- Решение зафиксировано в `memory.md` (раздел «Решения и договорённости», 2026-08-29).

## Изменённые файлы

- `backend/src/main/kotlin/com/sotospeak/security/RateLimitingFilter.kt` — whitelist логика,
  право-налево разбор XFF, валидация IP.
- `backend/src/test/kotlin/com/sotospeak/security/RateLimitingFilterTest.kt` — тесты переписаны/добавлены:
  спуфинг XFF и X-Real-IP от недоверенного пира НЕ обходит лимит (429 по remoteAddr);
  от доверенного прокси лимит общий по правому недоверенному IP, поддельные левые IP не помогают;
  setup явно передаёт пустой whitelist (независимость от env).
- `docker-compose.yml`, `docker-compose.staging.yml`, `docker-compose.prod.yml` —
  `RATE_LIMIT_TRUSTED_PROXIES` с дефолтом `172.16.0.0/12` (переопределяется через env).
- `memory.md` — запись о решении.

## Как проверить

- `.\gradlew.bat :backend:test --tests "com.sotospeak.security.RateLimitingFilterTest"` (гейт драйвера).
- Ручной smoke против dev-стека: 6+ раз `curl -X POST http://localhost:8080/api/auth/login`
  с разными `X-Forwarded-For` при прямом доступе → 429 (раньше — всегда 200/400).
- Через admin-nginx (docker): XFF проставляется nginx, remoteAddr ∈ 172.16.0.0/12 →
  лимит считается per-client IP (в логе `Rate limit exceeded for IP: <client>`).
- `docker compose config` — валиден (проверено для dev; staging/prod падают только на
  отсутствующих обязательных секретах — предсуществующее поведение, новые строки интерполируются).

## Замечания

- Спеки/PRD не затронуты — правка спеки не требуется (ADR-007 не применялся).
- Принятый риск: в dev/staging порт backend опубликован, доверие `172.16.0.0/12` означает,
  что прямой запрос на опубликованный порт с поддельным XFF будет принят (не-prod окружения).
  В prod поверхность закрыта (backend только expose, вход через Caddy).
- bd-задача оставлена `in_progress` — закрытие/гейты за драйвером. Сборки/тесты не запускались.
