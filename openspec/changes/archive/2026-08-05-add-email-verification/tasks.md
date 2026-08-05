# Tasks: add-email-verification

> Каждая задача дублируется в bd перед реализацией (`bd create`, родитель `So to Speak-8gj`).
> Критический путь: 1.1 → 1.3 → 2.1 → 2.2 → 3.1 → 4.
> Предусловие: утверждение владельцем политики доступа (design D2) и этого proposal (ADR-007).

## 1. Backend: инфраструктура и модель

- [x] 1.1 EV-1: Зависимость `spring-boot-starter-mail` в `backend/build.gradle.kts`; mail-конфиг в `application.yml` (env `SPRING_MAIL_*`, `MAIL_FROM`, `EMAIL_VERIFICATION_ENABLED=false` дефолт, `PUBLIC_APP_URL`). Зависимости: —
- [x] 1.2 EV-2: Flyway `V22__add_email_verification.sql` (users.email_verified + email_verification_tokens, design — Data model). Зависимости: —
- [x] 1.3 EV-3: `EmailVerificationToken` entity + репозиторий; `User.emailVerified`; `@EnableAsync`; `EmailService` (sendVerification, @Async, текстовое/HTML-письмо со ссылкой). Зависимости: 1.1, 1.2

## 2. Backend: API

- [x] 2.1 EV-4: Регистрация при flag=on — `emailVerified=false`, БЕЗ токена в ответе (`emailSent: true`), async-письмо; initializers admin/demo → `emailVerified=true`. Зависимости: 1.3
- [x] 2.2 EV-5: `GET /api/auth/verify-email` (HTML success/error, одноразовость, TTL 24ч) + `POST /api/auth/resend-verification` (anti-enumeration 200, инвалидация старых токенов, rate-limit корзина в `RateLimitingFilter`). Зависимости: 1.3
- [x] 2.3 EV-6: Login-block: проверка `emailVerified` в `AuthService.login` при flag=on → 403 `EMAIL_NOT_VERIFIED` через `GlobalExceptionHandler`. Зависимости: 1.3
- [x] 2.4 EV-7: Backend-тесты: unit (EmailService mock JavaMailSender, токен-сервис) + integration (register→без токена→login 403→verify→login 200; resend anti-enumeration; flag=off — без верификации). Полный `:backend:test` зелёный по exit code. Зависимости: 2.1–2.3

## 3. Клиент (composeApp + shared)

- [x] 3.1 EV-8: shared: register-ответ без токена (nullable), `resendVerification(email)` в `SoToSpeakApi`, маппинг 403-кода `EMAIL_NOT_VERIFIED`. Зависимости: 2.1–2.3
- [x] 3.2 EV-9: RegisterScreen — состояние «Проверьте почту» (Playful Coach, email, resend-CTA, «Войти») вместо auto-login. Зависимости: 3.1
- [x] 3.3 EV-10: LoginScreen — при 403 `EMAIL_NOT_VERIFIED` плашка «Подтвердите почту» + resend. Зависимости: 3.1
- [x] 3.4 EV-11: desktopTest новых состояний + регресс `:composeApp:desktopTest`, компиляция wasm/android. Зависимости: 3.2, 3.3

## 4. Gate: живой прогон и документация

- [x] 4.1 EV-12: Живой прогон на staging (Mailpit, flag=on): register→письмо→verify→login→practice 201; unverified login→403; demo/admin verified. Maestro 4/4. Зависимости: 2.4, 3.4
- [x] 4.2 EV-13: Документация: `docs/API.md` (verify/resend, 403-код), `DOCKER.md` (SMTP env, SPF/DKIM-заметка для prod), memory.md (решения+грабли). Диффы спек Part 1/Part 2 — по ADR-007. Зависимости: 4.1
