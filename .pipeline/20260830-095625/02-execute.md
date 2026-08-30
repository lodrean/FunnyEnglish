# 02-execute — bd FunnyEnglish-0w3.3: LC: shared legacy-модели → legacy/ + contracts/

## Что сделано

1. **Восстановлена и доведена прерванная попытка**: предыдущая сессия 0w3.3 была прервана,
   её работа сохранена в stash-коммите `93db4f9` («wip 0w3.3 прервано»). Дифф применён к
   ветке `kimi/FunnyEnglish-0w3.3-20260830-095625`, кроме `.beads/issues.jsonl` и
   `PracticeViewModel.kt` (см. ниже).

2. **Пакет `com.sotospeak.shared.model` упразднён**:
   - Активные контракты → `com.sotospeak.shared.contracts`:
     Auth, AuthMode, ClientLog, GuestEvent, GuestSession, Message, Speaking, StudentGroup, User
     (файлы физически уже лежали в `shared/contracts/` после LC-2 — сменена только package-декларация).
   - Legacy-модели → `com.sotospeak.shared.legacy`:
     Achievement, AdaptiveLesson, AudioTest, LessonModels, Progress, Quest, Streak, Test.
   - Кросс-ссылок между двумя пакетами нет (проверено grep по всем типам обоих пакетов).

3. **Обновлены импорты** во всех потребителях:
   - `:shared` — `api/AuthApi.kt`, `api/GuestApi.kt`, `api/MessagingApi.kt`, `api/SoToSpeakApi.kt`,
     `api/SpeakingApi.kt`, `repository/GuestProgressRepository.kt`, `util/ClientLogQueue.kt`, `util/Logger.kt`.
   - `:composeApp` — App.kt, data/SpeakingRepository, screens (Library/Messages/MySubmissions),
     util (GuestAnalytics/LogUploader), viewmodel (Auth/Groups/Library/Messages/MySubmissions/
     Practice/Profile/Questions/Training/Video), androidMain preview, androidInstrumentedTest
     screenshot, commonTest/desktopTest тесты и TestMocks.
   - **Backend НЕ тронут**: он не зависит от `:shared` (bd 0w3.2) и использует собственные копии
     моделей в `backend/.../shared/model/` — старый FQN там намеренно сохранён.

4. **Запрет новых клиентских импортов legacy**: в `config/detekt/detekt.yml` активировано
   правило `ForbiddenImport` с паттерном `com.sotospeak.shared.legacy.*` (style-секция, общий
   конфиг :backend + :composeApp). Текущих нарушений нет (проверено grep — ни один файл
   :composeApp не импортирует legacy), поэтому baseline `config/detekt/baseline.xml` не менялся.

5. **memory.md**: обновлена строка архитектуры про `shared/` + запись в разделе
   «Решения и договорённости» (2026-08-30).

## Грабля, которую удалось избежать

`PracticeViewModel.kt` — единственный файл с CRLF в viewmodel/ (см. memory.md, решение 5tf.5).
Stash-версия конвертировала его в LF (diff «весь файл», 568 строк). Вместо этого import
исправлен точечно с сохранением CRLF — diff = 1 строка.

## Изменённые/созданные файлы (53)

- `config/detekt/detekt.yml` — ForbiddenImport для `com.sotospeak.shared.legacy.*`
- `memory.md` — архитектура + запись решения
- `shared/src/commonMain/kotlin/com/sotospeak/shared/contracts/*.kt` (9 файлов) — package → contracts
- `shared/src/commonMain/kotlin/com/sotospeak/shared/legacy/*.kt` (8 файлов) — package → legacy
- `shared/.../api/{AuthApi,GuestApi,MessagingApi,SoToSpeakApi,SpeakingApi}.kt`,
  `shared/.../repository/GuestProgressRepository.kt`, `shared/.../util/{ClientLogQueue,Logger}.kt`
- `composeApp/.../App.kt`, `data/SpeakingRepository.kt`, `screens/{LibraryScreen,MessagesScreen,
  MySubmissionsScreen}.kt`, `util/{GuestAnalytics,LogUploader}.kt`,
  `viewmodel/{AuthViewModel,GroupsViewModel,LibraryViewModel,MessagesViewModel,
  MySubmissionsViewModel,PracticeViewModel,ProfileViewModel,QuestionsViewModel,
  TrainingViewModel,VideoViewModel}.kt`,
  `androidMain/.../preview/AppPreviews.kt`,
  `androidInstrumentedTest/.../screenshot/ScreenshotTest.kt`,
  `commonTest/.../di/TestMocks.kt`, `commonTest/.../tests/{EmailVerificationUiTest,
  LoginUserFlowTest,ProfileScreenTest,VideoScreenTest}.kt`,
  `desktopTest/.../tests/ClientLoggingTest.kt`

## Как проверить (гейты драйвера)

1. `./gradlew :composeApp:desktopTest` — компиляция common/desktop + тесты.
2. `./gradlew :composeApp:compileKotlinWasmJs :composeApp:compileDebugKotlinAndroid` — все таргеты.
3. `./gradlew :composeApp:detekt :backend:detekt` — новое правило ForbiddenImport (ожидается 0 новых findings).
4. `./gradlew :backend:test` — backend не затронут (свои копии моделей), гейт должен остаться зелёным.

Статические проверки, выполненные агентом (без gradle, по ограничению задачи):
- ни одной ссылки `com.sotospeak.shared.model` вне `backend/` (grep по всем `*.kt`, включая комментарии);
- все 29 уникальных импортов `shared.contracts.*` / `shared.legacy.*` резолвятся в объявления
  соответствующего пакета;
- файлы с wildcard-импортом `contracts.*` (SoToSpeakApi, AuthViewModel, TestMocks) не используют
  ни одного legacy-типа;
- `config/detekt/detekt.yml` валиден (yaml.safe_load);
- ни один файл :composeApp/:shared не импортирует `shared.legacy.*` → правило detekt не даст
  ложных срабатываний, baseline менять не нужно.

## НЕ сделано (вне скоупа / требует владельца)

- Среднесрочная генерация моделей из OpenAPI — отдельная задача (упомянута в bd-описании
  как «среднесрочно», здесь не реализуется).
- Спеки/PRD не трогались (ADR-007) — правок спек задача не потребовала.
