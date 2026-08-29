# 02-execute — bd FunnyEnglish-nj2.7: SEC полноценные refresh-токены

## Что сделано

Заменена схема «refresh = тот же access-токен в 7-дневном окне» на полноценные refresh-токены:

1. **Таблица `refresh_tokens`** (Flyway V26, `IF NOT EXISTS`): id, user_id (FK, cascade), jti (unique),
   token_hash (SHA-256, unique — в БД только хэш), expires_at, rotated_at, revoked_at, created_at + 2 индекса.
2. **Refresh = отдельный JWT** (`JwtService.generateRefreshToken`): claim `type=refresh`, уникальный JTI,
   TTL `app.jwt.refresh-expiration` (env `JWT_REFRESH_EXPIRATION`, дефолт 7 дней).
   Старое свойство `refresh-window`/`JWT_REFRESH_WINDOW` удалено (в docker/env не использовалось).
3. **`RefreshTokenService`**: выдача при login/register/oauth; **ротация** при каждом `/auth/refresh`
   (токен одноразовый, старый помечается `rotated_at`); **reuse-detection** — повторное предъявление
   ротированного/отозванного токена отзывает ВСЕ refresh-токены пользователя + 401;
   **отзыв** через новый `POST /auth/logout {refreshToken}` (идемпотентный, всегда 200).
4. **Сверка роли с БД**: `JwtAuthenticationFilter` берёт роль из БД (claim `role` токена игнорируется),
   кэш userId→role TTL 60с; удалённый пользователь → аноним. Refresh-токен как Bearer access отсекается
   (claim `type=refresh` → 401 на защищённых путях).
5. **HTTP-семантика (401 вместо 403/400)**: аноним на защищённом пути → 401 `UNAUTHORIZED`;
   неверные креды login → 401 `INVALID_CREDENTIALS`; невалидный/истёкший/отозванный refresh → 401
   `INVALID_REFRESH_TOKEN`. Аутентифицированному без роли — по-прежнему 403; `EMAIL_NOT_VERIFIED` — 403.
6. **DTO**: `AuthResponse` и `RegisterResponse` получили поле `refreshToken`.

## Изменённые/созданные файлы

**Созданы:**
- `backend/src/main/resources/db/migration/V26__create_refresh_tokens.sql`
- `backend/src/main/kotlin/com/sotospeak/entity/RefreshToken.kt`
- `backend/src/main/kotlin/com/sotospeak/repository/RefreshTokenRepository.kt`
- `backend/src/main/kotlin/com/sotospeak/service/RefreshTokenService.kt`
- `backend/src/main/kotlin/com/sotospeak/exception/AuthExceptions.kt`

**Изменены (main):**
- `backend/src/main/kotlin/com/sotospeak/security/JwtService.kt` — генерация/парсинг refresh-JWT, `extractType`
- `backend/src/main/kotlin/com/sotospeak/security/JwtAuthenticationFilter.kt` — роль из БД (кэш 60с), отсев type=refresh
- `backend/src/main/kotlin/com/sotospeak/service/AuthService.kt` — выдача refresh, rotate в refreshToken(), logout(), InvalidCredentialsException
- `backend/src/main/kotlin/com/sotospeak/controller/AuthController.kt` — `POST /auth/logout`
- `backend/src/main/kotlin/com/sotospeak/controller/GlobalExceptionHandler.kt` — 401-хендлеры
- `backend/src/main/kotlin/com/sotospeak/config/SecurityConfig.kt` — аноним → 401 UNAUTHORIZED
- `backend/src/main/kotlin/com/sotospeak/dto/AuthDto.kt` — поле refreshToken
- `backend/src/main/resources/application.yml` — refresh-window → refresh-expiration

**Изменены (tests):**
- `backend/src/test/kotlin/com/sotospeak/controller/TokenRefreshIntegrationTest.kt` — переписан под новый контракт (10 тестов: ротация, reuse-detection с отзывом цепочки, logout, refresh≠access, 401-семантика)
- `backend/src/test/kotlin/com/sotospeak/controller/AuthControllerIntegrationTest.kt` — login 400→401, refreshToken в ответе, новый тест «роль из БД, не из claim» (подделанный ADMIN-claim → 403)
- `backend/src/test/kotlin/com/sotospeak/controller/UserControllerIntegrationTest.kt` — аноним 403→401
- `backend/src/test/kotlin/com/sotospeak/controller/ClientLogControllerTest.kt` — аноним 403→401
- `backend/src/test/kotlin/com/sotospeak/controller/SpeakingFlowIntegrationTest.kt` — гость POST submissions 403→401
- `backend/src/test/kotlin/com/sotospeak/service/audio/AudioTestIntegrationTest.kt` — admin-пользователю в сиде добавлен `role="ADMIN"` (роль теперь из БД)

**Документация:** запись в `memory.md` (раздел «Решения и договорённости», 2026-08-30).

## Как проверить

```bash
.\gradlew.bat :backend:test   # гейт драйвера (H2 test-profile)
```
Ключевые классы: `TokenRefreshIntegrationTest`, `AuthControllerIntegrationTest`.

## Требуется решение владельца / отдельные задачи (НЕ блокирует backend-гейт)

1. **Клиентский авто-refresh сломан по контракту** (ожидаемо, задача — backend): `shared/api/SoToSpeakApi.kt`
   `refreshAccessToken()` шлёт в `/auth/refresh` старый ACCESS-токен → после деплоя получит 401 и сбросит
   сессию при первом истечении access-токена (грациозная деградация: пользователь перелогинится).
   Нужна follow-up задача: хранить `refreshToken` (PersistentTokenProvider), слать его в `/auth/refresh`,
   сохранять ротированный токен из ответа, logout дергать `POST /auth/logout`.
2. **Спека/документация (ADR-007)**: `docs/API.md` §`POST /auth/refresh` описывает старый контракт
   («отдельных refresh-токенов нет») — требуется правка владельцем: новые поля refreshToken в
   login/register/refresh-ответах, `POST /auth/logout`, коды 401 (`UNAUTHORIZED`/`INVALID_CREDENTIALS`/`INVALID_REFRESH_TOKEN`).
3. Newman-коллекция `api-tests/` ранее ждала 401 (memory №10) — теперь поведение совпадает; при прогоне
   учесть, что `/auth/refresh` требует refreshToken из login-ответа.
