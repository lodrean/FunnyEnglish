# So to Speak Design System Specification

## Version 3.1.1 — Playful Coach × Material 3
**Date:** 2026-08-10
**Причина patch-версии:** errata DSM-5 §1.1 (утверждена владельцем 2026-08-08, M3_PIXEL_REPORT): dark `onPrimary`/`onSecondary` `#FFFFFF` → `#1A2F5E` (WCAG FAIL на `#8FB3F5`/`#B79EED`); ссылки на токены v1.3.0 → v1.3.1.
**Target:** Android (Compose Multiplatform) + Admin-web (React/MUI)
**Причина minor-версии:** новые компоненты `SpeakingAppBar` и `FadingEdgeText`, правило токенов статус-чипов (dark theme), список топиков по frame-topics. Дифф утверждён владельцем 2026-08-08 (`docs/plan/SPEC_DIFFS_TRANSCRIPT_APPBAR.md`, ADR-007). v3.0 — M3-редизайн (`docs/plan/M3_DESIGN_SYSTEM_SPEC_DIFF.md`).

---

## 1. Источники истины

| Артефакт | Роль |
|---|---|
| `.docs/design-system/tokens.json` **v1.3.1** | **Канонические токены** (HEX 1:1 обязателен во всех реализациях) |
| `.docs/design-system/mockups.html` **v2.0** | Эталонные мокапы экранов (15 фреймов, light+dark) — **поведенческие требования авторитетны** |
| `.docs/design-system/styleguide.html` **v2.0** | M3-компоненты в фирменной теме + brand-блок (rec-кнопка, таймер-кольцо, waveform) |
| `.docs/design-system/icons.svg` | Иконки (stroke 2 round / fill) |
| `docs/design/M3_REPLACEMENT_REGISTRY.md` | Реестр замен «компонент/экран → M3-аналог» (обязательный спутник спеки) |
| `docs/design/M3_IMPLEMENTATION_MAPPING.md` | Механический маппинг Compose M3 + MUI 6 (импровизация в коде запрещена) |
| `docs/DESIGN_BRIEF_SPEAKING_TRAINER.md` | Дизайн-бриф пивота |

## 2. Токены (v1.3.0)

