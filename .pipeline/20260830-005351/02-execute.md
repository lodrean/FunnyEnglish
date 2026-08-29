# 02-execute — bd FunnyEnglish-2oz.6: DS: удалить зомби DS 1.x + расформировать :design

## Что сделано

1. **SpeakingIcons перенесён в designsystem.** `design/.../icons/SpeakingIcons.kt` → `composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/icons/SpeakingIcons.kt`, пакет `com.sotospeak.designsystem.icons`. Обновлены ссылки в 10 файлах (App.kt — fully-qualified, AppPreviews.kt, SpeakingRecording.kt, 7 экранов).
2. **Модуль `:design` расформирован.** Каталог `design/` удалён целиком (CustomIcons и остальные 33 файла — 0 потребителей, проверено грепом). Убраны: `include(":design")` из settings.gradle.kts (+ висячий комментарий), `implementation(projects.design)` из composeApp/build.gradle.kts, `api(project(":design"))` из core/build.gradle.kts и feature-home/build.gradle.kts (оба модуля не импортировали `com.sotospeak.design.*`), path-триггер `design/**` из `.github/workflows/cmp-e2e-tests.yml`.
3. **Удалены Funny*-зомби DS 1.x в composeApp/designsystem** (все с 0 потребителей вне designsystem):
   - `theme/FunnyColorScheme.kt` (FunnyColorScheme, Light/DarkFunnyColorScheme, LocalFunnyColorScheme, `funnyColors`);
   - весь каталог `components/` — FunnyButton, FunnyCard, FunnyTextField, FunnyBadge, FunnyProgress, FunnySnackbar;
   - `tokens/FunnyColors.kt` (палитра DS 1.x + `funnyLight/DarkColorScheme()`);
   - `animations/AnimationDurations.kt`, `animations/AnimationEasings.kt`, `animations/PageTransitions.kt` (FunnyAnimations/FunnyEasings + API переходов — 0 потребителей; единственный внутренний потребитель PageTransitions удалён вместе);
   - мёртвые функции `funnyShapes()` (FunnyShapes.kt) и `funnyTypography()` (FunnyTypography.kt) + их импорты `Shapes`/`Typography`. **Токен-валы (Space*, Shape*, Button*, Input*, Card*, Elevation*, NunitoFontFamily) НЕ тронуты — они живые, используются экранами и LoadingSkeleton/SpeakingPressable.**
4. **FunnyTheme.kt:** убрано провайдинг `LocalFunnyColorScheme` (комментарий DSM-5 обновлён). Сама тема (speaking-схемы) не менялась.
5. **core/ui удалён целиком** (`FunnyColors.kt`, `Theme.kt` с SoToSpeakTheme/LocalExtendedColors, components/{Buttons,Inputs,Feedback,Badges,Layout}.kt) — 0 потребителей во всём репо (D-6).
6. Комментарии: SpeakingTokens.kt (legacy-палитра удалена), LoginUserFlowTest.kt (упоминание FunnyTextField → «поле ввода»).
7. Запись в `memory.md` (раздел решений).

## Изменённые/созданные файлы

**Создан/перенесён:** `composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/icons/SpeakingIcons.kt`

**Изменены:**
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/App.kt` (только FQN иконок)
- `composeApp/src/androidMain/.../preview/AppPreviews.kt`, `.../app/components/SpeakingRecording.kt`, `.../app/screens/{Library,Onboarding,Practice,Questions,Topics,Training,Video}Screen.kt` (только import иконок)
- `composeApp/.../designsystem/theme/FunnyTheme.kt`, `.../theme/SpeakingTokens.kt` (комментарий), `.../tokens/FunnyShapes.kt`, `.../tokens/FunnyTypography.kt`
- `composeApp/src/commonTest/.../LoginUserFlowTest.kt` (комментарий)
- `settings.gradle.kts`, `composeApp/build.gradle.kts`, `core/build.gradle.kts`, `feature-home/build.gradle.kts`, `.github/workflows/cmp-e2e-tests.yml`
- `memory.md`

**Удалены:** каталог `design/` (модуль, 34 kt + build + stitch-prompt.md); `designsystem/theme/FunnyColorScheme.kt`; `designsystem/tokens/FunnyColors.kt`; `designsystem/animations/{AnimationDurations,AnimationEasings,PageTransitions}.kt`; `designsystem/components/` (6 файлов); `core/src/commonMain/kotlin/com/sotospeak/core/ui/` (7 файлов).

## Как проверить

Гейты драйвера: `:composeApp:desktopTest`, `:composeApp:compileDebugKotlinAndroid`, `:composeApp:compileKotlinWasmJs --no-configuration-cache`.

Статически проверено грепом (без запуска gradle): 0 ссылок на `LocalFunnyColorScheme|FunnyColorScheme|FunnyButton|FunnyCard|FunnyTextField|FunnyBadge|FunnySnackbar|FunnyProgress|AnimationDurations|PageTransition|com.sotospeak.core.ui|com.sotospeak.design.|projects.design|CustomIcons|funnyShapes|funnyTypography` в исходниках; 0 ссылок на `:design` в gradle/CI; 10 файлов используют новый пакет `designsystem.icons.SpeakingIcons`.

## Замечания

- `designsystem/animations/LoadingSkeleton.kt` и `accessibility/ReduceMotionProvider.kt` (FunnyAnimationSpecs — 1 потребитель в app) оставлены: у LoadingSkeleton нет потребителей, но он не Funny*-зомби из скоупа D-6 и не зависит от удалённого — кандидат на отдельную чистку.
- docs/ARCHITECTURE.md и docs/research/* упоминают `:design`/Funny* — справочные/аудитные документы, не трогал (спеки по ADR-007 не правятся).
