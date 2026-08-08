# DSM-5: Маппинг реализации — Material 3 (Compose M3 + MUI 6)

**Версия:** 1.0 · **Дата:** 2026-08-07 · **Тикет:** bd `FunnyEnglish-dmb` (P2) · **Этап:** DSM-5 по `docs/design/M3_REDESIGN_TASK.md`

**Назначение:** реализация по этому документу — механическая, без дизайн-решений в коде. Каждая строка реестра `docs/design/M3_REPLACEMENT_REGISTRY.md` (DSM-1) получает конкретный компонент **Compose Material3** (`androidx.compose.material3.*`) и **MUI 6** (+ точку настройки в `admin-web/src/theme/Theme.ts`).

**Источники значений:** `.docs/design-system/tokens.json` **v1.3.0** (цвета — HEX 1:1, не вычислять в коде «на глаз»), `mockups.html` v2.0, `styleguide.html` v2.0.

**Существующая база:** Compose — `designsystem/theme/SpeakingTokens.kt` (`SpeakingColors`, light/dark) поверх `FunnyTheme`; MUI — `Theme.ts` (palette light/dark L261/L544, components-overrides L312/L615). Код в рамках этой задачи **не меняется** (§8.3 брифа) — документ для отдельного плана реализации после утверждения дизайна.

---

## 1. Тема: цвета (color scheme)

### 1.1. Compose Material3 — `MaterialTheme.colorScheme`

Расширить `SpeakingTokens.kt` → свести к `lightColorScheme(...)` / `darkColorScheme(...)` (brand/semantic-роли остаются в `SpeakingColors` как расширение через `staticCompositionLocalOf`, M3-роли — в стандартной схеме):

| M3 colorScheme-роль | Light (HEX) | Dark (HEX) | Токен |
|---|---|---|---|
| `primary` | `#3B6FD4` ⚠️ | `#8FB3F5` | brand.primaryStrong / dark.primary |
| `onPrimary` | `#FFFFFF` | `#FFFFFF` | brand.onPrimary |
| `primaryContainer` | `#DDE8FD` | `#2E3E6E` | brand.primaryContainer / dark.* |
| `onPrimaryContainer` | `#1A2F5E` | `#DDE8FD` | brand.onPrimaryContainer / dark.* |
| `secondary` | `#9B7EDE` | `#B79EED` | brand.secondary / dark.secondary |
| `onSecondary` | `#FFFFFF` | `#FFFFFF` | brand.onSecondary |
| `secondaryContainer` | `#E5DCFF` | `#46366F` | brand.secondaryContainer / dark.* |
| `onSecondaryContainer` | `#5B3FA8` | `#E5DCFF` | brand.onSecondaryContainer / dark.* |
| `tertiary` | `#006C4C` | `#006C4C` | brand.tertiary (dark-override не требуется: белый на нём 5.9:1) |
| `onTertiary` | `#FFFFFF` | `#FFFFFF` | brand.onTertiary |
| `error` | `#E53935` | `#E53935` | semantic.error |
| `onError` | `#FFFFFF` | `#FFFFFF` | semantic.onError |
| `background` | `#EEF3FF` | `#161A2E` | neutral.background / dark.background |
| `onBackground` | `#2D3561` | `#E8EAF6` | neutral.text / dark.text |
| `surface` | `#FFFFFF` | `#1F2440` | neutral.surface / dark.surface |
| `onSurface` | `#2D3561` | `#E8EAF6` | neutral.text / dark.text |
| `surfaceVariant` | `#D8E2FA` | `#2B3152` | neutral.surfaceVariant / dark.surfaceVariant |
| `onSurfaceVariant` | `#58609A` | `#9AA0C4` | neutral.textMuted / dark.textMuted |
| `surfaceContainerLowest` | `#FFFFFF` | `#101424` | m3.* (v1.3.0) |
| `surfaceContainerLow` | `#F6F8FF` | `#181D36` | m3.* (v1.3.0) |
| `surfaceContainer` | `#E9EFFE` | `#1F2440` | m3.* (v1.3.0) |
| `surfaceContainerHigh` | `#E2E9FB` | `#262B49` | m3.* (v1.3.0) |
| `surfaceContainerHighest` | `#D8E2FA` | `#2B3152` | m3.* (v1.3.0) |
| `outline` | `#B9C7EE` | `#3D4568` | neutral.outline / dark.outline |
| `outlineVariant` | `#D4DDF5` | `#2E3556` | m3.* (v1.3.0) |
| `inverseSurface` | `#2D3561` | `#E8EAF6` | m3.* (v1.3.0) |
| `inverseOnSurface` | `#EEF3FF` | `#2D3561` | m3.* (v1.3.0) |
| `inversePrimary` | `#8FB3F5` | `#3B6FD4` | m3.* (v1.3.0) |
| `scrim` | `#00000080` | `#00000080` | m3.scrim |
| `surfaceTint` | `#5B8DEF` | `#8FB3F5` | m3.surfaceTint (= brand.primary / dark.primary) |

