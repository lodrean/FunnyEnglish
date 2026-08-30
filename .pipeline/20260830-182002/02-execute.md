# 02-execute — bd FunnyEnglish-5tf.9: Декомпозиция App.kt + DI по фичам

## Что сделано

**App.kt: 737 → 231 строка.** Оставлено: `App()` (старт Koin с guard'ом `KoinApplicationAlreadyStartedException`),
`AppThemedContent` (тема, клиентские логи, `RecordingStore.prune()`), `AppContent` (флоу Splash/Onboarding/Login/Register
+ `MergeProgressDialog`). Поведение не менялось — чистый перенос кода.

Вынесено в новый пакет `com.sotospeak.app.navigation`:

- `AppScreen.kt` — sealed-класс маршрутов (без изменений).
- `NavItems.kt` — `MainNavItem` + `mainNavItems` + `BottomNavigationBar` + `SpeakingNavigationRail`
  (обе нави-рейки используют один список пунктов — устранено дублирование ~120 строк).
- `ScreenTransition.kt` — `rememberScreenTransition()`: единый M3 Emphasized transition
  (раньше — две одинаковые копипасты в AppContent и MainAppContent), учитывает `LocalReduceMotion`.
- `MainNavHost.kt` — диспетчер контентных экранов (бывший внутренний AnimatedContent MainAppContent):
  Profile/DebugMenu/Messages/Settings + speaking-флоу Library → Topics → Video → Questions → Training/Practice → MySubmissions.
- `AppScaffold.kt` — адаптивный каркас (бывший `MainAppContent`): BoxWithConstraints → rail/bottom nav,
  Scaffold, MaxContentWidth, исключение padding для Video (грабля №0 cutout).

**DI по фичам.** `appModule` (118 строк «на всё») стал агрегатором:
`includes(coreModule, authModule, settingsModule, messagingModule, speakingModule)`.
- `CoreModule.kt` — Settings, TokenProvider, GuestProgressRepository, аналитика, AppConfig/логи,
  SessionEvents, SoToSpeakApi + API-срезы, media HttpClient; классы `SessionEvents`/`PersistentTokenProvider`
  переехали сюда же (пакет `app.di` не изменился — импорт в AuthViewModel не тронут).
- `AuthModule.kt` — AuthViewModel, ProfileViewModel.
- `SettingsModule.kt` — SettingsViewModel.
- `MessagingModule.kt` — GroupsViewModel, MessagesViewModel (legacy).
- `SpeakingModule.kt` — RecordingFileStorage/RecordingStore/SpeakingRepository/AudioPlayer + 7 speaking-VM.

Прочее: в `BottomNavTest.kt` обновлены 2 импорта (`com.sotospeak.app.AppScreen/BottomNavigationBar` →
`com.sotospeak.app.navigation.*`); запись в `memory.md` (раздел «Решения и договорённости»).

## Изменённые/созданные файлы

Созданы:
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/navigation/AppScreen.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/navigation/NavItems.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/navigation/ScreenTransition.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/navigation/MainNavHost.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/navigation/AppScaffold.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/di/CoreModule.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/di/AuthModule.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/di/SettingsModule.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/di/MessagingModule.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/di/SpeakingModule.kt`

Изменены:
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/App.kt` (737 → 231 строка)
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/di/AppModule.kt` (агрегатор includes)
- `composeApp/src/commonTest/kotlin/com/sotospeak/app/tests/BottomNavTest.kt` (2 импорта)
- `memory.md` (запись о решении)

## Как проверить

Гейты драйвера:
```bash
./gradlew :composeApp:desktopTest
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinWasmJs --no-configuration-cache
```
Ожидание: компиляция без ошибок (перенос 1:1, сигнатуры экранов/VM не менялись),
`BottomNavTest` зелёный (BottomNavigationBar в новом пакете, internal — доступен из commonTest того же модуля).
Ручная проверка поведения: старт приложения (Splash → Onboarding/Library), табы bottom nav,
rail на wide-экране, переходы между экранами с анимацией, debug-меню через 7 тапов по версии.

## Риски/замечания

- detekt не входит в гейты задачи; `MainNavHost` — длинная when-функция (как и исходный MainAppContent),
  baseline detekt пустой, но исходный код с той же структурой проходил gate.
- Сборки/тесты не запускались исполнителем (по требованиям задачи — гоняет драйвер).
