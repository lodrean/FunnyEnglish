# 02-execute — bd FunnyEnglish-2oz.1: DS errata dark-ролей в extended-палитре (WCAG FAIL)

## Что сделано

1. **`SpeakingColors`**: возвращены/добавлены поля `onSecondary` и `onSecondaryContainer` (extended-палитра приведена к M3-схеме из `SpeakingColorScheme.kt`, значения HEX 1:1).
2. **`LightSpeakingColors`**: `onSecondary = #FFFFFF`, `onSecondaryContainer = #5B3FA8`.
3. **`DarkSpeakingColors`** (errata dark-ролей): переопределены `onPrimary = #1A2F5E`, `onSecondary = #1A2F5E` (белый на #8FB3F5/#B79EED = ~2.2:1 FAIL), добавлен `onSecondaryContainer = #E5DCFF` (v1.3.0 M3 dark).
4. **Аватар профиля** (`ProfileScreen.kt`): переведён с `secondary` + белые инициалы (в dark #B79EED + #FFFFFF ≈ 2.2:1 FAIL) на контейнерную пару `secondaryContainer` / `onSecondaryContainer` — AA в обеих темах (light: #5B3FA8 на #E5DCFF ≈ 7:1; dark: #E5DCFF на #46366F ≈ 8:1).

Проверка использований: `speaking.onPrimary` использовался только в `ProfileScreen.kt` (строка 143 — заменена; строка 431 — `onPrimaryContainer`, не затронута). Других конструкторов `SpeakingColors(...)` в проекте нет — добавление полей ничего не ломает.

## Изменённые файлы

- `composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/theme/SpeakingTokens.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/ProfileScreen.kt`

Созданных файлов нет. Спеки/PRD не тронуты (правок не потребовалось — значения уже зафиксированы в M3-схеме и отчёте §3.1 Д1).

## Как проверить

- Гейты драйвера: `./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinWasmJs :composeApp:desktopTest --no-configuration-cache` (сам не запускал — по ограничению задачи).
- Визуально: экран профиля в dark/light — аватар на фиолетовой подложке-контейнере, инициалы контрастные; контраст #1A2F5E на #8FB3F5/#B79EED ≈ 7:1 (AA/AAA).
