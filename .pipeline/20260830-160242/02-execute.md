# 02-execute — bd FunnyEnglish-wy7.8: BE request-id/MDC-трейсинг

## Что сделано

1. **`RequestIdFilter`** (новый, `com.sotospeak.config`) — `OncePerRequestFilter` с `@Order(HIGHEST_PRECEDENCE)` (до RateLimitingFilter +1 и RequestLoggingFilter +2):
   - принимает входящий `X-Request-Id` ТОЛЬКО если валиден (`^[A-Za-z0-9._-]{1,64}$` — защита от log injection через CRLF/произвольные байты), иначе генерирует UUID;
   - кладёт id в SLF4J MDC (ключ `requestId`) на время обработки запроса, снимает в `finally` (в т.ч. при исключении — утечки в thread-pool нет);
   - возвращает id клиенту в ответном заголовке `X-Request-Id` (можно цитировать в баг-репортах/суппорте).
2. **Паттерн логов** в `application.yml`: console и file — добавлен `[%X{requestId:-}]`. Теперь все строки лога за время запроса (включая access-log `RequestLoggingFilter` и WARN rate-limit'а) несут request-id. Тестовые профили (test/integration-test) паттерн не переопределяют — наследуют новый.
3. **Unit-тест** `RequestIdFilterTest` (стиль как `RateLimitingFilterTest`, Mockito): генерация при отсутствии заголовка, эхо валидного, замена CRLF/слишком длинного (log injection), очистка MDC после запроса и при исключении в цепочке.

## Изменённые/созданные файлы

- `backend/src/main/kotlin/com/sotospeak/config/RequestIdFilter.kt` (новый)
- `backend/src/main/resources/application.yml` (logging.pattern.console + добавлен logging.pattern.file)
- `backend/src/test/kotlin/com/sotospeak/config/RequestIdFilterTest.kt` (новый)

Спеки/PRD не трогались (инфраструктурное наблюдаемость-изменение, контракты API не меняются; новый ответный заголовок аддитивен).

## Как проверить

- Гейт драйвера: `.\gradlew.bat :backend:test` (включает `RequestIdFilterTest`).
- Живьём: `curl -i http://localhost:8080/api/actuator/health` → заголовок `X-Request-Id: <uuid>`; `curl -i -H "X-Request-Id: test-42" ...` → эхо `test-42`; в `logs/backend.log` строки запроса содержат `[test-42]`.