> ⚠️ **Ключевое правило (WCAG):** M3-роль `primary` в светлой теме = **primaryStrong #3B6FD4**, потому что M3 по умолчанию кладёт `onPrimary` (белый) на `primary`. «Красивый» `#5B8DEF` остаётся в `surfaceTint`, иконках навигации, play-контролах и ссылках — там, где на нём нет белого текста. Это прямое следствие аудита 2026-08-01 (белый на `#5B8DEF` = 3.23:1 FAIL).

**Brand-расширение (остаётся в `SpeakingColors`, не в M3-схеме):** `record #FF9F6B` (dark `#FFB27D`), `onRecord #2D3561`, `recordActive/recordShadow #D97238`, `recordContainer #FFE3D1` (dark `#59311C`), `onRecordContainer #8A3B0E` (dark `#FFD9C2`), `waveformPlayback #5B8DEF`, `timerLevel80/50/30`, `statusNew(+Container)`, `statusReviewed(+Container)`, `scrimSubtitle #000000B3`, `scrimVideoControls #00000080`, `errorText #B3261E`.

### 1.2. MUI 6 — `Theme.ts` palette

| MUI palette | Light | Dark | Комментарий |
|---|---|---|---|
| `primary.main` | `#3B6FD4` | `#8FB3F5` | то же правило primaryStrong ⚠️ |
| `primary.light` | `#5B8DEF` | `#8FB3F5` | «красивый» primary — иконки/ссылки |
| `secondary.main` | `#9B7EDE` | `#B79EED` | |
| `success.main` | `#43A047` | `#43A047` | статус REVIEWED |
| `warning.main` | `#FB8C00` | `#FB8C00` | статус NEW (только large/графика) |
| `error.main` | `#E53935` | `#E53935` | |
| `background.default` | `#EEF3FF` | `#161A2E` | |
| `background.paper` | `#FFFFFF` | `#1F2440` | |
| `text.primary` | `#2D3561` | `#E8EAF6` | |
| `text.secondary` | `#58609A` | `#9AA0C4` | |
| `divider` | `#D4DDF5` | `#2E3556` | = outlineVariant (v1.3.0) |
| custom-объект темы (уже есть в Theme.ts) | record/timer/status/scrim | + dark-контейнеры v1.3.0 | расширить `recordContainer`-парой dark |

---

## 2. Тема: типографика (M3 type scale)