### Цвета
| Токен | HEX | Назначение |
|---|---|---|
| primary | `#5B8DEF` | Основной акцент: иконки навигации, play-контролы, ссылки (БЕЗ белого текста!) |
| primaryStrong | `#3B6FD4` | Белый текст на кнопках/nav/CTA (WCAG AA); **= M3-роль `primary` в light-схеме** (см. §3) |
| primaryContainer | container-фон primary | Карточки, чипы, индикатор активного пункта |
| secondary | `#9B7EDE` | Фиолетовый акцент |
| record | `#FF9F6B` | Запись (≠ error!) |
| recordContainer / onRecordContainer | контейнер записи / тёмный текст на нём | Rec-элементы |
| error | `#E53935` | Ошибки (крупные элементы) |
| errorText | `#B3261E` | Мелкий текст ошибок (AA: 4.29:1 у #E53935 — FAIL) |
| background | `#EEF3FF` | Фон приложения |
| text | `#2D3561` | Основной текст |
| textMuted | `#58609A` | Вторичный текст (5.32:1, AA) |
| timer levels | `#4A7FE8` / `#8A68D6` / `#D97238` | Таймер Training L1/L2/L3, recordActive |
| status NEW / REVIEWED | container + тёмный текст | Чипы статусов (белый на #FB8C00 = 2.37:1 FAIL — запрещён) |

Полный список цветов (включая dark) — `tokens.json` v1.3.0; HEX 1:1, без вычислений «на глаз».

### M3-роли (v1.3.0)

Компонентная база — Material 3; M3-компонентам обязательны следующие роли (все значения — в tokens.json v1.3.0, `color.m3.*` / `color.dark.*`):

- **Surface-контейнеры** (light / dark): `surfaceContainerLowest #FFFFFF / #101424`, `Low #F6F8FF / #181D36`, `Container #E9EFFE / #1F2440`, `High #E2E9FB / #262B49`, `Highest #D8E2FA / #2B3152` (= surfaceVariant; трек таймер-кольца).
- **Outline**: `outlineVariant #D4DDF5 / #2E3556` (мягкие разделители, неактивные бордеры).
- **Inverse** (Snackbar): `inverseSurface = text`, `inverseOnSurface = background` (пара ≈11:1 AA), `inversePrimary #8FB3F5 / #3B6FD4` (action на инверсной поверхности).
- **Scrim** модалок/шторок: `#00000080`.
- **SurfaceTint**: `= primary` (`#5B8DEF` / `#8FB3F5`) — tonal elevation tint.
- **Тёмные контейнеры**: `primaryContainer #2E3E6E` (on `#DDE8FD`), `secondaryContainer #46366F` (on `#E5DCFF`), `recordContainer #59311C` (on `#FFD9C2`) — индикаторы активных пунктов, выбранные элементы, record-подложки в dark.
- **State layers** (alpha-оверлей цвета контента поверх контейнера; применяются и к brand-компонентам — rec-кнопка: оверлей onRecord): hover 8%, focus 12%, pressed 12%, dragged 16%; disabled: контент 38% / контейнер 12% от onSurface.
- **Tonal elevation**: level0–5 (1/3/6/8/12dp) через surfaceTint поверх surface — вместо кастомных теней (см. «Формы и тени»).

### Формы и тени
- Radius: 16 (кнопки/поля), **22 (squircle rec-кнопка)**, 12 (мелкие элементы/chip), 22 (card), 26 (card-large), 28 (sheet/dialog).
- M3 shapes-шкала: small=12, medium=16, large=22, extraLarge=28.
- Тени: **M3 tonal elevation** вместо кастомных теней; исключения — brand-тень rec-кнопки `0 4px 0 rgba(217,114,56,.55)` (pressed — `0 1px 0`) и focus-ring. Тень `card` (`0 1px 2px rgba(45,53,97,.06), 0 2px 8px rgba(45,53,97,.05)`) остаётся для ElevatedCard (level1) до полного перехода на tonal.
- Touch-таргеты: минимум 48dp.

### Типографика и иконки
- Шрифт: **Nunito** (admin-web MUI; composeApp — системный стек до подключения Nunito); моно — **JetBrains Mono** (tnum) для таймера/длительностей/метаданных.
- Размеры и веса — без изменений (tokens.json font.scale); **маппинг на M3 type roles** (display/headline/title/body/label) — обязательная таблица в `M3_IMPLEMENTATION_MAPPING.md` §2 (разработчик использует роль M3, не выбирает сам).
- Иконки: `design/.../icons/SpeakingIcons.kt` — 15 ImageVector из icons.svg (Home/Send/User для bottom nav, Lock, CheckCircle и др.).

### Motion
- **M3-easing**: `m3Standard cubic-bezier(0.2, 0, 0, 1)` — стандартные переходы; `m3Emphasized cubic-bezier(0.05, 0.7, 0.1, 1)` — экранные переходы; `m3DurationState 200ms` — state-анимации.
- **Brand-моушен** (остаётся): recPulse 1600ms, CheckPopAppear (overshoot scale 0.3→1.18→1.0, 500ms), `SpeakingMotion.EasingBounce`.
- Reduce-motion: `platformReduceMotionEnabled()` expect/actual (Android animator scale / iOS / wasm matchMedia / desktop false); brand-моушен гасится при Reduce motion.

## 3. Светлая/тёмная тема

**Правило M3 primary (WCAG):** в светлой теме M3-роль `primary` = **primaryStrong `#3B6FD4`**, потому что M3 кладёт `onPrimary` (белый) на `primary`; белый на `#5B8DEF` = 3.23:1 FAIL. «Красивый» `#5B8DEF` остаётся в `surfaceTint`, иконках навигации, play-контролах и ссылках — там, где на нём нет белого текста. В dark: `primary = #8FB3F5`. Тёмные контейнеры — по §2 «M3-роли».

### Admin-web
- Ключ localStorage: `sotospeak-theme-mode`.
- Значения: `light` | `dark`.
- Default: системное предпочтение (`prefers-color-scheme`).
- Toggle: иконка в правом верхнем углу `Header.tsx` (`LightMode`/`DarkMode`), вызывает `toggleTheme()` из `ThemeProvider`.
- Выбор применяется ко всей MUI-теме и сохраняется между сессиями.

### Приложение (composeApp)
- Ключ Settings: `theme_mode`.
- Значения: `system` | `light` | `dark`.
- Default: `system`.
- `SettingsScreen` предоставляет dropdown «Тема»; `SettingsViewModel` сохраняет выбор.
- `MainActivity` читает `theme_mode` до `setContent` и ставит системный splash-фон (`splash_background_light`/`splash_background_dark`).
- `App.kt` передаёт effective `darkTheme` в `SoToSpeakTheme`: `system` → `isSystemInDarkTheme()`.
- Выбор переживает configuration changes (rotation) через Settings.

## 4. Реализация

**Компонентная база — Material 3** (Compose Material3 / MUI 6). Кастомные компоненты DS 2.0 (`SpeakingPrimaryButton/Ghost/DangerGhost`, `SpeakingField`, кастом-карточки) заменяются стоковыми M3-компонентами — механический маппинг: `docs/design/M3_IMPLEMENTATION_MAPPING.md` (Compose §4, MUI §5); реестр соответствий: `docs/design/M3_REPLACEMENT_REGISTRY.md`. Новые компоненты сверх реестра не вводятся.

**Оставшийся brand-кастом** (осознанные фирменные элементы, состояния — по M3 state layers):
- B1 — rec-кнопка (squircle 22dp, тень `0 4px 0 rgba(217,114,56,.55)`);
- B2 — таймер-кольцо `SpeakingTimerRing` (уровни 80/50/30);
- B3 — waveform (RecordingWaveform/PlaybackWaveform);
- B4 — ThemeCover (градиент по хешу id + инициалы);
- B5/B6 — CheckPopAppear / recPulse (brand-моушен);
- B7 — REC-индикатор;
- B8 — `SpeakingAppBar` (v3.1): заголовок (titleMedium, ExtraBold) + breadcrumb-подзаголовок (labelSmall, textMuted, SemiBold), БЕЗ navigationIcon (мокап); «назад» — системный (`PlatformBackHandler`);
- B9 — `FadingEdgeText` (v3.1): перенос по словам (без разрыва слов), максимум 3 строки, при переполнении — затухание градиентом в цвет фона вместо многоточия; применяется в списках тем/топиков.

| Платформа | Файлы |
|---|---|
| `:design` (KMP DS) | `SoToSpeakColorScheme` (+ семантические record/timer/status/scrim), `SpeakingTypography`, `SpeakingElevation`, `SpeakingIcons.kt`, `SpeakingMotion` |
| composeApp (рабочая тема) | `designsystem/theme/SpeakingTokens.kt` (`SpeakingColors`/`LocalSpeakingColors`/`SpeakingTextStyles`/`SpeakingShapes`), M3-схема `speakingLightColorScheme/speakingDarkColorScheme` (DSM-5 §1.1), провайдер — `FunnyTheme.kt`; brand-компоненты `app/components/SpeakingRecording.kt` (B1–B3, B5–B7), `MockupVideoControls` |
| admin-web | `src/theme/Theme.ts` — палитра HEX 1:1 (light `primary.main = #3B6FD4` — правило §3), custom-палитра record/timer/status, Nunito, radius 16/22/12, M3-индикатор активного пункта сайдбара (pill primaryContainer) |

> В composeApp ТРИ темы: реально используется `designsystem/theme/FunnyTheme.kt`; `app/theme/` (Stitch) — мёртвый код; `:design` SoToSpeakTheme приложением не используется.

**Legacy DS 1.x (архив, решение владельца 2026-08-07, Q2/Q3 реестра):** gamification-компоненты (`FunnyXPCounter`, `FunnyStreakWidget`, `FunnyQuestCard`, `FunnyLevelProgress`, `FunnyAchievementBadge`, `ConfettiAnimation`) и дубликаты `FunnyButton`/`FunnyTextField` из `app/components/Common.kt` выведены из кодовой базы (архив — git-история); speaking-экранами не использовались.

## 5. Поведенческие требования (мокапы авторитетны)

- **Guest-first**: Splash → (первый запуск → Onboarding 3 слайда «Начать») → Library; регистрация только из авторизованной зоны (Practice-гейт SpeakingGate, гостевой профиль).
- **Training**: ровно 3 попытки на топик (лимиты 80→50→30 сек), попытка = одна запись на ВСЕ вопросы, без удаления — только прослушивание, авто-✅, финальные CTA.
- **Practice**: 30 сек, без Review — автостоп/ручной стоп → автоотправка; плашка автоотправки жёлтая ВНИЗУ.
- **Video**: кастомные контролы (big-play 64dp, play/seek/time/CC), mode-chips на экране (bottom-sheet удалён).
- **Record-кнопка** — squircle 22dp с recPulse, НЕ круг.
- **Навигация wide/desktop** (решение владельца 2026-08-07, Q4): compact → `NavigationBar`, medium/expanded → `NavigationRail` (канонический M3-паттерн адаптивности).

## 6. Доступность (WCAG)

- Чипы статусов — только container-фон + текст из токенов `statusNew`/`statusReviewed` (v3.1: dark-варианты `#FFB74D`/`#81C784` на контейнерах `#3D2A0A`/`#1B4D1F`; hardcoded `#256629`/`#8A5200` запрещены — нечитаемы в dark theme).
- record `#FF9F6B` ≠ error `#E53935` — не путать.
- Мелкий текст ошибок — `errorText #B3261E`.
- Белый текст на кнопках — только на `primaryStrong` и темнее (правило §3: light M3 `primary` = primaryStrong).
- Snackbar (inverse-пара v1.3.0): `inverseOnSurface` на `inverseSurface` ≈11:1 AA.
- Тёмные контейнеры v1.3.0 — пары on*Container → container проверены AA (см. tokens.json).
- State layers и focus-видимость — по M3 (§2).

## 7. Дизайн-конформити

Реестр расхождений приложение ↔ мокапы и E2E-прогоны (скриншоты, pixel-diff): `docs/qa/design-conformance/REPORT.md`. **Сверка — против v2.0-артефактов** (`mockups.html` v2.0, 15 фреймов light+dark; `styleguide.html` v2.0); процедура прежняя (аудит DC-1…DC-7 против v1.x закрыт 2026-08-01, эпик bd `So to Speak-qnr`). E2E-скрипты: `e2e-cmp/shoot-app2.js`, `shoot-app-auth.js`.

---

## Приложение A. Архив

- **v2.x (Playful Coach до M3)** — в git-истории файла (коммиты до 2026-08-07). Спека v2.0/2.1 описывала кастомную компонентную базу DS 2.0 (`Speaking*`-компоненты), заменённую стоковым M3 в v3.0.
- **v1.0 (2024, gamification-first)** — «Playful but Clear», аудитория 7–14 лет, gamification-компоненты, breakpoints 600/1200dp. Устарела с пивотом продукта (2026-08-01, bd `8tg.6`/`8tg.7`). Полный текст — в git-истории.
- **Gamification-компоненты DS 1.x** — удалены из кодовой базы 2026-08-07 (M3-редизайн, Q2); архив — git-история.

**Changelog:**
- v3.1.1 (2026-08-10): errata DSM-5 §1.1 (утв. владельцем 2026-08-08): dark `onPrimary`/`onSecondary` = `#1A2F5E`; токены v1.3.1; mockups v2.1 (тексты CTA frame-questions эталонно из приложения по Q5, replay-оверлей frame-video).
- v3.0 (2026-08-07): компонентная база Material 3; токены v1.3.0 (M3-роли, state layers, tonal elevation, M3-motion); правило light primary=primaryStrong; brand-кастом B1–B7; NavigationRail на wide (Q4); архивация legacy-геймификации (Q2) и дубликатов Common.kt (Q3). Дифф: `docs/plan/M3_DESIGN_SYSTEM_SPEC_DIFF.md` (утверждён владельцем).
- v2.1 (2026-08-02): добавлен §3 «Светлая/тёмная тема» — behavior для admin-web (`sotospeak-theme-mode`, toggle в Header) и приложения (`theme_mode`=`system`/`light`/`dark`, default `system`).
- v2.0 (2026-08-01): полная замена на Playful Coach v1.1/v1.2.0 (пивот в speaking-тренажёр; утверждено владельцем, bd `So to Speak-hxd`).
- v1.0 (2024-02-03): первоначальная gamification-first спека.
