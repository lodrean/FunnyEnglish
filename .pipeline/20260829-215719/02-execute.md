# 02-execute — FunnyEnglish-0w3.1: LC legacy deprecated + убрать permitAll

## Что сделано

1. **SecurityConfig: убраны legacy-роуты из `permitAll`** (backend/.../config/SecurityConfig.kt):
   - `GET /categories/**`, `GET /tests/**`, `GET /api/audio-tests/**`, `/leaderboard/**` — допивотный quiz/leaderboard-стек (PROJECT_AUDIT_2026-08-29, AR-5). Теперь попадают под `anyRequest().authenticated()` → attack surface для неавторизованных запросов сокращён.
   - Оставлены живые permitAll: `/auth/**`, `/actuator/health`, `/public/**`.
   - `/public/**` НЕ сужен сознательно: под ним живые гостевые эндпоинты (`/public/speaking/**`, `/public/guest-events`, `/public/logs`) + legacy `/public/tests`, `/public/adaptive`; сужение требует решения владельца в `8zm` (от `/public/tests/{id}/validate` зависит `GuestFlowE2ETest`). Оставлен комментарий в коде.
   - Удалён ставший неиспользуемым импорт `HttpMethod`.

2. **Legacy-контроллеры помечены `@Deprecated`** (level=WARNING, в сообщении ссылка на AR-5 и bd `FunnyEnglish-8zm`):
   - `GamificationController` (`/api/v1/gamification`) — сломан на runtime (500: `@AuthenticationPrincipal UserDetails`, а principal — `UserPrincipal`).
   - `AdaptiveLessonController` (`/api/v1/adaptive-lessons`) — та же поломка.
   - `LeaderboardController` (`/leaderboard`) — роут убран из permitAll, живые клиенты не используют.

## Изменённые файлы

- `backend/src/main/kotlin/com/sotospeak/config/SecurityConfig.kt`
- `backend/src/main/kotlin/com/sotospeak/controller/GamificationController.kt`
- `backend/src/main/kotlin/com/sotospeak/controller/AdaptiveLessonController.kt`
- `backend/src/main/kotlin/com/sotospeak/controller/LeaderboardController.kt`

## Почему это безопасно (проверено грепом)

- Живой клиент (shared `SoToSpeakApi.kt`, composeApp) не вызывает `/categories`, `/tests`, `/api/audio-tests`, `/leaderboard`, `/api/v1/*` — только `/api/auth`, `/api/public/*`, `/api/speaking/*`, `/api/users/*`, `/api/groups/*`.
- admin-web использует только `/admin/**`, `/auth/login`, `/public/logs` — не затронуты.
- Backend-тесты: `AudioTestIntegrationTest` всегда шлёт `Authorization: Bearer` — удаление permitAll для `/api/audio-tests/**` не ломает; `GuestFlowE2ETest` ходит на `/public/tests/**` — остаётся permitAll; тестов, ходящих на `/categories`, `/tests`, `/leaderboard` без токена, нет.
- `@Deprecated(WARNING)` не ломает компиляцию (`allWarningsAsErrors` в backend/convention-плагинах не включён), бины Spring создаются как раньше — поведение эндпоинтов не меняется, только разметка.
- Feature-toggle (`FeatureToggleController`, `/api/features`) и так требует аутентификации — отдельного закрытия не потребовалось; выбран вариант «убрать permitAll» из формулировки задачи.

## Как проверить (гейт драйвера)

```bash
.\gradlew.bat :backend:test
```

Ожидание: все тесты зелёные (H2 test-profile). Дополнительно вручную: `GET /api/tests/...`, `GET /api/categories/...`, `GET /api/leaderboard` без токена → 403 (ранее 200); с валидным JWT — прежнее поведение.

## Что НЕ сделано (ждёт владельца, ADR-007)

- Удаление legacy-контроллеров/сервисов/таблиц — решение bd `FunnyEnglish-8zm` (deferred, P1).
- Сужение `/public/**` permitAll (убрать legacy `/public/tests`, `/public/adaptive`) — тоже в `8zm`, т.к. затрагивает гостевой флоу валидации тестов.
