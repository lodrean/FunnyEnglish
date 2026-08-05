# DESIGN BRIEF: M3-редизайн So to Speak (Material 3 + фирменная палитра)

> Статус: на утверждении владельца · Дата: 2026-08-05 · Автор: по решению владельца
> Исполнитель: дизайн-агент (Kimi, Figma MCP / HTML-мокапы — как итерации DS 1.x–2.x)

## 1. Цель

Пересобрать дизайн-проект приложения и админки на основе **рекомендаций Material 3**
(компоненты, паттерны, motion, доступность), **сохранив текущую фирменную цветовую
гамму** Playful Coach (tokens v1.2.0). Результат заменяет текущий дизайн-проект
(`.docs/design-system/mockups.html`, `styleguide.html`) новой версией.

Это НЕ смена бренда и НЕ смена UX-флоу: пользовательские сценарии, поведение
экранов и цвета остаются, меняется компонентная база и визуальный язык на M3.

## 2. Что сохраняем (жёсткие ограничения)

### 2.1. Цветовая гамма — HEX 1:1, без изменений

Источник: `.docs/design-system/tokens.json` v1.2.0 (Playful Coach, вариант B).

| Роль | Light | Dark |
|---|---|---|
| primary / primaryStrong | `#5B8DEF` / `#3B6FD4` | `#8FB3F5` |
| secondary | `#9B7EDE` | `#B79EED` |
| record (фирменный акцент записи) | `#FF9F6B` (active `#D97238`) | `#FFB27D` |
| background / surface | `#EEF3FF` / `#FFFFFF` | `#161A2E` / `#1F2440` |
| text / textMuted | `#2D3561` / `#58609A` | `#E8EAF6` / `#9AA0C4` |
| error / errorText | `#E53935` / `#B3261E` | — |
| success / warning | `#43A047` / `#FB8C00` | — |
| timer levels 80/50/30 | `#4A7FE8` / `#8A68D6` / `#D97238` | — |
| status new/reviewed (+containers) | `#FB8C00`/`#FFE0B2`, `#43A047`/`#C8E6C9` | — |
| outline / surfaceVariant | `#B9C7EE` / `#D8E2FA` | `#3D4568` / `#2B3152` |

