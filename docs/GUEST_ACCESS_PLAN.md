# План: Guest-доступ для обучения без авторизации

## 1. Executive Summary

**Цель:** Предоставить неавторизованным пользователям возможность проходить обучение (тесты, адаптивные уроки, просматривать каталог), но **зачитывать прогресс, XP, достижения и streak только после авторизации**.

**Ключевой принцип безопасности:** Backend не доверяет клиенту. Все мутации (submit test, save progress, award XP) остаются защищены JWT. Guest-режим работает полностью на клиенте + публичные read-only API.

---

## 2. Security Principles (Non-negotiable)

| # | Принцип | Обоснование |
|---|---------|-------------|
| 1 | **Backend мутации = только JWT** | Любой `POST/PUT/DELETE` требует валидного токена. Guest не может подделать сохранение прогресса. |
| 2 | **Guest ID — чисто локальный** | UUID генерируется на устройстве, никогда не передаётся на backend как идентификатор пользователя. |
| 3 | **No PII для гостей** | Email, пароль, аватар не запрашиваются до авторизации. |
| 4 | **Rate limiting на public endpoints** | Существующий `RateLimitingFilter` должен охватывать публичные read-only endpoint'ы (`/categories`, `/tests`, `/audio-tests`). |
| 5 | **Client-side validation != trust** | Расчёт результатов теста на клиенте используется только для отображения гостю. После авторизации пересчёт выполняется на backend. |

---

## 3. Current State Analysis

### 3.1 Backend (Spring Boot)
- **Публично:** `GET /categories/**`, `GET /tests/**`, `/leaderboard/**`, `/auth/**`
- **Защищено:** `POST /tests/{id}/submit`, `/users/me/**`, `/groups/**`, `/adaptive/**`, `/audio-tests/submit`, `/audio-tests/my-progress`
- **JWT Filter:** `JwtAuthenticationFilter` — корректно отделяет anonymous от authenticated.

### 3.2 Client (Compose KMP)
- `App.kt` полностью блокирует доступ: если `!authState.isLoggedIn` → показывает `LoginScreen`.
- `AuthViewModel` бинарен: либо есть токен, либо нет.
- `TokenProvider` хранит только токен. Нет механизма guest-сессии.
- `SettingsRepository` (multiplatform-settings) уже доступен — можно использовать для хранения guest-прогресса.

---

## 4. Target Architecture

### 4.1 High-level Flow

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Launch App    │────▶│  Check Token    │────▶│  No Token?      │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                                         │
                            ┌────────────────────────────┘
                            ▼
                   ┌─────────────────┐
                   │  Guest Home     │◄───── Публичные API
                   │  (read-only)    │        (categories, tests)
                   └─────────────────┘
                            │
           ┌────────────────┼────────────────┐
           ▼                ▼                ▼
    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
    │ Play Test   │  │ Adaptive    │  │ Audio Test  │
    │ (local VM)  │  │ (local VM)  │  │ (local VM)  │
    └─────────────┘  └─────────────┘  └─────────────┘
           │                │                │
           ▼                ▼                ▼
    ┌─────────────────────────────────────────────┐
    │  Finish ──▶ "Save progress? Login/Register" │
    │  Result shown locally, NOT sent to backend  │
    └─────────────────────────────────────────────┘