| Токен DS (px/weight) | Compose M3 роль | MUI 6 вариант |
|---|---|---|
| timerDisplay 64 · mono tnum · 700 | `displayLarge` (override fontFamily=JetBrains Mono, `fontFeatureSettings="tnum"`) | `typography.h3` + `fontFamily: 'JetBrains Mono'`, `fontVariantNumeric: 'tabular-nums'` |
| headlineSmall 31 · 800 | `headlineSmall` | `typography.h4` (override 31px/800) |
| questionText 25/1.35 · 600 | `titleLarge` | `typography.h6` (override 25px/600) |
| titleMedium 20 · 800 | `titleMedium` | `typography.subtitle1` (override 20px/800) |
| subtitleText 17/1.4 · 600 | `bodyLarge` (на scrim) | `typography.body1` (override 17px) |
| bodyMedium 16 · 400 | `bodyLarge` | `typography.body1` |
| bodySmall 14 · 400 | `bodyMedium` | `typography.body2` |
| labelSmall 12 · 800 · caps | `labelSmall` | `typography.caption` (override 800, uppercase, letterSpacing .06em) |
| timestamps · mono tnum | `labelMedium` (mono) | `typography.overline` (mono, tnum) |

**Shape-тема:** Compose `Shapes(small=12dp(chip), medium=16dp(button), large=22dp(card), extraLarge=28dp(sheet/dialog))`; cardLarge 26 и recorder 22 (squircle) — brand-расширение. MUI: `shape.borderRadius` базовый 16 + per-component overrides (Card 22, Dialog 28, Chip 12).

---

## 3. Тема: motion и state

| Аспект | Compose M3 | MUI 6 |
|---|---|---|
| State layers | встроены в M3-компоненты (hover 8/focus 12/pressed 12); для brand rec-кнопки — вручную: `Modifier.background(onRecord.copy(alpha=…))`-оверлей по `interactionSource` | встроены (`action.hover` 0.08, `action.selected` 0.12); проверить overrides MuiButton не гасят |
| Standard easing | `CubicBezierEasing(0.2f, 0f, 0f, 1f)` | `transitions.easing.easeInOut` override → `cubic-bezier(0.2,0,0,1)` |
| Emphasized easing | `CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)` — экранные переходы (замена `PageTransitions`) | `transitions.easing.sharp` |
| State duration 200ms | `tween(200)` | `transitions.duration.short` |
| Brand recPulse 1600ms / CheckPopAppear 500ms overshoot | остаются (`SpeakingMotion`), при `LocalReduceMotion` — статика | CSS keyframes в компоненте плеера (уже есть), `prefers-reduced-motion` в tokens.css |
| Tonal elevation | `surfaceColorAtElevation(level1..5)` = 1/3/6/8/12dp + `surfaceTint` | `shadows` уже есть; Paper с `elevation` + тональный оверлей не нужен — цвета surfaceContainer* заданы явно |

---

## 4. Компонентный маппинг — приложение (Compose)

