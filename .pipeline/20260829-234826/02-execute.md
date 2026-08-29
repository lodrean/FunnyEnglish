# 02-execute — bd FunnyEnglish-0w3.2: LC: удалить/изолировать legacy backend (~7k строк)

## Решение о подходе

Тикет допускал «удалить ИЛИ изолировать». Выбрана **изоляция за `@Profile("legacy")` (off по умолчанию)**:
блокер `8zm` (deferred) содержит решение владельца «legacy не удалять до go/no-go после MVP»,
поэтому код сохранён, но эндпоинты выключены в дефолтном профиле и обратимо включаются
через `SPRING_PROFILES_ACTIVE=...,legacy`.

## Что сделано

### 1. Изоляция legacy-эндпоинтов — `@Profile("legacy")` на 14 контроллеров (13 файлов)

| Файл | Контроллер(ы) | Маппинг |
|---|---|---|
| controller/AchievementController.kt | AchievementController | /achievements |
| controller/AdaptiveLessonController.kt | AdaptiveLessonController | /api/v1/adaptive-lessons |
| controller/CategoryController.kt | CategoryController | /categories |
| controller/FeatureToggleController.kt | FeatureToggleController | /api/features |
| controller/GamificationController.kt | GamificationController | /api/v1/gamification |
| controller/LeaderboardController.kt | LeaderboardController | /leaderboard |
| controller/MessageController.kt | AdminMessageController + UserMessageController | /admin/users, /users/me/messages |
| controller/PublicAdaptiveController.kt | PublicAdaptiveController | /public/adaptive |
| controller/PublicTestController.kt | PublicTestController | /public/tests |
| controller/QuestionController.kt | QuestionController | /questions |
| controller/StudentGroupController.kt | StudentGroupController | /api/groups |
| controller/TestController.kt | TestController | /tests |
| controller/audio/AudioTestController.kt | AudioTestController | /api/audio-tests |

**Сознательно НЕ изолированы** (используются keep-функциональностью, изоляция сломала бы prod):
- Сервисы/репозитории/entities доменов Test/Question/Progress/Achievement/Category/Streak —
  их используют `AdminService` (дашборд-аналитика), `UserService`, `UserController` (/users/me/*).
- `TestService` и `AdminController` целиком — AdminController смешивает keep-эндпоинты
  (/admin/analytics и пр.) с legacy `/admin/tests/**`. Разделение AdminController — отдельная задача.
- Миграции/таблицы БД не тронуты.

### 2. Снята зависимость `implementation(project(":shared"))` (backend/build.gradle.kts:19)

Backend импортировал `com.sotospeak.shared.model.*` в 15 файлах (грабля memory.md №51).
8 используемых файлов моделей скопированы из `shared/src/commonMain/kotlin/com/sotospeak/shared/model/`
в `backend/src/main/kotlin/com/sotospeak/shared/model/` **с сохранением FQN** — ни один импорт
(включая ~10 fully-qualified обращений) не потребовал правки:
`Achievement.kt, AdaptiveLesson.kt, LessonModels.kt, Progress.kt, Quest.kt, Streak.kt, Test.kt, User.kt`.
Копии самодостаточны (только `kotlinx.serialization`, он есть в зависимостях backend).
Оригиналы в `:shared` НЕ удалены (использует composeApp). В пакете-копии лежит README.md с правилами синхронизации.

### 3. Тесты, ходящие в legacy-эндпоинты, переведены на opt-in профиль

- `AudioTestIntegrationTest` → `@ActiveProfiles("test", "legacy")`
- `GuestFlowE2ETest` → `@ActiveProfiles("test", "legacy")` (использует /public/tests/{id}/validate)

Остальные тесты (Speaking/Auth/User/ClientLog/TokenRefresh/RateLimiting и unit TestServiceTest на mockk)
legacy-эндпоинтов не касаются — профили не менялись.

## Изменённые/созданные файлы

Изменено (17):
- backend/build.gradle.kts
- backend/src/main/kotlin/com/sotospeak/controller/{Achievement,AdaptiveLesson,Category,FeatureToggle,Gamification,Leaderboard,Message,PublicAdaptive,PublicTest,Question,StudentGroup,Test}Controller.kt
- backend/src/main/kotlin/com/sotospeak/controller/audio/AudioTestController.kt
- backend/src/test/kotlin/com/sotospeak/service/audio/AudioTestIntegrationTest.kt
- backend/src/test/kotlin/com/sotospeak/controller/GuestFlowE2ETest.kt
- memory.md (запись о решении, правило AGENTS.md №2)

Создано (9):
- backend/src/main/kotlin/com/sotospeak/shared/model/{Achievement,AdaptiveLesson,LessonModels,Progress,Quest,Streak,Test,User}.kt
- backend/src/main/kotlin/com/sotospeak/shared/model/README.md

## Как проверить

1. `.\gradlew.bat :backend:test` — гейт драйвера (H2 test-profile; legacy-тесты работают через профиль "legacy").
2. Дефолтный прогон: `GET /api/tests`, `/api/groups/...`, `/api/audio-tests`, `/api/public/tests/**` → 404/403 (бины не созданы).
3. С `SPRING_PROFILES_ACTIVE=legacy` эндпоинты снова доступны.
4. `grep -rn 'project(":shared")' backend/build.gradle.kts` — пусто.

## Известные последствия (для владельца, не блокеры тикета)

- В дефолтном профиле перестают работать: Messages/Groups в composeApp (`/users/me/messages`, `/api/groups/student/**`)
  и GroupManager + отправка сообщений ученику в admin-web (`/api/groups/**`, `/admin/users/{id}/messages`).
  Это прямое следствие тикета (Groups/Message в списке). Если фичи ещё нужны — включать профиль `legacy`
  в окружении до их удаления из клиентов.
- `/admin/tests/**` (AdminController) остался активным — требует отдельного решения о разделении AdminController.
- Дубликат моделей shared↔backend устраняется при окончательном удалении legacy (bd 8zm).

Сборки/тесты не запускались (гейты прогоняет драйвер). Спеки/PRD не тронуты.
