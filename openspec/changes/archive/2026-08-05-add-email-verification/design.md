# Design: add-email-verification

## Context

Backend: Spring Boot 3.4.1 (Kotlin), JWT (jjwt), Flyway + PostgreSQL. Mail-инфраструктуры нет. Auth — `AuthController`/`AuthService` (`/api/auth/register|login`), JWT в `SecurityConfig`. Practice — `SpeakingSubmissionController` (`/api/speaking/submissions`), гейтинг по роли через JWT-фильтр. Клиент: composeApp MVI (RegisterScreen, QuestionsScreen + SpeakingGate), shared `SoToSpeakApi` (Ktor).

## Goals / Non-Goals

- Goals: своя SMTP-верификация email; flag-gated; блокировка login для unverified; Mailpit на staging; без регресса существующих флоу.
- Non-goals: password reset, смена email, OAuth, внешний auth-сервис.

## Decisions

### D1. Своя верификация (не внешний сервис)
`spring-boot-starter-mail` + JavaMailSender. Альтернативы (Supabase/Firebase Auth, Keycloak) отклонены владельцем 2026-08-01: миграция auth-стека, внешняя зависимость, Firebase ограничен в РФ.

### D2. Политика: блокировка login (решение владельца 2026-08-01)
`POST /api/auth/login` → 403 `EMAIL_NOT_VERIFIED` для `email_verified=false` при включённом флаге. Регистрация при флаге НЕ выдаёт токен (ответ `{emailSent: true}`) — auto-login отсутствует, пользователь подтверждает почту и затем логинится. Practice-гейт в service-слое НЕ нужен: неподтверждённый пользователь не получает токен. Альтернатива (Practice-gate без блокировки login) отклонена владельцем. Существующие пользователи не затронуты (миграция проставляет verified=true); `AdminUserInitializer`/`DemoUserInitializer` создают пользователей с `email_verified=true` (иначе сломаются admin/Maestro-флоу при flag=on).

### D3. Токен: случайный UUID-секрет в БД (не JWT)
Таблица `email_verification_tokens` — проще инвалидация (одноразовость, resend инвалидирует старые), TTL 24ч. JWT-верификация отклонена: нельзя инвалидировать до истечения.

### D4. Проверка verified — в AuthService.login (не JWT-claim)
`AuthService.login` при включённом флаге бросает `EmailNotVerifiedException` → 403 `EMAIL_NOT_VERIFIED` в `GlobalExceptionHandler`. JWT-claim не используем: токен живёт долго, верификация произойдёт после его выдачи — claim устареет. Дополнительных проверок в защищённых эндпоинтах нет — неподтверждённый пользователь токена не получает.

### D5. Письмо — простое текстовое/HTML со ссылкой
Шаблоны (Thymeleaf) избыточны для MVP. Ссылка: `${PUBLIC_APP_URL}/api/auth/verify-email?token=...` → GET возвращает минимальную HTML-страницу (успех/ошибка) — работает и с письма на десктопе, и на телефоне.

### D6. Async-отправка письма
`EmailService.sendVerification` — `@Async` (иначе SMTP-таймауты растягивают register). Ошибка логируется, регистрация не откатывается (D5 proposal: resend покрывает).

### D7. Resend: anti-enumeration + rate-limit
Одинаковый 200 для существующего/несуществующего email; rate-limit отдельной корзиной (`RATE_LIMIT_RESEND_CAPACITY`, дефолт 3/мин/IP) в существующем `RateLimitingFilter`.

## Data model (Flyway V22)

```sql
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT false;
UPDATE users SET email_verified = true;            -- существующие не блокируем
ALTER TABLE users ALTER COLUMN email_verified SET DEFAULT false;

CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    confirmed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_evt_token ON email_verification_tokens(token);
```

## API

- `GET /api/auth/verify-email?token=...` — public, HTML-ответ (200 success / 200 error-page; без раскрытия деталей).
- `POST /api/auth/resend-verification` — public, body `{email}`, всегда 200; rate-limited.
- `POST /api/auth/login` — при flag=on: 403 `{"error":"EMAIL_NOT_VERIFIED"}` для unverified.
- `POST /api/auth/register` — при flag=on: пользователь с `emailVerified=false`, БЕЗ `token` в ответе (`emailSent: true`), async-письмо; при flag=off поведение не меняется.

## Клиент

- `RegisterScreen`: после успеха (flag on, `emailVerified=false`) → состояние «Проверьте почту» (иконка 📬-стиль Playful Coach, email пользователя, кнопка «Отправить письмо повторно», ссылка «Войти»).
- `LoginScreen`: при 403 `EMAIL_NOT_VERIFIED` — плашка «Подтвердите почту» + resend-CTA (повторная отправка на тот же email).
- shared: `AuthResponse.emailVerified: Boolean?`, метод `resendVerification(email)`, маппинг 403-кода.

## Окружения

- dev: `EMAIL_VERIFICATION_ENABLED=false` (дефолт) — регресса нет.
- staging: `true` + `SPRING_MAIL_HOST=mailpit` (уже в `docker-compose.staging.yml`).
- prod: `true` + env SMTP-провайдера (Yandex 360 / Unisender / SendPulse) + `PUBLIC_APP_URL=https://<API_HOST>`.

## Риски

| Риск | Митигация |
|---|---|
| Письма в спам у реального провайдера | SPF/DKIM на домене (задача prod-деплоя), MAIL_FROM на своём домене |
| SMTP-таймаут растягивает register | @Async отправка (D6) |
| Перебор токенов | 128-бит случайный токен, TTL 24ч, одноразовость |
| User enumeration через resend | одинаковый 200 (D7) |
| Регресс существующих тестов | flag=off по умолчанию, отдельные тесты с flag=on |
