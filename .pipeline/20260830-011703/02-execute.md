# 02-execute — bd FunnyEnglish-5tf.2: KMP: скоуп ViewModel по маршруту

## Что сделано

Закрыт К3 (PROJECT-REVIEW-2026-08-28 §2.2): `koinViewModel()` резолвился в ViewModelStore
Activity — все VM переживали переходы между экранами (компенсаторный «ручной сброс в load()»,
таймеры/джобы жили между визитами).

1. **Новый хелпер `routeViewModel(key)`** (`app/util/RouteViewModel.kt`): каждому значению
   ключа (маршрут с параметрами) выделяется собственный `ViewModelStore` (`remember(key)`);
   при уходе с маршрута/смене ключа store очищается (`DisposableEffect` → `clear()` →
   `onCleared()`, отмена viewModelScope/таймеров/джобов). Внутри — стандартный
   `koinViewModel(viewModelStoreOwner = owner)` (koin-compose-viewmodel 4.0.0, сигнатуру
   проверил по sources-jar).
2. **Переведены на route-скоуп** (ключ — `AppScreen` data-класс с параметрами):
   Library, Topics, Questions, Profile (+ MySubmissionsViewModel для stat-карточек),
   Messages, MySubmissions — в `App.kt`; VideoRoute/TrainingRoute/PracticeRoute — в
   экранах (ключ `"video|training|practice:$topicId"` — на каждый топик своя VM).
3. **ProfileViewModel/MessagesViewModel перенесены** с уровня `MainAppContent` (создавались
   всегда) в свои ветки `when` — создаются только при входе на экран.
4. **Осознанно оставлены на Activity-скоупе**: `AuthViewModel` (auth-state драйвит всё
   приложение, мост «сессия истекла») и `SettingsViewModel` (тема). DI (AppModule) не менялся.
5. «Ручные сбросы в load()» в VM **не удалял** — при чистой VM на каждый вход безвредны;
   удаление — отдельная задача (риск регрессий в MVI-флоу). Зафиксировано в memory.md.
6. memory.md дополнен записью решения (раздел 5, 2026-08-30).

## Изменённые/созданные файлы

- `composeApp/src/commonMain/kotlin/com/sotospeak/app/util/RouteViewModel.kt` — **новый** хелпер.
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/App.kt` — 7 VM на routeViewModel, перенос 2 VM в ветки, импорт.
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/PracticeScreen.kt` — PracticeRoute.
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/TrainingScreen.kt` — TrainingRoute.
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/VideoScreen.kt` — VideoRoute.
- `memory.md` — запись решения.

Спеки/PRD не тронуты (правок не потребовалось). Gradle-сборки/тесты не запускались (гейты драйвера).

## Как проверить

- Гейты драйвера: `:composeApp:desktopTest`, `:composeApp:compileDebugKotlinAndroid`,
  `:composeApp:compileKotlinWasmJs` (--no-configuration-cache).
- Ручная проверка поведения: Library → Topics(A) → назад → Topics(B) — TopicsViewModel
  пересоздаётся (свежий стейт); выход с Training/Practice — таймеры/рекордер останавливаются
  (onCleared); возврат на Practice того же топика — чистая фаза Ready.
- Тесты UI (commonTest) не затронуты: экраны тестируются с моковыми state, koinViewModel
  в тестах не используется (проверено grep).
