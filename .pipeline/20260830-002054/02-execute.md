# 02-execute — bd FunnyEnglish-2oz.3 «DS: Nunito в Compose + JetBrains Mono в админке»

## Что сделано

1. **Bundled Nunito в Compose (composeResources)**
   - Скачаны полные статические TTF Nunito (Google Fonts API, v32; веса 400/500/600/700/800, с кириллицей) в `composeApp/src/commonMain/composeResources/font/`.
   - `FunnyTypography.kt:27`: `NunitoFontFamily = FontFamily.SansSerif` заменён на `@Composable`-геттер, собирающий `FontFamily` из `Res.font.nunito_*` (Normal/Medium/SemiBold/Bold/ExtraBold). Зависимые top-level TextStyle-валы и `funnyTypography()` переведены на `@Composable`-геттеры (вне файла они нигде не используются — call-сайтов не сломано).
   - `SpeakingColorScheme.kt`: `speakingTypography()` стал `@Composable`; всем не-mono стилям (headlineSmall/titleLarge/titleMedium/bodyLarge/bodyMedium/labelSmall/labelLarge) задан `fontFamily = NunitoFontFamily`. Mono-стили (displayLarge — таймер, labelMedium — таймстемпы) оставлены `FontFamily.Monospace` + tnum по спеке (timerDisplay mono tnum).
   - `SpeakingTokens.kt`: `SpeakingTextStyles.QuestionText`/`SubtitleText` получили `fontFamily = NunitoFontFamily` (@Composable-геттеры); `TimerDisplay` оставлен Monospace. Единственный call-сайт `speakingTypography()` (`FunnyTheme.kt`) уже в composable-контексте — не менялся.
   - wasm-dist: `build-wasm-distribution.gradle.kts` уже копирует `composeResources/**` — шрифты попадут в wasm-сборку автоматически.

2. **JetBrains Mono в админке**
   - `admin-web/src/index.css`: `@import` Google Fonts расширен `family=JetBrains+Mono:wght@400;600;700` (используется в `Theme.ts` h3/overline с `tabular-nums`, веса 600/700). Раньше шрифт был упомянут в font-family, но не импортировался — рендерился fallback monospace.

## Изменённые/созданные файлы

- `composeApp/src/commonMain/composeResources/font/nunito_regular.ttf` (новый)
- `composeApp/src/commonMain/composeResources/font/nunito_medium.ttf` (новый)
- `composeApp/src/commonMain/composeResources/font/nunito_semibold.ttf` (новый)
- `composeApp/src/commonMain/composeResources/font/nunito_bold.ttf` (новый)
- `composeApp/src/commonMain/composeResources/font/nunito_extrabold.ttf` (новый)
- `composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/tokens/FunnyTypography.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/theme/SpeakingColorScheme.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/theme/SpeakingTokens.kt`
- `admin-web/src/index.css`
- `memory.md` (запись в «Решения и договорённости»)

## Как проверить (гейты драйвера)

- `./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:desktopTest`
- `./gradlew :composeApp:compileKotlinWasmJs --no-configuration-cache`
- Визуально: текст приложения (вопросы, кнопки, заголовки) рендерится Nunito на всех платформах; таймер — mono.
- Админка: `cd admin-web && npm run dev` → h3 (таймеры/длительности) и overline (таймстемпы) рендерятся JetBrains Mono (Network: fonts.gstatic.com jetbrains-mono).

## Замечания

- Скриншот-эталоны Dropshots и pixel-diff e2e могут дать diff из-за смены гарнитуры — ожидаемо; эталоны переснимать по решению драйвера/владельца.
- `OpenDyslexicFontFamily` (FunnyTypography.kt) оставлен placeholder'ом — вне скоупа задачи.
