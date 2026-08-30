# 02-execute — bd FunnyEnglish-2oz.8: DS: a11y таймера (TalkBack/liveRegion)

## Что сделано

Реализовано требование §3.1 Д2 (`docs/research/PROJECT-REVIEW-2026-08-28.md`) и брифа
`docs/DESIGN_BRIEF_SPEAKING_TRAINER.md` §3 (таймер — `liveRegion`/`stateDescription`,
обновление не чаще 1/5с):

1. **Токен в коде**: `SpeakingMotion.TimerAnnounceIntervalSeconds = 5`
   (`composeApp/designsystem/theme/SpeakingTokens.kt`) — соответствует
   `motion.timerAnnounceInterval: 5s` из `.docs/design-system/tokens.json`.
2. **`SpeakingTimerRing`** (`composeApp/.../app/components/SpeakingRecording.kt`) — единая точка
   для Training (176dp) и Practice (150dp), idle и recording:
   - **stateDescription**: цифры таймера несут «Осталось N секунд(ы/а)» (приватная
     `secondsPlural` — плюрализация) — читается TalkBack при фокусе на таймере.
   - **liveRegion**: визуально скрытый узел (1dp, alpha 0, `LiveRegionMode.Polite`) с текстом,
     квантованным вниз кратно 5с («Осталось 45 секунд» → «Осталось менее 5 секунд» →
     «Время вышло»). Текст меняется только на границах интервала → TalkBack анонсирует
     остаток раз в 5с без спама (кратные 5 в русском всегда «секунд»).
   - **Звук/вибро последних 5с** (`FINAL_COUNTDOWN_SECONDS = 5`): `LaunchedEffect(remainingSeconds)`
     при `remainingSeconds in 1..5` — `LocalHapticFeedback.performHapticFeedback(LongPress)` +
     `playTimerWarningSound()`. Idle-кольца (remaining == лимит ≥ 30с) сюда не попадают.
3. **expect/actual звука** `app/accessibility/TimerAlert.*.kt`:
   - Android: `ToneGenerator` (STREAM_NOTIFICATION, TONE_PROP_BEEP 120ms, lazy-синглтон);
   - iOS: `AudioServicesPlaySystemSound(1057)` («Tock»);
   - Desktop: `Toolkit.getDefaultToolkit().beep()`;
   - WASM: no-op (Web Audio не подключён, autoplay policy заблокировал бы звук без жеста;
     live-region работает через DOM).

## Изменённые/созданные файлы

- `composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/theme/SpeakingTokens.kt` (изм.)
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/components/SpeakingRecording.kt` (изм.)
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/accessibility/TimerAlert.kt` (новый)
- `composeApp/src/androidMain/kotlin/com/sotospeak/app/accessibility/TimerAlert.android.kt` (новый)
- `composeApp/src/iosMain/kotlin/com/sotospeak/app/accessibility/TimerAlert.ios.kt` (новый)
- `composeApp/src/desktopMain/kotlin/com/sotospeak/app/accessibility/TimerAlert.desktop.kt` (новый)
- `composeApp/src/wasmJsMain/kotlin/com/sotospeak/app/accessibility/TimerAlert.wasmJs.kt` (новый)
- `memory.md` (запись о решении)

Спеки/PRD не тронуты (ADR-007 не требовался — требование уже зафиксировано в брифе и ревью).

## Риски и решения

- **UI-тесты**: `mergeDescendants` НЕ использовался; скрытый узел без testTag; все запросы
  таймера в тестах идут с `useUnmergedTree = true` → дерево семантики не сломано.
  `onNodeWithText` в тестах не пересекается со строками анонсов.
- **WASM-гейт**: использованы только common API (`LocalHapticFeedback` из compose.ui),
  expect/actual покрывает все 4 платформенных source-set'а.

## Как проверить

- Гейты драйвера: `:composeApp:desktopTest`, `:composeApp:compileDebugKotlinAndroid`,
  `:composeApp:compileKotlinWasmJs --no-configuration-cache`.
- Ручная проверка (Android): включить TalkBack → Training/Practice → запись:
  анонсы остатка раз в 5с, фокус на цифрах читает «Осталось N секунд»,
  последние 5с — вибрация + короткий beep каждую секунду.

## Не сделано (осознанно, минимальный дифф)

- Настройка `hapticsEnabled` (SettingsViewModel) к финальному отсчёту не привязана —
  таймерный сигнал трактован как a11y-фидбек, а не UI-haptics. При желании — отдельной задачей.
- WASM-звук (Web Audio beep) — требует js-интеропа, оставлен no-op с комментарием.
