# Proposal: add-email-verification

## Why

Перед MVP-тестированием (go/no-go) владелец поставил требование: регистрация должна подтверждаться по email (план `~/.kimi/plans/zatanna-shadowcat-cyborg.md`, шаг 3). Сейчас регистрация (`POST /api/auth/register`) не проверяет владение почтой — любой адрес принимается, mail-инфраструктуры в backend нет вообще. Это риск: фейковые адреса, невозможность восстановления доступа, спам-регистрации.

Решение владельца (2026-08-01): **своя верификация через SMTP** (не внешний auth-сервис) — сохраняем текущую JWT-авторизацию, полный контроль, работает с российскими SMTP-провайдерами (Yandex 360 / Unisender / SendPulse).

bd-задача: `So to Speak-8gj` (MVP-3).

## What Changes

- **Backend mail-инфраструктура**: `spring-boot-starter-mail`, конфиг через env (`SPRING_MAIL_HOST/PORT/USERNAME/PASSWORD`, `MAIL_FROM`), `EmailService`.
- **Модель верификации** (Flyway V22): `users.email_verified` (существующие пользователи — `true`, чтобы не заблокировать), таблица `email_verification_tokens` (user_id, token, expires_at, confirmed_at).
- **API**: `GET /api/auth/verify-email?token=...` (подтверждение, HTML-ответ), `POST /api/auth/resend-verification` (rate-limited, как login — грабля №23). Регистрация при включённом флаге отправляет письмо со ссылкой.
- **Политика доступа (решение владельца 2026-08-01): блокировка login.** `POST /api/auth/login` для неподтверждённого email → 403 `EMAIL_NOT_VERIFIED`. Регистрация НЕ выдаёт сессию (без auto-login): ответ `{emailSent: true}`, клиент показывает «Проверьте почту». Practice-гейт 403 не требуется — неподтверждённый пользователь не получает токен.
- **Feature-flag** `EMAIL_VERIFICATION_ENABLED` (dev=false — существующие тесты/флоу не ломаются; staging/prod=true). На staging письма перехватывает Mailpit.
- **Клиент (composeApp)**: после регистрации — состояние «Проверьте почту» (Playful Coach, без auto-login); на LoginScreen при 403 `EMAIL_NOT_VERIFIED` — плашка «Подтвердите почту» + «Отправить письмо повторно».
- **shared**: поле `emailVerified` в auth-моделях, обработка 403 `EMAIL_NOT_VERIFIED`.

Breaking changes: нет (при `EMAIL_VERIFICATION_ENABLED=false` поведение API идентично текущему).

## Capabilities

### New Capabilities
- `email-verification`: выдача и подтверждение токенов верификации email, письма через SMTP, блокировка login для неподтверждённых, resend с rate-limiting, UI-состояния клиента.

### Modified Capabilities
(пусто — auth-спеки в `openspec/specs/` отсутствуют; изменение регистрации/логина фиксируется в новой capability)

## Non-goals

- Восстановление пароля (password reset) — отдельный change, переиспользует mail-инфраструктуру.
- Смена email с повторной верификацией.
- Внешние auth-провайдеры (Google/VK OAuth) — заглушки удалены из UI ранее, не возвращаем.
- Красивые HTML-шаблоны писем (MVP — простое текстовое/HTML-письмо со ссылкой).
- Реальный SMTP-провайдер на staging (там Mailpit; провайдер выбирается при prod-деплое).

## Impact

- **Backend**: новый `EmailService`, `AuthService`/`AuthController` (verify/resend), `SpeakingSubmissionController` (проверка verified), Flyway V22, `application.yml` (mail), зависимость spring-boot-starter-mail.
- **composeApp/shared**: RegisterScreen («Проверьте почту», без auto-login), LoginScreen (unverified-плашка + resend), auth-модели.
- **Окружения**: `docker-compose.staging.yml` уже передаёт `SPRING_MAIL_*`/`EMAIL_VERIFICATION_ENABLED` (Mailpit); prod — env SMTP-провайдера (шаг 6 плана).
- **Тесты**: backend unit/integration (register→без токена→login 403→verify→login 200, resend anti-enumeration, flag=off регресс), desktopTest новых UI-состояний, живой прогон на staging с Mailpit.
- **Документация**: `docs/API.md` (2 новых эндпоинта), `DOCKER.md` (SMTP env), спека Part 1/Part 2 — диффы по ADR-007 после утверждения.
