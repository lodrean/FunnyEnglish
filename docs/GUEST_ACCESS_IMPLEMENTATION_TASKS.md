# Задачи по реализации Guest-доступа

## Phase 1: Core Infrastructure

### Backend
- [ ] **BE-1** Добавить `/public/adaptive/**` в `SecurityConfig.kt` (`permitAll` для `GET`)
- [ ] **BE-2** Добавить `/api/audio-tests/**` в `SecurityConfig.kt` (`permitAll` для `GET`)
- [ ] **BE-3** Создать `PublicAdaptiveController` с endpoint `GET /public/adaptive/random-lesson`
- [ ] **BE-4** Добавить rate limiting для `/public/adaptive/**` в `RateLimitingFilter`

### Shared (KMP)
- [ ] **SHARED-1** Создать модели: `GuestSession`, `GuestTestProgress`, `GuestTestResult`
- [ ] **SHARED-2** Создать `GuestProgressRepository` interface
- [ ] **SHARED-3** Реализовать `GuestProgressRepositoryImpl` через `SettingsRepository` + JSON
- [ ] **SHARED-4** Создать `TestResultCalculator` (pure function) с unit tests
- [ ] **SHARED-5** Добавить `AuthMode` enum и обновить `AuthState`

### ComposeApp
- [ ] **UI-1** Обновить `AuthViewModel`:
  - `checkAuthStatus()` поддерживает 3 состояния (UNKNOWN, GUEST, AUTHENTICATED)
  - Добавить `startGuestSession()`
  - Добавить `clearGuestSession()`
- [ ] **UI-2** Обновить `App.kt`: заменить `!isLoggedIn` на `when (authState.mode)`
- [ ] **UI-3** Добавить кнопку "Продолжить без регистрации" на `LoginScreen`

---

## Phase 2: Test Guest Flow

### ComposeApp
- [ ] **UI-4** Обновить `TestViewModel.submitTest()`:
  - Branch для `GUEST` → использовать `TestResultCalculator`
  - Branch для `AUTHENTICATED` → вызов `api.submitTest()`
- [ ] **UI-5** Создать `GuestResultOverlay` / модифицировать `TestResultView`:
  - Показывать banner: "Войдите, чтобы сохранить прогресс"
  - Кнопки: "Войти", "Зарегистрироваться", "Продолжить как гость"
- [ ] **UI-6** При нажатии "Войти" из GuestResult — после авторизации автоматически предложить merge

### Tests
- [ ] **TEST-1** Unit test: `TestResultCalculator` для всех типов вопросов
- [ ] **TEST-2** Integration test: guest проходит тест → в БД нет новой записи Progress

---

## Phase 3: Adaptive Lesson Guest Flow

### Backend
- [ ] **BE-5** Реализовать `PublicAdaptiveController.getRandomLessonContent()`
- [ ] **BE-6** Добавить DTO `PublicLessonContentResponse`

### ComposeApp
- [ ] **UI-7** Создать `GuestAdaptiveLessonViewModel`
  - Загрузка из `/public/adaptive/random-lesson`
  - Локальный state management
  - Локальный подсчёт результатов
- [ ] **UI-8** Обновить `AdaptiveLessonScreen` для использования разных VM в зависимости от `AuthMode`

---

## Phase 4: Post-Auth Merge

### Backend
- [ ] **BE-7** Добавить `POST /users/me/merge-guest-progress` в `UserController`
- [ ] **BE-8** Создать DTO `MergeGuestProgressRequest`, `MergeGuestProgressResponse`
- [ ] **BE-9** Реализовать `UserService.mergeGuestProgress()`:
  - Валидация `score <= maxScore`
  - `max(existing, guest)` логика
  - Начисление XP (только разница)
  - Проверка achievements
- [ ] **BE-10** Добавить rate limit на merge endpoint (1 per 10 sec)

### Tests
- [ ] **TEST-3** Unit test: `mergeGuestProgress` с существующим прогрессом (берётся max)
- [ ] **TEST-4** Unit test: `mergeGuestProgress` с попыткой читерства (`score > maxScore` → exception)
- [ ] **TEST-5** Integration test: anonymous `POST /users/me/merge-guest-progress` → 401

### ComposeApp
- [ ] **UI-9** После `login`/`register` проверять `GuestProgressRepository.hasProgress()`
- [ ] **UI-10** Показывать `MergeProgressDialog` при наличии guest-прогресса
- [ ] **UI-11** Вызов `api.mergeGuestProgress()` и очистка guest-сессии при успехе
- [ ] **UI-12** Обновить `HomeScreen` после merge (reload data)

---

## Phase 5: Audio Tests & Other Features

### Backend
- [ ] **BE-11** Проверить что `GET /api/audio-tests` и `GET /api/audio-tests/{id}` доступны anonymous

### ComposeApp
- [ ] **UI-13** Обновить `AudioTestViewModel` для guest mode (локальный submit)
- [ ] **UI-14** Обновить `LeaderboardScreen` — не пытаться загрузить `userRank` если `GUEST`
- [ ] **UI-15** Обновить `ProfileScreen` — показывать stub для гостя
- [ ] **UI-16** Обновить `GroupsScreen` — redirect на login для гостя
- [ ] **UI-17** Обновить `AchievementScreen` — показывать список без "получено"

---

## Phase 6: QA & Security Audit

- [ ] **QA-1** E2E тест: Guest → Test → Login → Merge → Home with progress
- [ ] **QA-2** Security review: все `POST/PUT/DELETE` endpoints требуют JWT
- [ ] **QA-3** Security review: `SecurityConfig` не разрешает anonymous к `/users/me/**`
- [ ] **QA-4** Проверка rate limiting на публичных endpoints
- [ ] **QA-5** Проверка очистки guest-данных при logout
- [ ] **QA-6** Regression test: существующие auth flows не сломаны

---

## Приоритеты

**P0 (MVP):**
- BE-1, BE-2, SHARED-1..5, UI-1..3, UI-4..6, BE-7..12, UI-9..12

**P1 (Polish):**
- BE-3, BE-5, BE-6, UI-7, UI-8, UI-13..17

**P2 (QA):**
- TEST-1..5, QA-1..6