| Реестр | Текущий | Compose Material3 | Ключевые параметры (механически) |
|---|---|---|---|
| C1 | `SpeakingPrimaryButton` | `Button` (Filled) | `colors=ButtonDefaults.buttonColors(containerColor=primary /*=primaryStrong*/)`; shape=medium(16); `Modifier.height(56.dp)` для CTA |
| C2 | `SpeakingGhostButton` | `OutlinedButton` | `border=BorderStroke(1.dp, outline)`; shape=medium |
| C3 | `SpeakingDangerGhostButton` | `OutlinedButton` | `contentColor=error`, border `error` |
| C4 | `SpeakingTextLink` | `TextButton` | `contentColor=primary`(=#3B6FD4) |
| C5 | `SpeakingField` | `OutlinedTextField` | `label={}` (в бордере), `isError`, `supportingText` (errorText #B3261E), shape=medium, focused border 2dp primary |
| C6 | `SpeakingGate` | `Card`(Filled) + `Button`/`TextButton` | container=`surfaceContainerHigh`, shape=large(22) |
| C7 | `LockedFeature` | `Card`(Filled) + `Icon` + `TextButton` | как C6 |
| C8 | `MergeProgressDialog` | `AlertDialog` + `LinearProgressIndicator` | shape=extraLarge(28), container=`surfaceContainerHigh` |
| C9 | `LoadingIndicator` | `CircularProgressIndicator` | `color=primary` |
| C10 | `ErrorMessage` | `Snackbar` / inline row | Snackbar: container=`inverseSurface`, action=`inversePrimary` |
| C11/C12 | дубликаты в `Common.kt` | удалить при реализации (Q3) | — |
| C13 | `speakingPressable` | остаётся; duration → `tween(200, easing=Standard)` | brand micro-motion |
| B1 | `SpeakingRecordButton` | **brand-кастом** | squircle `RoundedCornerShape(22.dp)`, размер 72/56, тень — `Modifier.drawBehind`/offset-тень `0 4px 0 rgba(217,114,56,.55)`, pressed → `0 1px 0`; state layers: оверлей `onRecord` 8/12% по `interactionSource`; disabled 12/38% от `onSurface` |
| B2 | `SpeakingTimerRing` | **brand-кастом** (Canvas) | track=`surfaceContainerHighest`, уровни timer80/50/30, переход цвета `tween(300, Standard)`; цифры mono tnum |
| B3 | `RecordingWaveform`/`PlaybackWaveform` | **brand-кастом** (Canvas/Row) | цвета recordActive / waveformPlayback |
| B4 | `ThemeCover` | **brand-кастом** | градиент по хешу id + инициалы (без изменений) |
| B5/B6 | `CheckPopAppear`, `recPulse` | **brand-моушен** | `spring`/`keyframes`: scale 0.3→1.18→1.0, 500ms; pulse 1600ms |
| B7 | `RecIndicator` | `Badge` + brand-точка | контейнер `recordContainer` |
| Навигация | bottom nav в `App.kt` | `NavigationBar` + `NavigationBarItem` | container=`surfaceContainer`; индикатор — дефолтный M3 pill (`primaryContainer`); `selectedIconColor=onPrimaryContainer`, текст активного пункта — `onSurface`, неактивные — `onSurfaceVariant` (по M3); лейблы «Темы/Отправки/Профиль» без изменений |
| Навигация wide | `WindowSize.kt` | `NavigationRail` (Q4 — решение владельца) | compact→NavigationBar, medium/expanded→Rail |
| Экраны A1–A15 | композиция вышеуказанного | `Scaffold` + слоты | карточки: Library→`ElevatedCard`(level1, shape large 22); вопросы/плашки→`Card`(Filled, `surfaceContainerHigh`); статистика профиля→`OutlinedCard`(`outlineVariant`); чипы статусов→`AssistChip`; режимы субтитров→`FilterChip`; списки→`ListItem` (min 56dp); прогресс темы→`LinearProgressIndicator` (4dp, track=`surfaceContainerHighest`) |

---

## 5. Компонентный маппинг — admin-web (MUI 6)

| Реестр | Компонент | MUI 6 | Точка настройки в Theme.ts |
|---|---|---|---|
| E5/W3/W5/W7/W9/W11 | Таблицы (DataTable, Users, Logs) | `Table`/`TableRow`/`TableCell` | `MuiTableRow` hover → `action.hover` (L406/L697 уже есть — проверить alpha 0.08) |
| E6/W7 | StatusBadge | `Chip` | `MuiChip` (L482): radius 12; цвета — newContainer/reviewedContainer + тёмный текст (custom variants `status="new"|"reviewed"`) |
| E7/W2/W10 | StatsCard / панели | `Card` | `MuiCard` (L360/L668): radius 22, `elevation=1`; outlined-вариант — `variant="outlined"` с `outlineVariant` |
| E9/W1/W4 | FormField | `TextField variant="outlined"` | `MuiTextField` (L461/L754): label в бордере (дефолт outlined), focused 2px primary, error+helperText |
| E10 | SearchInput | `TextField` + `InputAdornment` | те же overrides |
| E11 | RichTextEditor | кастом-ядро + обрамление | outlined container: border 1px `outline`, radius 16 |
| E12/W8 | FormActions / действия | `Button variant="contained"` + `variant="text"` | `MuiButton` (L313/L617): contained → `primary.main`(#3B6FD4), radius 16, text-transform none, weight 800 |
| E13/E14/W6 | ImageUploader / MediaUploader | кастом-dropzone | Paper variant outlined + hover `action.hover` |
| E15 | Toast | `Snackbar` + `Alert` | `MuiSnackbar`: anchor bottom-center; `MuiAlert` radius 12 |
| E16 | ConfirmDialog | `Dialog` | `MuiDialog` (L521/L793): radius 28, container `surfaceContainerHigh` |
| E17/E18 | ErrorBoundary / ErrorDisplay | `Card` + `Button` / inline `Typography color="error"` | — |
| E2/W-нав | Sidebar | `Drawer` + `ListItemButton` | `MuiListItemButton` (L430/L723): selected → pill: radius 999, bg `primaryContainer`(#DDE8FD / dark #2E3E6E), color `onPrimaryContainer` — **это и есть M3-индикатор активного пункта** |
| E3 | Header | `AppBar` | `MuiAppBar` (L415/L706): bg `surfaceContainer`, без тени |
| E4 | Breadcrumbs | `Breadcrumbs` | токены цвета текста |
| E8 | SkeletonCard | `Skeleton` | `MuiSkeleton` (L529/L802): bg `surfaceContainerHigh` |
| E21 | GradingNavBadge | `Badge` | `MuiBadge`: color warning (#FB8C00) — графика ≥3:1, ок |
| E22/W8 | RubricForm | `Slider` ×4 | `MuiSlider` (новый override): track 6px radius 3, thumb 22px primary, value — крупный `titleMedium` справа (по DC-1 G2) |
| E23/W8 | SubmissionAudioPlayer | **brand-кастом** (waveform 56 баров, played/unplayed, seek) + `IconButton` | цвета waveformPlayback/surfaceContainerHighest из токенов; кнопка play — `IconButton` contained-стиля primaryStrong |
| E24/W6 | TopicQuestionsEditor | `List` + `Dialog` | стандартные overrides |
| W2 | Dashboard progress | `LinearProgressIndicator` | track `surfaceContainerHighest`, bar `primary` |
| Grading avg-панель | avg-box | `Card`(Filled) | bg `secondaryContainer` (#E5DCFF), текст `onSecondaryContainer` (по DC-1 G3) |

**Сквозное для MUI:** `MuiButton` — отключить `text-transform: uppercase` (Nunito 800 уже даёт акцент); `shape.borderRadius`=16; `transitions.easing/duration` — по §3; dark-контейнеры v1.3.0 (primaryContainer #2E3E6E и пр.) — в custom-объект темы для `MuiListItemButton selected` и `MuiChip` variants в dark-режиме.

---

## 6. Правило «механичности»

1. Любое значение цвета/размера/радиуса/длительности берётся из tokens.json v1.3.0 (или из таблиц §1–§3 этого документа) — если нужного значения нет, это вопрос владельцу (§8.4), а не повод подобрать в коде.
2. Новые компоненты сверх реестра DSM-1 не вводятся.
3. Brand-кастом — только B1–B7 (§4 таблицы выше) и ThemeCover/видео-контролы; всё остальное — стоковые M3/MUI компоненты + theme overrides.
4. Поведение экранов (3 попытки, автоотправка, guest-first) реализацией не пересматривается — источник: мокапы v2.0, аннотации §3.2 брифа.

---

## Ченджлог

- **2026-08-07 · v1.0** — Первая версия маппинга: цвета (Compose colorScheme + MUI palette), type scale, motion/state, 24 строки Compose, 25 строк MUI. Ключевое правило: light `primary` = primaryStrong #3B6FD4 (WCAG).
