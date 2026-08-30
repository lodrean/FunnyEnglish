# 02-execute — bd FunnyEnglish-h3l.11: PR i18n (EN-UI)

## Что сделано

Параметризован `Strings.kt` и заменён хардкод русского во всех 7 speaking-экранах
(аудит PROJECT_AUDIT_2026-08-29, находки F-3/F-F).

1. **`localization/Strings.kt`** — интерфейс `AppStrings` расширен с ~35 legacy-ключей
   до ~120: добавлены ключи для Library/Topics/Questions/Video/Training/Practice/MySubmissions.
   - RU-значения = прежний хардкод **символ-в-символ** (Maestro/UI-тесты матчат русский точно;
     поведение по умолчанию RU не изменилось).
   - EN-значения — полный английский перевод.
   - Строки с параметрами — шаблоны `{0}`/`{1}` + default-методы через `.replace()`
     (String.format недоступен на WASM — грабля №38).
   - Плюрализация — функции `topicsCount(count)`/`questionsCount(count)` с разной
     реализацией в RU («1 топик/3 топика/6 топиков») и EN ("1 topic/N topics").
   - Добавлен `LocalAppStrings = staticCompositionLocalOf<AppStrings> { RussianStrings }`.
2. **`App.kt`** (`AppThemedContent`) — контент обёрнут в
   `CompositionLocalProvider(LocalAppStrings provides Strings.get(settingsState.language))`:
   выбор языка в настройках (селектор уже существовал) теперь реально переключает
   speaking-экраны RU ↔ EN.
3. **7 экранов** — все пользовательски видимые строки (Text, чипы, EmptyState,
   contentDescription кнопок записи/плеера, диалог «Прервать запись?», рубрика оценки и пр.)
   заменены на `LocalAppStrings.current`. testTag'и, логика, навигация не тронуты.
   `LibraryScreen.topicsCountText` оставлен делегатом к `RussianStrings.topicsCount`
   (на него завязан `LibraryScreenTest` — совместимость сохранена).

## Изменённые файлы (9)

- `composeApp/src/commonMain/kotlin/com/sotospeak/app/localization/Strings.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/App.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/LibraryScreen.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/TopicsScreen.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/QuestionsScreen.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/VideoScreen.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/TrainingScreen.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/PracticeScreen.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/MySubmissionsScreen.kt`

Плюс запись о решении в `memory.md` (правило AGENTS.md №2).

## Осознанно НЕ переведено (вне скоупа «~7 экранов»)

- Экраны Login/Register/Onboarding/Settings/Profile/Messages/Debug и нижняя навигация (NavItems).
- `app/error/UiText` (сетевые ошибки — строки RU).
- a11y-анонсы таймера внутри `components/SpeakingRecording.kt` («Осталось N секунд») и
  строки `components/MergeProgressDialog.kt` (компоненты не принимают строки параметрами).

## Как проверить

- Сборка: `./gradlew :composeApp:compileKotlinWasmJs :composeApp:compileDebugKotlinAndroid`
  (не запускалась — гейты прогоняет драйвер).
- Тесты: `./gradlew :composeApp:desktopTest` (RU-тексты не изменились, Maestro-флоу тоже должны пройти).
- Ручная проверка EN: Настройки → Язык → English → speaking-экраны на английском.
- Статические проверки (выполнены): кириллица в литералах 7 экранов отсутствует
  (perl `\p{Cyrillic}` — только комментарий); все `strings.xxx` соответствуют объявленным
  ключам интерфейса; `git status` — только 9 разрешённых файлов.

## Риски

- Компиляция локально не прогонялась (ограничение задачи) — ключи сверены скриптом,
  но окончательный вердикт за gradle-гейтами драйвера.
