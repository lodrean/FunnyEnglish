# So to Speak Design System Specification

## Version 2.1 — Playful Coach (Speaking-тренажёр)
**Date:** 2026-08-02
**Target:** Android (Compose Multiplatform) + Admin-web (React/MUI)
**Причина major-версии:** пивот продукта So to Speak → Speaking-тренажёр (2026-07-30, эпик bd `So to Speak-8tg`). Gamification-first спека v1.0 (2024, аудитория 7–14 лет) заменена дизайн-системой **Playful Coach v1.1** (выбрана владельцем 2026-07-31, вариант B) + аудит-токены v1.2.0 (2026-08-01).

---

## 1. Источники истины

| Артефакт | Роль |
|---|---|
| `.docs/design-system/tokens.json` | **Канонические токены** (HEX 1:1 обязателен во всех реализациях) |
| `.docs/design-system/mockups.html` | Эталонные мокапы экранов — **поведенческие требования авторитетны** |
| `.docs/design-system/styleguide.html` | Компоненты (`.field/.btn-danger-ghost/.link-btn/.onb-dots` и пр.) |
| `.docs/design-system/icons.svg` | Иконки (stroke 2 round / fill) |
| `docs/DESIGN_BRIEF_SPEAKING_TRAINER.md` | Дизайн-бриф пивота |

## 2. Токены (v1.1 + аудит v1.2.0)

### Цвета
| Токен | HEX | Назначение |
|---|---|---|
| primary | `#5B8DEF` | Основной акцент |
| primaryStrong | `#3B6FD4` | Белый текст на кнопках/nav/CTA (WCAG AA) |
| primaryContainer | container-фон primary | Карточки, чипы |
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

### Формы и тени
- Radius: 16 (кнопки/поля), **22 (squircle rec-кнопка)**, 12 (мелкие элементы), 26 (card-large).
- Тени: индиго-оттенок; rec-кнопка — жёсткая тень (offset вниз), pressed — прижата (`SpeakingElevation`).
- Touch-таргеты: минимум 48dp (vc-btn, mode-chip).

### Типографика и иконки
- Шрифт: **Nunito** (admin-web MUI; composeApp — системный стек до подключения Nunito).
- `SpeakingTypography`, `SpeakingTextStyles` (в т.ч. `TimerDisplay`).
- Иконки: `design/.../icons/SpeakingIcons.kt` — 15 ImageVector из icons.svg (Home/Send/User для bottom nav, Lock, CheckCircle и др.).

### Motion
- `SpeakingMotion`: easingStandard/easeBounce, длительности 150/300/500, recPulse 1600ms.
- Reduce-motion: `platformReduceMotionEnabled()` expect/actual (Android animator scale / iOS / wasm matchMedia / desktop false).

## 3. Светлая/тёмная тема

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

| Платформа | Файлы |
|---|---|
| `:design` (KMP DS) | `SoToSpeakColorScheme` (+ семантические record/timer/status/scrim), `SpeakingTypography`, `SpeakingElevation`, `SpeakingIcons.kt`, `SpeakingMotion` |
| composeApp (рабочая тема) | `designsystem/theme/SpeakingTokens.kt` (`SpeakingColors`/`LocalSpeakingColors`/`SpeakingTextStyles`/`SpeakingShapes`), провайдер — `FunnyTheme.kt`; компоненты `app/components/SpeakingAuth.kt`, `SpeakingRecording.kt` (RecIndicator, CheckPopAppear, waveform), `SpeakingRecordButton` (squircle 22dp), `SpeakingTimerRing` (Training 176dp / Practice 150dp), `MockupVideoControls` |
| admin-web | `src/theme/Theme.ts` — полный ребренд (HEX 1:1, палитра `speaking` record/timer/status, Nunito, radius 16/22/12, индиго-тени) |

> В composeApp ТРИ темы: реально используется `designsystem/theme/FunnyTheme.kt`; `app/theme/` (Stitch) — мёртвый код; `:design` SoToSpeakTheme приложением не используется.

## 5. Поведенческие требования (мокапы авторитетны)

- **Guest-first**: Splash → (первый запуск → Onboarding 3 слайда «Начать») → Library; регистрация только из авторизованной зоны (Practice-гейт SpeakingGate, гостевой профиль).
- **Training**: ровно 3 попытки на топик (лимиты 80→50→30 сек), попытка = одна запись на ВСЕ вопросы, без удаления — только прослушивание, авто-✅, финальные CTA.
- **Practice**: 30 сек, без Review — автостоп/ручной стоп → автоотправка; плашка автоотправки жёлтая ВНИЗУ.
- **Video**: кастомные контролы (big-play 64dp, play/seek/time/CC), mode-chips на экране (bottom-sheet удалён).
- **Record-кнопка** — squircle 22dp с recPulse, НЕ круг.

## 6. Доступность (WCAG)

- Чипы статусов — только container-фон + тёмный текст (9.2/8.7:1 AA).
- record `#FF9F6B` ≠ error `#E53935` — не путать.
- Мелкий текст ошибок — `errorText #B3261E`.
- Белый текст на кнопках — только на `primaryStrong` и темнее.

## 7. Дизайн-конформити

Реестр расхождений приложение ↔ мокапы и E2E-прогоны (скриншоты, pixel-diff): `docs/qa/design-conformance/REPORT.md` (аудит DC-1…DC-7, эпик bd `So to Speak-qnr` — закрыт 2026-08-01, 29 расхождений реализованы). E2E-скрипты: `e2e-cmp/shoot-app2.js`, `shoot-app-auth.js`.

---

## Приложение A. Архив: v1.0 (2024, gamification-first)

Спека v1.0 («Playful but Clear», аудитория 7–14 лет, gamification-компоненты, breakpoints 600/1200dp) устарела вместе с пивотом продукта и удалением gamification-фич из приложения (2026-08-01, bd `8tg.6`/`8tg.7`). Полный текст — в git-истории файла (коммиты до 2026-08-01). Gamification-компоненты в `:design` сохранены как библиотечный код, но приложением не используются.

**Changelog:**
- v2.1 (2026-08-02): добавлен §3 «Светлая/тёмная тема» — behavior для admin-web (`sotospeak-theme-mode`, toggle в Header) и приложения (`theme_mode`=`system`/`light`/`dark`, default `system`).
- v2.0 (2026-08-01): полная замена на Playful Coach v1.1/v1.2.0 (пивот в speaking-тренажёр; утверждено владельцем, bd `So to Speak-hxd`).
- v1.0 (2024-02-03): первоначальная gamification-first спека.