Полный список — в tokens.json (включая containers, onRecord `#2D3561`, scrim'ы).
Проверенные WCAG-пары (аудит 2026-08-01) обязаны сохранить AA: белый текст только
на `primaryStrong` (4.76:1), `onRecord` на record (5.81:1), `textMuted` 5.32:1 —
для мелкого текста, recordActive/timer — ≥3:1 для графики.

### 2.2. Поведенческие требования мокапов (авторитетны, не пересматриваются)

- Training = ровно 3 попытки/топик (80→50→30 сек), без удаления записей, авто-✅.
- Practice = без Review, автостоп 30с → автоотправка; повторная отправка запрещена (409).
- Guest-first: Splash → Onboarding (3 слайда) → Library; регистрация — гейт при Practice.
- Запись голоса — центральная механика; rec-кнопка — фирменный компонент
  (squircle 22dp с «оттопыренной» тенью `0 4px 0 rgba(217,114,56,.55)`) — остаётся
  как brand-компонент, но состояния (hover/pressed/disabled) оформить по M3 state layers.
- Таймер-кольцо (SpeakingTimerRing) с уровнями цвета — остаётся.

### 2.3. Типографика и платформы

- Шрифт бренда: **Nunito**; моно для таймера/длительностей: **JetBrains Mono** (tnum).
- Платформы: Compose Multiplatform (android/desktop/wasm) + admin-web (MUI 6).
  Мокапы должны маппиться на **Compose Material3** и **MUI 6** без кастомной магии.

## 3. Что берём из Material 3

1. **Компонентная база**: заменить кастомные компоненты на M3-аналоги с фирменной темой:
   - Кнопки: Filled / FilledTonal / Outlined / Text (вместо SpeakingPrimaryButton/Ghost — маппинг, не удаление поведения).
   - TextFields: Outlined (M3-стиль: label в бордере, состояния error/focused).
   - Карточки: Elevated/Filled/Outlined по семантике (тема библиотеки, отправка, статистика).
   - Navigation: NavigationBar (bottom nav «Темы/Отправки/Профиль») с M3-индикатором активного пункта.
   - Chips (assist/filter), Dialogs, Snackbar, BottomSheet, List items, LinearProgress/CircularProgress — где уместно.
2. **M3 type scale**: маппинг `SpeakingTextStyles` на шкалу display/headline/title/body/label
   (размеры Nunito можно сохранить, но роли — по M3).
3. **State layers и elevation**: hover/focus/pressed/dragged (8%/12%…), tonal elevation
   вместо кастомных теней (кроме фирменной rec-кнопки).
4. **M3 motion**: маппинг `SpeakingMotion` на Emphasized/Standard easing и длительности M3;
   recPulse (1600ms) и CheckPopAppear (overshoot) остаются как brand-моушен.
5. **Адаптивность**: window size classes; канонические раскладки для desktop/wide.
6. **Accessibility**: touch-таргеты ≥48dp, фокус-видимость, контрасты AA (см. 2.1).

## 4. Артефакты (deliverables)

1. **Реестр замен**: таблица «текущий компонент/экран → M3-аналог → что меняется»
   (по образцу `docs/qa/design-conformance/REPORT.md`). Экраны: Onboarding, Login,
   Register, Library, Topics, Video (плеер + субтитры), Questions/Training/Practice,
   MySubmissions, Profile (+guest), Debug Menu; admin-web: Login, Dashboard, Speaking
   (Libraries/Topics/Questions), Grading (Inbox/Detail), Users, Analytics, ClientLogs, Settings.
2. **`tokens.json/css` → v1.3.0**: цвета не меняются; добавить недостающие M3-роли
   (state-layer alpha, surfaceContainer*, outlineVariant) при необходимости.
3. **`mockups.html` v2.0**: все экраны приложения на M3-компонентах, light+dark,
   с сохранением поведенческих аннотаций (3 попытки, автоотправка и т.д.).
4. **`styleguide.html` v2.0**: страница M3-компонентов в фирменной теме
   (кнопки/поля/карточки/чипы/навигация/диалоги/снэкбар + brand: rec-кнопка, таймер-кольцо, waveform).
5. **Дифф `docs/DESIGN_SYSTEM_SPEC.md`** — только через ADR-007 (предложить дифф, ждать
   согласования владельца, затем bump версии).
6. **Маппинг реализации**: для каждого M3-компонента — соответствие Compose Material3
   (`androidx.compose.material3.*`) и MUI 6 (компонент + overrides в `Theme.ts`),
   чтобы реализация была механической.

## 5. Критерии приёмки

- Все экраны/компоненты представлены на M3-базе; кастом остался только там, где это
  осознанный brand-элемент (rec-кнопка, таймер-кольцо, waveform, ThemeCover-градиенты).
- Цвета — HEX 1:1 из tokens.json; WCAG AA-пары из §2.1 не деградировали.
- Тёмная тема проработана для всех экранов.
- Поведенческие аннотации мокапов сохранены дословно.
- Реестр замен покрывает 100% текущих экранов (приложение + админка).

## 6. Non-goals

- Смена палитры, шрифтов, названия бренда, UX-флоу и поведения экранов.
- Реализация в коде (после утверждения дизайна — отдельный план миграции, DC-этапы).
- Правки спек Part 1–3 (только если дизайн выявит расхождение — через ADR-007).

## 7. Референсы в репо

- Текущая DS: `.docs/design-system/` (tokens.json v1.2.0, mockups.html, styleguide.html, icons.svg)
- Спека DS: `docs/DESIGN_SYSTEM_SPEC.md` v2.1
- Конформити-отчёт и скриншоты реального приложения: `docs/qa/design-conformance/REPORT.md`
- Реализация темы: `composeApp/.../designsystem/theme/SpeakingTokens.kt`, `design/`, `admin-web/src/theme/Theme.ts`
- История решений: `memory.md` §5 (2026-07-31 вариант B, 2026-08-01 аудит-токены v1.2.0, DC-1…DC-5)