```

### 4.2 Auth Mode Enum (Client)

```kotlin
enum class AuthMode {
    AUTHENTICATED, // JWT есть, backend доверяет
    GUEST,         // Нет JWT, локальная сессия
    UNKNOWN        // Идёт проверка при старте
}
```

`AuthState` расширяется:
```kotlin
data class AuthState(
    val isLoading: Boolean = false,
    val mode: AuthMode = AuthMode.UNKNOWN,
    val user: User? = null,
    val error: String? = null
)
```

---

## 5. Detailed Implementation Plan

### Phase 1: Core Guest Infrastructure (2–3 дня)

**5.1.1 Domain Layer — `shared` модуль**
- Добавить `GuestSession` data class:
  ```kotlin
  data class GuestSession(
      val guestId: String,
      val createdAt: Instant,
      val localProgress: List<GuestTestProgress>,
      val totalXpEarned: Int
  )
  ```
- Добавить `GuestProgressRepository` interface в `shared`:
  ```kotlin
  interface GuestProgressRepository {
      fun getSession(): GuestSession?
      fun saveSession(session: GuestSession)
      fun clearSession()
      fun addTestProgress(progress: GuestTestProgress)
      fun getTestProgress(testId: String): GuestTestProgress?
  }
  ```

**5.1.2 Data Layer — Settings-based impl**
- Реализовать `GuestProgressRepositoryImpl` через `SettingsRepository` + `kotlinx.serialization.json`.
- Ключи: `guest_session_id`, `guest_progress_json`, `guest_xp`.
- Генерация `guestId`: `uuid4()` при первом входе в guest mode.

**5.1.3 Auth Flow Refactoring**
- `AuthViewModel.checkAuthStatus()`:
  1. Пытается загрузить текущего пользователя по токену.
  2. Если токен отсутствует — проверяет `GuestProgressRepository.getSession()`.
  3. Если есть guest session → `mode = GUEST`.
  4. Если нет ничего → `mode = UNKNOWN` (показываем Welcome / Login с опцией "Продолжить как гость").

**5.1.4 `App.kt` Navigation Update**
- Заменить бинарную проверку `!isLoggedIn` на `when (authState.mode)`:
  ```kotlin
  when (authState.mode) {
      AuthMode.UNKNOWN -> Splash / Login screen
      AuthMode.GUEST -> MainAppContent(...) // все экраны доступны
      AuthMode.AUTHENTICATED -> MainAppContent(...)
  }
  ```
- На `LoginScreen` добавить кнопку: **"Попробовать без регистрации"** → `authViewModel.startGuestSession()`.

---

### Phase 2: Test Guest Flow (2–3 дня)

**5.2.1 Local Result Calculation**
- Уже есть логика `ProgressService.submitTest()` на backend. Нужен её аналог на клиенте для guest-режима.
- Создать `TestResultCalculator` (pure function) в `shared`:
  ```kotlin
  object TestResultCalculator {
      fun calculate(test: TestDetail, answers: List<SubmitAnswer>): GuestTestResult { ... }
  }
  ```
  - Поддерживает `TEXT_SELECT`, `DRAG_DROP_IMAGE`, `IMAGE_WORD_MATCH`.
  - Возвращает `score`, `maxScore`, `percentage`, `stars`.

**5.2.2 `TestViewModel` — Guest Branch**
- В методе `submitTest()`:
  ```kotlin
  if (isGuest) {
      val result = TestResultCalculator.calculate(test, answers)
      guestRepo.addTestProgress(...)
      _state.value = _state.value.copy(result = result.toLocalResult())
  } else {
      api.submitTest(...)
  }
  ```

**5.2.3 UI — Result Screen for Guest**
- `TestResultView` показывает:
  - Звёзды, процент, локальный XP (только для информирования).
  - **Banner:** "Войдите или зарегистрируйтесь, чтобы сохранить прогресс и получить достижения".
  - Кнопки: "Войти" | "Зарегистрироваться" | "Продолжить как гость".

---

### Phase 3: Adaptive Lesson Guest Flow (2 дня)

**5.3.1 Backend — Public Lesson Content Endpoint**
- Сейчас адаптивные уроки (`/api/v1/adaptive-lessons/**`) полностью закрыты.
- Нужен **новый публичный endpoint** только для получения контента (read-only):
  ```kotlin
  @RestController
  @RequestMapping("/public/adaptive")
  class PublicAdaptiveController(private val questionService: QuestionService) {
      
      @GetMapping("/random-lesson")
      fun getRandomLessonContent(
          @RequestParam categoryId: String?,
          @RequestParam difficulty: Int?
      ): ResponseEntity<PublicLessonContentResponse>
  }
  ```
  - Возвращает список вопросов с ответами (без персонализации, без сохранения состояния).
  - Добавить в `SecurityConfig`:
    ```kotlin
    .requestMatchers(HttpMethod.GET, "/public/adaptive/**").permitAll()
    ```

**5.3.2 Client — Guest Adaptive ViewModel**
- `GuestAdaptiveLessonViewModel`:
  - Загружает контент из `/public/adaptive/random-lesson`.
  - Ведёт локальный state (`currentQuestionIndex`, `answers`, `score`).
  - По завершению показывает результат локально, без вызова `completeAdaptiveLesson`.

**5.3.3 UI Integration**
- `AdaptiveLessonScreen` принимает `isGuest: Boolean`.
- В guest mode используется `GuestAdaptiveLessonViewModel`, в auth mode — существующий `AdaptiveLessonViewModel`.

---

### Phase 4: Post-Auth Merge (2–3 дня)

**5.4.1 Merge API (Backend)**
- Новый endpoint: `POST /users/me/merge-guest-progress`:
  ```kotlin
  data class MergeGuestProgressRequest(
      val testProgress: List<GuestTestProgressDto>,
      val xpEarned: Int
  )
  ```
- Логика `UserService.mergeGuestProgress()`:
  1. Валидировать каждый `testId` (существует ли тест).
  2. Для каждого теста:
     - Если у пользователя уже есть лучший результат → **не перезаписывать**, берём `max()`.
     - Если нет прогресса → создаём `Progress` entity с `score` из guest-данных.
  3. Начислить XP, но **только за недостающую разницу** (если XP за тесты уже были начислены — не дублируем).
  4. Не начислять achievements на этом этапе; проверить achievements отдельным вызовом `achievementService.checkAndAwardAchievements()`.

**5.4.2 Security на Merge**
- Только authenticated пользователь может вызвать.
- Валидация: `score` не может превышать `maxScore` теста (server-side check).
- Rate limit: max 1 merge в 10 секунд.

**5.4.3 Client Merge Flow**
- После успешного `login`/`register`:
  1. `AuthViewModel` проверяет `guestRepo.hasProgress()`.
  2. Если есть → показывает BottomSheet/Dialog: "У вас есть прогресс из гостевой сессии. Сохранить?"
  3. При согласии вызывает `api.mergeGuestProgress(...)`.
  4. После успеха `guestRepo.clearSession()`.
  5. Загружает `homeViewModel.loadHomeData()` для отображения актуального прогресса.

---

### Phase 5: Audio Tests & Other Features (1–2 дня)

**5.5.1 Audio Tests**
- `GET /api/audio-tests` уже публичен (через `SecurityConfig` `/tests/**` не покрывает `/api/audio-tests`, нужно убедиться что `GET /api/audio-tests` разрешён anonymous).
- Guest mode: локальное проигрывание аудио, локальный подсчёт результатов.
- Submit: перенаправление на login с предложением сохранить.

**5.5.2 Leaderboard**
- Уже публичен. Для гостя показывать как есть, но без подсветки своей позиции.

**5.5.3 Profile / Groups / Achievements**
- **Profile** → показывать stub: "Войдите, чтобы увидеть свой профиль".
- **Groups** → redirect to login.
- **Achievements** → показывать список всех достижений (уже есть `GET /achievements`), но без индикации "получено".

---

## 6. API Changes

### New Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `GET` | `/public/adaptive/random-lesson` | Anonymous | Получить случайный набор вопросов для адаптивного урока |
| `POST` | `/users/me/merge-guest-progress` | Authenticated | Смержить локальный guest-прогресс в аккаунт |

### Modified Endpoints

| Method | Endpoint | Change |
|--------|----------|--------|
| `GET` | `/api/audio-tests` | Убедиться, что доступен anonymous (добавить в `SecurityConfig`) |
| `GET` | `/api/audio-tests/{id}` | Убедиться, что доступен anonymous |

### Security Config Updates

```kotlin
// SecurityConfig.kt
.authorizeHttpRequests { auth ->
    auth
        .requestMatchers("/auth/**").permitAll()
        .requestMatchers("/actuator/health").permitAll()
        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/tests/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/audio-tests/**").permitAll()  // NEW
        .requestMatchers(HttpMethod.GET, "/public/adaptive/**").permitAll() // NEW
        .requestMatchers("/leaderboard/**").permitAll()
        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
        .anyRequest().authenticated()
}
```

---

## 7. Database Changes

**Минимальные или отсутствуют.**
- Новые таблицы не требуются.
- Merge использует существующие `Progress`, `User` entities.
- Guest-данные хранятся исключительно в `multiplatform-settings` на клиенте.

---

## 8. Client Architecture (Modules)

```
shared/
  ├── model/
  │     ├── GuestSession.kt
  │     ├── GuestTestProgress.kt
  │     └── GuestTestResult.kt
  ├── repository/
  │     ├── GuestProgressRepository.kt
  │     └── GuestProgressRepositoryImpl.kt
  └── util/
        └── TestResultCalculator.kt

composeApp/
  ├── viewmodel/
  │     ├── AuthViewModel.kt (refactored)
  │     └── GuestAdaptiveLessonViewModel.kt
  ├── screens/
  │     ├── LoginScreen.kt (add guest button)
  │     └── GuestTestResultOverlay.kt
  └── App.kt (refactored navigation)

backend/
  ├── controller/
  │     ├── PublicAdaptiveController.kt
  │     └── UserController.kt (+ merge endpoint)
  ├── service/
  │     └── UserService.kt (+ mergeGuestProgress)
  └── config/
        └── SecurityConfig.kt (updated)
```

---

## 9. Testing Strategy

### Unit Tests
- `TestResultCalculatorTest` — проверка подсчёта для всех типов вопросов.
- `GuestProgressRepositoryImplTest` — сериализация/десериализация session.
- `UserService.mergeGuestProgress()` — edge cases (дубликаты, превышение maxScore).

### Integration Tests
- Guest может открыть `HomeScreen` без токена.
- Guest может пройти тест, результат не создаёт записи в БД backend.
- После login с merge, прогресс появляется в `GET /users/me/progress`.
- Anonymous `POST /users/me/merge-guest-progress` → 401.

### E2E Tests
- Flow: Launch → Guest mode → Play Test → Result Screen → Login → Save Progress → Home with progress.

---

## 10. Security Checklist

- [ ] Все `POST/PUT/DELETE` endpoints (кроме `/auth/**`) требуют JWT.
- [ ] `SecurityConfig` не разрешает anonymous доступ к `/users/me/**`, `/groups/**`, `POST /tests/**/submit`.
- [ ] `RateLimitingFilter` активен на `/public/adaptive/**` и `/tests/**`.
- [ ] `merge-guest-progress` валидирует `score <= maxScore` и `percentage <= 100`.
- [ ] `merge-guest-progress` не перезаписывает существующий лучший результат пользователя.
- [ ] Guest ID никогда не передаётся в backend как идентификатор.
- [ ] Локальные guest-данные шифруются/хранятся в `Settings` (рассмотреть `EncryptedSettings` на Android).
- [ ] Нет возможности для guest получить персональные данные других пользователей.

---

## 11. Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Пользователь теряет guest-прогресс при очистке данных приложения | Medium | Предупреждение в UI: "Прогресс сохраняется только на этом устройстве" |
| Читерство с локальным результатом перед merge | Low | Backend пересчитывает/валидирует score при merge; берёт `max()` существующего |
| DDoS на `/public/adaptive/random-lesson` | Medium | Rate limiting + кэширование (Redis) |
| Сложность поддержки двух VM (auth/guest) для каждого экрана | Medium | Абстракция через `LessonViewModelFactory` / `TestViewModelFactory` |

---

## 12. Estimated Effort

| Phase | Duration |
|-------|----------|
| Phase 1: Core Infrastructure | 2–3 дня |
| Phase 2: Test Guest Flow | 2–3 дня |
| Phase 3: Adaptive Lesson Guest Flow | 2 дня |
| Phase 4: Post-Auth Merge | 2–3 дня |
| Phase 5: Audio Tests & Polish | 1–2 дня |
| Testing & QA | 2 дня |
| **Total** | **~11–15 дней** |

---

## 13. Next Steps

1. **Утвердить план** с product/командой.
2. Создать подзадачи в трекере по каждой Phase.
3. Начать с Phase 1 (domain models + Auth refactoring) — это фундамент.
4. Параллельно: обновить `SecurityConfig` и добавить `/public/adaptive/**` на backend.
