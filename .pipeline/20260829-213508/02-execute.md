# Отчёт: bd FunnyEnglish-nj2.3 — SEC: OAuth-логин без проверки у провайдера

## Решение

Выбран вариант **«отключить endpoint до реализации»** (второй вариант из задачи).
Полноценная верификация токена у Google/VK/Telegram — это новая фича (нужны client-id/secret,
bot token, HTTP-вызовы к провайдерам, контракты), требующая спеки и решения владельца (ADR-007);
клиентское OAuth-UI уже удалено из приложения (2026-08-01), поэтому отключение endpoint
ничего не ломает.

## Что сделано

1. **Endpoint отключён фиче-флагом** `app.oauth.enabled` (env `OAUTH_ENABLED`, дефолт `false`).
   `POST /api/auth/oauth/{provider}` при выключенном флаге отдаёт **404** (паттерн как у
   verify-email/resend-verification). Флаг выставлен в `application.yml`, гард — в
   `AuthController.oauthLogin` (сервис экспонирует `oauthEnabled`).
2. **Убран `user.copy()` на entity** в `AuthService.oauthLogin`: поля `User.avatarUrl`,
   `User.authProvider`, `User.providerId` переведены на `var`, линковка аккаунта — мутация
   managed-entity + `save()` (copy() ломал dirty-checking и делал лишний merge+SELECT).
3. **Документирующие комментарии**: в `AuthService.oauthLogin` — TODO(security) с перечнем
   верификаций (Google tokeninfo / VK users.get / Telegram HMAC) перед включением флага.
4. **Регрессионный тест** `AuthControllerIntegrationTest.oauth login is disabled by default
   and returns 404` — POST `/auth/oauth/google` с поддельным токеном → 404.
5. `memory.md` — запись решения в раздел «Решения и договорённости».

## Изменённые файлы

- `backend/src/main/resources/application.yml` — `app.oauth.enabled: ${OAUTH_ENABLED:false}` + комментарий
- `backend/src/main/kotlin/com/sotospeak/controller/AuthController.kt` — гард 404 на `/auth/oauth/{provider}`
- `backend/src/main/kotlin/com/sotospeak/service/AuthService.kt` — флаг `oauthEnabled`, TODO(security), мутация вместо copy()
- `backend/src/main/kotlin/com/sotospeak/entity/User.kt` — `avatarUrl`/`authProvider`/`providerId`: val → var
- `backend/src/test/kotlin/com/sotospeak/controller/AuthControllerIntegrationTest.kt` — новый тест 404
- `memory.md` — запись решения

## Как проверить

```bash
.\gradlew.bat :backend:test   # H2 test-profile; новый тест: AuthControllerIntegrationTest
```

Вручную против живого стека: `POST http://localhost:8080/api/auth/oauth/google`
с телом `{"token":"x","email":"a@b.c"}` → ожидается 404 (при `OAUTH_ENABLED` не задан).

## Требует решения владельца (НЕ в scope задачи)

- Включение OAuth (`OAUTH_ENABLED=true`) допустимо только после реализации верификации
  токена у провайдера — это новая фича: нужна спека (ADR-007) и конфиги провайдеров
  (GOOGLE_CLIENT_ID, VK_CLIENT_ID/SECRET, TELEGRAM_BOT_TOKEN). Сейчас флаг включать нельзя.
